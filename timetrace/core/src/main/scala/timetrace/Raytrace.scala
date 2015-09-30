package timetrace

import timetrace.light.Light
import timetrace.math.Vector3
import timetrace.math.MathUtils._
import timetrace.math.Vector4
import timetrace.math.RayLike
import timetrace.photon.Photon
import scala.util.Random
import org.apache.commons.math3.random.RandomGenerator
import timetrace.photon.PhotonMap
import timetrace.math.MathUtils

class Raytrace(val scene: Scene) {

  def raytrace(ray: Ray, photonMap: PhotonMap): Color = {
    assert(ray.direction.t == -1.0)

    val hit: Option[Hit[Ray]] = firstHit(ray)
    hit.map(calculateGlobalLighting(photonMap)).getOrElse(Color.BLACK)

  }

  def firstHit[R <: RayLike](ray: R): Option[Hit[R]] = {
    def pickClosest(a: Hit[R], b: Hit[R]): Hit[R] = {
      if (a.shapeHit.t < b.shapeHit.t) a else b
    }

    scene.things.flatMap(_.intersect(ray)).reduceOption(pickClosest _)
  }

  def calculateGlobalLighting(photonMap: PhotonMap)(hit: Hit[Ray]): Color = {
    val hitLocation: Vector4 = hit.ray.march(hit.shapeHit.t)

    val incomingLights: List[PhotonMap.Contribution] = photonMap.incomingLightAt(hitLocation, hit.shapeHit.normal)

    def contributionFromPhoton(photon: PhotonMap.Contribution): Color = {
      val contribution: Double = - (photon.incomingDirection.truncateTo3() dot hit.shapeHit.normal)
      
      photon.color * contribution
    }
    
    incomingLights.map(contributionFromPhoton _).reduce(_+_)
  }

  def calculateDirectLighting(hit: Hit[Ray]): Color = {
	  val hitLocation: Vector4 = hit.ray.march(hit.shapeHit.t)

    def contributionFromLight(light: Light): Color = {

      val pathToLight: Vector3 = light.location - hitLocation.truncateTo3
      val contribution: Double = pathToLight.normalize dot hit.shapeHit.normal
      val distance: Double = pathToLight.magnitude
      val attenuation: Double = 1.0 / square(distance)

      val relevantTimeAtLightSource = hitLocation.t - distance

      light.colorAtTime(relevantTimeAtLightSource) * contribution * attenuation
    }

    scene.lights.map(contributionFromLight _).reduce(_ + _)
  }

  def generatePhotonStrikes(rng: RandomGenerator): List[Photon] = {
    assume(scene.lights.size == 1)
    
    val light = scene.lights(0)
    val photon: Photon = light.emitPhoton(rng)
    generatePhotonStrikes(rng, photon)
  }

  def generatePhotonStrikes(rng: RandomGenerator, emittedPhoton: Photon): List[Photon] = {

    val hit: Option[Hit[Photon]] = firstHit(emittedPhoton)

    hit.map(ph => {
    	val hitLocation = ph.ray.march(ph.shapeHit.t)
			val photonHere = new Photon(hitLocation, ph.ray.direction, ph.ray.color)
      
    	val diffuse = ph.material.diffuseComponent()
    	
    	val furtherStrikes :List[Photon] = 
      	if (rng.nextDouble() < diffuse) {
      	  val bouncedPhotonDirection: Vector3.Normalized = MathUtils.randomDirectionInHemisphere(rng, ph.shapeHit.normal)
      	  val bouncedPhoton: Photon = new Photon(hitLocation, bouncedPhotonDirection.toSpatiallyNormalized4(1.0), emittedPhoton.color).tweakForward()
          generatePhotonStrikes(rng, bouncedPhoton)
      	} else List.empty

    	photonHere :: furtherStrikes
    	
    }).getOrElse(List.empty)
  }
  
}
