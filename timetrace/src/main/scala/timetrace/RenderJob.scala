package timetrace

import java.io.File
import timetrace.camera.Camera

case class RenderJob(
  val scene: Scene,
  val camera: Camera,
  val maxT: Double,
  val photonCount: Int,
  val widthInPixels: Int,
  val heightInPixels: Int,
  val frameCount: Int,
  val outputFolder: File) {

}