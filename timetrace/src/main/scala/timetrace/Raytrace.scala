package timetrace

import timetrace.light.Light
import timetrace.math.Vector3
import timetrace.math.MathUtils._
import timetrace.math.Vector4

class Raytrace(val scene: Scene) {

  def raytrace(ray: Ray): Color = {

    val hit: Option[RayHit] = firstHit(ray)
    hit.map(calculateDirectLighting _).getOrElse(Color.BLACK)

  }

  def firstHit(ray: Ray): Option[RayHit] = {
    def pickClosest(a: RayHit, b: RayHit) = {
      if (a.shapeHit.t < b.shapeHit.t) a else b
    }

    scene.things.flatMap(_.intersect(ray)).reduceOption(pickClosest _)
  }

  def calculateDirectLighting(hit: RayHit): Color = {

    def contributionFromLight(light: Light): Color = {
      val hitLocation: Vector4 = hit.ray.marchBackwardInTime(hit.shapeHit.t)

      val pathToLight: Vector3 = light.location - hitLocation.truncateTo3
      val contribution: Double = pathToLight.normalize dot hit.shapeHit.normal
      val distance: Double = pathToLight.magnitude
      val attenuation: Double = 1.0 / square(distance)

      val relevantTimeAtLightSource = hitLocation.t - distance

      light.colorAtTime(relevantTimeAtLightSource) * contribution * attenuation
    }

    scene.lights.map(contributionFromLight _).reduce(_ + _)
  }

}
