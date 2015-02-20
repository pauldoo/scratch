package timetrace.math

import timetrace.math.Vector4.Normalized
import timetrace.math.Vector4.SpatiallyNormalized

object Vector4 {
  class Normalized(x: Double, y: Double, z: Double, t: Double) extends Vector4(x, y, z, t) {
    assume(Math.abs(magnitude - 1.0) < 1e-6)
    override val normalize = this
    override def isNormalized() = true
  }

  // Normalized (x, y, z) part, ensure t is +/- 1.0.
  // Used mainly to describe ray directions
  class SpatiallyNormalized(v3: Vector3.Normalized, t: Double) extends Vector4(v3.x, v3.y, v3.z, t) {
    assume(t == -1.0 || t == 1.0)
    override val spatiallyNormalize = this
  }

  def componentMinimums(v1: Vector4, v2: Vector4): Vector4 = {
    return new Vector4( //
      scala.math.min(v1.x, v2.x), //
      scala.math.min(v1.y, v2.y), //
      scala.math.min(v1.z, v2.z), //
      scala.math.min(v1.t, v2.t))
  }

  def componentMaximums(v1: Vector4, v2: Vector4): Vector4 = {
    return new Vector4( //
      scala.math.max(v1.x, v2.x), //
      scala.math.max(v1.y, v2.y), //
      scala.math.max(v1.z, v2.z), //
      scala.math.max(v1.t, v2.t))
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
