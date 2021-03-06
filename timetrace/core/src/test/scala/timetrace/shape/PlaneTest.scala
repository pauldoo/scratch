package timetrace.shape

import timetrace.UnitSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import timetrace.math.Vector4
import org.scalacheck.Gen
import timetrace.math.Vector4Test
import timetrace.RayTest
import timetrace.Ray
import timetrace.Generators
import timetrace.RayTest
import timetrace.math.Vector3
import timetrace.math.Vector3Test

object PlaneTest {
  val planes: Gen[Plane] = for (
    n <- Vector3Test.vector3s;
    o <- Generators.numbers
  ) yield new Plane(n.normalize, o)
}

@RunWith(classOf[JUnitRunner])
class PlaneTest extends UnitSpec {

  "planes" should "intersect with rays as expected" in {
    forAll(PlaneTest.planes, RayTest.rays) {
      (plane: Plane, ray: Ray) =>
        {
          val currentSide = Math.signum((ray.location.truncateTo3 dot plane.normal) - plane.offset)
          val eventualSide = Math.signum(ray.direction.truncateTo3() dot plane.normal)

          val rayHit: Option[ShapeHit] = plane.intersect(ray)

          if (currentSide != eventualSide) {
            val location = ray.march(rayHit.get.t)
            (location.truncateTo3 dot plane.normal) should equal(plane.offset +- 1e-6)
          } else {
            rayHit should not be ('defined)
          }
        }
    }
  }

  it should "fail to intersect when rays are parallel" in {
    val plane = new Plane(Vector3(1.0, 0.0, 0.0).normalize, 0.0)
    val ray = new Ray(Vector4(1.0, 0.0, 0.0, 0.0), Vector4(0.0, 1.0, 0.0, 1.0).spatiallyNormalize())

    val rayHit = plane.intersect(ray)
  }
}
