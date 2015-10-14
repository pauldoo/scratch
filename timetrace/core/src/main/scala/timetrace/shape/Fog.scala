package timetrace.shape

import timetrace.Thing
import org.apache.commons.math3.random.RandomGenerator
import timetrace.math.RayLike
import timetrace.shape.Fog._
import timetrace.math.MathUtils

object Fog {
  val log_2 = math.log(2.0)
}

/**
 * WUT?!  Yes, turns out I might be able to model participating media as a geometry type.
 */
class Fog(val halfLifeLength: Double) extends Shape {
  assert(0.0 < halfLifeLength)
  
  def intersect(ray: RayLike, rng: RandomGenerator): Option[ShapeHit] = {
    Some(rng.nextDouble()) //
      .filter( _ > 0.0 ) //
      .map( p => {
        val halfLifeLengthsTravelled = math.log(1.0 / p) / log_2
        ShapeHit(halfLifeLengthsTravelled * halfLifeLength, MathUtils.randomDirectionInHemisphere(rng, ray.direction.truncateTo3() * (-1.0)))       
      } )
    }
}

