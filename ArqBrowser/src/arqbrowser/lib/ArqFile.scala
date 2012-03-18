package arqbrowser.lib
import java.io.OutputStream

class ArqFile(val dataHashes: List[Hash]) {
  def writeContentTo(arqBucket: ArqBucket, outstream: OutputStream): Unit = {
    for (hash <- dataHashes) {
      Utils.withInputStream(arqBucket.getDecompressedAndDecryptedStreamForObject(hash))(Utils.copyAll(_, outstream));
    }
  }
}

