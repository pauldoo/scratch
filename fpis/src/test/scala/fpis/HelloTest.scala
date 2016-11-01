package fpis

class HelloTest extends UnitSpec {
  "The answer" should "be 42" in {
    val h: Hello = new Hello()
    assertResult(42) { h.answer }
  }
}
