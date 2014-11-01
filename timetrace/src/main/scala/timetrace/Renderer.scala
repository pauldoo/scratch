package timetrace

import java.io.File
import timetrace.camera.Camera
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import timetrace.math.Vector4
import org.apache.spark.rdd.RDD
import timetrace.photon.Photon
import timetrace.RenderJob
import timetrace.light.Light
import org.apache.spark.broadcast.Broadcast

object Renderer {

  def main(args: Array[String]): Unit = {
    render(new RenderJob(null, null, 1.0, 1000000, 640, 480, 100, null));
  }

  def render(job: RenderJob): Unit = {
    val sparkConf = new SparkConf().setAppName("timetrace").setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    try {
      val photons: RDD[Photon] = sparkContext.parallelize(1 to 1000, 1000).flatMap(generatePhotonBatch(job))

      val photonMap: Broadcast[PhotonMap] = sparkContext.broadcast(buildPhotonMap(photons.collect))

      val frames: RDD[Frame] = sparkContext.parallelize(1 to job.frameCount).map(renderFrame(job, photonMap))

      frames.saveAsObjectFile("var/frames")

    } finally {
      sparkContext.stop
    }

  }

  def buildPhotonMap(photons: Array[Photon]) = {
    PhotonMap.build(photons)
  }

  def renderFrame(job: RenderJob, photonMap: Broadcast[PhotonMap])(n: Int): Frame = {
    println(s"Rendering frame ${n}, found ${photonMap.value.photons.length} photons.")

    val pixels = new Array[Color](job.widthInPixels * job.heightInPixels)
    for (i <- 0 until pixels.length) {
      pixels(i) = Color.BLACK
    }

    new Frame(job.widthInPixels, job.heightInPixels, pixels)
  }

  def generatePhotonBatch(job: RenderJob)(n: Int): Seq[Photon] = {
    Iterator.continually(generatePhotonsFromSingleEmission(job)).flatten.take(job.photonCount / 1000).toSeq
  }

  def generatePhotonsFromSingleEmission(job: RenderJob): Seq[Photon] = {
    List(Photon(null, null, null))
  }
}