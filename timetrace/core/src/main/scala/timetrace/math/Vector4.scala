package timetrace.math

import timetrace.math.Vector4.Normalized
import timetrace.math.Vector4.SpatiallyNormalized
import scala.math.{ min, max }

object Vector4 {
  class Normalized(x: Double, y: Double, z: Double, t: Double) extends Vector4(x, y, z, t) {
    assume(Math.abs(magnitude - 1.0) < 1e-6)
    override def normalize = this
    override def isNormalized() = true
  }

  // Normalized (x, y, z) part, ensure t is +/- 1.0.
  // Used mainly to describe ray directions
  class SpatiallyNormalized(v3: Vector3.Normalized, t: Double) extends Vector4(v3.x, v3.y, v3.z, t) {
    assume(t == -1.0 || t == 1.0)
    override def spatiallyNormalize = this
  }

  def componentMinimums(v1: Vector4, v2: Vector4): Vector4 = {
    return new Vector4( //
      min(v1.x, v2.x), //
      min(v1.y, v2.y), //
      min(v1.z, v2.z), //
      min(v1.t, v2.t))
  }

  def componentMaximums(v1: Vector4, v2: Vector4): Vector4 = {
    return new Vector4( //
      max(v1.x, v2.x), //
      max(v1.y, v2.y), //
      max(v1.z, v2.z), //
      max(v1.t, v2.t))
  }

  def clamp(mins: Vector4, maxs: Vector4)(v: Vector4): Vector4 = {
    assume(mins.x <= maxs.x)
    assume(mins.y <= maxs.y)
    assume(mins.z <= maxs.z)
    assume(mins.t <= maxs.t)

    return new Vector4( //
      max(mins.x, min(maxs.x, v.x)), //
      max(mins.y, min(maxs.y, v.y)), //
      max(mins.z, min(maxs.z, v.z)), //
      max(mins.t, min(maxs.t, v.t)))
  }
}

sealed case class Vector4(val x: Double, val y: Double, val z: Double, val t: Double) extends VectorN[Vector4] {

  override def toString(): String = s"[$x, $y, $z, $t]"

  def truncateTo3(): Vector3 = new Vector3(x, y, z)

  def dot(that: Vector4): Double = (
    this.x * that.x +
    this.y * that.y +
    this.z * that.z +
    this.t * that.t)

  def *(s: Double): Vector4 = Vector4(x * s, y * s, z * s, t * s)

  def +(that: Vector4): Vector4 = Vector4(
    this.x + that.x,
    this.y + that.y,
    this.z + that.z,
    this.t + that.t)

  def normalize(): Vector4.Normalized = {
    val mag = magnitude()

    new Normalized(x / mag, y / mag, z / mag, t / mag)
  }

  def spatiallyNormalize(): Vector4.SpatiallyNormalized = {
    assert(t == -1.0 || t == 1.0)
    new SpatiallyNormalized(truncateTo3().normalize(), t)
  }
}
