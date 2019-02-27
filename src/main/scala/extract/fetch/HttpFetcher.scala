package extract.fetch
import cats.effect.Effect

trait HttpFetcher[F[_]] {
  def fetchHtmlFromUrl(url: String)(implicit F: Effect[F]): F[String]
}
