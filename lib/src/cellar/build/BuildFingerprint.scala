package cellar.build

import cats.effect.IO
import java.nio.file.{Files, Path}
import java.security.MessageDigest

object BuildFingerprint:
  def compute(files: List[Path], module: String): IO[String] =
    IO.blocking {
      val digest = MessageDigest.getInstance("SHA-256")
      digest.update(module.getBytes(java.nio.charset.StandardCharsets.UTF_8))
      files.sorted.foreach { path =>
        if Files.exists(path) then
          digest.update(path.toString.getBytes(java.nio.charset.StandardCharsets.UTF_8))
          digest.update(Files.readAllBytes(path))
      }
      digest.digest().map(b => f"$b%02x").mkString
    }
