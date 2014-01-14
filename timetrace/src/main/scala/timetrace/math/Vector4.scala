package timetrace.math

sealed class Vector4(val x: Double, val y: Double, val z: Double, val t: Double) {

  override def toString(): String = s"[$x, $y, $z, $t]"
}