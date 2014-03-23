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

object PlaneTest {
  val planes: Gen[Plane] = for (
    n <- Vector4Test.vector4s;
    o <- Generators.numbers
  ) yield new Plane(n, o)
}

@RunWith(classOf[JUnitRunner])
class PlaneTest extends UnitSpec {

  "planes" should "intersect with rays as expected" in {
    forAll(PlaneTest.planes, RayTest.rays) {
      (plane: Plane, ray: Ray) =>
        {
          val rh: RayHit = plane.intersect(ray)

          rh.location
        }
    }
  }
}