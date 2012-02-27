package arqbrowser.gui

import com.amazonaws.auth.AWSCredentials
import arqbrowser.lib.ArqStore
import com.amazonaws.services.s3.AmazonS3Client
import scala.collection.JavaConverters._
import javax.swing.tree.TreeNode
import javax.swing.tree.DefaultMutableTreeNode
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import scala.xml.XML
import com.amazonaws.services.s3.model.S3ObjectSummary
import arqbrowser.lib.Utils
import java.io.DataInputStream
import java.io.IOException
import arqbrowser.lib.ArqBucket
import arqbrowser.lib.Hash
import arqbrowser.lib.ArqCommit
import java.io.InputStream
import scala.xml.Elem
import java.io.DataInput
import arqbrowser.lib.Decrypter
import arqbrowser.lib.StandardDecrypter
import javax.swing.JOptionPane
import javax.swing.JPasswordField
import javax.swing.JPanel
import javax.swing.JLabel

class TreeNodeAdapter extends TreeNode {
  def children(): java.util.Enumeration[java.lang.Object] = {
    throw new UnsupportedOperationException();
  }

  def isLeaf(): Boolean = {
    throw new UnsupportedOperationException();
  };

  def getAllowsChildren(): Boolean = {
    throw new UnsupportedOperationException();
  };

  def getIndex(node: TreeNode): Int = {
    throw new UnsupportedOperationException();
  }
  def getParent(): TreeNode = {
    throw new UnsupportedOperationException();
  }
  def getChildCount(): Int = {
    throw new UnsupportedOperationException();
  }
  def getChildAt(v: Int): TreeNode = {
    throw new UnsupportedOperationException();
  }
}

