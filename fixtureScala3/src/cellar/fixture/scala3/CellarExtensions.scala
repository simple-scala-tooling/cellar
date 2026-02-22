package cellar.fixture.scala3

/** Extension methods for CellarTC. */
extension [A: CellarTC](a: A)
  def rendered: String = summon[CellarTC[A]].render(a)
  def renderedWith(prefix: String): String = s"$prefix: ${a.rendered}"
