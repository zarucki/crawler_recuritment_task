import cats.data.EitherT
import cats.effect.{Effect, Sync}
import extract.DomainProfile
import extract.DomainProfile.{CssSelector, HtmlValueExtractor}
import extract.fetch.ReusableHttpClient
import extract.parse.{HtmlParser, ParsedHtml}
import org.scalatest._

import scala.collection.immutable.Queue

abstract class UnitSpec extends FlatSpec with Assertions with OptionValues with Inspectors {
  protected def mockHttpClient[F[_]: Effect]() = new MockReusableHttpClient[F](Effect[F].unit)
  protected def mockParser[F[_]: Effect]() = new MockParser[F]()

  class MockReusableHttpClient[F[_]](unit: F[Unit]) extends ReusableHttpClient[F] {
    var visitedUrlHistory = Queue[String]()

    override def fetchHtmlFromUrl(url: String)(
        implicit F: Effect[F]
    ): EitherT[F, Throwable, String] = {
      visitedUrlHistory = visitedUrlHistory :+ url
      EitherT.right(F.pure(""))
    }

    override def shutDown: F[Unit] = unit
  }

  case class MockParsedHtml(cssSelectorHistory: Queue[CssSelector] = Queue.empty) extends ParsedHtml {
    var accessedValuesHistory: Queue[HtmlValueExtractor] = Queue[HtmlValueExtractor]()

    override def getMatchingElements(cssSelector: CssSelector): Either[Throwable, List[ParsedHtml]] = {
      Right(List(MockParsedHtml(cssSelectorHistory :+ cssSelector)))
    }

    override def getString(htmlValueExtractor: DomainProfile.HtmlValueExtractor): Either[Throwable, String] = {
      accessedValuesHistory = accessedValuesHistory :+ htmlValueExtractor
      Right("")
    }
  }

  class MockParser[F[_]] extends HtmlParser[F] {
    override def parse(html: String)(
        implicit F: Sync[F]
    ): EitherT[F, Throwable, ParsedHtml] = {
      EitherT.right(F.pure(new MockParsedHtml()))
    }
  }
}
