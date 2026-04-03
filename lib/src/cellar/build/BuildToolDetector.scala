package cellar.build

import cats.effect.IO
import cats.syntax.all.*
import fs2.io.file.{Files, Path}

enum BuildToolKind:
  case Mill, Sbt, ScalaCli

object BuildToolDetector:
  private val millMarkers = List("build.mill", "build.sc", "build.mill.yaml", "build.yaml")

  /** Detect the build tool kind from marker files only (no binary check). */
  def detectKind(dir: Path): IO[BuildToolKind] =
    {
      val millMarker = millMarkers.findM(m => Files[IO].exists(dir.resolve(m)))
      val hasSbt = Files[IO].exists(dir.resolve("build.sbt"))

      millMarker.map(_.isDefined).ifM(
        IO.pure(BuildToolKind.Mill),
        hasSbt.ifF(BuildToolKind.Sbt, BuildToolKind.ScalaCli) /* scala-cli is also fallback */
      )
    }

