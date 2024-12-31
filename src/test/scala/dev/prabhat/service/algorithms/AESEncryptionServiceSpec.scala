package dev.prabhat.service.algorithms

import cats.effect.unsafe.implicits.global
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AESEncryptionServiceSpec extends AnyWordSpec with Matchers {

  "AESEncryptionService" should {

    "correctly encrypt and decrypt content" in {
      val secretKey       = "MySuperSecretKey"
      val originalContent = "Hello, World!"

      val encrypted = AESEncryptionService.encrypt(originalContent, secretKey).unsafeRunSync()

      encrypted should not be empty
      encrypted should not equal originalContent

      val decrypted = AESEncryptionService.decrypt(encrypted, secretKey).unsafeRunSync()

      decrypted should equal(originalContent)
    }

    "throw an exception for invalid encrypted content format" in {
      val secretKey      = "MySuperSecretKey"
      val invalidContent = "InvalidFormatContent"

      an[IllegalArgumentException] should be thrownBy {
        AESEncryptionService.decrypt(invalidContent, secretKey).unsafeRunSync()
      }
    }

    "throw an exception for incorrect key during decryption" in {
      val secretKey       = "MySuperSecretKey"
      val incorrectKey    = "WrongSecretKey3"
      val originalContent = "Hello, AES!"

      val encrypted = AESEncryptionService.encrypt(originalContent, secretKey).unsafeRunSync()

      an[java.security.InvalidKeyException] should be thrownBy {
        AESEncryptionService.decrypt(encrypted, incorrectKey).unsafeRunSync()
      }
    }
  }
}
