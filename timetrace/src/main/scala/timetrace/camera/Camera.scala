package timetrace.camera

import timetrace.Ray

trait Camera {
  def generateRay(x: Double, y: Double, t: Double): Ray
}