package cellar.fixture.scala2

/** A Scala 2 type class trait for testing pickle-based symbol extraction. */
trait CellarTypeClass[A] {
  def describe(a: A): String
}

object CellarTypeClass {
  def apply[A](implicit ev: CellarTypeClass[A]): CellarTypeClass[A] = ev

  def describe[A: CellarTypeClass](a: A): String = CellarTypeClass[A].describe(a)
}
