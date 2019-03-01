package extract.profiles
import extract.DomainProfile
import extract.DomainProfile.HtmlValueExtractor
import extract.parse.{Attribute, InnerHtml, Text}

case class BashOrgContent(id: Long = -1L, points: Long = -1L, content: String = "")

object BashOrgProfile
    extends DomainProfile[BashOrgContent](
      urlPattern = "http://bash.org.pl/latest/?page=%d",
      mainCssSelector = ".post",
      entityDetailsExtractors = Seq( // TODO: Maybe Monocle library here?
        (HtmlValueExtractor(None, Attribute("id")), (entity, value) => entity.copy(id = value.substring(1).toLong)),
        (HtmlValueExtractor(Some(".points"), Text), (entity, value) => entity.copy(points = value.toLong)),
        (HtmlValueExtractor(Some(".post-content"), InnerHtml), (entity, value) => entity.copy(content = value))
      )
    ) {
  override def emptyEntity: BashOrgContent = BashOrgContent()
}
