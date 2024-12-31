package dev.prabhat.models

import cats.data.ValidatedNel
import cats.syntax.validated._
import com.monovore.decline.Argument

enum Operation:
  case Encrypt, Decrypt

object Operation:
  given Argument[Operation] = Argument.from("operation") {
    case "encrypt" => Operation.Encrypt.validNel
    case "decrypt" => Operation.Decrypt.validNel
    case other => s"Invalid operation: $other. Valid operations are: encrypt, decrypt.".invalidNel
  }

end Operation
