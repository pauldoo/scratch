package timetrace.light

import timetrace.math.Vector3
import timetrace.Color

trait Light extends Serializable {
  val location: Vector3
  val color: Color
}
