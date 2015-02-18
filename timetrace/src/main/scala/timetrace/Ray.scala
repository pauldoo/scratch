package timetrace

import timetrace.math.Vector3
import timetrace.math.Vector4
import timetrace.math.RayLike

class Ray(val location: Vector4, val direction: Vector4.SpatiallyNormalized) extends RayLike {
  assume(direction.t == -1.0) // Photons only travel backward in time

  override def toString: String = s"Ray($location -> $direction)"
}
