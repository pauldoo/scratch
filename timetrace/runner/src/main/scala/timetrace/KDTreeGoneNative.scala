package timetrace

import timetrace.kdtree.KDTree
import timetrace.kdtree.KDTreeInMemory
import timetrace.photon.Photon
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

object KDTreeGoneNative {
  def buildFromInMemory(kdTree: KDTreeInMemory[Photon]): KDTreeGoneNative = {
    val output = new ByteArrayOutputStream()
    val traversed: IndexedSeq[Photon] = KDTreeInMemory.inOrderTraversalWrite(output, kdTree)

    new KDTreeGoneNative(traversed.toArray, output.toByteArray())
  }
}

class KDTreeGoneNative(val traversed: Array[Photon], val serializedForm: Array[Byte]) extends KDTree[Photon] {

  @transient
  val childProcess = new ThreadLocal[Process]() {
    override def initialValue(): Process = {
      val file = File.createTempFile("photon-map", ".map")
      println(s"Saving to ${file.getAbsolutePath}")
      file.deleteOnExit();
      val output = new FileOutputStream(file)
      output.write(serializedForm)
      output.close()

    }
  }

  def findClosestTo(target: Vector4, n: Int, interestingHemisphere: Vector4): List[Photon] = {
    val proc = getProcessStartingIfNecessary()
  }

}
