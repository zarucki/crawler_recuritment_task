package extract.fetch

import cats.effect.Effect
import cats.implicits._
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client
import org.http4s.{Request, Uri}

// TODO: Does not encapsulate sequence of calls
class Http4sHttpFetcher[F[_]] extends HttpFetcher[F] {
  override def fetchHtmlFromUrl(url: String, client: Client[F])(implicit F: Effect[F]): F[String] = {
    // TODO: parse uri better
    val req = Request[F](uri = Uri.unsafeFromString(url))

    client
      .streaming(req)(_.bodyAsText)
      .compile
      .toVector
      .map(_.reduce(_ + _))

  }
  override def startClient()(implicit F: Effect[F]): F[Client[F]] = {
    // TODO: config http client?
    Http1Client.apply[F]()
  }
}
