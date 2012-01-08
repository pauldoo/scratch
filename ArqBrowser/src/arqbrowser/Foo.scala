package arqbrowser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.Security

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client

import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

/*
 * Messy notes on how to decrypt objects from Arq.
 * 
 * Expects to find a Secrets singleton with various secret keys etc, this hasn't been committed.
 * 
 * See also:
 * 	http://www.haystacksoftware.com/arq/s3_data_format.txt
 * 	https://github.com/sreitshamer/arq_restore/blob/master/io/CryptoKey.m
 * 
 * Also my first go at writing Scala with Eclipse, quite confused by the auto-indent behavior.
 * 
 */

object Foo extends Application {

	go();

	def go() : Unit  = {
			val credentials :AWSCredentials = new BasicAWSCredentials(
					Secrets.awsAccessKey, Secrets.awsSecretKey);

			val client :AmazonS3 = new AmazonS3Client(credentials);
		
			val saltdata : Array[Byte] = readAll(getObjectStream(client, Secrets.bucket, Secrets.saltPath));
			val objectdata : Array[Byte] = readAll(getObjectStream(client, Secrets.bucket, Secrets.objectPath));
		
			println(hexString(sha1(objectdata)));
		
			val password = System.console().readPassword("[%s]", "Password:");
		
			val decryptedData = decrypt(objectdata, password, saltdata);
		
			val os = new FileOutputStream("foobar");
			os.write(decryptedData);
			os.close();
	}

	def hexString(data : Array[Byte]) : String = {
			val result = new StringBuffer();
			for (v <- data) {
				result.append( "%02x".format(toUnsignedValue(v)))
			}
			result.toString();
	}

	def toUnsignedValue(v : Byte) : Int = {
			val r : Int = v;
			if (r < 0) {
				r + 256;
			} else {
				r;
			}
	}

	def decrypt(data : Array[Byte], password : Array[Char], salt : Array[Byte]) : Array[Byte] = {
			val secretKeyFactory :SecretKeyFactory =
				SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			val pbeKeySpec : PBEKeySpec =
				new PBEKeySpec(password, salt, 1000, 48 * 8);
			val pbeKey : SecretKey =
				secretKeyFactory.generateSecret(pbeKeySpec);
		
			val (key, iv) = EVP_BytesToKey(salt, pbeKey.getEncoded(), 1000, 32, 16);
		
			val cipher :Cipher =
				Cipher.getInstance("AES/CBC/PKCS5Padding");
		
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
			val decrypted :Array[Byte] = cipher.doFinal(data);
			decrypted;
	}

	// A pseudo re-implementation of EVP_BytesToKey from OpenSSL
	def EVP_BytesToKey(
			salt : Array[Byte], 
			data : Array[Byte], 
			iterations : Int,
			keylength : Int,
			ivlength : Int) : Tuple2[Array[Byte], Array[Byte]] = {

			var previous = new Array[Byte](0);
			val result = new ByteArrayOutputStream();
			while (result.size() < (keylength + ivlength)) {
				previous = multihash(concat(List(previous, data, salt)), iterations);
				result.write(previous);
			}

			val key = new Array[Byte](keylength);
			val iv = new Array[Byte](ivlength);
			val is = new ByteArrayInputStream(result.toByteArray());
			is.read(key);
			is.read(iv);

			return (key, iv);
	}

	def concat(lst : List[Array[Byte]]) : Array[Byte] = {
			val result = new ByteArrayOutputStream();
			for (f <- lst) {
				result.write(f);
			}
			return result.toByteArray();
	}

	def multihash(data : Array[Byte], iterations : Int) : Array[Byte] = {
			iterations match {
				case 0 =>
					data
				case 1 =>
					sha1(data)
				case _ => {
					val a = iterations / 2;
					val b = iterations - a;
					multihash(multihash(data, a), b)
				}
			}
	}

	def sha1(data : Array[Byte]) : Array[Byte] = {
			MessageDigest.getInstance("SHA-1").digest(data)
	}

	def getObjectStream(client :AmazonS3, bucket :String, path :String) : InputStream = {
			client.getObject(new GetObjectRequest(bucket, path)).getObjectContent()
	}

	def readAll(is :InputStream) : Array[Byte] = {
			val buffer = new ByteArrayOutputStream();
			val data = new Array[Byte](10240);
			while(true) {
				val length = is.read(data);
				if (length != -1) {
					buffer.write(data, 0, length);
				} else {
					return buffer.toByteArray();
				}
			}
			throw new RuntimeException("Unreachable.");
	}
}

