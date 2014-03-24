package timetrace

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import timetrace.math.Vector3
import timetrace.math.Vector4
import timetrace.shape.Plane
import timetrace.material.WhiteDiffuseMaterial

@RunWith(classOf[JUnitRunner])
class RaytraceTest extends UnitSpec {
  "raytrace" should "find nothing in an empty scene" in {
    val scene = new Scene(List.empty, List.empty)
    val ray = new Ray(new Vector4(0.0, 0.0, 0.0, 0.0), new Vector3(1.0, 0.0, 0.0).normalize)
    val hit = Raytrace.firstHit(scene, ray)

    hit should be(None)
  }

  it should "find the plane in a simple scene" in {
    val scene = new Scene(
      List(Thing(new Plane(Vector4(0.0, 0.0, 1.0, 0.0).normalize, 0.0), WhiteDiffuseMaterial)),
      List.empty)
    val ray = new Ray(new Vector4(0.0, 0.0, 1.0, 0.0), new Vector3(0.0, 0.0, -1.0).normalize)
    val hit: Option[RayHit] = Raytrace.firstHit(scene, ray)

    hit should be('defined)
  }
}