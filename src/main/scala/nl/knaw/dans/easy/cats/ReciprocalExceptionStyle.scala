package nl.knaw.dans.easy.cats

object ReciprocalExceptionStyle extends App {

  def parse(s: String): Int = {
    if (s.matches("-?[0-9]+")) s.toInt
    else throw new NumberFormatException(s"$s is not a valid integer.")
  }

  def reciprocal(i: Int): Double = {
    if (i == 0) throw new IllegalArgumentException("Cannot take reciprocal of 0.")
    else 1.0 / i
  }

  def stringify(d: Double): String = d.toString

  def magic(s: String): String = {
    stringify(reciprocal(parse(s)))
  }
      
  try {
    val s = magic("123")
    println(s"Got reciprocal: $s")
  }
  catch {
    case _: NumberFormatException => println("not a number!")
    case _: IllegalArgumentException => println("can't take reciprocal of 0!")
    case _ => println("got unknown exception")
  }
}
