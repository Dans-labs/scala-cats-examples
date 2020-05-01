/**
 * Copyright (C) 2020 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.cats

import cats.data.{ Validated, ValidatedNel }
import cats.syntax.apply._
import cats.syntax.validated._

object PersonParserWithValidated extends App {

  case class Person(name: String, age: Int, phone: String)

  type Data = Map[String, String]
  type Parsed[T] = ValidatedNel[String, T] // type alias for Validated[NonEmptyList[String], T] --> It's either a Valid[T] or an Invalid[NonEmptyList[String]]
//  val data: Data = Map("name" -> "Richard", "age" -> "27")
//  val p = Person("Richard", 27)

  def getName(data: Data): Parsed[String] = {
    // e.invalidNel turns a value of type E into a value of type ValidatedNel[E, ?]
    // t.validNel turns a value of type T into a value of type ValidatedNel[?, T]
    
    data.get("name")
      .map(name => if (name.length < 3) s"name too short: '$name'".invalidNel
                   else name.validNel)
      .getOrElse("no name found".invalidNel)
  }

  def getAge(data: Data): Parsed[Int] = {
    data.get("age")
      .map(ageString => parseInt(ageString))
      .getOrElse("no age found".invalidNel)
  }

  def getPhone(data: Data): Parsed[String] = {
    data.get("phone")
      .map(phone => phone.validNel)
      .getOrElse("no phone found".invalidNel)
  }

  def parseInt(s: String): Parsed[Int] = {
    // Validated.catchOnly[Exception type] { ... } only catches exceptions of the given type; other exceptions are thrown
    // Validated.catchNonFatal { ... } catches all exception types
    Validated.catchOnly[NumberFormatException] { s.toInt }
      .leftMap(_ => s"value '$s' is not a number")
      .toValidatedNel
  }

  def getPerson(data: Data): Parsed[Person] = {
    /*
       (getName(data), getAge(data)).tupled turns a (Parsed[String], Parsed[Int]) into a Parsed[(String, Int)]
       
       getName(data)     | getAge(data)      | result
       ----------------- | ----------------- | ------
       Valid(name)       | Valid(age)        | Valid((name, age))
       Invalid([e1, e2]) | Valid(age)        | Invalid([e1, e2])
       Valid(name)       | Invalid([e1, e2]) | Invalid([e1, e2])
       Invalid([e1, e2]) | Invalid([e3, e4]) | Invalid([e1, e2, e3, e4])
     */

    (getName(data), getAge(data), getPhone(data)).tupled.map(tuple => Person(tuple._1, tuple._2, tuple._3))
    (getName(data), getAge(data), getPhone(data)).mapN((name, age, phone) => Person(name, age, phone))
    (getName(data), getAge(data), getPhone(data)).mapN(Person)
  }

  println(getPerson(Map("name" -> "Richard", "age" -> "27", "phone" -> "12345")))
  println(getPerson(Map("name" -> "Richard", "age" -> "abc", "phone" -> "12345")))
  println(getPerson(Map("name" -> "Ab", "age" -> "abc")))
}
