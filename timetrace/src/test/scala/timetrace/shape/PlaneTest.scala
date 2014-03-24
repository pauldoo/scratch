package timetrace.shape

import timetrace.UnitSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import timetrace.math.Vector4
import org.scalacheck.Gen
import timetrace.math.Vector4Test
import timetrace.RayTest
import timetrace.Ray
import timetrace.RayHit
import timetrace.Generators
import timetrace.RayTest
import timetrace.math.Vector3

object PlaneTest {
  val planes: Gen[Plane] = for (
    n <- Vector4Test.vector4s;
    o <- Generators.numbers
  ) yield new Plane(n.normalize, o)
}

@RunWith(classOf[JUnitRunner])
class PlaneTest extends UnitSpec {

  "planes" should "intersect with rays as expected" in {
    forAll(PlaneTest.planes, RayTest.rays) {
      (plane: Plane, ray: Ray) =>
        {
          val currentSide = Math.signum((ray.start dot plane.normal) - plane.offset)
          val eventualSide = Math.signum(ray.direction.to4 dot plane.normal)

          val rayHit: Option[RayHit] = plane.intersect(ray)

          if (currentSide != eventualSide) {
            (rayHit.get.location dot plane.normal) should equal(plane.offset +- 1e-6)
          } else {
            rayHit should not be ('defined)
          }
        }
    }
  }

  it should "fail to intersect when rays are parallel" in {
    val plane = new Plane(Vector4(1.0, 0.0, 0.0, 0.0).normalize, 0.0)
    val ray = Ray(Vector4(1.0, 0.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0).normalize)

    val rayHit = plane.intersect(ray)
  }
}