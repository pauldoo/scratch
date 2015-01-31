package timetrace.shape

import timetrace.math.Vector4
import timetrace.Ray
import timetrace.RayHit
import timetrace.math.Vector3

/**
 * Set of points x, satisfying x dot normal == offset
 */
class Plane(val normal: Vector3.Normalized, val offset: Double) extends Shape {

  override def toString = s"Plane($normal, $offset)"

  def intersect(ray: Ray): Option[ShapeHit] = {
    val t = (offset - (ray.start.truncateTo3 dot normal)) / (ray.direction dot normal)

    if (t > 0.0 && t < Double.PositiveInfinity)
      Some(new ShapeHit(t, normal))
    else
      None
  }

}