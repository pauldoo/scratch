package timetrace.light

import timetrace.Color
import timetrace.math.Vector3

class StaticPointLight(val location: Vector3, val color: Color) extends Light {
  def colorAtTime(t: Double) = color
}
