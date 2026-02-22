package cellar

import cats.effect.IO
import fs2.Stream
import tastyquery.Classpaths.{Classpath, ClasspathEntry}
import tastyquery.Contexts.Context
import tastyquery.Symbols.TermOrTypeSymbol

object AllSymbolsStream:
  /** Streams all public symbols from the classpath, excluding the given JRE entries. */
  def stream(classpath: Classpath, jrePaths: Seq[java.nio.file.Path])(using ctx: Context): Stream[IO, TermOrTypeSymbol] =
    val jreStrings = jrePaths.map(_.toString).toSet
    val libEntries: List[ClasspathEntry] = classpath.filterNot(e => jreStrings.contains(e.toString))
    Stream
      .emits(libEntries)
      .flatMap { entry =>
        Stream
          .eval(IO.blocking {
            try ctx.findSymbolsByClasspathEntry(entry).toList
            catch case _: Throwable => Nil
          })
          .flatMap(syms => Stream.emits(syms))
      }
      .filter(PublicApiFilter.isPublic)
