package timetrace.shape

import timetrace.Ray
import timetrace.math.Vector4
import timetrace.math.RayLike

trait Shape extends Serializable {

  def intersect(ray: RayLike): Option[ShapeHit]
}
