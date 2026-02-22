package cellar.fixture.scala2

/** Extension-method syntax for CellarTypeClass. */
object CellarSyntax {
  implicit class CellarOps[A: CellarTypeClass](val a: A) {
    def described: String = CellarTypeClass[A].describe(a)

    def describedWith(prefix: String): String = s"$prefix: ${described}"
  }
}
