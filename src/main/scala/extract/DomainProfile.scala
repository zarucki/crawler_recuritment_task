package extract

import extract.DomainProfile.{CssSelector, HtmlValueExtractor}
import extract.parse.HtmlValue

abstract class DomainProfile[TEntity](
    val urlPattern: String,
    val mainCssSelector: CssSelector,
    val entityDetailsExtractors: Seq[(HtmlValueExtractor, (TEntity, String) => TEntity)],
    val firstIndex: Int = 1
) {
  def emptyEntity: TEntity
}

object DomainProfile {
  type CssSelector = String

  case class HtmlValueExtractor(relativeCssSelector: Option[CssSelector], valueToExtract: HtmlValue)
}
