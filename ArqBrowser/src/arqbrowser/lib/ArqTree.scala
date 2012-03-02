package arqbrowser.lib
import java.io.DataInput
import java.io.IOException

class ArqTree(val children: List[Tuple2[String, ArqTreeNode]]) {

}

object ArqTree {
  private def readNameAndNode(stream: DataInput): Tuple2[String, ArqTreeNode] = {
    val name = Utils.readStringWithIsNotNullHeader(stream);
    val node = ArqTreeNode.read(stream);
    return (name, node);
  }

  def read(stream: DataInput): ArqTree = {
    {
      val a = stream.readInt();
      val b = stream.readByte();
      if (a != 0x54726565 ||
        b != 0x56) {
        throw new IOException("Bad magic");
      }
    }

    val version = Utils.readVersionBytes(stream);

    version match {
      case 15 => {
        val xattrs_are_compressed = stream.readBoolean();
        val acl_is_compressed = stream.readBoolean();
        val xattrs_sha1 = Utils.readStringWithIsNotNullHeader(stream);
        val is_xattrs_encryption_key_stretched = stream.readBoolean();
        val xattrs_size = stream.readLong();
        val acl_sha1 = Utils.readStringWithIsNotNullHeader(stream);
        val is_acl_encryption_key_stretched = stream.readBoolean();
        val uid = stream.readInt();
        val gid = stream.readInt();
        val mode = stream.readInt();
        val mtime_sec = stream.readLong();
        val mtime_nsec = stream.readLong();
        val flags = stream.readLong();
        val finderFlags = stream.readInt();
        val extendedFinderFlags = stream.readInt();
        val st_dev = stream.readInt();
        val st_ino = stream.readInt();
        val st_nlink = stream.readInt();
        val st_rdev = stream.readInt();
        val ctime_sec = stream.readLong();
        val ctime_nsec = stream.readLong();
        val st_blocks = stream.readLong();
        val st_blksize = stream.readInt();
        val aggregate_size_on_disk = stream.readLong();
        val create_time_sec = stream.readLong();
        val create_time_nsec = stream.readLong();
        val node_count = stream.readInt();
        val child_nodes: List[Tuple2[String, ArqTreeNode]] = (for (i <- 0 until node_count) yield readNameAndNode(stream)).toList;
        return new ArqTree(child_nodes);
      }

      case _ => throw new IOException("Unexpected version");
    }

  }
}
