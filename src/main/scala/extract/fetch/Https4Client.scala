package extract.fetch

import cats.data.EitherT
import cats.effect.Effect
import cats.implicits._
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client
import org.http4s.{Method, ParseFailure, Request, Uri}

object Https4Client {
  def apply[F[_]: Effect](): F[Https4Client[F]] = {
    Http1Client.apply[F]().map(new Https4Client[F](_))
  }
}

class Https4Client[F[_]](client: Client[F]) extends ReusableHttpClient[F] {
  override def fetchHtmlFromUrl(url: String)(implicit F: Effect[F]): EitherT[F, Throwable, String] = {
    val httpResult: Either[ParseFailure, F[Either[Throwable, String]]] = Uri
      .fromString(url)
      .map { uri =>
        Request[F](method = Method.GET, uri)
      }
      .map { req =>
        client
          .streaming(req)(_.bodyAsText)
          .attempt
          .compile
          .foldMonoid
      }

    EitherT(httpResult match {
      case Left(parseFailure) => F.pure(Left(parseFailure))
      case Right(value)       => value
    })
  }

  override def shutDown: F[Unit] = client.shutdown
}
