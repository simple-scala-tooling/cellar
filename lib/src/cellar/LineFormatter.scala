package cellar

import tastyquery.Contexts.Context
import tastyquery.Symbols.Symbol

object LineFormatter:
  def formatLine(sym: Symbol)(using ctx: Context): String =
    try TypePrinter.printSymbolSignatureSafe(sym).linesIterator.mkString(" ").trim
    catch case _: Exception => sym.displayFullName
