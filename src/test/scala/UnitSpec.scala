import cats.effect.Effect
import extract.DomainProfile
import extract.DomainProfile.{CssSelector, HtmlValueExtractor}
import extract.fetch.ReusableHttpClient
import extract.parse.{HtmlParser, ParsedHtml}
import org.scalatest._

import scala.collection.immutable.Queue

abstract class UnitSpec extends FlatSpec with Assertions with OptionValues with Inspectors {
  protected def mockHttpClient[F[_]: Effect]() = new MockReusableHttpClient[F](Effect[F].unit)
  protected def mockParser() = new MockParser

  class MockReusableHttpClient[F[_]](unit: F[Unit]) extends ReusableHttpClient[F] {
    var visitedUrlHistory = Queue[String]()

    override def fetchHtmlFromUrl(url: String)(implicit F: Effect[F]): F[String] = {
      visitedUrlHistory = visitedUrlHistory :+ url
      F.pure("")
    }

    override def shutDown: F[Unit] = unit
  }

  case class MockParsedHtml(cssSelectorHistory: Queue[CssSelector] = Queue.empty) extends ParsedHtml {
    var accessedValuesHistory: Queue[HtmlValueExtractor] = Queue[HtmlValueExtractor]()

    override def getMatchingElements(cssSelector: CssSelector): List[ParsedHtml] =
      List(MockParsedHtml(cssSelectorHistory :+ cssSelector))

    override def getString(htmlValueExtractor: DomainProfile.HtmlValueExtractor): String = {
      accessedValuesHistory = accessedValuesHistory :+ htmlValueExtractor
      ""
    }
  }

  class MockParser extends HtmlParser {
    override def parse(html: String): ParsedHtml = new MockParsedHtml()
  }
}
