package dev.prabhat.service

import cats.effect.IO
import dev.prabhat.models.config.LockboxConfig
import dev.prabhat.service.algorithms.AESEncryptionService
import dev.prabhat.service.algorithms.RSAEncryptionService
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64

object DecryptionService {
  def decryptFile(filePath: Path, config: LockboxConfig): IO[String] = {
    val fileName = filePath.getFileName.toString
    val algorithms = config.fileNameAlgorithms
      .flatMap {
        algos =>
          algos.patterns.collectFirst {
            case (pattern, algos) if fileName.matches(convertGlobToRegex(pattern)) => algos.reverse
          }
      }
      .getOrElse {
        List(config.algorithm.primary) ++ config.algorithm.secondary.toList.reverse
      }

    FileOperations.readFileContent(filePath).flatMap {
      content =>
        algorithms.reverse.foldLeft(IO.pure(content)) {
          (decryptedIO, algorithm) =>
            decryptedIO.flatMap {
              decryptedContent =>
                algorithm match {
                  case "AES" =>
                    decryptWithAES(decryptedContent, config.settings.aes.get.key)
                  case "RSA" =>
                    decryptWithRSA(decryptedContent, config.settings.rsa.get.privateKey)
                  case _ =>
                    IO.raiseError(new IllegalArgumentException(s"Unsupported algorithm: $algorithm"))
                }
            }
        }
    }
  }

  def decryptFileName(encryptedFileName: String, key: String): IO[String] = {
    val sanitized = encryptedFileName
      .replace("_SLASH_", "/")
      .replace("_PLUS_", "+")
      .replace("_EQUAL_", "=")

    val decoded = new String(Base64.getDecoder.decode(sanitized), "UTF-8")
    decryptWithAES(decoded, key)
  }

  private def convertGlobToRegex(glob: String): String =
    glob.replace(".", "\\.").replace("*", ".*")

  private def decryptWithAES(content: String, key: String): IO[String] =
    AESEncryptionService.decrypt(content, key)

  private def decryptWithRSA(content: String, privateKey: String): IO[String] =
    for {
      decodedPrivateKey <- RSAEncryptionService.decodePrivateKey(privateKey)
      result            <- RSAEncryptionService.decrypt(content, decodedPrivateKey)
    } yield result
}
