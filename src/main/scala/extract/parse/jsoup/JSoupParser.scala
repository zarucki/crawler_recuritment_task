package extract.parse.jsoup
import extract.parse.{HtmlParser, ParsedHtml}
import org.jsoup.Jsoup

class JSoupParser extends HtmlParser {
  override def parse(html: String): ParsedHtml = {
    // TODO: parsing can crash hard
    JSoupParsedHtml(Jsoup.parse(html).body())
  }
}
