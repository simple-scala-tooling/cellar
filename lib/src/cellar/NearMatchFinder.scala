package cellar

import cats.effect.IO
import tastyquery.Classpaths.Classpath
import tastyquery.Contexts.Context

object NearMatchFinder:
  def findNearMatches(fqn: String, classpath: Classpath)(using ctx: Context): IO[List[String]] =
    IO.blocking {
      val simpleName = fqn.lastIndexOf('.') match
        case -1  => fqn
        case idx => fqn.substring(idx + 1)
      val lowerName = simpleName.toLowerCase

      val matches = List.newBuilder[String]
      var count   = 0
      val entries = classpath.iterator
      while entries.hasNext && count < 10 do
        val entry = entries.next()
        try
          val syms = ctx.findSymbolsByClasspathEntry(entry)
          val it   = syms.iterator
          while it.hasNext && count < 10 do
            val sym = it.next()
            if PublicApiFilter.isPublic(sym) && sym.name.toString.toLowerCase == lowerName then
              matches += sym.displayFullName
              count += 1
        catch case _: Exception => ()
      matches.result()
    }
