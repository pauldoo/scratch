package timetrace.math

import timetrace.kdtree.PointLike

trait RayLike extends PointLike {
  val direction: Vector4.SpatiallyNormalized

  def march(v: Double): Vector4 = {
    location + direction * v
  }
}
