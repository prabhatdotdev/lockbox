package dev.prabhat.config

import cats.effect.kernel.Resource
import cats.effect.IO
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Properties

object ConfigReader:

  def loadProperties(filePath: String): Resource[IO, Properties] =
    Resource
      .fromAutoCloseable(
        IO.blocking(new FileInputStream(filePath))
          .handleErrorWith {
            case _: FileNotFoundException =>
              IO.raiseError(new RuntimeException(s"Configuration file not found: $filePath"))
            case e: IOException =>
              IO.raiseError(new RuntimeException(s"Error reading configuration file: $filePath", e))
          }
      )
      .flatMap {
        fis =>
          Resource.eval(
            IO.blocking {
              val properties = new Properties()
              properties.load(fis)
              properties
            }.handleErrorWith {
              e =>
                IO.raiseError(new RuntimeException(s"Failed to load properties from file: $filePath", e))
            }
          )
      }

end ConfigReader
