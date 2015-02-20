package timetrace.math

import timetrace.UnitSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalacheck.Gen
import timetrace.Generators

object Vector4Test {
  val vector4s: Gen[Vector4] = for (
    v <- Vector3Test.vector3s;
    t <- Generators.numbers
  ) yield new Vector4(v.x, v.y, v.z, t)

  val vector4sSpatiallyNormalized: Gen[Vector4.SpatiallyNormalized] = for (
    v <- Vector3Test.vector3sNormalized;
    t <- Gen.oneOf(-1.0, 1.0)
  ) yield new Vector4.SpatiallyNormalized(v, t)
}

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

  it should "have a working dot product" in {
    forAll(Vector4Test.vector4s, Vector4Test.vector4s) { (a: Vector4, b: Vector4) =>
      {
        val r: Double = a dot b

        r should equal(a.x * b.x + a.y * b.y + a.z * b.z + a.t * b.t)
      }
    }
  }

  it should "have a working scalar product" in {
    forAll(Vector4Test.vector4s, Generators.numbers) { (v: Vector4, s: Double) =>
      val r: Vector4 = v * s

      r should equal(Vector4(v.x * s, v.y * s, v.z * s, v.t * s))
    }
  }

  it should "have a working addition" in {
    forAll(Vector4Test.vector4s, Vector4Test.vector4s) { (a: Vector4, b: Vector4) =>
      {
        val r: Vector4 = a + b

        r should equal(Vector4(a.x + b.x, a.y + b.y, a.z + b.z, a.t + b.t))
      }
    }
  }

}
