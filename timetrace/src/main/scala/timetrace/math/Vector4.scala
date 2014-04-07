package timetrace.math

import timetrace.math.Vector4.Normalized

object Vector4 {
  class Normalized(x: Double, y: Double, z: Double, t: Double) extends Vector4(x, y, z, t) {
    assume(Math.abs(magnitude - 1.0) < 1e-6)
    override val normalize = this
    override def isNormalized() = true
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

}