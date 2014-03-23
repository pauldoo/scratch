package timetrace

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import timetrace.math.Vector3
import timetrace.math.Vector4

@RunWith(classOf[JUnitRunner])
class RaytraceTest extends UnitSpec {
  "raytrace" should "find nothing in an empty scene" in {
    val scene = new Scene(List.empty, List.empty)
    val ray = new Ray(new Vector4(0.0, 0.0, 0.0, 0.0), new Vector3(1.0, 0.0, 0.0).normalize)
    val hit = Raytrace.firstHit(scene, ray)

    hit should equal(None)
  }
}