package timetrace.math

import timetrace.UnitSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Vector4Test extends UnitSpec {

  "vectors" should "have a constructor that preserves the data" in {
    forAll { (x: Double, y: Double, z: Double, t: Double) =>
      {
        val v = new Vector4(x, y, z, t)

        v.x should equal(x)
        v.y should equal(y)
        v.z should equal(z)
        v.t should equal(t)
      }
    }
  }

  it should "have a nice toString" in {
    new Vector4(1.0, 2.0, 3.0, 4.0).toString() should equal("[1.0, 2.0, 3.0, 4.0]")
  }

}