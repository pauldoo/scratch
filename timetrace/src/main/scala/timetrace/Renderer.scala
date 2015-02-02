package timetrace

import java.io.File
import timetrace.camera.Camera
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import timetrace.math.Vector4
import org.apache.spark.rdd.RDD
import timetrace.photon.Photon
import timetrace.light.Light
import org.apache.spark.broadcast.Broadcast
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import timetrace.camera.DefaultStillCamera
import timetrace.light.StaticPointLight
import timetrace.shape.Plane
import timetrace.math.Vector3
import timetrace.light.StaticPointLight
import timetrace.material.WhiteDiffuseMaterial

object Renderer {

  def main(args: Array[String]): Unit = {
    val camera: Camera = DefaultStillCamera
    val scene = new Scene( //
      List(Thing(new Plane(Vector3(0.0, 1.0, 0.0).normalize(), -1.0), WhiteDiffuseMaterial)), //
      List(new StaticPointLight(Vector3(0.0, 1.0, 0.0), Color.WHITE)))
    render(new RenderJob(scene, camera, 1.0, 100000, 320, 240, 10, null, 1.0, 1.0 / 1.8))
  }

  def render(job: RenderJob): Unit = {
    val sparkConf = new SparkConf().setAppName("timetrace").setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    try {
      val photons: RDD[Photon] = sparkContext.parallelize(1 to 1000, 1000).flatMap(generatePhotonBatch(job))

      val photonMap: Broadcast[PhotonMap] = sparkContext.broadcast(buildPhotonMap(photons.collect))

      val frames: RDD[Frame] = sparkContext.parallelize(1 to job.frameCount).map(renderFrame(job, photonMap))

      //frames.saveAsObjectFile("var/frames")

      val images: Iterator[(Int, Array[Byte])] = frames.map(convertToImageFile(job)).toLocalIterator

      for (i <- images) {
        val filename = s"var/frame_${i._1}.png"
        println(s"Saving ${filename}")
        val out = new FileOutputStream(filename)
        out.write(i._2)
        out.close()
      }

    } finally {
      sparkContext.stop
    }

  }

  def buildPhotonMap(photons: Array[Photon]) = {
    PhotonMap.build(photons)
  }

  def renderFrame(job: RenderJob, photonMap: Broadcast[PhotonMap])(n: Int): Frame = {
    println(s"Rendering frame ${n}, found ${photonMap.value.photons.length} photons.")

    val raytracer: Raytrace = new Raytrace(job.scene)
    val t: Double = n.toDouble / job.frameCount

    val pixels: Seq[Color] =
      for {
        yi <- 0 until job.heightInPixels
        y = yi.toDouble / (job.heightInPixels - 1)
        xi <- 0 until job.widthInPixels
        x = xi.toDouble / (job.widthInPixels - 1)
        ray = job.camera.generateRay(x, y, t)
      } yield {
        raytracer.raytrace(ray)
      }

    val pixelsArray: Array[Color] = pixels.toArray
    new Frame(n, job.widthInPixels, job.heightInPixels, pixelsArray)
  }

  def generatePhotonBatch(job: RenderJob)(n: Int): Seq[Photon] = {
    Iterator.continually(generatePhotonsFromSingleEmission(job)).flatten.take(job.photonCount / 1000).toSeq
  }

  def generatePhotonsFromSingleEmission(job: RenderJob): Seq[Photon] = {
    List(Photon(null, null, null))
  }

  def convertToImageFile(job: RenderJob)(frame: Frame): (Int, Array[Byte]) = {
    val image = new BufferedImage(frame.width, frame.height, BufferedImage.TYPE_INT_ARGB);
    def exposeAndGamma(v: Double): Int = {
      val r: Double = Math.pow(1.0 - Math.exp(-v * job.exposure), job.encodingGamma) * 256;
      Math.max(0, Math.min(Math.round(r).toInt, 255))
    }
    def colorToRgb(c: Color): Int = {
      255 << 24 |
        exposeAndGamma(c.red) << 16 |
        exposeAndGamma(c.green) << 8 |
        exposeAndGamma(c.blue)
    }
    for (y <- 0 until frame.height; x <- 0 until frame.width) {
      image.setRGB(x, y, colorToRgb(frame.pixels(y * frame.width + x)))
    }
    val buffer = new ByteArrayOutputStream()
    ImageIO.write(image, "png", buffer)
    (frame.number, buffer.toByteArray())
  }
}
