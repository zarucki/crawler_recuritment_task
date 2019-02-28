package extract
import cats.effect.Effect
import extract.fetch.ReusableHttpClient
import extract.parse.HtmlParser

trait Extractor[F[_]] {
  // TODO: better return type
  def fetchAndExtractData(numberOfPages: Int,
                          domainProfile: DomainProfile,
                          httpFetcher: F[ReusableHttpClient[F]],
                          htmlParser: HtmlParser)(implicit F: Effect[F]): F[List[Seq[String]]]
}
