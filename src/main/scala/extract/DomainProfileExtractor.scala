package extract
import cats.effect._
import cats.implicits._
import extract.fetch.HttpFetcher
import extract.parse._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.Client
import fs2.{Stream => FStream}

class DomainProfileExtractor[F[_]] extends Extractor[F] {
  def extractData(numberOfPages: Int,
                  domainProfile: DomainProfile,
                  httpFetcher: HttpFetcher[F],
                  htmlParser: HtmlParser)(implicit F: Effect[F]): F[List[Seq[String]]] = {
    assert(numberOfPages > 0, "number of pages needs to be greater than 0")

    def useHttpClient(httpClient: Client[F]) = {
      val pagesToFetch = (domainProfile.firstIndex until (domainProfile.firstIndex + numberOfPages)).toList

      val result = Slf4jLogger.create[F].flatMap { logger =>
        F.pure(pagesToFetch).flatMap { pages =>
          pages
            .map { pageIndex =>
              domainProfile.urlPattern.format(pageIndex)
            }
            .map { urlToFetch =>
              httpFetcher
                .fetchHtmlFromUrl(urlToFetch, httpClient)
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

    FStream.bracket(httpFetcher.startClient())(useHttpClient, release = _.shutdown).compile.foldMonoid
  }
}
