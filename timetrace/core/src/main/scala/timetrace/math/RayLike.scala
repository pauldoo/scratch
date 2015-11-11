package timetrace.math

trait RayLike extends PointLike {
  val direction: Vector4.SpatiallyNormalized

  def march(v: Double): Vector4 = {
    location + direction * v
  }
}
