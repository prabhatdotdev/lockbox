package dev.prabhat.service

import cats.effect.IO
import cats.effect.Resource
import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import scala.io.Source

object FileOperations:

  def readFileContent(filePath: Path): IO[String] = {
    Resource
      .fromAutoCloseable(IO.blocking(Source.fromFile(filePath.toFile)))
      .use {
        source =>
          IO.blocking(source.getLines().mkString(System.lineSeparator()))
      }
  }

  def writeFile(filePath: Path, content: String): IO[Unit] = {
    IO.blocking {
      val parentDir = filePath.getParent
      if (parentDir != null && !Files.exists(parentDir)) {
        Files.createDirectories(parentDir)
      }
    }.flatMap {
      _ =>
        Resource
          .fromAutoCloseable(
            IO.blocking(
              Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            )
          )
          .use {
            writer =>
              IO.blocking {
                writer.write(content)
              }
          }
    }
  }

  def deleteFile(filePath: Path): IO[Unit] = IO.blocking {
    if (Files.exists(filePath)) {
      Files.delete(filePath)
    } else {
      throw new IllegalArgumentException(s"File not found: $filePath")
    }
  }

end FileOperations
