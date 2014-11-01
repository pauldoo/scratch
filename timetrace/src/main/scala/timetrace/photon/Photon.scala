package timetrace.photon

import timetrace.Color
import timetrace.math.Vector3
import timetrace.math.Vector4

case class Photon(val location: Vector4, val direction: Vector3.Normalized, val color: Color) {

}