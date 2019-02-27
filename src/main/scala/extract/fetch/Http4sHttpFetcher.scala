package extract.fetch

import cats.effect.Effect
import cats.implicits._
import org.http4s.client.blaze.Http1Client
import org.http4s.{Request, Uri}

class Http4sHttpFetcher[F[_]] extends HttpFetcher[F] {
  override def fetchHtmlFromUrl(url: String)(implicit F: Effect[F]): F[String] = {
    // TODO: parse uri better
    val req = Request[F](uri = Uri.unsafeFromString(url))

    Http1Client
      .stream[F]()
      .flatMap(_.streaming(req)(_.bodyAsText))
      .compile
      .toVector
      .map(_.reduce(_ + _))
  }
}
