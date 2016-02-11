package timetrace.kdtree

import timetrace.math.Vector4

sealed trait Axis {
  def extractor(p: Vector4): Double

  def set(p: Vector4, v: Vector4): Vector4
}

case object X extends Axis {
  def extractor(p: Vector4): Double = p.x

  def set(p: Vector4, v: Vector4): Vector4 =
    new Vector4(v.x, p.y, p.z, p.t)
}

case object Y extends Axis {
  def extractor(p: Vector4): Double = p.y

  def set(p: Vector4, v: Vector4): Vector4 =
    new Vector4(p.x, v.y, p.z, p.t)
}

case object Z extends Axis {
  def extractor(p: Vector4): Double = p.z

  def set(p: Vector4, v: Vector4): Vector4 =
    new Vector4(p.x, p.y, v.z, p.t)
}

case object T extends Axis {
  def extractor(p: Vector4): Double = p.t

  def set(p: Vector4, v: Vector4): Vector4 =
    new Vector4(p.x, p.y, p.z, v.t)
}
