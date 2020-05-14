package nl.knaw.dans.easy.cats

import cats.instances.list._
import cats.instances.try_._
import cats.syntax.traverse._

import scala.util.{ Failure, Success, Try }

object ReciprocalTryStyle extends App {

  def parse(s: String): Try[Int] = {
    if (s.matches("-?[0-9]+")) Success(s.toInt)
    else Failure(new NumberFormatException(s"$s is not a valid integer."))
  }

  def reciprocal(i: Int): Try[Double] = {
    if (i == 0) Failure(new IllegalArgumentException("Cannot take reciprocal of 0."))
    else Success(1.0 / i)
  }

  def stringify(d: Double): String = d.toString

  def magic(s: String): Try[String] = {
    parse(s)
      .flatMap(reciprocal)
      .map(stringify)
  }

  def muchMagic(list: List[String]): Try[List[String]] = {
    list.traverse(input => magic(input)) // fail-fast
  }

  magic("123") match {
    case Success(s) => println(s"Got reciprocal: $s")
    case Failure(_: NumberFormatException) => println("not a number!")
    case Failure(_: IllegalArgumentException) => println("can't take reciprocal of 0!")
    case Failure(_) => println("got unknown exception")
  }

  println(muchMagic(List("123", "456", "789")))
  println(muchMagic(List("123", "abc", "789", "0")))
}
