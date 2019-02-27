package extract.fetch

import cats.effect.Effect
import cats.implicits._
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client
import org.http4s.{Request, Uri}

object Https4Client {
  def apply[F[_]: Effect]() = {
    // TODO: change default config of http4s client?
    Http1Client.apply[F]().map(new Https4Client[F](_))
  }
}

class Https4Client[F[_]](client: Client[F]) extends ReusableHttpClient[F] {
  override def fetchHtmlFromUrl(url: String)(implicit F: Effect[F]): F[String] = {
    // TODO: parse uri better
    val req = Request[F](uri = Uri.unsafeFromString(url))

    client
      .streaming(req)(_.bodyAsText)
      .compile
      .toVector
      .map(_.reduce(_ + _))
  }

  override def shutDown: F[Unit] = client.shutdown
}
