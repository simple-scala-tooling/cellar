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
        val stderrDrainer = Thread.startVirtualThread { () =>
          stderr = new String(process.getErrorStream.readAllBytes())
        }
        val stdout = new String(process.getInputStream.readAllBytes())
        stderrDrainer.join()
        val exitCode = process.waitFor()
        ProcessResult(exitCode, stdout, stderr)
      finally process.destroyForcibly()
    }.adaptError { case e: java.io.IOException =>
      new RuntimeException(s"Command not found: '${command.headOption.getOrElse("")}'. Ensure it is installed and on PATH.", e)
    }
