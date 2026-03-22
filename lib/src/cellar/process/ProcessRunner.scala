package cellar.process

import cats.effect.IO
import cats.syntax.all.*
import java.nio.file.Path

final case class ProcessResult(exitCode: Int, stdout: String, stderr: String)

object ProcessRunner:
  def run(command: List[String], workingDir: Option[Path] = None): IO[ProcessResult] =
    IO.blocking {
      val builder = new ProcessBuilder(command*)
      workingDir.foreach(dir => builder.directory(dir.toFile))
      builder.redirectErrorStream(false)
      val process = builder.start()
      try
        // Drain stderr on a separate thread to avoid pipe buffer deadlock
        @volatile var stderr = ""
        val stderrDrainer = new Thread(() =>
          stderr = new String(process.getErrorStream.readAllBytes())
        )
        stderrDrainer.start()
        val stdout = new String(process.getInputStream.readAllBytes())
        stderrDrainer.join()
        val exitCode = process.waitFor()
        ProcessResult(exitCode, stdout, stderr)
      finally process.destroyForcibly()
    }.adaptError { case e: java.io.IOException =>
      val cmd = command.headOption.getOrElse("")
      val detail = Option(e.getMessage).getOrElse("I/O error")
      new RuntimeException(s"Failed to start '$cmd': $detail", e)
    }
