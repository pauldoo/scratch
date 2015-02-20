package timetrace

import timetrace.light.Light
import timetrace.math.Vector3
import timetrace.math.MathUtils._
import timetrace.math.Vector4
import timetrace.math.RayLike
import timetrace.photon.Photon
import scala.util.Random
import org.apache.commons.math3.random.RandomGenerator

class Raytrace(val scene: Scene) {

  def raytrace(ray: Ray): Color = {
    assert(ray.direction.t == -1.0)

    val hit: Option[Hit[Ray]] = firstHit(ray)
    hit.map(calculateDirectLighting _).getOrElse(Color.BLACK)

  }

  def firstHit[R <: RayLike](ray: R): Option[Hit[R]] = {
    def pickClosest(a: Hit[R], b: Hit[R]): Hit[R] = {
      if (a.shapeHit.t < b.shapeHit.t) a else b
    }

    scene.things.flatMap(_.intersect(ray)).reduceOption(pickClosest _)
  }

  def calculateDirectLighting(hit: Hit[Ray]): Color = {

    def contributionFromLight(light: Light): Color = {
      val hitLocation: Vector4 = hit.ray.march(hit.shapeHit.t)

      val pathToLight: Vector3 = light.location - hitLocation.truncateTo3
      val contribution: Double = pathToLight.normalize dot hit.shapeHit.normal
      val distance: Double = pathToLight.magnitude
      val attenuation: Double = 1.0 / square(distance)

      val relevantTimeAtLightSource = hitLocation.t - distance

      light.colorAtTime(relevantTimeAtLightSource) * contribution * attenuation
    }

    scene.lights.map(contributionFromLight _).reduce(_ + _)
  }

  def generatePhotons(rng: RandomGenerator): List[Photon] = {
    assume(scene.lights.size == 1)

    val light = scene.lights(0)

    val photon: Photon = light.emitPhoton(rng)

    val hit: Option[Hit[Photon]] = firstHit(photon)

    hit.map(ph => {
      val hitLocation = ph.ray.march(ph.shapeHit.t)

      new Photon(hitLocation, ph.ray.direction, ph.ray.color)
    }).toList
  }

}
