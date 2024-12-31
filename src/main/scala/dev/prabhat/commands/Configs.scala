package dev.prabhat.commands

import cats.implicits.*
import com.monovore.decline.Opts
import dev.prabhat.models.AppConfig
import dev.prabhat.models.Operation

object Configs:

  private val configOpt: Opts[String] = Opts.option[String](
    long = "config",
    help = "Path to the configuration file",
    short = "c"
  )

  private val operation: Opts[Operation] = Opts.option[Operation](
    long = "operation",
    help = "Specify the operation to perform (encrypt, decrypt)",
    short = "o"
  )

  private val encryptFolder: Opts[String] = Opts.option[String](
    long = "encrypt",
    help = "Path to the folder to encrypt",
    short = "ef"
  )

  private val decryptFolder: Opts[String] = Opts.option[String](
    long = "decrypt",
    help = "Path to the folder to decrypt",
    short = "df"
  )

  val appConfigOpts: Opts[AppConfig] = (configOpt, operation, encryptFolder, decryptFolder).mapN(AppConfig.apply)

end Configs
