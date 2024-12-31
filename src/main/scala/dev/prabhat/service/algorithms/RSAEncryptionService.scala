package dev.prabhat.service.algorithms

import cats.effect.IO
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Base64
import javax.crypto.Cipher

object RSAEncryptionService {

  private val RSA                    = "RSA"
  private val CIPHER_TRANSFORMATION  = "RSA/ECB/PKCS1Padding"
  private val MAX_ENCRYPT_BLOCK_SIZE = 245
  private val MAX_DECRYPT_BLOCK_SIZE = 256

  def encodeKey(key: java.security.Key): String =
    Base64.getEncoder.encodeToString(key.getEncoded)

  def decodePublicKey(base64Key: String): IO[PublicKey] = IO.blocking {
    val keyBytes = Base64.getDecoder.decode(base64Key)
    val keySpec  = new X509EncodedKeySpec(keyBytes)
    KeyFactory.getInstance(RSA).generatePublic(keySpec)
  }

  def decodePrivateKey(base64Key: String): IO[PrivateKey] = IO.blocking {
    val keyBytes = Base64.getDecoder.decode(base64Key)
    val keySpec  = new PKCS8EncodedKeySpec(keyBytes)
    KeyFactory.getInstance(RSA).generatePrivate(keySpec)
  }

  def encrypt(content: String, publicKey: PublicKey): IO[String] = IO.blocking {
    val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)

    val contentBytes = content.getBytes("UTF-8")
    val encryptedChunks = contentBytes
      .grouped(MAX_ENCRYPT_BLOCK_SIZE)
      .map(chunk => cipher.doFinal(chunk))
      .toArray

    Base64.getEncoder.encodeToString(encryptedChunks.flatten)
  }

  def decrypt(encryptedContent: String, privateKey: PrivateKey): IO[String] = IO.blocking {
    val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
    cipher.init(Cipher.DECRYPT_MODE, privateKey)

    val encryptedBytes = Base64.getDecoder.decode(encryptedContent)
    val decryptedChunks = encryptedBytes
      .grouped(MAX_DECRYPT_BLOCK_SIZE)
      .map(chunk => cipher.doFinal(chunk))
      .toArray

    new String(decryptedChunks.flatten, "UTF-8")
  }
}
