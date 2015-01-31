package timetrace.camera

import timetrace.Ray
import timetrace.math.Vector4
import timetrace.math.Vector3

object DefaultStillCamera extends Camera {
  def generateRay(x: Double, y: Double, t: Double): Ray = {
    return new Ray( //
      Vector4(0.0, 0.0, 0.0, t), //
      Vector3((x - 0.5), (y - 0.5), 0.5).normalize)
  }
}
