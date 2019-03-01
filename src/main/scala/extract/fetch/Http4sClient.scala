package extract.fetch

import cats.data.EitherT
import cats.effect.Effect
import cats.implicits._
import org.http4s.client.Client
import org.http4s.client.blaze.{BlazeClientConfig, Http1Client}
import org.http4s.headers.{`User-Agent`, AgentProduct}
import org.http4s.{BuildInfo, Method, ParseFailure, Request, Status, Uri}

object Http4sClient {
  def apply[F[_]: Effect](): F[Http4sClient[F]] = {
    val defaultConfigWithCustomUserAgent = BlazeClientConfig.defaultConfig.copy(
      userAgent = Some(
        `User-Agent`(
          AgentProduct(
            "http4s-blaze scraper (https://github.com/zarucki/crawler_recuritment_task)",
            Some(BuildInfo.version)
          )
        )
      )
    )

    Http1Client.apply[F](defaultConfigWithCustomUserAgent).map(new Http4sClient[F](_))
  }
}

class Http4sClient[F[_]](client: Client[F]) extends ReusableHttpClient[F] {
  override def fetchHtmlFromUrl(url: String)(implicit F: Effect[F]): EitherT[F, Throwable, String] = {
    val httpResult: Either[ParseFailure, F[Either[Throwable, String]]] = Uri
      .fromString(url)
      .map { uri =>
        Request[F](method = Method.GET, uri)
      }
      .map { req =>
        client
          .streaming[Either[Throwable, String]](req) { response =>
            if (response.status == Status.Ok) {
              response.bodyAsText.map[Either[Throwable, String]](Right(_))
            } else {
              fs2.Stream.eval[F, Either[Throwable, String]](
                F.pure(Left(new Exception(s"Got status ${response.status} for request $req.")))
              )
            }
          }
          .attempt
          .map[Either[Throwable, String]] {
            case Left(throwable)        => Left(throwable)
            case Right(Left(throwable)) => Left(throwable)
            case Right(Right(value))    => Right(value)
          }
          .map(_.left.map[Throwable] {
            new Exception(s"Error while running req: $req.", _)
          })
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
