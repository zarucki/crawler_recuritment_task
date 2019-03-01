package extract.parse

import cats.data.EitherT
import cats.effect.Sync
import extract.DomainProfile.{CssSelector, HtmlValueExtractor}

trait ParsedHtml {
  def getMatchingElements(cssSelector: CssSelector): Either[Throwable, List[ParsedHtml]]
  def getString(htmlValueExtractor: HtmlValueExtractor): Either[Throwable, String]
}

trait HtmlParser[F[_]] {
  def parse(html: String)(implicit F: Sync[F]): EitherT[F, Throwable, ParsedHtml]
}
