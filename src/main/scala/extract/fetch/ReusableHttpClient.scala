package extract.fetch
import cats.effect.Effect

trait ReusableHttpClient[F[_]] {
  def fetchHtmlFromUrl(url: String)(implicit F: Effect[F]): F[String]
  def shutDown: F[Unit]
}
