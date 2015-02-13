package timetrace.light

import timetrace.Color
import timetrace.math.Vector3

class SinglePulsePointLight(val location: Vector3, val color: Color, val pulseDuration: Double) extends Light {
  def colorAtTime(t: Double): Color = {
    if (0.0 <= t && t < pulseDuration) {
      color
    } else {
      Color.BLACK
    }
  }
}
