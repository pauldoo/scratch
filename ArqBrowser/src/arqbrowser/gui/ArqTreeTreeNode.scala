package arqbrowser.gui
import arqbrowser.lib.ArqBucket
import arqbrowser.lib.Hash
import arqbrowser.lib.ArqTree
import arqbrowser.lib.Utils
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import java.io.IOException

class ArqTreeTreeNode(
  val arqBucket: ArqBucket,
  val treeHash: Hash,
  val displayName: String) extends TreeNodeAdapter {

  val arqTree = ArqTree.read(Utils.asDataInput(arqBucket.getDecryptedStreamForObject(treeHash)));

  override val toString = displayName;

  override def isLeaf() = false;

  override def getChildCount() = arqTree.children.size;

  override def getChildAt(v: Int): TreeNode = {
    val child = arqTree.children(v);
    if (child._2.isTree) {
      if (child._2.dataHashes.size != 1) {
        throw new IOException("Tree nodes should have only one hash.");
      }
      new ArqTreeTreeNode(
        arqBucket,
        child._2.dataHashes(0)._1,
        child._1);
    } else {
      new DefaultMutableTreeNode(child._1);
    }
  }
}
