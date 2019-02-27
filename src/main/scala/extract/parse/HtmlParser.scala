package extract.parse

import extract.DomainProfile.{CssSelector, HtmlValueExtractor}

trait ParsedHtml {
  def getMatchingElements(cssSelector: CssSelector): List[ParsedHtml]
  def getString(htmlValueExtractor: HtmlValueExtractor): String
}

trait HtmlParser {
  def parse(html: String): ParsedHtml
}
