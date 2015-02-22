package timetrace.math

trait VectorN[Self] {
  this: Self =>

  def magnitude(): Double = Math.sqrt(this dot this)

  def isNormalized() = false

  def dot(that: Self): Double

  def *(s: Double): Self

  def +(that: Self): Self

  def -(that: VectorN[Self]) = this + (that * -1.0)
}
