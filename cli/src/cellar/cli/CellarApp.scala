package cellar.cli

import cats.effect.{ExitCode, IO}
import cats.syntax.all.*
import cellar.*
import cellar.handlers.{DepsHandler, GetHandler, GetSourceHandler, ListHandler, SearchHandler}
import com.monovore.decline.*
import com.monovore.decline.effect.*
import java.nio.file.Path

object CellarApp
    extends CommandIOApp(
      name = "cellar",
      header = "Inspect Maven-published JVM dependency APIs",
      version = "0.1.0-SNAPSHOT"
    ):

  override def main: Opts[IO[ExitCode]] =
    getSubcmd orElse getSourceSubcmd orElse listSubcmd orElse searchSubcmd orElse depsSubcmd

  private val coordArg: Opts[String] =
    Opts.argument[String]("coordinate")

  private val symbolArg: Opts[String] =
    Opts.argument[String]("fully-qualified-symbol")

  private val javaHomeOpt: Opts[Option[Path]] =
    Opts.option[Path]("java-home", "Use a specific JDK for JRE classpath").orNone

  private val limitOpt: Opts[Int] =
    Opts
      .option[Int]("limit", "Maximum number of results to return", short = "l", metavar = "N")
      .withDefault(50)

  private val getSubcmd: Opts[IO[ExitCode]] =
    Opts.subcommand("get", "Fetch all information about a named symbol") {
      (coordArg, symbolArg, javaHomeOpt).mapN { (rawCoord, fqn, javaHome) =>
        MavenCoordinate.parse(rawCoord) match
          case Left(err)    => IO.blocking(System.err.println(err)).as(ExitCode.Error)
          case Right(coord) => GetHandler.run(coord, fqn, javaHome)
      }
    }

  private val getSourceSubcmd: Opts[IO[ExitCode]] =
    Opts.subcommand("get-source", "Fetch the source code of a named symbol") {
      (coordArg, symbolArg, javaHomeOpt).mapN { (rawCoord, fqn, javaHome) =>
        MavenCoordinate.parse(rawCoord) match
          case Left(err)    => IO.blocking(System.err.println(err)).as(ExitCode.Error)
          case Right(coord) => GetSourceHandler.run(coord, fqn, javaHome)
      }
    }

  private val listSubcmd: Opts[IO[ExitCode]] =
    Opts.subcommand("list", "List symbols in a package or class") {
      (coordArg, symbolArg, limitOpt, javaHomeOpt).mapN { (rawCoord, fqn, limit, javaHome) =>
        MavenCoordinate.parse(rawCoord) match
          case Left(err)    => IO.blocking(System.err.println(err)).as(ExitCode.Error)
          case Right(coord) => ListHandler.run(coord, fqn, limit, javaHome)
      }
    }

  private val searchSubcmd: Opts[IO[ExitCode]] =
    Opts.subcommand("search", "Substring search for symbol names") {
      (coordArg, Opts.argument[String]("query"), limitOpt, javaHomeOpt).mapN {
        (rawCoord, query, limit, javaHome) =>
          MavenCoordinate.parse(rawCoord) match
            case Left(err)    => IO.blocking(System.err.println(err)).as(ExitCode.Error)
            case Right(coord) => SearchHandler.run(coord, query, limit, javaHome)
      }
    }

  private val depsSubcmd: Opts[IO[ExitCode]] =
    Opts.subcommand("deps", "Print the transitive dependency list") {
      coordArg.map { rawCoord =>
        MavenCoordinate.parse(rawCoord) match
          case Left(err)    => IO.blocking(System.err.println(err)).as(ExitCode.Error)
          case Right(coord) => DepsHandler.run(coord)
      }
    }
