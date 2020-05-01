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

import scala.util.{ Failure, Success, Try }

object PersonParserWithTry extends App {

  case class Person(name: String, age: Int)

  type Data = Map[String, String]
//  val data: Data = Map("name" -> "Richard", "age" -> "27")
//  val p = Person("Richard", 27)

  def getName(data: Data): Try[String] = {
    data.get("name")
      .map(name => if (name.length < 3) Failure(new Exception(s"name too short: $name"))
                   else Success(name))
      .getOrElse(Failure(new NoSuchElementException("no name found")))
  }

  def getAge(data: Data): Try[Int] = {
    data.get("age")
      .map(ageString => parseInt(ageString))
      .getOrElse(Failure(new NoSuchElementException("no age found")))
  }

  def parseInt(s: String): Try[Int] = Try {
    s.toInt
  }

  def getPerson(data: Data): Try[Person] = {
    for {
      name <- getName(data)
      age <- getAge(data)
    } yield Person(name, age)
  }

  println(getPerson(Map("name" -> "Richard", "age" -> "27")))
  println(getPerson(Map("name" -> "Richard", "age" -> "abc")))
  println(getPerson(Map("name" -> "Ab", "age" -> "abc")))
}
