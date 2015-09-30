package timetrace.shape

import timetrace.UnitSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import timetrace.math.Vector3
import timetrace.math.Vector4
import timetrace.Ray

object SphereTest {
  
}

@RunWith(classOf[JUnitRunner])
class SphereTest extends UnitSpec {
  
  "spheres" should "intersect with basic ray" in {
    val sphere :Sphere = new Sphere(Vector3(0.0, 0.0, 5.0), 1.0)
    val ray :Ray = new Ray(Vector4(0.0, 0.5, 0.0, 0.0), Vector4(0.0, 0.0, 1.0, -1.0).spatiallyNormalize())

    val rayHit = sphere.intersect(ray)
    println(rayHit)
  }
}