package nl.knaw.dans.easy.cats

import cats.syntax.either._
import cats.syntax.traverse._
import cats.instances.list._
import cats.instances.either._

object ReciprocalEitherStyle extends App {

  sealed abstract class MyErrorStructure(msg: String)
  case class InvalidNumber(s: String) extends MyErrorStructure(s"Cannot parse string '$s'")
  case class CannotDivideByZero() extends MyErrorStructure("Cannot divide by 0")

  def parse(s: String): Either[InvalidNumber, Int] = {
    if (s.matches("-?[0-9]+")) s.toInt.asRight
    else InvalidNumber(s).asLeft
  }

  def reciprocal(i: Int): Either[CannotDivideByZero, Double] = {
    if (i == 0) CannotDivideByZero().asLeft
    else (1.0 / i).asRight
  }

  def stringify(d: Double): String = d.toString

  def magic(s: String): Either[MyErrorStructure, String] = {
    println(s">>> $s")
    parse(s)
      .flatMap(reciprocal)
      .map(stringify)
  }
  
  def muchMagic(list: List[String]): Either[MyErrorStructure, List[String]] = {
    list.traverse(magic) //fail-fast
  }

  magic("123") match {
    case Right(s) => println(s"Got reciprocal: $s")
    case Left(_: InvalidNumber) => println("not a number!")
    case Left(_: CannotDivideByZero) => println("can't take reciprocal of 0!")
  }

  println(muchMagic(List("123", "456", "789")))
  println(muchMagic(List("123", "abc", "789", "0")))
}
