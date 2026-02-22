package cellar.fixture.scala3

/** Opaque type alias for testing opaque type extraction. */
opaque type Celsius = Double

object Celsius:
  def apply(d: Double): Celsius = d

  extension (c: Celsius)
    def toFahrenheit: Double = c * 9.0 / 5.0 + 32
    def value: Double        = c
