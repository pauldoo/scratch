package timetrace

import timetrace.math.Vector4
import timetrace.material.Material
import timetrace.shape.ShapeHit

sealed case class RayHit(
  val ray: Ray,
  val shapeHit: ShapeHit,
  val material: Material) {
}
