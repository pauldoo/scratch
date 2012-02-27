package arqbrowser.lib

import java.io.IOException
import java.io.DataInputStream
import java.io.ByteArrayInputStream

final class Hash(val value: Array[Byte]) extends Ordered[Hash] {
  if (value.length != 20) {
    throw new RuntimeException("Invalid hash length: %d".format(value.length));
  }

  override val hashCode: Int =
    (new DataInputStream(new ByteArrayInputStream(value, 10, 4))).readInt();

  override def compare(other: Hash): Int = {
    for ((a, b) <- this.value.toIterable zip other.value.toIterable) {
      val r = a compare b;
      if (r != 0) {
        return r;
      }
    }
    return this.value.length compare other.value.length;
  }

  override def equals(other: Any): Boolean = {
    other match {
      case o: Hash => (this compare o) == 0;
      case _ => false;
    }
  }

  override val toString: String =
    Utils.bytesToHex(value);
}

object Hash {
  def fromString(v: String): Hash = {
    if (v.length() != 40) {
      throw new IOException("Hash String should be exactly 40 chars.");
    }

    val r = new Array[Byte](20);

    for (i <- 0 until r.length) {
      r(i) = Utils.toSignedByte(Integer.parseInt(v.substring(i * 2, (i + 1) * 2), 16));
    };

    return new Hash(r);
  }
}
