package timetrace.kdtree

import timetrace.photon.Photon
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.File
import timetrace.math.Vector4
import timetrace.math.Vector4
import java.io.OutputStream
import java.io.InputStream
import java.io.BufferedOutputStream
import java.io.BufferedInputStream
import java.lang.ProcessBuilder.Redirect

object KDTreeGoneNative {
  def buildFromInMemory(kdTree: KDTreeInMemory[Photon]): KDTreeGoneNative = {
    val output = new ByteArrayOutputStream()
    val traversed: IndexedSeq[Photon] = KDTreeInMemory.inOrderTraversalWrite(output, kdTree)

    val result = new KDTreeGoneNative(traversed.toArray, output.toByteArray())

    {
      // sanity test
      val a =kdTree.findClosestTo(Vector4(0.0, 0.0, 0.0, 0.0), 100, Vector4(0.0, 0.0, 0.0, 1.0)) 
      val b =  result.findClosestTo(Vector4(0.0, 0.0, 0.0, 0.0), 100, Vector4(0.0, 0.0, 0.0, 1.0)) 
      assert(a == b)
    }

    result
  }
}

class KDTreeGoneNative(val traversed: Array[Photon], val serializedForm: Array[Byte]) extends KDTree[Photon] {

  @transient
  lazy val childProcess = {
    val file = File.createTempFile("photon-map", ".map")
    println(s"Saving to ${file.getAbsolutePath}")
    file.deleteOnExit();
    val output = new FileOutputStream(file)
    output.write(serializedForm)
    output.close()

    new ThreadLocal[(Process, OutputStream, InputStream)]() {
      override def initialValue(): (Process, OutputStream, InputStream) = {

        println("Starting pmapd")

        val p = new ProcessBuilder("../pmapd/pmapd", file.getAbsolutePath) //
          .redirectError(ProcessBuilder.Redirect.INHERIT) //
          .start()

        val out = new BufferedOutputStream(p.getOutputStream)
        val in = new BufferedInputStream(p.getInputStream)

        (p, out, in)
      }
    }
  }

  def findClosestToImp(target: Vector4, n: Int, interestingHemisphere: Vector4): Seq[Photon] = {
    val proc = childProcess.get

    sendRequest(proc._2, target, n, interestingHemisphere)

    val r = readResponse(proc._3, n)

    r
  }

  private def sendRequest(out: OutputStream, target: Vector4, n: Int, interestingHemisphere: Vector4) = {
    IOUtils.writeVec(out, target)
    IOUtils.writeVec(out, interestingHemisphere)
    IOUtils.writeLittleEndianInteger(out, n)
    out.flush()
  }

  private def readResponse(in: InputStream, n: Int): Seq[Photon] = {
    for (i <- 0 until n) yield {
      val index = IOUtils.readLittleEndianInteger(in)
      traversed(index)
    }
  }

}
