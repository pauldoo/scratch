package arqbrowser.lib

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3
import java.security.spec.AlgorithmParameterSpec
import java.security.Key
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.model.ListObjectsRequest
import scala.collection.JavaConverters._
import java.util.regex.Pattern
import java.util.regex.Matcher
import java.io.IOException
import com.amazonaws.services.s3.model.GetObjectRequest
import java.io.InputStream
import java.io.DataInput
import java.io.DataInputStream
import java.io.ByteArrayInputStream
import com.amazonaws.services.s3.model.S3Object
import scala.util.Sorting

object ArqStore {
  def bucketName(awsAccessKeyId: String): String =
    awsAccessKeyId.toLowerCase() + "-com-haystacksoftware-arq-eu";

  def lastSegment(s: String): String = {
    val r = "[^/]+";
    val p = Pattern.compile(r);
    val m: Matcher = p.matcher(s);

    def lastMatch(m: Matcher): Option[String] = {
      if (m.find()) {
        val curr = Some(m.group(m.groupCount()));
        val latter: Option[String] = lastMatch(m);
        return latter.orElse(curr);
      } else {
        return None;
      };
    };

    return lastMatch(m) match {
      case Some(v) => {
        v;
      }
      case None => {
        throw new IOException("Couldn't match '" + s + "'");
      }
    }
  }

  def listPrefixesAndObjects(s3client: AmazonS3, s3bucket: String, prefix: String): Tuple2[List[String], List[S3ObjectSummary]] = {
    def extractListings(ol: ObjectListing): Tuple2[List[String], List[S3ObjectSummary]] = {
      val prefixList = ol.getCommonPrefixes().asScala.map(lastSegment).toList;
      val objectsList = ol.getObjectSummaries().asScala.toList;

      val rest = {
        if (ol.isTruncated()) {
          extractListings(s3client.listNextBatchOfObjects(ol));
        } else {
          (List(), List());
        }
      };

      return (prefixList ++ rest._1, objectsList ++ rest._2);
    };

    return extractListings(s3client.listObjects((new ListObjectsRequest()) //
      .withBucketName(s3bucket) //
      .withDelimiter("/") //
      .withPrefix(prefix)));
  }

  def listPrefixes(s3client: AmazonS3, s3bucket: String, prefix: String): List[String] = {
    return listPrefixesAndObjects(s3client, s3bucket, prefix)._1;
  }

  def listObjects(s3client: AmazonS3, s3bucket: String, prefix: String): List[S3ObjectSummary] = {
    return listPrefixesAndObjects(s3client, s3bucket, prefix)._2;
  }

  def listAvailableComputerUuids(s3client: AmazonS3, s3bucket: String): List[String] =
    listPrefixes(s3client, s3bucket, "");
  def listAvailableBuckets(s3client: AmazonS3, s3bucket: String, computerUuid: String): List[S3ObjectSummary] =
    listObjects(s3client, s3bucket, computerUuid + "/buckets/");
}

