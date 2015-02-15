package timetrace

import timetrace.math.Vector3
import timetrace.math.Vector4

sealed case class Ray(val start: Vector4, val direction: Vector4.SpatiallyNormalized) {

  override def toString: String = s"Ray($start -> $direction)"

  def march(v: Double): Vector4 = {
    start + direction * v
  }
}
