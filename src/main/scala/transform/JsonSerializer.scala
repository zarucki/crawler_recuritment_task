package transform
import cats.effect.Sync

trait JsonSerializer[F[_], TInput, TOutput] {
  def asJson(input: TInput)(implicit F: Sync[F]): F[TOutput]
  def arrayAsJson(input: List[TInput])(implicit F: Sync[F]): F[TOutput]
}
