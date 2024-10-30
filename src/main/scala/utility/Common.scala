package utility

object FunctionOps {

  implicit class FunctionComposition[A, B](f: A => B) {
    require(f != null, "Function must be non-null")

    def ◦[C](g: C => A): C => B = f compose g
  }
}
