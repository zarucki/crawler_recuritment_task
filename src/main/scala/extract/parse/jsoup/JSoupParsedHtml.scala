package extract.parse.jsoup
import extract.DomainProfile.{CssSelector, HtmlValueExtractor}
import extract.parse._
import org.jsoup.nodes.Element
import scala.collection.JavaConverters._

case class JSoupParsedHtml(element: Element) extends ParsedHtml {

  override def getMatchingElements(cssSelector: CssSelector): List[ParsedHtml] = {
    element.select(cssSelector).asScala.map(JSoupParsedHtml(_)).toList
  }

  override def getString(htmlValueExtractor: HtmlValueExtractor): String = {
    htmlValueExtractor.relativeCssSelector
      .map(selector => Option(element.select(selector).first()))
      .getOrElse(Some(element))
      .map { elementToExtractFrom =>
        htmlValueExtractor.valueToExtract match {
          case Attribute(name) => elementToExtractFrom.attr(name)
          case Text            => elementToExtractFrom.text()
          case InnerHtml       => elementToExtractFrom.html()
        }
      }
      .getOrElse("")
  }
}
