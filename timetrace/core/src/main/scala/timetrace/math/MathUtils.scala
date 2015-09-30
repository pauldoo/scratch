package timetrace.math

import org.apache.commons.math3.random.RandomGenerator

object MathUtils {
  val SMALL_CONSTANT = 1e-6
  
  def square(x: Double): Double = x * x

  def degreesToRadians(x: Double): Double = x * (Math.PI / 180.0)
  
  def lowestPositiveQuadraticSolution(a: Double, b: Double, c: Double): Option[Double] = {
    val det = (b*b) - (4*a*c)

    if (det >= 0.0) {
      val detSqrt = Math.sqrt(det)
      val x1 = (-detSqrt - b) / (2*a)
      val x2 = (detSqrt - b) / (2*a)
      if (Math.min(x1, x2) > 0.0) {
        return Some(Math.min(x1, x2))
      }
      if (Math.max(x1, x2) > 0.0) {
        return Some(Math.max(x1, x2))
      }
    }
    return Option.empty
  }
  
  def randomDirectionInHemisphere(rng: RandomGenerator, hemisphere: Vector3.Normalized) : Vector3.Normalized = {
    Iterator.continually(Vector3.randomUnit(rng)).find(v => (v dot hemisphere) > 0.0).get
  }
}