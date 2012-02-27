package arqbrowser.gui

import com.amazonaws.services.s3.AmazonS3
import arqbrowser.lib.Decrypter
import arqbrowser.lib.Hash
import scala.xml.XML
import arqbrowser.lib.Utils
import arqbrowser.lib.ArqBucket
import com.amazonaws.services.s3.model.GetObjectRequest
import javax.swing.tree.TreeNode
import java.io.InputStream

class ArqBucketTreeNode(
  val s3client: AmazonS3,
  val s3bucket: String,
  val computerUuid: String,
  val bucketUuid: String,
  val decrypter: Decrypter) extends TreeNodeAdapter {

  val bucketInfo = XML.load(s3client.getObject(new GetObjectRequest(s3bucket, computerUuid + "/buckets/" + bucketUuid)).getObjectContent());
  val headRef: Hash = Utils.withInputStream(s3client.getObject(
    new GetObjectRequest(
      s3bucket,
      computerUuid + "/bucketdata/" + bucketUuid + "/refs/heads/master")).getObjectContent())(
    (is: InputStream) => Utils.readHashText(Utils.asDataInput(is)));
  val arqBucket = new ArqBucket(s3client, s3bucket, computerUuid, bucketUuid, decrypter);

  override val toString = Utils.plistToMap(bucketInfo)("BucketName") + ":" + headRef;

  override def isLeaf() = false;

  override def getChildCount() = 1;

  override def getChildAt(v: Int): TreeNode = {
    return new ArqCommitTreeNode(arqBucket, headRef);
  };
}
