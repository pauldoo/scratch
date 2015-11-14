package timetrace.kdtree

import java.io.DataOutputStream
import timetrace.math.Vector4
import java.io.OutputStream
import timetrace.photon.Photon
import java.io.InputStream
import java.io.DataInputStream
import java.io.EOFException

object IOUtils {
  def writeLittleEndianInteger(out: OutputStream, i: Int): Unit = {
    out.write((i >>> 0) & 0xFF)
    out.write((i >>> 8) & 0xFF)
    out.write((i >>> 16) & 0xFF)
    out.write((i >>> 24) & 0xFF)
  }

  def writeLittleEndianFloat(out: OutputStream, v: Float): Unit = {
    writeLittleEndianInteger(out, java.lang.Float.floatToIntBits(v))
  }

  def writeVec(out: OutputStream, v: Vector4): Unit = {
    writeLittleEndianFloat(out, v.x.toFloat)
    writeLittleEndianFloat(out, v.y.toFloat)
    writeLittleEndianFloat(out, v.z.toFloat)
    writeLittleEndianFloat(out, v.t.toFloat)
  }

  def writePhoton(out: OutputStream, p: Photon): Unit = {
    writeVec(out, p.location)
    writeVec(out, p.direction)
  }

  def readLittleEndianInteger(in: InputStream): Int = {
    val ch4 = in.read()
    val ch3 = in.read()
    val ch2 = in.read()
    val ch1 = in.read()

    if ((ch1 | ch2 | ch3 | ch4) < 0)
      throw new EOFException()

    ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4 << 0))
  }

}
