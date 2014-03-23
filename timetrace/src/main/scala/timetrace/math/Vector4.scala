package timetrace.math

sealed case class Vector4(val x: Double, val y: Double, val z: Double, val t: Double) {

  override def toString(): String = s"[$x, $y, $z, $t]"

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

}