package timetrace.shape

import timetrace.math.Vector4
import timetrace.math.Vector3

// TODO: Accept a Vector4 to allow for moving surfaces
sealed case class ShapeHit(val t: Double, val normal: Vector3) {

  assume(t > 0)

}