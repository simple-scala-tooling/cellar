package cellar.fixture.scala3

/** Sealed ADT hierarchy for testing sealedChildren extraction. */
sealed trait CellarADT

final case class CellarA(value: Int) extends CellarADT

case object CellarB extends CellarADT

final case class CellarC[A](items: List[A]) extends CellarADT
