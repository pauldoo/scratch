package timetrace

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SampleTest extends UnitSpec {

  "basic arithmetic" should "make basic sense" in {
    forAll { (a: Int, b: Int) =>
      (a + b - b - a) should equal(0)
    }
  }

  it should "continue to make further sense" in {
    forAll { (a: Int, b: Int) =>
      (a * b - b * a) should equal(0)
    }
  }

  "some other numbers" should "be appoximately equal" in {
    5.0 should equal(4.0 +- 2.0)
  }

}