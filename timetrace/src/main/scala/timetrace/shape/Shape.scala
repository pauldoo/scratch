package timetrace.shape

import timetrace.Ray
import timetrace.RayHit

trait Shape {

  def intersect(ray: Ray): RayHit
}