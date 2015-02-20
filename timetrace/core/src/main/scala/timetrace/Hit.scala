package timetrace

import timetrace.math.Vector4
import timetrace.material.Material
import timetrace.shape.ShapeHit
import timetrace.math.RayLike

sealed class Hit[R <: RayLike](
  val ray: R,
  val shapeHit: ShapeHit,
  val material: Material) {
}
