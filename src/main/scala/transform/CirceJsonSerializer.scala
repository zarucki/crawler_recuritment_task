package transform

import cats.data.EitherT
import cats.effect.Sync
import io.circe.{Encoder, Json}
import io.circe.syntax._

class CirceJsonSerializer[F[_], TInput](implicit encoder: Encoder[TInput])
    extends JsonSerializer[F, TInput, io.circe.Json] {

  override def asJson(input: TInput)(implicit F: Sync[F]): EitherT[F, Throwable, Json] = {
    EitherT.right(F.delay(input.asJson))
  }

  override def arrayAsJson(input: List[TInput])(implicit F: Sync[F]): EitherT[F, Throwable, Json] = {
    EitherT.right(F.delay(input.asJson))
  }
}
