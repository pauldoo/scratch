package timetrace.math

trait VectorN[Self] {
  this: Self =>

  def magnitude(): Double = Math.sqrt(this dot this)

  def isNormalized() = false

  def dot(that: Self): Double
}