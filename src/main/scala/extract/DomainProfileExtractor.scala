package extract
import cats.data.{EitherT, ValidatedNel}
import cats.effect._
import cats.implicits._
import extract.fetch.ReusableHttpClient
import extract.parse._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import fs2.{Stream => FStream}
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import org.apache.logging.log4j.LogManager

class DomainProfileExtractor[F[_], TEntity] extends Extractor[F, TEntity] {
  def fetchAndExtractData(
      numberOfPages: Int,
      domainProfile: DomainProfile[TEntity],
      httpFetcher: F[ReusableHttpClient[F]],
      htmlParser: HtmlParser[F]
  )(implicit F: Effect[F]): EitherT[F, Throwable, List[TEntity]] = {
    if (numberOfPages < 1) {
      EitherT.leftT(new IllegalArgumentException("numberOfPages can't be less than 1."))
    } else {
      def useHttpClient(httpClient: ReusableHttpClient[F]) = {
        val pagesToFetch = (domainProfile.firstIndex until (domainProfile.firstIndex + numberOfPages)).toList

        val resultValidated: F[ValidatedNel[Throwable, List[TEntity]]] = Slf4jLogger.create[F].flatMap { logger =>
          F.pure(pagesToFetch).flatMap { pages =>
            pages
              .map { pageIndex =>
                domainProfile.urlPattern.format(pageIndex)
              }
              .map { urlToFetch =>
                fetchByUrl(urlToFetch, httpClient, domainProfile, logger, htmlParser)
              }
              .sequence
              .map(_.map(_.toValidatedNel).combineAll)
          }
        }

        // Logging all throwables
        val resultAfterLoggingAllErrors = resultValidated.map {
          _.leftMap { nonEmptyErrorList =>
            // TODO: not functional, unsafe :(
            val unsafeLogger = LogManager.getLogger
            nonEmptyErrorList.map { throwable =>
              unsafeLogger.warn("Error while fetching by url.", throwable)
            }
            nonEmptyErrorList
          }
        }

        FStream.eval(resultAfterLoggingAllErrors.map(_.toEither.left.map(_.head)))
      }

      EitherT(FStream.bracket(httpFetcher)(useHttpClient, release = _.shutDown).compile.foldMonoid)
    }
  }

  private def fetchByUrl(
      urlToFetch: String,
      httpClient: ReusableHttpClient[F],
      domainProfile: DomainProfile[TEntity],
      logger: SelfAwareStructuredLogger[F],
      htmlParser: HtmlParser[F]
  )(implicit F: Effect[F]): F[Either[Throwable, List[TEntity]]] = {
    val html = for {
      htmlString <- httpClient.fetchHtmlFromUrl(urlToFetch)
      _ <- EitherT.right(logger.info(s"Fetched html from $urlToFetch"))
      parsedHtml <- htmlParser.parse(htmlString)
    } yield parsedHtml

    html.value.map {
      _.right.map { parsedHtml =>
        parsedHtml.getMatchingElements(domainProfile.mainCssSelector).map { matchingRootElement =>
          domainProfile.entityDetailsExtractors.foldLeft(domainProfile.emptyEntity) {
            case (entity, (extractor, modifier)) =>
              modifier(entity, matchingRootElement.getString(extractor))
          }
        }
      }
    }
  }
}
