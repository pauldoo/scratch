package timetrace

import timetrace.shape.Shape
import timetrace.material.Material
import timetrace.math.RayLike
import org.apache.commons.math3.random.RandomGenerator

sealed case class Thing(val shape: Shape, val material: Material) {

  def intersect[R <: RayLike](ray: R, rng: RandomGenerator): Option[Hit[R]] = {
    shape.intersect(ray, rng).map(sh => new Hit[R](ray, sh, material))
  }
}
