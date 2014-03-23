package timetrace.shape

import timetrace.math.Vector4
import timetrace.Ray
import timetrace.RayHit

/**
 * Set of points x, st x `dot` normal == ofset
 */
class Plane(val normal: Vector4, val offset: Double) extends Shape {

  def intersect(ray: Ray): RayHit = {
    val t = -(offset + (ray.start dot normal)) / (ray.direction.to4 dot normal)
    return new RayHit(ray, t)
  }

}