package timetrace.math

trait RayLike {
  def location(): Vector4

  def direction(): Vector4.SpatiallyNormalized

  def march(v: Double): Vector4 = {
    location + direction * v
  }
}
