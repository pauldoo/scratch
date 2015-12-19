package timetrace

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import timetrace.photon.Photon
import timetrace.math.Vector4

sealed class FlatPhoton(
    val x: Float,
    val y: Float,
    val z: Float,
    val t: Float,
    val xd: Float,
    val yd: Float,
    val zd: Float,
    val td: Float,
    val col: Int) {
}

@RunWith(classOf[JUnitRunner])
class PhotonMemoryTest extends UnitSpec {
  "photons" should "fit in memory" in {

    //var vec = Vector[Photon]()
    //var vec = Vector[Array[Byte]]()
    var vec = Vector[FlatPhoton]()

    def createPhoton(): Photon = {
      new Photon(new Vector4(1, 2, 3, 4), new Vector4(5, 6, 7, 1).spatiallyNormalize(), 20)
    }

    def createFlatPhoton(): FlatPhoton = {
      new FlatPhoton(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 20)
    }

    try {
      while (true) {
        //val p: Photon = createPhoton()
        //val p: Array[Byte] = new Array[Byte](36)
        val p: FlatPhoton = createFlatPhoton()
        vec = vec :+ p

        if (vec.size == -1000000) {
          Thread.sleep(Integer.MAX_VALUE)
        }
      }
    } catch {
      case oom: OutOfMemoryError => {
        val s = vec.size
        vec = null
        println(s"\nOOM at ${s} elements")
      }
    }

    println("done")
  }
}
