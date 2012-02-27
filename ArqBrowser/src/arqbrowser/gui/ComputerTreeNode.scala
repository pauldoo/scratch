package arqbrowser.gui

import scala.xml.Elem
import arqbrowser.lib.Utils
import arqbrowser.lib.Decrypter
import arqbrowser.lib.StandardDecrypter
import com.amazonaws.services.s3.model.S3ObjectSummary
import arqbrowser.lib.ArqStore
import com.amazonaws.services.s3.AmazonS3
import javax.swing.tree.TreeNode
import com.amazonaws.services.s3.model.GetObjectRequest
import javax.swing.tree.DefaultMutableTreeNode
import scala.xml.XML

class ComputerTreeNode(val s3client: AmazonS3, val s3bucket: String, val uuid: String, val password: Array[Char]) extends TreeNodeAdapter {

  val computerInfo: Elem = Utils.withInputStream(
    s3client.getObject(new GetObjectRequest(s3bucket, uuid + "/computerinfo")).getObjectContent()) {
      XML.load(_)
    };

  val decrypter: Decrypter = {
    val salt: Array[Byte] = Utils.withInputStream(
      s3client.getObject(new GetObjectRequest(s3bucket, uuid + "/salt")).getObjectContent()) {
        Utils.readAll(_)
      };

    StandardDecrypter.fromPassword(password, salt)
  };

  val buckets: List[S3ObjectSummary] = ArqStore.listAvailableBuckets(s3client, s3bucket, uuid);

  override val toString = (computerInfo \\ "string").map(_.text).reduce(_ + ":" + _);

  override def getChildCount(): Int = buckets.length;

  override def getChildAt(v: Int): TreeNode = {
    try {
      new ArqBucketTreeNode(s3client, s3bucket, uuid, ArqStore.lastSegment(buckets(v).getKey()), decrypter);
    } catch {
      case ioe: Exception =>
        return new DefaultMutableTreeNode(ioe.toString);
    }
  }

  override def isLeaf() = false;
}
