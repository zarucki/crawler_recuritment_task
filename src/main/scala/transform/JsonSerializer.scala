package transform
import cats.data.EitherT
import cats.effect.Sync

trait JsonSerializer[F[_], TInput, TOutput] {

  def asJson(input: TInput)(implicit F: Sync[F]): EitherT[F, Throwable, TOutput]
  def arrayAsJson(input: List[TInput])(implicit F: Sync[F]): EitherT[F, Throwable, TOutput]
}
