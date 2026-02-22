package cellar

import cats.effect.IO
import munit.CatsEffectSuite
import tastyquery.Contexts.Context

class SymbolResolverTest extends CatsEffectSuite:

  private def withCtx[A](body: Context => IO[A]): IO[A] =
    TestFixtures.assumeFixturesAvailable()
    for
      jrePaths <- JreClasspath.jrtPath()
      jars     <- CoursierFetchClient.fetchClasspath(
                    TestFixtures.scala3Coord, Seq(TestFixtures.localM2Repo))
      result   <- ContextResource.make(jars, jrePaths).use { (ctx, _) => body(ctx) }
    yield result

  test("resolve class FQN returns Found with ClassSymbol"):
    withCtx { ctx =>
      given Context = ctx
      SymbolResolver.resolve("cellar.fixture.scala3.CellarADT").map {
        case LookupResult.Found(syms) => assert(syms.nonEmpty)
        case other                    => fail(s"Expected Found, got $other")
      }
    }

  test("resolve package FQN returns IsPackage"):
    withCtx { ctx =>
      given Context = ctx
      SymbolResolver.resolve("cellar.fixture.scala3").map {
        case LookupResult.IsPackage => ()
        case other                  => fail(s"Expected IsPackage, got $other")
      }
    }

  test("resolve non-existent FQN returns NotFound"):
    withCtx { ctx =>
      given Context = ctx
      SymbolResolver.resolve("cellar.fixture.scala3.DoesNotExist99999").map {
        case LookupResult.NotFound => ()
        case other                 => fail(s"Expected NotFound, got $other")
      }
    }

  test("resolve case class FQN returns Found"):
    withCtx { ctx =>
      given Context = ctx
      SymbolResolver.resolve("cellar.fixture.scala3.CellarA").map {
        case LookupResult.Found(syms) => assert(syms.nonEmpty)
        case other                    => fail(s"Expected Found, got $other")
      }
    }

  test("resolve member method returns Found with TermSymbols"):
    withCtx { ctx =>
      given Context = ctx
      // CellarTC has a `render` method
      SymbolResolver.resolve("cellar.fixture.scala3.CellarTC.render").map {
        case LookupResult.Found(syms) =>
          assert(syms.nonEmpty)
          assert(syms.forall(_.name.toString == "render"))
        case other => fail(s"Expected Found for render, got $other")
      }
    }

  test("resolve non-existent method returns NotFound"):
    withCtx { ctx =>
      given Context = ctx
      SymbolResolver.resolve("cellar.fixture.scala3.CellarADT.nonExistentXYZ").map {
        case LookupResult.NotFound => ()
        case other                 => fail(s"Expected NotFound, got $other")
      }
    }
