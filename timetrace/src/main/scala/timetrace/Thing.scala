package timetrace

import timetrace.shape.Shape
import timetrace.material.Material

sealed case class Thing(val shape: Shape, val material: Material) {

  def intersect(ray: Ray): Option[RayHit] = {
    shape.intersect(ray).map(sh => RayHit(ray, sh, material))
  }
}