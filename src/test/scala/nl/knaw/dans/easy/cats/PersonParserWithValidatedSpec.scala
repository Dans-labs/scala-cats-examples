package nl.knaw.dans.easy.cats

import cats.scalatest.ValidatedValues
import nl.knaw.dans.easy.cats.PersonParserWithValidated._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PersonParserWithValidatedSpec extends AnyFlatSpec with Matchers with ValidatedValues {

  "parseInt" should "parse an integer correctly" in {
    parseInt("123").value shouldBe 123
  }
  
  it should "fail on a non-integer input" in {
    parseInt("abc").invalidValue.toList should contain only "value 'abc' is not a number"
  }
  
  
}
