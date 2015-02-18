package timetrace.light

import timetrace.math.Vector3
import timetrace.Color
import timetrace.photon.Photon

trait Light extends Serializable {
  val location: Vector3
  def colorAtTime(t: Double): Color

  def emitPhoton: Photon
}
