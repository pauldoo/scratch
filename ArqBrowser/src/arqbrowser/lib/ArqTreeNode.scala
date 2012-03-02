package arqbrowser.lib
import java.io.DataInput

class ArqTreeNode(val isTree: Boolean) {

}

object ArqTreeNode {
  def read(stream: DataInput): ArqTreeNode = {
    val isTree = stream.readBoolean();
    val data_are_compressed = stream.readBoolean();
    val xattrs_are_compressed = stream.readBoolean();
    val acl_is_compressed = stream.readBoolean();
    val data_sha1s_count = stream.readInt();
    for (j <- 0 until data_sha1s_count) {
      val data_sha1 = Utils.readStringWithIsNotNullHeader(stream)
      val is_data_encryption_key_stretched = stream.readBoolean();
    }
    val data_size = stream.readLong();
    val thumbnail_sha1 = Utils.readStringWithIsNotNullHeader(stream);
    val is_thumbnail_encryption_key_stretched = stream.readBoolean();
    val preview_sha1 = Utils.readStringWithIsNotNullHeader(stream);
    val is_preview_encryption_key_stretched = stream.readBoolean();
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
    val finder_file_type = Utils.readStringWithIsNotNullHeader(stream);
    val finder_file_creator = Utils.readStringWithIsNotNullHeader(stream);
    val is_file_extension_hidden = stream.readBoolean();
    val st_dev = stream.readInt();
    val st_ino = stream.readInt();
    val st_nlink = stream.readInt();
    val st_rdev = stream.readInt();
    val ctime_sec = stream.readLong();
    val ctime_nsec = stream.readLong();
    val create_time_sec = stream.readLong();
    val create_time_nsec = stream.readLong();
    val st_blocks = stream.readLong();
    val st_blksize = stream.readInt();

    new ArqTreeNode(isTree);

  }
}