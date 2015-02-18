package timetrace

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import timetrace.math.Vector3
import timetrace.math.Vector4
import timetrace.shape.Plane
import timetrace.material.WhiteDiffuseMaterial
import timetrace.light.StaticPointLight
import timetrace.math.MathUtils._

@RunWith(classOf[JUnitRunner])
class RaytraceTest extends UnitSpec with ColorMatchers {
  "raytrace" should "find nothing in an empty scene" in {
    val scene = new Scene(List.empty, List.empty)
    val ray = new Ray(new Vector4(0.0, 0.0, 0.0, 0.0), new Vector4(1.0, 0.0, 0.0, -1.0).spatiallyNormalize())
    val hit = new Raytrace(scene).firstHit(ray)

    hit should be(None)
  }

  it should "find the plane in a simple scene" in {
    val scene = new Scene(
      List(Thing(new Plane(Vector3(0.0, 0.0, 1.0).normalize, 0.0), WhiteDiffuseMaterial)),
      List.empty)
    val ray = new Ray(new Vector4(0.0, 0.0, 1.0, 0.0), new Vector4(0.0, 0.0, -1.0, -1.0).spatiallyNormalize())
    val hit: Option[Hit[Ray]] = new Raytrace(scene).firstHit(ray)

    hit should be('defined)
  }

  private def singleRayTest(rayStart: Vector3, lightPos: Vector3): Color = {
    val scene = new Scene(
      List(Thing(new Plane(Vector3(0.0, 0.0, 1.0).normalize, 0.0), WhiteDiffuseMaterial)),
      List(new StaticPointLight(lightPos, Color(1.0, 2.0, 3.0))))

    val raytrace = new Raytrace(scene)
    val colorHeadOn: Color = raytrace.raytrace(new Ray(rayStart.to4, (rayStart * -1.0).to4(-1.0).spatiallyNormalize()))
    colorHeadOn
  }

  "directLighting" should "be exactly as expected for direct hit" in {
    val colorHeadOn = singleRayTest(Vector3(0.0, 0.0, 1.0), Vector3(0.0, 0.0, 1.0))
    colorHeadOn should be(Color(1.0, 2.0, 3.0))
  }

  it should "be 4 times dimmer when light is twice as far away" in {
    val colorHeadOn = singleRayTest(Vector3(0.0, 0.0, 1.0), Vector3(0.0, 0.0, 2.0))

    colorHeadOn should be(Color(0.25, 0.5, 0.75))
  }

  it should "be no dimmer when ray starts further away" in {
    val colorHeadOn = singleRayTest(Vector3(0.0, 0.0, 10.0), Vector3(0.0, 0.0, 1.0))

    colorHeadOn should be(Color(1.0, 2.0, 3.0))
  }

  it should "be no dimmer when ray comes from an angle" in {
    val colorHeadOn = singleRayTest(Vector3(1.0, 1.0, 1.0), Vector3(0.0, 0.0, 1.0))

    colorHeadOn should be(Color(1.0, 2.0, 3.0))
  }

  it should "be dimmer when light is at an angle" in {
    val colorHeadOn = singleRayTest(Vector3(0.0, 0.0, 1.0), Vector3(0.0, 1.0, 1.0).normalize)
    colorHeadOn should colorMatch(Color(1.0, 2.0, 3.0) * Math.cos(degreesToRadians(45)))
  }
}
