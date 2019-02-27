package extract.fetch
import cats.effect.Effect
import org.http4s.client.Client

trait HttpFetcher[F[_]] {
  def fetchHtmlFromUrl(url: String, client: Client[F])(implicit F: Effect[F]): F[String]
  def startClient()(implicit F: Effect[F]): F[Client[F]]
}
