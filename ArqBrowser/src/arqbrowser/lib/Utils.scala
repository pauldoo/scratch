package arqbrowser.lib

import scala.xml.Node
import java.io.DataInput
import java.io.DataInputStream
import java.io.InputStream
import java.io.ByteArrayOutputStream

object Utils {
  def iterated[T](
    fn: (T) => T,
    iterations: Int): (T) => T = {
    iterations match {
      case 0 => identity
      case 1 => fn
      case _ => {
        val a = iterations / 2;
        val b = iterations - a;
        iterated(fn, a) andThen iterated(fn, b);
      }
    }
  }

  def plistToMap(xml: Node): Map[String, String] =
    (xml \\ "dict" \ "_").grouped(2).map {
      case Seq(keyNode, elementNode) => (keyNode.text, elementNode.text)
    }.toMap;

  def bytesToHex(bytes: Array[Byte]): String =
    bytes.map(toUnsigned).map("%02X".format(_)).reduce(_ + " " + _);

  def toUnsigned(b: Byte): Int = {
    val r = b.toInt;
    if (r < 0) {
      return r + 256;
    } else {
      return r;
    }
  }

  def toSignedByte(i: Int): Byte = {
    if (i >= 0 && i <= 127) {
      return i.toByte;
    } else if (i >= 128 && i <= 255) {
      return (i - 256).toByte;
    } else {
      throw new RuntimeException("Not in range 0 to 255");
    }
  }

  def readHashRaw(stream: DataInput): Hash = {
    val r = new Array[Byte](20);
    stream.readFully(r);
    return new Hash(r);
  }

  def readHashText(stream: DataInput): Hash = {
    val r = new Array[Byte](40);
    stream.readFully(r);
    return Hash.fromString(new String(r, "UTF-8"));
  }

  def readStringWithIsNullHeader(stream: DataInput): String = {
    val isNull = stream.readBoolean();
    if (isNull) {
      return null;
    } else {
      val length = stream.readLong().toInt;
      val bytes = new Array[Byte](length);
      stream.readFully(bytes);
      return new String(bytes, "UTF-8");
    }
  }

  def readStringWithIsNotNullHeader(stream: DataInput): String = {
    val isNotNull = stream.readBoolean();
    if (isNotNull) {
      val length = stream.readLong().toInt;
      val bytes = new Array[Byte](length);
      stream.readFully(bytes);
      return new String(bytes, "UTF-8");
    } else {
      return null;
    }
  }

  def asDataInput(stream: InputStream): DataInput = {
    stream match {
      case r: DataInput => r;
      case _ => new DataInputStream(stream);
    }
  }

  def withInputStream[I <: InputStream, R](stream: I)(f: I => R): R = {
    try {
      return f(stream);
    } finally {
      if (stream != null) {
        try {
          println("Check me out, closing a stream..");
          stream.close();
        } catch {
          // Swallow
          case e: Exception => None;
        }
      }
    }
  }

  def readAll(is: InputStream): Array[Byte] = {
    val buffer = new ByteArrayOutputStream();
    val data = new Array[Byte](10240);
    while (true) {
      val length = is.read(data);
      if (length != -1) {
        buffer.write(data, 0, length);
      } else {
        return buffer.toByteArray();
      }
    }
    throw new RuntimeException("Unreachable.");
  }
}