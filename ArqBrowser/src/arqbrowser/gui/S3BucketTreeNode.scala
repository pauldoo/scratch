package arqbrowser.gui

import com.amazonaws.services.s3.AmazonS3
import arqbrowser.lib.ArqStore
import arqbrowser.lib.ArqStore
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.AWSCredentials
import javax.swing.tree.TreeNode
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JOptionPane
import javax.swing.JLabel

class S3BucketTreeNode(val awsCredentials: AWSCredentials) extends TreeNodeAdapter {

  val s3bucket = ArqStore.bucketName(awsCredentials.getAWSAccessKeyId());
  val s3client: AmazonS3 = new AmazonS3Client(awsCredentials);
  val uuids = ArqStore.listAvailableComputerUuids(s3client, s3bucket);

  override val toString = s3bucket;

  /*
  override def children(): java.util.Enumeration[java.lang.Object] = {
    return (new java.util.Vector[java.lang.Object](uuids.asJava)).elements();
  }
  */

  override def isLeaf(): Boolean = false;

  override def getAllowsChildren(): Boolean = true;

  override def getChildCount(): Int = {
    return uuids.length;
  }

  override def getChildAt(v: Int): TreeNode = {
    return new ComputerTreeNode(s3client, s3bucket, uuids(v), promptForPassword(uuids(v)));
  }

  private def promptForPassword(uuid: String): Array[Char] = {
    val panel = new JPanel();
    panel.add(new JLabel("Enter password for computer uuid %s".format(uuid)));
    val password = new JPasswordField(40);
    panel.add(password);
    JOptionPane.showMessageDialog(null, panel);
    password.getPassword()
  }
}
