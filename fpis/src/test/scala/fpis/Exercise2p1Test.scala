package fpis

class Exercise2p1Test extends UnitSpec {
  "the first fibbonacci number" should "be 0" in {
    assertResult(0) { Exercise2p1.fib(0) }
  }

  "the 2nd fibbonacci number" should "be 1" in {
    assertResult(1) { Exercise2p1.fib(1) }
  }

  "the 10th fibbonacci number" should "be 55" in {
    assertResult(34) { Exercise2p1.fib(9) }
  }

}
