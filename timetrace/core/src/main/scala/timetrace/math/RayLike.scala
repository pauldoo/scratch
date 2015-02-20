package timetrace.math

import timetrace.kdtree.PointLike

trait RayLike extends PointLike {
  def location(): Vector4

  def direction(): Vector4.SpatiallyNormalized

  def march(v: Double): Vector4 = {
    location + direction * v
  }
}
