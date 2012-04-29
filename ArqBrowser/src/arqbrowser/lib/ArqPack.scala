package arqbrowser.lib

import java.io.IOException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.model.GetObjectRequest
import java.io.InputStream
import java.io.DataInputStream
import java.io.DataInput

class ArqPack(
  val s3client: AmazonS3,
  val indexObject: S3ObjectSummary,
  val packObject: S3ObjectSummary) {

  val entries: Map[Hash, Tuple3[Long, Long, Long]] =
    Utils.withInputStream(
      s3client.getObject(new GetObjectRequest(
        indexObject.getBucketName(),
        indexObject.getKey())).getObjectContent())(
        (stream: InputStream) => parseIndexStream(new DataInputStream(stream)));

  println("Pack contains %d entries".format(entries.size))

  def getObject(hash: Hash): Option[InputStream] = {
    entries.get(hash).map((p) => {
      val offset = p._1;
      val length = p._2 + p._3;

      val request = new GetObjectRequest(
        packObject.getBucketName(),
        packObject.getKey()).withRange(offset, offset + length - 1);

      val stream = s3client.getObject(request).getObjectContent();
      verifyHeaders(Utils.asDataInput(stream));
      stream
    });
  }

  private def verifyHeaders(stream: DataInput): Unit = {
    val mimeType = Utils.readStringWithIsNotNullHeader(stream);
    val name = Utils.readStringWithIsNotNullHeader(stream);
    val length = stream.readLong();
  }

  private def parseIndexStream(stream: DataInput): Map[Hash, Tuple3[Long, Long, Long]] = {
    val magic = stream.readInt();
    if (magic != 0xFF744F63) {
      throw new IOException("Bad magic.");
    }

    val version = stream.readInt();
    version match {
      case 2 => {
        val fanouts: IndexedSeq[Int] = for (i <- 0 to 255) yield stream.readInt();

        val entries: Map[Hash, Tuple2[Long, Long]] = (for (i <- 0 until fanouts.last) yield {
          val offset = stream.readLong();
          val length = stream.readLong();
          val hash = Utils.readHashRaw(stream);
          val padding = stream.readInt();
          if (padding != 0) {
            throw new IOException("Bad padding.");
          }

          (hash, (offset, length))
        }).toMap;

        Utils.readHashRaw(stream);

        val indices = (entries.values.map(_._1) ++ List(packObject.getSize())).toList.sortWith(_ < _);

        return entries.mapValues(t => {
          val currOffset = t._1;
          val currLength = t._2;
          val nextObjOffset = indices.find(t._1 < _).get;
          (currOffset, (nextObjOffset - currOffset) - currLength, currLength)
        });
      }
      case _ => {
        throw new IOException("Unexpected version");
      }
    }
  }
}
