package arqbrowser.gui
import arqbrowser.lib.ArqBucket
import arqbrowser.lib.Hash
import arqbrowser.lib.ArqTree
import arqbrowser.lib.Utils
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode

class ArqTreeTreeNode(
  val arqBucket: ArqBucket,
  val treeHash: Hash) extends TreeNodeAdapter {

  val arqTree = ArqTree.read(Utils.asDataInput(arqBucket.getDecryptedStreamForObject(treeHash)));

  override val toString = "[tree]: " + treeHash.toString;

  override def isLeaf() = false;

  override def getChildCount() = arqTree.children.size;

  override def getChildAt(v: Int): TreeNode = {
    return new DefaultMutableTreeNode(
      arqTree.children(v)._1, arqTree.children(v)._2.isTree)
  }
}
