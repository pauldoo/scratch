package timetrace.shape

import timetrace.Ray
import timetrace.RayHit
import timetrace.math.Vector4

trait Shape extends Serializable {

  def intersect(ray: Ray): Option[ShapeHit]
}
