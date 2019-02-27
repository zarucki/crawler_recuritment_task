package extract.parse

sealed trait HtmlValue extends Any
case class Attribute(name: String) extends AnyVal with HtmlValue
case object Text extends HtmlValue
case object InnerHtml extends HtmlValue
