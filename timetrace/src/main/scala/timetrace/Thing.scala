package timetrace

import timetrace.shape.Shape
import timetrace.material.Material
import timetrace.math.RayLike

sealed case class Thing(val shape: Shape, val material: Material) {

  def intersect[R <: RayLike](ray: R): Option[Hit[R]] = {
    shape.intersect(ray).map(sh => new Hit[R](ray, sh, material))
  }
}
