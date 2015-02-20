package timetrace.camera

import timetrace.Ray

trait Camera extends Serializable {
  /**
   * x, y centered on (0.0, 0.0), ranging +/- 1.0 ish
   */
  def generateRay(x: Double, y: Double, t: Double): Ray
}
