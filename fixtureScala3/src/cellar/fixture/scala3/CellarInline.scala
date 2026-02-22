package cellar.fixture.scala3

/** Inline definitions for testing inline symbol extraction. */
inline def cellarIdentity[A](a: A): A = a

type Head[T <: Tuple] <: Any = T match
  case h *: ? => h

type Tail[T <: Tuple] <: Tuple = T match
  case ? *: t => t
