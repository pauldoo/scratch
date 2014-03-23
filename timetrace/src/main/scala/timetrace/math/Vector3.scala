package timetrace.math

sealed case class Vector3(val x: Double, val y: Double, val z: Double) {

  override def toString(): String = s"[$x, $y, $z]"

  def to4(): Vector4 = new Vector4(x, y, z, 1.0)
}