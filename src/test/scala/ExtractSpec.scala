import cats.effect.IO
import extract.DomainProfileExtractor

import scala.collection.immutable.Queue

// TODO: more tests
class ExtractSpec extends UnitSpec {
  behavior of "DomainProfileExtractor"

  it should "properly visit paged results" in {
    val httpClient = mockHttpClient[IO]()
    val parser = mockParser()

    val extractor =
      new DomainProfileExtractor[IO]()
        .fetchAndExtractData(
          numberOfPages = 3,
          domainProfile = BashOrgProfile,
          httpFetcher = IO(httpClient),
          htmlParser = parser
        )
        .unsafeRunSync()

    assert(
      httpClient.visitedUrlHistory == Queue("http://bash.org.pl/latest/?page=1",
                                            "http://bash.org.pl/latest/?page=2",
                                            "http://bash.org.pl/latest/?page=3"))
  }

  it should "should not visit any urls when IO not run" in {
    val httpClient = mockHttpClient[IO]()
    val parser = mockParser()

    val extractor =
      new DomainProfileExtractor[IO]()
        .fetchAndExtractData(
          numberOfPages = 3,
          domainProfile = BashOrgProfile,
          httpFetcher = IO(httpClient),
          htmlParser = parser
        )

    assert(httpClient.visitedUrlHistory == Queue.empty)
  }

  it should "should return error when invalid number of pages" in {
    val httpClient = mockHttpClient[IO]()
    val parser = mockParser()

    val extractor =
      new DomainProfileExtractor[IO]()
        .fetchAndExtractData(
          numberOfPages = 0,
          domainProfile = BashOrgProfile,
          httpFetcher = IO(httpClient),
          htmlParser = parser
        )

    assert(httpClient.visitedUrlHistory == Queue.empty)
  }
}
