package fpis

import scala.annotation.tailrec

object Exercise2p1 {
  def fib(n: Int): Int = {
    @tailrec
    def imp(a: Int, b: Int, n: Int): Int = {
      if (n <= 0) a
      else imp(b, a + b, n - 1)
    }

    imp(0, 1, n)
  }
}
