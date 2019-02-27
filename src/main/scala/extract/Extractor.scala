package extract
import cats.effect.Effect
import extract.fetch.HttpFetcher
import extract.parse.HtmlParser

trait Extractor[F[_]] {
  // TODO: better return type
  def extractData(numberOfPages: Int,
                  domainProfile: DomainProfile,
                  httpFetcher: HttpFetcher[F],
                  htmlParser: HtmlParser)(implicit F: Effect[F]): F[List[Seq[String]]]
}