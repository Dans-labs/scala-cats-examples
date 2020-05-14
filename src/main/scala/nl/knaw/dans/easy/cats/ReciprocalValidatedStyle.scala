package nl.knaw.dans.easy.cats

import cats.data.Validated.{ Invalid, Valid }
import cats.data.{ Validated, ValidatedNel }
import cats.instances.list._
import cats.syntax.traverse._
import cats.syntax.validated._

object ReciprocalValidatedStyle extends App {

  sealed abstract class Error(msg: String)
  case class InvalidNumber(s: String) extends Error(s"Cannot parse string '$s'")
  case class CannotDivideByZero() extends Error("Cannot divide by 0")

  def parse(s: String): Validated[InvalidNumber, Int] = {
    if (s.matches("-?[0-9]+")) s.toInt.valid
    else InvalidNumber(s).invalid
  }

  def reciprocal(i: Int): Validated[CannotDivideByZero, Double] = {
    if (i == 0) CannotDivideByZero().invalid
    else (1.0 / i).valid
  }

  def stringify(d: Double): String = d.toString

  def magic(s: String): Validated[Error, String] = {
    println(s">>> $s")
    parse(s)
      .andThen(reciprocal)
      .map(stringify)
  }

  def muchMagic(list: List[String]): ValidatedNel[Error, List[String]] = {
    list.traverse(input => magic(input).toValidatedNel) // fail-slow
  }

  magic("123") match {
    case Valid(s) => println(s"Got reciprocal: $s")
    case Invalid(_: InvalidNumber) => println("not a number!")
    case Invalid(_: CannotDivideByZero) => println("can't take reciprocal of 0!")
  }

  println(muchMagic(List("123", "456", "789")))
  println(muchMagic(List("123", "abc", "789", "0")))
}
