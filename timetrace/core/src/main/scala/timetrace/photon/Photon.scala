package timetrace.photon

import timetrace.Color
import timetrace.math.Vector3
import timetrace.math.Vector4
import timetrace.math.RayLike
import timetrace.math.MathUtils
import timetrace.photon.Photon._

object Photon {
  val goldenRatio = (1.0 + Math.sqrt(5.0)) / 2.0 
}

/// Represents light from a light source as it is inbound on an interacting thing
case class Photon(val location: Vector4, val direction: Vector4.SpatiallyNormalized, val bounceCount: Int) extends RayLike {
  assume(direction.t == 1.0) // Photons only travel forward in time
  
  def tweakForward(): Photon = new Photon(this.march(MathUtils.SMALL_CONSTANT), direction, bounceCount)
  
  def color(): Color = {
    val hue = (bounceCount * goldenRatio * 6.0) % 6.0
    assert(0.0 <= hue && hue < 6.0)
    
    def foo(c: Double) = {
      MathUtils.clamp(0.0, 2.0 - Math.abs(hue - c), 1.0) + //
      MathUtils.clamp(0.0, 2.0 - Math.abs(hue - (c+6.0)), 1.0) + //
      MathUtils.clamp(0.0, 2.0 - Math.abs((hue+6.0) - c), 1.0)
    }
    
    Color(foo(1.0), foo(3.0), foo(5.0))
  }
}
