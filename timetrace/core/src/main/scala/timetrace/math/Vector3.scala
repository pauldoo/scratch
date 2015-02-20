package timetrace.math

import timetrace.math.Vector3.Normalized
import scala.util.Random
import org.apache.commons.math3.random.RandomGenerator

object Vector3 {
  class Normalized(x: Double, y: Double, z: Double) extends Vector3(x, y, z) {
    assume(Math.abs(magnitude - 1.0) < 1e-6)
    override val normalize = this
    override def isNormalized() = true

    def toSpatiallyNormalized4(t: Double): Vector4.SpatiallyNormalized = {
      assert(t == -1.0 || t == 1.0)
      new Vector4.SpatiallyNormalized(this, t)
    }
  }

  def randomUnit(rng: RandomGenerator): Vector3.Normalized = {
    def randomBoxVector(): Vector3 = new Vector3(rng.nextDouble() - 0.5, rng.nextDouble() - 0.5, rng.nextDouble() - 0.5) * 2.0
    def isAppropriatelySized(v: Vector3): Boolean = v.magnitude <= 1.0 && v.magnitude >= 0.01

    Iterator //
      .continually(randomBoxVector()) //
      .find(isAppropriatelySized) //
      .get.normalize
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
