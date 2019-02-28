package transform

import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import io.circe.syntax._

class CirceJsonSerializer[TInput](implicit encoder: Encoder[TInput]) extends JsonSerializer[TInput, io.circe.Json] {
  override def asJson(input: TInput): Json = {
    input.asJson
  }
  override def arrayAsJson(inputArray: List[TInput]): Json = {
    inputArray.asJson
  }
}
