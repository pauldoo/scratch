package timetrace.light

import timetrace.Color
import timetrace.math.Vector3
import scala.collection.immutable.NumericRange

class SinglePulsePointLight(val location: Vector3, val color: Color, val minT: Double, val maxT: Double) extends Light {
  def colorAtTime(t: Double): Color = {

    if (minT <= t && t < maxT) {
      color
    } else {
      Color.BLACK
    }
  }

  def emitPhoton = ???
}
