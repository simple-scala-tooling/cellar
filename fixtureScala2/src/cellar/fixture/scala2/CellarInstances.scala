package cellar.fixture.scala2

/** Implicit type class instances for primitive types. */
trait CellarInstances {
  implicit val intInstance: CellarTypeClass[Int] =
    (i: Int) => s"Int($i)"

  implicit val stringInstance: CellarTypeClass[String] =
    (s: String) => s"String($s)"

  implicit def listInstance[A: CellarTypeClass]: CellarTypeClass[List[A]] =
    (as: List[A]) => as.map(CellarTypeClass[A].describe).mkString("[", ", ", "]")
}

object CellarInstances extends CellarInstances
