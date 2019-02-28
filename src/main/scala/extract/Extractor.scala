package extract
import cats.data.EitherT
import cats.effect.Effect
import extract.fetch.ReusableHttpClient
import extract.parse.HtmlParser

trait Extractor[F[_], TEntity] {
  def fetchAndExtractData(
      numberOfPages: Int,
      domainProfile: DomainProfile[TEntity],
      httpFetcher: F[ReusableHttpClient[F]],
      htmlParser: HtmlParser[F]
  )(implicit F: Effect[F]): EitherT[F, Throwable, List[TEntity]]
}
