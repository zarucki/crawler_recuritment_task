package extract.fetch
import cats.data.EitherT
import cats.effect.Effect

trait ReusableHttpClient[F[_]] {
  def fetchHtmlFromUrl(url: String)(implicit F: Effect[F]): EitherT[F, Throwable, String]
  def shutDown: F[Unit]
}
