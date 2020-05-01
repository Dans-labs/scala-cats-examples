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

import cats.scalatest.ValidatedValues
import nl.knaw.dans.easy.cats.PersonParserWithValidated._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PersonParserWithValidatedSpec extends AnyFlatSpec with Matchers with ValidatedValues {

  "getName" should "fail on a short name" in {
    val data = Map("name" -> "Ab", "age" -> "12", "phone" -> "12345")
    getName(data).invalidValue.toList should contain only "name too short: 'Ab'"
  }

  it should "return the name if it's long enough" in {
    val data = Map("name" -> "FooBar", "age" -> "12", "phone" -> "12345")
    getName(data).value shouldBe "FooBar"
  }

  it should "fail if no name field is provided" in {
    val data = Map("age" -> "12", "phone" -> "12345")
    getName(data).invalidValue.toList should contain only "no name found"
  }

  "getAge" should "parse the number provided by the input" in {
    val data = Map("name" -> "FooBar", "age" -> "12", "phone" -> "12345")
    getAge(data).value shouldBe 12
  }

  it should "fail if the age is not a number" in {
    val data = Map("name" -> "FooBar", "age" -> "abc", "phone" -> "12345")
    getAge(data).invalidValue.toList should contain only "value 'abc' is not a number"
  }

  it should "fail if no age is provided" in {
    val data = Map("name" -> "FooBar", "phone" -> "12345")
    getAge(data).invalidValue.toList should contain only "no age found"
  }

  "parseInt" should "parse an integer correctly" in {
    parseInt("123").value shouldBe 123
  }

  it should "fail on a non-integer input" in {
    parseInt("abc").invalidValue.toList should contain only "value 'abc' is not a number"
  }

  "getPerson" should "return a Person object when all valid data is provided" in {
    val data = Map("name" -> "FooBar", "age" -> "12", "phone" -> "12345")
    getPerson(data).value shouldBe Person("FooBar", 12, "12345")
  }

  it should "combine errors in parsing step" in {
    getPerson(Map.empty[String, String]).invalidValue.toList should contain inOrderOnly(
      "no name found",
      "no age found",
      "no phone found",
    )
  }
}
