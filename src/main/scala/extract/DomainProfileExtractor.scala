package extract
import cats.effect._
import cats.implicits._
import extract.fetch.ReusableHttpClient
import extract.parse._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import fs2.{Stream => FStream}

class DomainProfileExtractor[F[_]] extends Extractor[F] {
  def extractData(numberOfPages: Int,
                  domainProfile: DomainProfile,
                  httpFetcher: F[ReusableHttpClient[F]],
                  htmlParser: HtmlParser)(implicit F: Effect[F]): F[List[Seq[String]]] = {
    assert(numberOfPages > 0, "number of pages needs to be greater than 0")

    def useHttpClient(httpClient: ReusableHttpClient[F]) = {
      val pagesToFetch = (domainProfile.firstIndex until (domainProfile.firstIndex + numberOfPages)).toList

      val result = Slf4jLogger.create[F].flatMap { logger =>
        F.pure(pagesToFetch).flatMap { pages =>
          pages
            .map { pageIndex =>
              domainProfile.urlPattern.format(pageIndex)
            }
            .map { urlToFetch =>
              httpClient
                .fetchHtmlFromUrl(urlToFetch)
                .flatMap { htmlString =>
                  logger.info(s"Fetched html from $urlToFetch").map { case _ => htmlString }
                }
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

      FStream.eval(result)
    }

    FStream.bracket(httpFetcher)(useHttpClient, release = _.shutDown).compile.foldMonoid
  }
}
