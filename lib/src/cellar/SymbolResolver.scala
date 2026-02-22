package cellar

import cats.effect.IO
import tastyquery.Contexts.Context
import tastyquery.Exceptions.MemberNotFoundException
import tastyquery.Names.termName
import tastyquery.Symbols.Symbol

sealed trait LookupResult
object LookupResult:
  final case class Found(symbols: List[Symbol]) extends LookupResult
  case object IsPackage                          extends LookupResult
  case object NotFound                           extends LookupResult

object SymbolResolver:
  def resolve(fqn: String)(using ctx: Context): IO[LookupResult] =
    IO.blocking(tryTopLevel(fqn)).flatMap {
      case Some(result) => IO.pure(result)
      case None         => IO.blocking(tryMemberLookup(fqn)).map(_.getOrElse(LookupResult.NotFound))
    }

  private def tryTopLevel(fqn: String)(using ctx: Context): Option[LookupResult] =
    tryOrNone(ctx.findStaticClass(fqn)).map(s => LookupResult.Found(List(s)))
      .orElse(tryOrNone(ctx.findStaticModuleClass(fqn)).map(s => LookupResult.Found(List(s))))
      .orElse(tryOrNone(ctx.findStaticTerm(fqn)).map(s => LookupResult.Found(List(s))))
      .orElse(tryOrNone(ctx.findStaticType(fqn)).map(s => LookupResult.Found(List(s))))
      .orElse(tryOrNone(ctx.findPackage(fqn)).map(_ => LookupResult.IsPackage))

  private def tryMemberLookup(fqn: String)(using ctx: Context): Option[LookupResult] =
    val lastDot = fqn.lastIndexOf('.')
    if lastDot <= 0 then return None
    val ownerFqn  = fqn.substring(0, lastDot)
    val memberStr = fqn.substring(lastDot + 1)

    val ownerOpt =
      tryOrNone(ctx.findStaticClass(ownerFqn))
        .orElse(tryOrNone(ctx.findStaticModuleClass(ownerFqn)))

    ownerOpt.map { owner =>
      val overloads = owner.getAllOverloadedDecls(termName(memberStr))
      val public    = overloads.filter(PublicApiFilter.isPublic)
      if public.isEmpty then LookupResult.NotFound
      else LookupResult.Found(public)
    }

  private def tryOrNone[A](thunk: => A): Option[A] =
    try Some(thunk)
    catch
      case _: MemberNotFoundException => None
      case _: Exception               => None
