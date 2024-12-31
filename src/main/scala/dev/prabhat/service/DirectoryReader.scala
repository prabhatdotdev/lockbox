package dev.prabhat.service

import cats.effect.IO
import java.nio.file.Files
import java.nio.file.Path
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import scala.jdk.CollectionConverters.*

object DirectoryReader {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def getAllFiles(directory: String): IO[List[Path]] = {
    val dirPath = Path.of(directory)

    if (!Files.isDirectory(dirPath)) {
      logger.error(s"Invalid directory: $directory") >>
        IO.raiseError(new IllegalArgumentException(s"Invalid directory: $directory"))
    } else {
      IO.blocking {
        Files
          .list(dirPath)
          .iterator()
          .asScala
          .filter(Files.isRegularFile(_))
          .toList
      }
    }
  }

}
