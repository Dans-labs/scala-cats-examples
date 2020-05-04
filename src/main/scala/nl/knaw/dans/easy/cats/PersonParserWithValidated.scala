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
import cats.instances.list._
import cats.syntax.apply._
import cats.syntax.traverse._
import cats.syntax.validated._

object PersonParserWithValidated extends App {

  case class Person(name: String, age: Int, phone: String)

  type Data = Map[String, String]
  // Validated is 'FailSlow': it is meant to collect errors before failing
  type Parsed[T] = ValidatedNel[ParseError, T] // type alias for Validated[NonEmptyList[String], T] --> It's either a Valid[T] or an Invalid[NonEmptyList[String]]
  //  val data: Data = Map("name" -> "Alice", "age" -> "27")
  //  val p = Person("Alice", 27)

  def getName(row: Int, data: Data): Parsed[String] = {
    // e.invalidNel turns a value of type E into a value of type ValidatedNel[E, ?]
    // t.validNel turns a value of type T into a value of type ValidatedNel[?, T]

    data.get("name")
      .map(name => if (name.length < 3) InvalidNameError(row, name).invalidNel
                   else name.validNel)
      .getOrElse(NoSuchValueError(row, "name").invalidNel)
  }

  def getAge(row: Int, data: Data): Parsed[Int] = {
    data.get("age")
      .map(ageString => parseInt(row, ageString))
      .getOrElse(NoSuchValueError(row, "age").invalidNel)
  }

  def getPhone(row: Int, data: Data): Parsed[String] = {
    data.get("phone")
      .map(phone => phone.validNel)
      .getOrElse(NoSuchValueError(row, "phone").invalidNel)
  }

  def parseInt(row: Int, s: String): Parsed[Int] = {
    // Validated.catchOnly[Exception type] { ... } only catches exceptions of the given type; other exceptions are thrown
    // Validated.catchNonFatal { ... } catches all exception types
    Validated.catchOnly[NumberFormatException] { s.toInt }
      .leftMap(_ => NumberParseError(row, s))
      .toValidatedNel
  }

  def getPerson(row: Int, data: Data): Parsed[Person] = {
    /*
       (getName(data), getAge(data)).tupled turns a (Parsed[String], Parsed[Int]) into a Parsed[(String, Int)]
       
       getName(data)     | getAge(data)      | result
       ----------------- | ----------------- | ------
       Valid(name)       | Valid(age)        | Valid((name, age))
       Invalid([e1, e2]) | Valid(age)        | Invalid([e1, e2])
       Valid(name)       | Invalid([e1, e2]) | Invalid([e1, e2])
       Invalid([e1, e2]) | Invalid([e3, e4]) | Invalid([e1, e2, e3, e4])
     */

    (getName(row, data), getAge(row, data), getPhone(row, data)).tupled.map(tuple => Person(tuple._1, tuple._2, tuple._3))
    (getName(row, data), getAge(row, data), getPhone(row, data)).mapN((name, age, phone) => Person(name, age, phone))
    (getName(row, data), getAge(row, data), getPhone(row, data)).mapN(Person)
  }

  val data1: Data = Map("name" -> "Alice", "age" -> "27", "phone" -> "12345")
  val data2: Data = Map("name" -> "Alice", "age" -> "abc", "phone" -> "12345")
  val data3: Data = Map("name" -> "Ab", "age" -> "abc")
  val data4: Data = Map.empty
  println(getPerson(1, data1))
  println(getPerson(2, data2))
  println(getPerson(3, data3))
  println(getPerson(4, data4))

  def getPersons(datas: List[Data]): Parsed[List[Person]] = {
    datas.zipWithIndex
      .map { case (data, index) => (data, index + 1) }
      .traverse { case (data, index) => getPerson(index, data) }
    // when xs.map(f) produces a M[N[T]]
    // then xs.traverse(f) produces a N[M[T]]
    // in this example:      `map` would give List[Parsed[Person]]
    //                  `traverse` would give Parsed[List[Person]]
  }

  println(getPersons(List(data1)))
  println(getPersons(List(data4)))
  println(getPersons(List(data1, data1.updated("name", "Bob").updated("age", "72"))))
  println(getPersons(List(data1, data2, data3, data4)))

  def validatePersons(persons: List[Person]): Parsed[Unit] = {
    // validate some arbitrary rules:
    //  * sum of all ages must not exceed 100
    //  * all phone numbers are unique

    def validateSumOfAges(max: Int): Parsed[Unit] = {
      if (persons.map(_.age).sum <= max)
        ().validNel
      else
        ValidationError(s"sum of ages exceeds $max").invalidNel
    }

    def validateUniquePhoneNumbers: Parsed[Unit] = {
      val ds = persons.map(_.phone).distinct
      if (persons.length == ds.length)
        ().validNel
      else
        ValidationError("not all phone numbers are unique").invalidNel
    }

    (validateSumOfAges(100), validateUniquePhoneNumbers).tupled.map(_ => ())
  }

  def readPersons(data: List[Data]): Parsed[List[Person]] = {
    // (1) read persons
    // (2) run validations

    // `left.andThen(right)` first evaluates `left` and then (if `left` is Valid) evaluate `right`
    getPersons(data)
      .andThen(persons => validatePersons(persons).map(_ => persons))
  }

  println(readPersons(List(data1)))
  println(readPersons(List(data4)))
  println(readPersons(List(data1, data1.updated("name", "Bob").updated("age", "82"))))
  println(readPersons(List(data1, data2, data3, data4)))
}
