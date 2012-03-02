package arqbrowser.lib

import java.io.IOException
import java.io.DataInput

class ArqCommit(val treeHash: Hash) {

}

object ArqCommit {
  def read(stream: DataInput): ArqCommit = {
    {
      val a = stream.readInt();
      val b = stream.readShort();
      val c = stream.readByte();

      if (a != 0x436f6d6d ||
        b != 0x6974 ||
        c != 0x56) {
        throw new IOException("Bad magic");
      }
    }

    val version = Utils.readVersionBytes(stream);

    version match {
      case 6 => {
        val author = Utils.readStringWithIsNotNullHeader(stream);
        val comment = Utils.readStringWithIsNotNullHeader(stream);
        val parentCommitCount = stream.readLong().toInt;
        for (i <- 0 until parentCommitCount) {
          Utils.readStringWithIsNotNullHeader(stream);
          stream.readBoolean();
        }
        val treeHash: Hash = Hash.fromString(Utils.readStringWithIsNotNullHeader(stream));

        return new ArqCommit(treeHash);
      }

      case _ => throw new IOException("Unexpected version");
    }
  }
}
