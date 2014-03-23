package timetrace.math

import timetrace.UnitSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import timetrace.Generators
import org.scalacheck.Gen

object Vector3Test {
  val vector3s: Gen[Vector3] = for (
    x <- Generators.numbers;
    y <- Generators.numbers;
    z <- Generators.numbers
  ) yield new Vector3(x, y, z)
}

@RunWith(classOf[JUnitRunner])
class Vector3Test extends UnitSpec {

  "vectors" should "have a constructor that preserves the data" in {
    forAll { (x: Double, y: Double, z: Double) =>
      {
        val v = new Vector3(x, y, z)

        v.x should equal(x)
        v.y should equal(y)
        v.z should equal(z)
      }
    }
  }

  it should "have a nice toString" in {
    new Vector3(1.0, 2.0, 3.0).toString() should equal("[1.0, 2.0, 3.0]")
  }

  it should "be extendable to a Vector4" in {
    forAll(Vector3Test.vector3s) { (v3: Vector3) =>
      {
        val v4: Vector4 = v3.to4

        v4.x should equal(v3.x)
        v4.y should equal(v3.y)
        v4.z should equal(v3.z)
        v4.t should equal(1.0)
      }
    }
  }

}