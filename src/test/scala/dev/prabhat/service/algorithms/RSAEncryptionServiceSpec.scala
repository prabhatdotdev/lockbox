package dev.prabhat.service.algorithms

import cats.effect.unsafe.implicits.global
import java.security.KeyPair
import java.security.KeyPairGenerator
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RSAEncryptionServiceSpec extends AnyWordSpec with Matchers {

  "RSAEncryptionService" should {

    "correctly encrypt and decrypt content" in {
      val content = "Hello, RSA!"

      val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048)
      val keyPair    = keyPairGenerator.generateKeyPair()
      val publicKey  = keyPair.getPublic
      val privateKey = keyPair.getPrivate

      val encrypted = RSAEncryptionService.encrypt(content, publicKey).unsafeRunSync()

      encrypted should not be empty
      encrypted should not equal content

      val decrypted = RSAEncryptionService.decrypt(encrypted, privateKey).unsafeRunSync()

      decrypted should equal(content)
    }

    "correctly encode and decode keys" in {
      val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048)
      val keyPair    = keyPairGenerator.generateKeyPair()
      val publicKey  = keyPair.getPublic
      val privateKey = keyPair.getPrivate

      val encodedPublicKey  = RSAEncryptionService.encodeKey(publicKey)
      val encodedPrivateKey = RSAEncryptionService.encodeKey(privateKey)

      val decodedPublicKey  = RSAEncryptionService.decodePublicKey(encodedPublicKey).unsafeRunSync()
      val decodedPrivateKey = RSAEncryptionService.decodePrivateKey(encodedPrivateKey).unsafeRunSync()

      decodedPublicKey should equal(publicKey)
      decodedPrivateKey should equal(privateKey)
    }

    "throw an exception for decryption with a mismatched key" in {
      val content = "Hello, RSA!"

      val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048)
      val keyPair1 = keyPairGenerator.generateKeyPair()
      val keyPair2 = keyPairGenerator.generateKeyPair()

      val publicKey  = keyPair1.getPublic
      val privateKey = keyPair2.getPrivate

      val encrypted = RSAEncryptionService.encrypt(content, publicKey).unsafeRunSync()

      an[javax.crypto.BadPaddingException] should be thrownBy {
        RSAEncryptionService.decrypt(encrypted, privateKey).unsafeRunSync()
      }
    }
  }
}
