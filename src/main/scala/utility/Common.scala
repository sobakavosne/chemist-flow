package utility

object FunctionOps {
  implicit class FunctionComposition[A, B](f: A => B) {
    def ~(g: A => A): A => B = f.compose(g)
  }
}
