package arqbrowser.gui
import arqbrowser.lib.ArqBucket
import arqbrowser.lib.ArqFile
import arqbrowser.lib.Hash
import java.io.File
import java.io.FileOutputStream

class ArqTreeFileNode(
  val arqBucket: ArqBucket,
  val dataHashes: List[Tuple2[Hash, Boolean]],
  val displayName: String) extends TreeNodeAdapter {

  val arqFile: ArqFile = {
    val sha1s: List[Hash] = dataHashes.map(_._1);
    new ArqFile(sha1s)
  }

  override val toString = displayName;

  override def isLeaf() = true;

  def saveToDisk(destination: File) = {
    val out = new FileOutputStream(destination);
    arqFile.writeContentTo(arqBucket, out);
    out.close();
  }
}
