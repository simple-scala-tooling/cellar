package cellar

import tastyquery.Symbols.{ClassSymbol, Symbol, TermSymbol, TypeSymbol}

object PublicApiFilter:
  def isPublic(sym: Symbol): Boolean =
    !isPrivateSym(sym) && !isSyntheticSym(sym)

  private def isPrivateSym(sym: Symbol): Boolean =
    sym match
      case s: (ClassSymbol | TermSymbol | TypeSymbol) => s.isPrivate
      case _              => false

  private def isSyntheticSym(sym: Symbol): Boolean =
    sym match
      case s: (ClassSymbol | TermSymbol | TypeSymbol) => s.isSynthetic || s.name.toString.startsWith("$")
      case _              => false
