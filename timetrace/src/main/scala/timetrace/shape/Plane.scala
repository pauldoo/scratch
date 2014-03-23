package timetrace.shape

import timetrace.math.Vector4
import timetrace.Ray
import timetrace.RayHit

/**
 * Set of points x, satisfying x dot normal == offset
 */
class Plane(val normal: Vector4, val offset: Double) extends Shape {
  assume(normal.isNormalized)

  override def toString = s"Plane($normal, $offset)"

  def intersect(ray: Ray): RayHit = {
    val t = (offset - (ray.start dot normal)) / (ray.direction.to4 dot normal)
    return new RayHit(ray, t)
  }

}