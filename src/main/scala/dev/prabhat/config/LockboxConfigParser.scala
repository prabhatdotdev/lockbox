package dev.prabhat.config

import cats.data.Validated
import cats.data.ValidatedNel
import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import dev.prabhat.models.config.*
import java.io.FileInputStream
import java.util.Properties
import scala.jdk.CollectionConverters.*

object LockboxConfigParser {

  def parseConfig(properties: Properties): IO[LockboxConfig] = {
    for {
      primaryAlgorithm <- IO(properties.getProperty("lockbox.algorithm.primary"))
      secondaryAlgorithm = Option(properties.getProperty("lockbox.algorithm.secondary")).filter(_.nonEmpty)

      fileNameAlgorithms <- IO {
        properties
          .stringPropertyNames()
          .asScala
          .filter(_.startsWith("lockbox.file-name-algorithms."))
          .map {
            key =>
              val pattern    = key.stripPrefix("lockbox.file-name-algorithms.")
              val algorithms = properties.getProperty(key).split(",").map(_.trim).toList
              pattern -> algorithms
          }
          .toMap
      }

      aesConfig <- validateAESConfig(properties)
      rsaConfig <- validateRSAConfig(properties)
      _         <- validateAlgorithms(primaryAlgorithm, secondaryAlgorithm, fileNameAlgorithms, aesConfig, rsaConfig)

    } yield LockboxConfig(
      algorithm = AlgorithmConfig(primaryAlgorithm, secondaryAlgorithm),
      fileNameAlgorithms = if (fileNameAlgorithms.nonEmpty) Some(FileNameAlgorithmConfig(fileNameAlgorithms)) else None,
      settings = SettingsConfig(aesConfig, rsaConfig),
      removeAfterEncryption = properties.getProperty("lockbox.settings.remove-after-operation").toBoolean
    )
  }

  private def validateAESConfig(properties: Properties): IO[Option[AESConfig]] = {
    val aesKey = Option(properties.getProperty("lockbox.settings.AES.key"))
    aesKey match {
      case Some(key) => IO.pure(Some(AESConfig(key)))
      case None => IO.pure(None)
    }
  }

  private def validateRSAConfig(properties: Properties): IO[Option[RSAConfig]] = {
    val publicKeyPath  = Option(properties.getProperty("lockbox.settings.RSA.public-key-file"))
    val privateKeyPath = Option(properties.getProperty("lockbox.settings.RSA.private-key-file"))

    (publicKeyPath, privateKeyPath).mapN {
      (publicKeyFile, privateKeyFile) =>
        for {
          publicKey  <- readKeyFromFile(publicKeyFile, "RSA public key")
          privateKey <- readKeyFromFile(privateKeyFile, "RSA private key")
        } yield RSAConfig(publicKey, privateKey)
    }.sequence
  }

  private def readKeyFromFile(filePath: String, keyType: String): IO[String] = {
    Resource
      .fromAutoCloseable(IO.blocking(new FileInputStream(filePath)))
      .use {
        fis =>
          IO.blocking(new String(fis.readAllBytes()))
            .handleErrorWith {
              _ =>
                IO.raiseError(new RuntimeException(s"Failed to read $keyType from file: $filePath"))
            }
      }
  }

  private def validateAlgorithms(
    primary: String,
    secondary: Option[String],
    fileAlgorithms: Map[String, List[String]],
    aesConfig: Option[AESConfig],
    rsaConfig: Option[RSAConfig]
  ): IO[Unit] = {
    val usedAlgorithms = (List(primary) ++ secondary ++ fileAlgorithms.values.flatten).distinct

    val validations: ValidatedNel[String, Unit] = (
      if (usedAlgorithms.contains("AES") && aesConfig.isEmpty)
        Validated.invalidNel("AES is used but lockbox.settings.AES.key is not provided.")
      else Validated.validNel(()),
      if (usedAlgorithms.contains("RSA") && rsaConfig.isEmpty)
        Validated.invalidNel(
          "RSA is used but lockbox.settings.RSA.public-key-file and lockbox.settings.RSA.private-key-file are not provided or invalid."
        )
      else Validated.validNel(())
    ).mapN((_, _) => ())

    validations match {
      case Validated.Valid(_) => IO.unit
      case Validated.Invalid(e) => IO.raiseError(new RuntimeException(e.toList.mkString("; ")))
    }
  }
}
