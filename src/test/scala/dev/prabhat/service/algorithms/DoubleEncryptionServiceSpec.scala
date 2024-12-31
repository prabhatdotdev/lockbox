package dev.prabhat.service.algorithms

import cats.effect.unsafe.implicits.global
import java.security.KeyPair
import java.security.KeyPairGenerator
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DoubleEncryptionServiceSpec extends AnyWordSpec with Matchers {

  "Double Encryption and Decryption" should {

    "correctly encrypt and decrypt content using AES and RSA" in {
      val content = "Hello, Double Encryption!"

      val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048)
      val keyPair       = keyPairGenerator.generateKeyPair()
      val rsaPublicKey  = keyPair.getPublic
      val rsaPrivateKey = keyPair.getPrivate

      val aesSecretKey = "MySuperSecretKey"

      val aesEncryptedContent = AESEncryptionService.encrypt(content, aesSecretKey).unsafeRunSync()

      val encryptedAESKey = RSAEncryptionService.encrypt(aesSecretKey, rsaPublicKey).unsafeRunSync()

      aesEncryptedContent should not be empty
      encryptedAESKey should not be empty
      aesEncryptedContent should not equal content
      encryptedAESKey should not equal aesSecretKey

      val decryptedAESKey = RSAEncryptionService.decrypt(encryptedAESKey, rsaPrivateKey).unsafeRunSync()
      decryptedAESKey should equal(aesSecretKey)

      val decryptedContent = AESEncryptionService.decrypt(aesEncryptedContent, decryptedAESKey).unsafeRunSync()
      decryptedContent should equal(content)
    }

    "fail to decrypt content with incorrect RSA private key" in {
      val content = "Hello, Double Encryption!"

      val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048)
      val keyPair1               = keyPairGenerator.generateKeyPair()
      val keyPair2               = keyPairGenerator.generateKeyPair()
      val rsaPublicKey           = keyPair1.getPublic
      val incorrectRSAPrivateKey = keyPair2.getPrivate

      val aesSecretKey = "MySuperSecretKey"

      val encryptedAESKey = RSAEncryptionService.encrypt(aesSecretKey, rsaPublicKey).unsafeRunSync()

      an[javax.crypto.BadPaddingException] should be thrownBy {
        RSAEncryptionService.decrypt(encryptedAESKey, incorrectRSAPrivateKey).unsafeRunSync()
      }
    }
  }
}
