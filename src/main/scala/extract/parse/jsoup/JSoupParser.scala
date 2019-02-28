package extract.parse.jsoup
import cats.data.EitherT
import cats.effect.Sync
import extract.parse.{HtmlParser, ParsedHtml}
import cats.implicits._
import org.jsoup.Jsoup

class JSoupParser[F[_]] extends HtmlParser[F] {
  override def parse(html: String)(implicit F: Sync[F]): EitherT[F, Throwable, ParsedHtml] = {
    EitherT(
      F.pure(
        Either.catchNonFatal(
          JSoupParsedHtml(Jsoup.parse(html).body())
        )
      )
    )
  }
}
