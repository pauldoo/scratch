package timetrace.math

case class Vector3(val x: Double, val y: Double, val z: Double) extends VectorN[Vector3] {

  override def toString(): String = s"[$x, $y, $z]"

  def to4(): Vector4 = new Vector4(x, y, z, 1.0)

  def dot(that: Vector3): Double = (
    this.x * that.x +
    this.y * that.y +
    this.z * that.z)

  def normalize(): Vector3 = {
    val mag = magnitude()

    new Vector3(x / mag, y / mag, z / mag) {
      override val normalize = this
      override def isNormalized() = true
    }
  }
}