package arqbrowser.lib

import java.io.InputStream

trait Decrypter {
  def decrypt(input: InputStream): InputStream;
}