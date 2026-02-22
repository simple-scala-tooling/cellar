package cellar.fixture

package object scala2 extends CellarInstances {
  type TC[A] = CellarTypeClass[A]
  val TC: CellarTypeClass.type = CellarTypeClass
}
