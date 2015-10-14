package timetrace.shape

import timetrace.math.RayLike
import timetrace.math.Vector3
import timetrace.math.MathUtils

/**
 * Static, unmoving sphere.
 */
class Sphere(val center: Vector3, val radius: Double) extends NonRandomShape {
  def intersect(ray: RayLike): Option[ShapeHit] = {
    val offset = ray.location.truncateTo3() - center 
    val a = ray.direction.truncateTo3().magnitudeSquared()
    val b = 2.0 * (ray.direction.truncateTo3() dot offset)
    val c = offset.magnitudeSquared() - (radius * radius)
    
    val solution :Option[Double] = MathUtils.lowestPositiveQuadraticSolution(a, b, c)
    
    solution.map(t => {
        val hitLocation :Vector3 = ray.march(t).truncateTo3()
        val absoluteDistanceFromSurface = Math.abs((hitLocation - center).magnitude() - radius)
        assert(absoluteDistanceFromSurface <= 1e-3)
        
        new ShapeHit(t, (hitLocation - center).normalize())
      })
      
  }
}