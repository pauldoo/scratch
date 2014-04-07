package timetrace.light

import timetrace.math.Vector3
import timetrace.Color

trait Light {
  val location: Vector3
  val color: Color
}