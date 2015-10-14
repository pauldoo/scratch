package timetrace.shape

import timetrace.math.RayLike
import org.apache.commons.math3.random.RandomGenerator

trait NonRandomShape extends Shape {
  
  def intersect(ray: RayLike, rng: RandomGenerator): Option[ShapeHit] = intersect(ray)
  
  def intersect(ray: RayLike): Option[ShapeHit]
}