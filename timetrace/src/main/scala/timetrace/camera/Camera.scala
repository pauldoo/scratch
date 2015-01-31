package timetrace.camera

import timetrace.Ray

trait Camera {
  /**
   * x, y in the range [0.0, 1.0]
   */
  def generateRay(x: Double, y: Double, t: Double): Ray
}