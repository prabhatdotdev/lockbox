package dev.prabhat.service

import cats.effect.IO
import dev.prabhat.models.config.LockboxConfig
import dev.prabhat.service.algorithms.AESEncryptionService
import dev.prabhat.service.algorithms.RSAEncryptionService
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

object EncryptionService {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def encryptFileContent(filePath: Path, config: LockboxConfig): IO[String] = {
    val fileName = filePath.getFileName.toString
    val algorithms = config.fileNameAlgorithms
      .flatMap {
        algos =>
          algos.patterns.collectFirst {
            case (pattern, algos) if fileName.matches(convertGlobToRegex(pattern)) => algos
          }
      }
      .getOrElse {
        List(config.algorithm.primary) ++ config.algorithm.secondary.toList
      }

    FileOperations.readFileContent(filePath).flatMap {
      content =>
        encryptContent(content, algorithms, config)
    }
  }

  private def encryptContent(content: String, algorithms: List[String], config: LockboxConfig): IO[String] = {
    algorithms.foldLeft(IO.pure(content)) {
      (encryptedContentIO, algorithm) =>
        encryptedContentIO.flatMap {
          encryptedContent =>
            logger.debug(s"Encrypting [[$encryptedContent]] with $algorithm algorithm") >>
              (algorithm match {
                case "AES" =>
                  encryptWithAES(encryptedContent, config.settings.aes.get.key)
                case "RSA" =>
                  encryptWithRSA(encryptedContent, config.settings.rsa.get.publicKey)
                case other =>
                  IO.raiseError(new IllegalArgumentException(s"Unsupported encryption algorithm: $other"))
              })
        }
    }
  }

  /** Encrypt a file name. */
  def encryptFileName(fileName: String, key: String): IO[String] =
    encryptWithAES(fileName, key).map {
      encrypted =>
        Base64.getEncoder
          .encodeToString(encrypted.getBytes("UTF-8"))
          .replace("/", "_SLASH_")
          .replace("+", "_PLUS_")
          .replace("=", "_EQUAL_")
    }

  private def convertGlobToRegex(glob: String): String =
    glob.replace(".", "\\.").replace("*", ".*")

  private def encryptWithAES(content: String, key: String): IO[String] =
    AESEncryptionService.encrypt(content, key)

  private def encryptWithRSA(content: String, publicKey: String): IO[String] =
    for {
      decodedPublicKey <- RSAEncryptionService.decodePublicKey(publicKey)
      result           <- RSAEncryptionService.encrypt(content, decodedPublicKey)
    } yield result

}
