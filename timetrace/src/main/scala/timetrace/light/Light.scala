package timetrace.light

import timetrace.math.Vector3
import timetrace.Color
import timetrace.photon.Photon
import scala.util.Random
import org.apache.commons.math3.random.RandomGenerator

trait Light extends Serializable {
  val location: Vector3
  def colorAtTime(t: Double): Color

  def emitPhoton(rng: RandomGenerator): Photon
}
