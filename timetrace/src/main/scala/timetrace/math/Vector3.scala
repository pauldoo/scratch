package timetrace.math

import timetrace.math.Vector3.Normalized

object Vector3 {
  class Normalized(x: Double, y: Double, z: Double) extends Vector3(x, y, z) {
    assume(Math.abs(magnitude - 1.0) < 1e-6)
    override val normalize = this
    override def isNormalized() = true
  }
}

case class Vector3(val x: Double, val y: Double, val z: Double) extends VectorN[Vector3] {

  override def toString(): String = s"[$x, $y, $z]"

  def to4(): Vector4 = new Vector4(x, y, z, 1.0)

  def to4(t: Double): Vector4 = new Vector4(x, y, z, t)

  def -(that: Vector3): Vector3 = Vector3(
    this.x - that.x,
    this.y - that.y,
    this.z - that.z)

  def dot(that: Vector3): Double = (
    this.x * that.x +
    this.y * that.y +
    this.z * that.z)

  def *(s: Double): Vector3 = Vector3(x * s, y * s, z * s)

  def normalize(): Vector3.Normalized = {
    val mag = magnitude()
    new Normalized(x / mag, y / mag, z / mag)
  }
}
