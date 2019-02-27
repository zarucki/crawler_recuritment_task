package extract
import cats.effect._
import cats.implicits._
import extract.fetch.HttpFetcher
import extract.parse._

class DomainProfileExtractor[F[_]] extends Extractor[F] {
  def extractData(numberOfPages: Int,
                  domainProfile: DomainProfile,
                  httpFetcher: HttpFetcher[F],
                  htmlParser: HtmlParser)(implicit F: Effect[F]): F[List[Seq[String]]] = {

    val pagesToFetch = (domainProfile.firstIndex until (domainProfile.firstIndex + numberOfPages)).toList

    F.pure(pagesToFetch).flatMap { pages =>
      pages
        .map { pageIndex =>
          httpFetcher
            .fetchHtmlFromUrl(domainProfile.urlPattern.format(pageIndex))
            .map(htmlParser.parse)
            .map { parsedHtml =>
              parsedHtml.getMatchingElements(domainProfile.mainCssSelector).map { matchingRootElement =>
                domainProfile.relativeDetailCssSelectors.map { htmlValueExtractor =>
                  matchingRootElement.getString(htmlValueExtractor)
                }
              }
            }
        }
        .sequence
        .map(_.flatten)
    }
  }
}
