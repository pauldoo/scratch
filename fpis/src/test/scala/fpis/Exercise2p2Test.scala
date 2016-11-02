package fpis

class Exercise2p2Test extends UnitSpec {

  {
    val as: Array[Int] = Array[Int](1, 2, 3, 4)

    "1, 2, 3, 4" should "isSorted by <=" in {
      assertResult(true) { Exercise2p2.isSorted(as, lte) }
    }

    it should "isSorted by <" in {
      assertResult(true) { Exercise2p2.isSorted(as, lt) }
    }
  }

  {
    val as: Array[Int] = Array[Int](1, 1, 2, 2)

    "1, 1, 2, 2" should "isSorted by <=" in {
      assertResult(true) { Exercise2p2.isSorted(as, lte) }
    }

    it should "not isSorted by <" in {
      assertResult(false) { Exercise2p2.isSorted(as, lt) }
    }
  }

  private def lte(a: Int, b: Int): Boolean = a <= b
  private def lt(a: Int, b: Int): Boolean = a < b

}
