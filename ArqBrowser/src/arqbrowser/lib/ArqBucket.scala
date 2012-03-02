package arqbrowser.lib

import java.io.InputStream
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.model.GetObjectRequest
import scala.actors.Futures
import scala.actors.Future

class ArqBucket(
  val s3client: AmazonS3,
  val s3bucket: String,
  val computerUuid: String,
  val bucketUuid: String,
  val decrypter: Decrypter) {

  val treePacks: Future[List[Future[ArqPack]]] = Futures.future { openAllPacks("trees") };
  val blobPacks: Future[List[Future[ArqPack]]] = Futures.future { openAllPacks("blobs") };

  def openAllPacks(packsetType: String): List[Future[ArqPack]] = {
    println("Scanning packs of " + packsetType + " ..");

    val fileList: List[S3ObjectSummary] = ArqStore.listObjects(
      s3client, s3bucket, computerUuid + "/packsets/" + bucketUuid + "-" + packsetType + "/");

    fileList.filter(_.getKey().endsWith(".index")).map((index) => {
      val packName = index.getKey().replaceAll(".index", ".pack");
      val pack = fileList.find(_.getKey().equals(packName)).get;
      Futures.future {
        new ArqPack(s3client, index, pack)
      }
    })
  };

  def getRawStreamForObject(hash: Hash): InputStream = {
    println("Fetching object with hash %s".format(hash.toString));

    for (tp <- treePacks.apply()) {
      val r = tp.apply().getObject(hash);
      if (r.isDefined) {
        return r.get;
      }
    }

    for (bp <- blobPacks.apply()) {
      val r = bp.apply().getObject(hash);
      if (r.isDefined) {
        return r.get;
      }
    }

    return s3client.getObject(new GetObjectRequest(
      s3bucket,
      computerUuid + "/objects/" + hash.toString.toLowerCase())).getObjectContent();
  }

  def getDecryptedStreamForObject(hash: Hash): InputStream =
    decrypter.decrypt(getRawStreamForObject(hash));
}
