package cellar.fixture.scala2

/** Fixture for testing that overloaded methods are preserved in Scala 2 artifacts. */
trait CellarOverloaded {
  def process(value: Int): String
  def process(value: String): String
  def process(value: Int, flag: Boolean): String
  def unique: Int
}
