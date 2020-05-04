package nl.knaw.dans.easy

package object cats {

  abstract class ParseError(row: Int, val msg: String) {
    override def toString: String = s"row $row: $msg"
  }
  case class NoSuchValueError(row: Int, valueType: String) extends ParseError(row, s"no $valueType found")
  case class InvalidNameError(row: Int, name: String) extends ParseError(row, s"name too short: '$name'")
  case class NumberParseError(row: Int, s: String) extends ParseError(row, s"value '$s' is not a number")
  case class ValidationError(message: String) extends ParseError(-1, message)
}
