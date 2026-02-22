package cellar

import cats.effect.IO
import cats.effect.std.Console
import fs2.Stream
import munit.CatsEffectSuite

class StreamOpsTest extends CatsEffectSuite:

  // Test console that captures stderr lines
  class CapturingConsole extends Console[IO]:
    var errLines: List[String]        = Nil
    var outLines: List[String]        = Nil
    def readLineWithCharset(charset: java.nio.charset.Charset): IO[String] = IO.pure("")
    def print[A](a: A)(using fmt: cats.Show[A]): IO[Unit]                 = IO.unit
    def println[A](a: A)(using fmt: cats.Show[A]): IO[Unit] =
      IO { outLines = fmt.show(a) :: outLines }
    def error[A](a: A)(using fmt: cats.Show[A]): IO[Unit]      = IO.unit
    def errorln[A](a: A)(using fmt: cats.Show[A]): IO[Unit] =
      IO { errLines = fmt.show(a) :: errLines }

  test("bounded: stream shorter than limit returns full list without truncation"):
    val console = CapturingConsole()
    given Console[IO] = console
    val stream = Stream.emits(1 to 30)
    StreamOps.bounded(stream, 50).map { result =>
      assertEquals(result.length, 30)
      assert(console.errLines.isEmpty, "no truncation note expected")
    }

  test("bounded: stream exactly at limit returns full list without truncation"):
    val console = CapturingConsole()
    given Console[IO] = console
    val stream = Stream.emits(1 to 50)
    StreamOps.bounded(stream, 50).map { result =>
      assertEquals(result.length, 50)
      assert(console.errLines.isEmpty)
    }

  test("bounded: stream one over limit returns limit elements with truncation note"):
    val console = CapturingConsole()
    given Console[IO] = console
    val stream = Stream.emits(1 to 51)
    StreamOps.bounded(stream, 50).map { result =>
      assertEquals(result.length, 50)
      assert(console.errLines.nonEmpty)
      assert(console.errLines.exists(_.contains("truncated")))
    }

  test("bounded: large stream returns only limit elements"):
    val console = CapturingConsole()
    given Console[IO] = console
    val stream = Stream.emits(1 to 10000)
    StreamOps.bounded(stream, 50).map { result =>
      assertEquals(result.length, 50)
      assertEquals(console.errLines.length, 1) // exactly one note
    }

  test("bounded: limit=0 on non-empty stream returns empty list with truncation"):
    val console = CapturingConsole()
    given Console[IO] = console
    val stream = Stream.emits(1 to 5)
    StreamOps.bounded(stream, 0).map { result =>
      assertEquals(result, Nil)
      assert(console.errLines.nonEmpty)
    }

  test("bounded: truncation note contains the limit value"):
    val console = CapturingConsole()
    given Console[IO] = console
    val stream = Stream.emits(1 to 100)
    StreamOps.bounded(stream, 7).map { _ =>
      assert(console.errLines.exists(_.contains("7")))
    }

  test("boundedWithFlag: returns false when not truncated"):
    val stream = Stream.emits(1 to 10)
    StreamOps.boundedWithFlag(stream, 50).map { (results, truncated) =>
      assertEquals(results.length, 10)
      assertEquals(truncated, false)
    }

  test("boundedWithFlag: returns true when truncated"):
    val stream = Stream.emits(1 to 100)
    StreamOps.boundedWithFlag(stream, 50).map { (results, truncated) =>
      assertEquals(results.length, 50)
      assertEquals(truncated, true)
    }
