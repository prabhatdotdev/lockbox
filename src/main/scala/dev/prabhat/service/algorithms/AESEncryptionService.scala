package dev.prabhat.service.algorithms

import cats.effect.IO
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey

object AESEncryptionService {

  private val AES        = "AES"
  private val AES_CIPHER = "AES/CBC/PKCS5Padding"

  private def generateIV(): IO[IvParameterSpec] = IO {
    val iv = new Array[Byte](16)
    new SecureRandom().nextBytes(iv)
    new IvParameterSpec(iv)
  }

  def encrypt(content: String, secretKey: String): IO[String] = {
    generateIV().flatMap {
      iv =>
        IO.blocking {
          val cipher  = Cipher.getInstance(AES_CIPHER)
          val keySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), AES)
          cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)

          val encryptedBytes  = cipher.doFinal(content.getBytes("UTF-8"))
          val ivBase64        = Base64.getEncoder.encodeToString(iv.getIV)
          val encryptedBase64 = Base64.getEncoder.encodeToString(encryptedBytes)

          s"$ivBase64:$encryptedBase64"
        }
    }
  }

  def decrypt(encryptedContent: String, secretKey: String): IO[String] = {
    IO.blocking {
      val parts = encryptedContent.split(":")
      if (parts.length != 2) {
        throw new IllegalArgumentException("Invalid encrypted content format. Expected IV:EncryptedData")
      }

      val iv             = Base64.getDecoder.decode(parts(0))
      val encryptedBytes = Base64.getDecoder.decode(parts(1))

      val cipher  = Cipher.getInstance(AES_CIPHER)
      val keySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), AES)
      cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv))

      new String(cipher.doFinal(encryptedBytes), "UTF-8")
    }
  }
}
