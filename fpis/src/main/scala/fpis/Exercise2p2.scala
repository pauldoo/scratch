package fpis

import scala.annotation.tailrec

object Exercise2p2 {
  def isSorted[A](as: Array[A], ordered: (A, A) => Boolean): Boolean = {
    @tailrec
    def imp(n: Int): Boolean = {
      if ((n + 1) < as.length) { ordered(as(n), as(n + 1)) && imp(n + 1) }
      else true
    }

    imp(0)
  }
}
