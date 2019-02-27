package extract

import extract.DomainProfile.{CssSelector, HtmlValueExtractor}
import extract.parse.HtmlValue

// TODO: more complex paging schemes?
case class DomainProfile(urlPattern: String,
                         mainCssSelector: CssSelector,
                         relativeDetailCssSelectors: Seq[HtmlValueExtractor],
                         firstIndex: Int = 1)

object DomainProfile {
  type CssSelector = String

  case class HtmlValueExtractor(relativeCssSelector: Option[CssSelector], valueToExtract: HtmlValue)
}
