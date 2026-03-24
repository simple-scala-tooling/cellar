package cellar.fixture.scala3

/** Fixture for testing that overloaded methods are all preserved in output. */
trait CellarOverloaded:
  def process(value: Int): String
  def process(value: String): String
  def process(value: Int, flag: Boolean): String
  def unique: Int

/** Fixture for testing overloads split across inheritance. */
trait CellarOverloadedBase:
  def action(value: Int): String

trait CellarOverloadedChild extends CellarOverloadedBase:
  def action(value: String): String
  def childOnly: Int
