package extract.parse.jsoup
import extract.DomainProfile.{CssSelector, HtmlValueExtractor}
import extract.parse._
import cats.implicits._
import org.jsoup.nodes.Element
import scala.collection.JavaConverters._

case class JSoupParsedHtml(element: Element) extends ParsedHtml {

  override def getMatchingElements(cssSelector: CssSelector): Either[Throwable, List[ParsedHtml]] = {
    Either.catchNonFatal(element.select(cssSelector)).map(_.asScala.map(JSoupParsedHtml(_)).toList).left.map {
      new Exception(
        s"Error while matching selector $cssSelector with ${elementAsConsoleSttring(element)}.",
        _
      )
    }
  }

  override def getString(htmlValueExtractor: HtmlValueExtractor): Either[Throwable, String] = {
    val elementToExtractFrom: Either[Throwable, Element] =
      htmlValueExtractor.relativeCssSelector match {
        case Some(selector) =>
          Either
            .catchNonFatal(element.select(selector).asScala.head)
            .left
            .map {
              new Exception(
                s"Exception while accessing applying selector $selector with ${elementAsConsoleSttring(element)}",
                _
              )
            }
        case None => Right(element)
      }

    elementToExtractFrom
      .map { el =>
        htmlValueExtractor.valueToExtract match {
          case Attribute(name) => el.attr(name)
          case Text            => el.text()
          case InnerHtml       => el.html()
        }
      }
  }

  private def elementAsConsoleSttring(element: Element) = {
    element.toString.replaceAll("\\s+", " ")
  }
}
