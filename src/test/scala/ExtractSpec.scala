import cats.data.EitherT
import cats.effect.IO
import extract.DomainProfile.HtmlValueExtractor
import extract.DomainProfileExtractor
import extract.fetch.Http4sClient
import extract.parse.{Attribute, InnerHtml, Text}
import extract.parse.jsoup.JSoupParser
import extract.profiles.{BashOrgContent, BashOrgProfile}
import io.circe.Json
import org.jsoup.select.Selector.SelectorParseException
import transform.CirceJsonSerializer

import scala.collection.immutable.Queue

class ExtractSpec extends UnitSpec {

  behavior of "DomainProfileExtractor"

  private val extractor = new DomainProfileExtractor[IO, BashOrgContent]()

  it should "properly visits paged results" in {
    val httpClient = mockHttpClient[IO]()
    val parser = mockParser[IO]()

    extractor
      .fetchAndExtractData(
        numberOfPages = 3,
        domainProfile = BashOrgProfile,
        httpFetcher = IO(httpClient),
        htmlParser = parser
      )
      .value
      .unsafeRunSync()

    assert(
      httpClient.visitedUrlHistory == Queue(
        "http://bash.org.pl/latest/?page=1",
        "http://bash.org.pl/latest/?page=2",
        "http://bash.org.pl/latest/?page=3"
      )
    )
  }

  it should "should not visit any urls when IO not run" in {
    val httpClient = mockHttpClient[IO]()
    val parser = mockParser[IO]()

    extractor
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
    val parser = mockParser[IO]()

    val result = extractor
      .fetchAndExtractData(
        numberOfPages = 0,
        domainProfile = BashOrgProfile,
        httpFetcher = IO(httpClient),
        htmlParser = parser
      )
      .value
      .unsafeRunSync()

    assert(result.isLeft)
    assert(result.left.get.getMessage == "numberOfPages can't be less than 1.")
  }

  it should "bash org profile should still work and return valid items" in {
    val extractor = new DomainProfileExtractor[IO, BashOrgContent]()
    val httpClient = Http4sClient.apply[IO]()
    val htmlParser = new JSoupParser[IO]()

    val pipeline = for {
      bashContentItems <- extractor.fetchAndExtractData(2, BashOrgProfile, httpClient, htmlParser)
    } yield bashContentItems

    val results = pipeline.value.unsafeRunSync().right.get

    assert(results.size == 40)
    assert(results.forall(_.id > 0))
    assert(results.forall(_.points != Long.MinValue))
    assert(results.forall(_.content.size > 0))
  }

  behavior of "Http4sClient"

  it should "should return connection error when invalid url" in {
    val program = for {
      client <- EitherT.right(Http4sClient[IO]())
      _      <- client.fetchHtmlFromUrl("invalid_url")
      _      <- EitherT.right[Throwable](client.shutDown)
    } yield ()

    val result = program.value.unsafeRunSync().left.get

    assert(result.getCause.isInstanceOf[java.net.ConnectException])
    assert(result.getMessage == "Error while running req: Request(method=GET, uri=invalid_url, headers=Headers()).")
  }

  it should "should return unresolved address error when url can't be resolved" in {
    val program = for {
      client <- EitherT.right(Http4sClient[IO]())
      _      <- client.fetchHtmlFromUrl("http://nonexistingsiteforsure.pl")
      _      <- EitherT.right[Throwable](client.shutDown)
    } yield ()

    val result = program.value.unsafeRunSync()

    assert(result.isLeft)
    assert(result.left.get.getCause.isInstanceOf[java.nio.channels.UnresolvedAddressException])
  }

  it should "should return 404 error when site exists but path is incorrect" in {
    val program = for {
      client <- EitherT.right(Http4sClient[IO]())
      _      <- client.fetchHtmlFromUrl("http://example.com/nonexistingpathforsure")
      _      <- EitherT.right[Throwable](client.shutDown)
    } yield ()

    val result = program.value.unsafeRunSync()

    assert(result.isLeft)
    assert(
      result.left.get.getMessage == "Error while running req: Request(method=GET, uri=http://example.com/nonexistingpathforsure, headers=Headers())."
    )
  }

  it should "should return not return when url is correct" in {
    val program = for {
      client <- EitherT.right(Http4sClient[IO]())
      result <- client.fetchHtmlFromUrl("http://example.com")
      _      <- EitherT.right[Throwable](client.shutDown)
    } yield result

    val result = program.value.unsafeRunSync()

    assert(result.isRight)
    assert(result.right.get.startsWith("<!doctype html>"))
  }

  it should "when shutdown run twice it does not crash" in {
    val program = for {
      client <- EitherT.right(Http4sClient[IO]())
      _      <- EitherT.right[Throwable](client.shutDown)
      _      <- EitherT.right[Throwable](client.shutDown)
    } yield ()

    val result = program.value.unsafeRunSync()

    assert(result.isRight)
  }

  behavior of "CirceJsonSerializer"

  case class SimpleCaseClass(number: Int, text: String)

  it should "properly serialize simple case class" in {
    import io.circe.generic.auto._

    val jsonSerializer = new CirceJsonSerializer[IO, SimpleCaseClass]()

    val program = for {
      jsonString <- jsonSerializer.asJson(SimpleCaseClass(11, "test"))
    } yield jsonString

    val result = program.value.unsafeRunSync()

    assert(result.isRight)

    result.map { rightResult =>
      assert(rightResult \\ "text" == List(Json.fromString("test")))
      assert(rightResult \\ "number" == List(Json.fromInt(11)))
    }
  }

  it should "properly serialize list of simple case classes" in {
    import io.circe.generic.auto._

    val jsonSerializer = new CirceJsonSerializer[IO, SimpleCaseClass]()

    val program = for {
      jsonString <- jsonSerializer.arrayAsJson(List(SimpleCaseClass(11, "test"), SimpleCaseClass(12, "test2")))
    } yield jsonString

    val result = program.value.unsafeRunSync()

    assert(result.isRight)

    result.map { rightResult =>
      assert(rightResult \\ "text" == List("test", "test2").map(Json.fromString))
      assert(rightResult \\ "number" == List(11, 12).map(Json.fromInt))
    }
  }

  behavior of "JSoupParser"

  val sampleHtml = "<html><body><div class=\"post\">content</div></body></html>"

  it should "correctly parse simple html" in {
    val program = for {
      parsedHtml <- new JSoupParser[IO].parse(sampleHtml)
    } yield parsedHtml

    val result = program.value.unsafeRunSync().right.get

    assert(result.getString(HtmlValueExtractor(None, Text)) == Right("content"))
    assert(result.getString(HtmlValueExtractor(Some("div"), Attribute("class"))) == Right("post"))
    assert(
      result.getString(HtmlValueExtractor(None, InnerHtml)).map(removeUneccessaryWhiteSpace) == Right(
        "<div class=\"post\"> content </div>"
      )
    )
  }

  it should "return selector parse error if selector is invalid" in {
    val program = for {
      parsedHtml <- new JSoupParser[IO].parse(sampleHtml)
    } yield parsedHtml

    val result = program.value.unsafeRunSync().right.get.getMatchingElements("#@#$.asdfasdf")

    assert(result.isLeft)
    assert(result.left.get.getCause.isInstanceOf[SelectorParseException])
  }

  it should "return no such element error if trying to extract value from not existing" in {
    val program = for {
      parsedHtml <- new JSoupParser[IO].parse(sampleHtml)
    } yield parsedHtml

    val result =
      program.value.unsafeRunSync().right.get.getString(HtmlValueExtractor(Some(".picture"), Attribute("src")))

    assert(result.isLeft)
    assert(result.left.get.getCause.isInstanceOf[NoSuchElementException])
  }

  it should "return empty string if extracted attribute is missing" in {
    val program = for {
      parsedHtml <- new JSoupParser[IO].parse(sampleHtml)
    } yield parsedHtml

    val result =
      program.value.unsafeRunSync().right.get.getString(HtmlValueExtractor(None, Attribute("notexisting")))

    assert(result.isRight)
    assert(result.right.get == "")
  }

  def removeUneccessaryWhiteSpace(string: String): String = {
    string.replaceAll("\\s+", " ")
  }
}
