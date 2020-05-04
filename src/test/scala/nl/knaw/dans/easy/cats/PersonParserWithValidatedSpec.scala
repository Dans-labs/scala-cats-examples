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

import cats.scalatest.{ ValidatedMatchers, ValidatedValues }
import nl.knaw.dans.easy.cats.PersonParserWithValidated._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PersonParserWithValidatedSpec extends AnyFlatSpec with Matchers with ValidatedValues with ValidatedMatchers {

  "getName" should "fail on a short name" in {
    val data = Map("name" -> "Ab", "age" -> "12", "phone" -> "12345")
    getName(1, data).invalidValue.toList should contain only InvalidNameError(1, "Ab")
  }

  it should "return the name if it's long enough" in {
    val data = Map("name" -> "FooBar", "age" -> "12", "phone" -> "12345")
    getName(1, data).value shouldBe "FooBar"
  }

  it should "fail if no name field is provided" in {
    val data = Map("age" -> "12", "phone" -> "12345")
    getName(1, data).invalidValue.toList should contain only NoSuchValueError(1, "name")
  }

  "getAge" should "parse the number provided by the input" in {
    val data = Map("name" -> "FooBar", "age" -> "12", "phone" -> "12345")
    getAge(1, data).value shouldBe 12
  }

  it should "fail if the age is not a number" in {
    val data = Map("name" -> "FooBar", "age" -> "abc", "phone" -> "12345")
    getAge(1, data).invalidValue.toList should contain only NumberParseError(1, "abc")
  }

  it should "fail if no age is provided" in {
    val data = Map("name" -> "FooBar", "phone" -> "12345")
    getAge(1, data).invalidValue.toList should contain only NoSuchValueError(1, "age")
  }

  "parseInt" should "parse an integer correctly" in {
    parseInt(1, "123").value shouldBe 123
  }

  it should "fail on a non-integer input" in {
    parseInt(1, "abc").invalidValue.toList should contain only NumberParseError(1, "abc")
  }

  "getPerson" should "return a Person object when all valid data is provided" in {
    val data = Map("name" -> "FooBar", "age" -> "12", "phone" -> "12345")
    getPerson(1, data).value shouldBe Person("FooBar", 12, "12345")
  }

  it should "combine errors in parsing step" in {
    getPerson(1, Map.empty[String, String]).invalidValue.toList should contain inOrderOnly(
      NoSuchValueError(1, "name"),
      NoSuchValueError(1, "age"),
      NoSuchValueError(1, "phone"),
    )
  }

  "getPersons" should "parse all input" in {
    val input = List(
      Map("name" -> "Alice", "age" -> "27", "phone" -> "12345"),
      Map("name" -> "Bob", "age" -> "72", "phone" -> "123456789")
    )
    getPersons(input).value should contain inOrderOnly(
      Person("Alice", 27, "12345"),
      Person("Bob", 72, "123456789"),
    )
  }

  it should "collect errors from parsing both persons" in {
    val input: List[Data] = List(
      Map("name" -> "Ab", "age" -> "abc", "phone" -> "12345"),
      Map.empty,
    )
    getPersons(input).invalidValue.toList should contain inOrderOnly(
      InvalidNameError(1, "Ab"),
      NumberParseError(1, "abc"),
      NoSuchValueError(2, "name"),
      NoSuchValueError(2, "age"),
      NoSuchValueError(2, "phone"),
    )
  }

  "validatePersons" should "yield Unit if all validations succeed" in {
    val input = List(
      Person("Alice", 27, "12345"),
      Person("Bob", 72, "123456789"),
    )
    validatePersons(input) shouldBe valid
  }

  it should "yield a list of errors if validations fail" in {
    val input = List(
      Person("Alice", 27, "12345"),
      Person("Bob", 82, "12345"),
    )
    validatePersons(input).invalidValue.toList should contain inOrderOnly(
      ValidationError("sum of ages exceeds 100"),
      ValidationError("not all phone numbers are unique"),
    )
  }

  "readPersons" should "parse raw data and validate the input" in {
    val input = List(
      Map("name" -> "Alice", "age" -> "27", "phone" -> "12345"),
      Map("name" -> "Bob", "age" -> "72", "phone" -> "123456789")
    )
    readPersons(input).value should contain inOrderOnly(
      Person("Alice", 27, "12345"),
      Person("Bob", 72, "123456789"),
    )
  }

  it should "yield a list of parse errors and not validate the input on invalid input" in {
    val input: List[Data] = List(
      Map("name" -> "Ab", "age" -> "abc", "phone" -> "12345"),
      Map.empty,
    )
    readPersons(input).invalidValue.toList should contain inOrderOnly(
      InvalidNameError(1, "Ab"),
      NumberParseError(1, "abc"),
      NoSuchValueError(2, "name"),
      NoSuchValueError(2, "age"),
      NoSuchValueError(2, "phone"),
    )
  }

  it should "yield a list of validation errors" in {
    val input = List(
      Map("name" -> "Alice", "age" -> "27", "phone" -> "12345"),
      Map("name" -> "Bob", "age" -> "82", "phone" -> "12345")
    )
    readPersons(input).invalidValue.toList should contain inOrderOnly(
      ValidationError("sum of ages exceeds 100"),
      ValidationError("not all phone numbers are unique"),
    )
  }
}
