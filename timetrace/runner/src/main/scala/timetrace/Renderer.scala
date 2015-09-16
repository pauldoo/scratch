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
import timetrace.light.SinglePulsePointLight
import timetrace.shape.Plane
import timetrace.math.Vector3
import timetrace.material.WhiteDiffuseMaterial
import scala.util.Random
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import timetrace.kdtree.KDTree
import timetrace.photon.PhotonMap
import org.apache.spark.storage.StorageLevel
import com.google.common.base.Stopwatch
import java.util.concurrent.TimeUnit

object Renderer {

  private val PHOTON_SCATTERING_PARTITIONS = 1000

  def main(args: Array[String]): Unit = {
    val camera: Camera = DefaultStillCamera
    val scene = new Scene( //
      List(Thing(new Plane(Vector3(0.0, 1.0, 0.0).normalize(), -1.0), WhiteDiffuseMaterial)), //
      List(new SinglePulsePointLight(Vector3(0.0, 1.0, 0.0), Color.WHITE, 0.0, 1.0)))

    val downscale = 4

    val job = new RenderJob(scene, camera, 20.0, 1000000, 1920 / downscale, 1080 / downscale, 100, null, 10.0, 1.0 / 1.8)

    render(job)
  }

  def render(job: RenderJob): Unit = {
    val sparkConf = new SparkConf().setAppName("timetrace").setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    val stopwatch = new Stopwatch().start()
    
    try {
      val photons: RDD[Photon] = sparkContext //
        .parallelize(1 to PHOTON_SCATTERING_PARTITIONS, PHOTON_SCATTERING_PARTITIONS) //
        .flatMap(generatePhotonBatch(job))

      val photonMap: Broadcast[PhotonMap] = sparkContext.broadcast(buildPhotonMap(job, photons.collect.toList))
      
      val frames: RDD[Frame] = sparkContext.parallelize(0 to job.frameCount).map(renderFrame(job, photonMap)).persist( StorageLevel.MEMORY_AND_DISK )

      // Force all calculations to occur, in parallel
      frames.count()
      
      val images: Iterator[(Int, Array[Byte])] = frames.map(convertToImageFile(job)).toLocalIterator

      for (i <- images) {
        val filename = f"../var/frame_${i._1}%06d.png"
        println(s"Saving ${filename}")
        val out = new FileOutputStream(filename)
        out.write(i._2)
        out.close()
      }

    } finally {
      sparkContext.stop
    }
    
    stopwatch.stop()
    val elapsedTimeInSeconds = stopwatch.elapsed(TimeUnit.SECONDS)
    println(f"Completed in ${elapsedTimeInSeconds}")

  }

  def buildPhotonMap(job: RenderJob, photons: List[Photon]): PhotonMap = {
    val kdtree = KDTree.build(photons)
    new PhotonMap(job.photonCount, kdtree)
  }

  def renderFrame(job: RenderJob, photonMapBroadcast: Broadcast[PhotonMap])(n: Int): Frame = {
    println(s"Rendering frame ${n}.")

    val photonMap = photonMapBroadcast.value

    val raytracer: Raytrace = new Raytrace(job.scene)
    val t: Double = n.toDouble * (job.maxT / job.frameCount)
    println(s"Rendering t=${t}")

    val averageHalfSizeInPixels: Double = (job.widthInPixels + job.heightInPixels) / 4.0;

    def iToR(i: Int, max: Int) = (i - ((max - 1) / 2.0)) / averageHalfSizeInPixels

    val pixels: Seq[Color] =
      for {
        yi <- 0 until job.heightInPixels
        y = iToR(yi, job.heightInPixels)
        xi <- 0 until job.widthInPixels
        x = iToR(xi, job.widthInPixels)
        ray = job.camera.generateRay(x, y, t)
      } yield {
        raytracer.raytrace(ray, photonMap)
      }

    val pixelsArray: Array[Color] = pixels.toArray
    new Frame(n, job.widthInPixels, job.heightInPixels, pixelsArray)
  }

  def generatePhotonBatch(job: RenderJob)(n: Int): Seq[Photon] = {
    val raytracer: Raytrace = new Raytrace(job.scene)

    val photonsToGenerate = job.photonCount / PHOTON_SCATTERING_PARTITIONS
    val rng: RandomGenerator = new MersenneTwister

    Iterator.continually(raytracer.generatePhotons(rng)).flatten.take(photonsToGenerate).toSeq
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
      image.setRGB(x, frame.height - y - 1, colorToRgb(frame.pixels(y * frame.width + x)))
    }
    val buffer = new ByteArrayOutputStream()
    ImageIO.write(image, "png", buffer)
    (frame.number, buffer.toByteArray())
  }
}
