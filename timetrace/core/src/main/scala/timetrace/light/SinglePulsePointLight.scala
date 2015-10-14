package timetrace.light

import timetrace.Color
import timetrace.math.Vector3
import scala.collection.immutable.NumericRange
import timetrace.photon.Photon
import scala.util.Random
import org.apache.commons.math3.random.RandomGenerator

class SinglePulsePointLight(val location: Vector3, val color: Color, val minT: Double, val maxT: Double) extends Light {

  def colorAtTime(t: Double): Color = {

    if (minT <= t && t < maxT) {
      color
    } else {
      Color.BLACK
    }
  }

  def emitPhoton(rng: RandomGenerator): Photon = {
    val t = rng.nextDouble() * (maxT - minT) + minT
    new Photon(location.to4(t), Vector3.randomUnit(rng).toSpatiallyNormalized4(1.0), 0)
  }
}
