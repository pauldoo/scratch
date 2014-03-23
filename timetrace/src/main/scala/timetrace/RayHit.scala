package timetrace

import timetrace.math.Vector4

sealed case class RayHit(val ray: Ray, val t: Double) {
  def location: Vector4 = {
    ray.start + ray.direction.to4() * t;
  }
}