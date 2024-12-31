package dev.prabhat.service

import cats.effect.IO
import cats.syntax.all.*
import dev.prabhat.models.config.LockboxConfig
import dev.prabhat.models.Operation
import dev.prabhat.models.Operation.Decrypt
import dev.prabhat.models.Operation.Encrypt
import java.nio.file.Path
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

object Processor {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def processFiles(
    decryptedFolder: String,
    encryptedFolder: String,
    config: LockboxConfig,
    operation: Operation
  ): IO[Unit] = {
    for {
      filesToProcess <- operation match {
        case Encrypt => DirectoryReader.getAllFiles(decryptedFolder)
        case Decrypt => DirectoryReader.getAllFiles(encryptedFolder)
      }
      _ <- logger.info(s"Found ${filesToProcess.size} files to process")
      _ <- logger.info(s"Files to process: ${filesToProcess.mkString(", ")}")

      _ <- filesToProcess.traverse {
        file =>
          for {
            processedContent <- processFileContent(file, config, operation)
            processedFileName <- operation match {
              case Encrypt => EncryptionService.encryptFileName(file.getFileName.toString, config.settings.aes.get.key)
              case Decrypt => DecryptionService.decryptFileName(file.getFileName.toString, config.settings.aes.get.key)
            }
            folderToWrite = operation match {
              case Encrypt => encryptedFolder
              case Decrypt => decryptedFolder
            }
            _ <- logger.info(s"Writing processed content to $folderToWrite/$processedFileName")
            _ <- FileOperations.writeFile(Path.of(folderToWrite, processedFileName), processedContent)
            _ <- deleteFileIfRequired(file, operation, config)
          } yield ()
      }
    } yield ()
  }

  private def processFileContent(file: Path, config: LockboxConfig, operation: Operation): IO[String] = {
    operation match {
      case Encrypt => EncryptionService.encryptFileContent(file, config)
      case Decrypt => DecryptionService.decryptFile(file, config)
    }
  }

  private def deleteFileIfRequired(file: Path, operation: Operation, config: LockboxConfig): IO[Unit] = {
    if (config.removeAfterEncryption) {
      FileOperations.deleteFile(file)
    } else {
      IO.unit
    }
  }

}
