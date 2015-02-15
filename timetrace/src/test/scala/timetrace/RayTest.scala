package timetrace

import timetrace.math.Vector3
import org.scalacheck.Gen
import timetrace.math.Vector4Test
import timetrace.math.Vector3Test
import timetrace.math.Vector4
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

object RayTest {
  val rays: Gen[Ray] = for (
    s <- Vector4Test.vector4s;
    d <- Vector4Test.vector4sSpatiallyNormalized
  ) yield new Ray(s, d)
}

@RunWith(classOf[JUnitRunner])
class RayTest extends UnitSpec {
  "rays" should "have a constructor that preserves the data" in {
    forAll(Vector4Test.vector4s, Vector4Test.vector4sSpatiallyNormalized) { (start: Vector4, direction: Vector4.SpatiallyNormalized) =>
      {
        val v = new Ray(start, direction)

        v.start should equal(start)
        v.direction should equal(direction)
      }
    }
  }

  it should "have a nice toString" in {
    val ray = Ray(
      Vector4(1.0, 2.0, 3.0, 4.0),
      Vector4(0.0, 1.0, 0.0, -1.0).spatiallyNormalize())
    ray.toString() should equal("Ray([1.0, 2.0, 3.0, 4.0] -> [0.0, 1.0, 0.0, -1.0])")
  }
}
