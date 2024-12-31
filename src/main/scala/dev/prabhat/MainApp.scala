package dev.prabhat

import cats.effect.*
import com.monovore.decline.*
import com.monovore.decline.effect.*
import dev.prabhat.commands.Configs
import dev.prabhat.config.ConfigReader
import dev.prabhat.config.LockboxConfigParser
import dev.prabhat.models.config.LockboxConfig
import dev.prabhat.models.AppConfig
import dev.prabhat.service.Processor
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

object MainApp
    extends CommandIOApp(
      name = "lockbox",
      header = "Lockbox - A secure file encryption and decryption tool",
      version = "0.1.0"
    ):

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private def loadProperties(filePath: String): IO[LockboxConfig] =
    ConfigReader
      .loadProperties(filePath)
      .use {
        properties =>
          LockboxConfigParser.parseConfig(properties)
      }

  private def printProperties(filePath: String): IO[Unit] =
    loadProperties(filePath).flatMap {
      config =>
        logger.info("Loaded properties from config file: " + filePath) *>
          logger.info("Lockbox Config: " + config)
    }

  private def printLogger(configPath: String): IO[Unit] = for {
    _ <- logger.info("Hello, World!")
    _ <- logger.error("This is an error message")
    _ <- logger.info("Reading properties from config file from path: " + configPath)
    _ <- printProperties(configPath)
    _ <- logger.error("This is an error 2 message")
  } yield ()

  private def startProcess(configPath: String, appConfig: AppConfig): IO[Unit] = for {
    _             <- logger.info("Performing operation: " + appConfig.operation)
    _             <- logger.info(s"Using config file at: ${appConfig.configPath}")
    lockboxConfig <- loadProperties(configPath)
    _             <- logger.info("Starting file processing...")
    _ <- Processor.processFiles(appConfig.decryptFolder, appConfig.encryptFolder, lockboxConfig, appConfig.operation)
  } yield ()

  override def main: Opts[IO[ExitCode]] = Configs.appConfigOpts.map {
    appConfig => startProcess(appConfig.configPath, appConfig).as(ExitCode.Success)
  }

end MainApp
