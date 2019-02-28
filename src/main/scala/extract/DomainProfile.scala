package extract

import extract.DomainProfile.{CssSelector, HtmlValueExtractor}
import extract.parse.HtmlValue

abstract class DomainProfile[TEntity](
    val urlPattern: String, // TODO: more complex paging schemes?
    val mainCssSelector: CssSelector,
    val entityDetailsExtractors: Seq[(HtmlValueExtractor, (TEntity, String) => TEntity)],
    val firstIndex: Int = 1,
    val itemsPerPage: Int = 20
) {
  def emptyEntity: TEntity
}

object DomainProfile {
  type CssSelector = String

  case class HtmlValueExtractor(relativeCssSelector: Option[CssSelector], valueToExtract: HtmlValue)
}
