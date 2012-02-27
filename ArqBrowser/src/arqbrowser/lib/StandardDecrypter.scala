package arqbrowser.lib

import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream

object StandardDecrypter {
  def fromPassword(password: Array[Char], salt: Array[Byte]): StandardDecrypter = {
    // A pseudo re-implementation of EVP_BytesToKey from OpenSSL
    def EVP_BytesToKey(
      salt: Array[Byte],
      data: Array[Byte],
      iterations: Int,
      keylength: Int,
      ivlength: Int): Tuple2[Array[Byte], Array[Byte]] = {

      def sha1(data: Array[Byte]): Array[Byte] = {
        MessageDigest.getInstance("SHA-1").digest(data)
      }

      def concat(lst: List[Array[Byte]]): Array[Byte] = {
        val result = new ByteArrayOutputStream();
        for (f <- lst) {
          result.write(f);
        }
        return result.toByteArray();
      }

      val multihash = Utils.iterated(sha1, iterations);

      var previous = new Array[Byte](0);
      val result = new ByteArrayOutputStream();
      while (result.size() < (keylength + ivlength)) {
        previous = multihash(concat(List(previous, data, salt)));
        result.write(previous);
      }

      val key = new Array[Byte](keylength);
      val iv = new Array[Byte](ivlength);
      val is = new ByteArrayInputStream(result.toByteArray());
      is.read(key);
      is.read(iv);

      return (key, iv);
    }

    val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    val pbeKeySpec = new PBEKeySpec(password, salt, 1000, 48 * 8);
    val pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);

    val (key, iv) = EVP_BytesToKey(salt, pbeKey.getEncoded(), 1000, 32, 16);

    new StandardDecrypter(new SecretKeySpec(key, "AES"), new IvParameterSpec(iv))
  }
}

class StandardDecrypter(key: Key, iv: AlgorithmParameterSpec) extends Decrypter {
  def decrypt(in: InputStream): InputStream = {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, key, iv);
    return new CipherInputStream(in, cipher);
  }
}
