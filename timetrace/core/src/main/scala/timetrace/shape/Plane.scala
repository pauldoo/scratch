package timetrace.shape

import timetrace.math.Vector4
import timetrace.Ray
import timetrace.math.Vector3
import timetrace.math.RayLike

/**
 * Set of points x, satisfying x dot normal == offset
 */
class Plane(val normal: Vector3.Normalized, val offset: Double) extends NonRandomShape {

  override def toString = s"Plane($normal, $offset)"

  def intersect(ray: RayLike): Option[ShapeHit] = {
    val t = (offset - (ray.location.truncateTo3 dot normal)) / (ray.direction.truncateTo3() dot normal)

    if (t > 0.0 && t < Double.PositiveInfinity)
      Some(new ShapeHit(t, normal))
    else
      None
  }

}
