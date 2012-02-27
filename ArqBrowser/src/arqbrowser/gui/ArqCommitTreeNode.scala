package arqbrowser.gui

import arqbrowser.lib.ArqBucket
import arqbrowser.lib.Hash
import arqbrowser.lib.ArqCommit
import javax.swing.tree.TreeNode
import javax.swing.tree.DefaultMutableTreeNode
import arqbrowser.lib.Utils

class ArqCommitTreeNode(
  val arqBucket: ArqBucket,
  val commitHash: Hash) extends TreeNodeAdapter {
  val arqCommit = ArqCommit.read(Utils.asDataInput(arqBucket.getDecryptedStreamForObject(commitHash)));

  override val toString = commitHash.toString;

  override def isLeaf() = false;

  override def getChildCount() = 1;

  override def getChildAt(v: Int): TreeNode = {
    return new DefaultMutableTreeNode(arqCommit.treeHash);
  }
}
