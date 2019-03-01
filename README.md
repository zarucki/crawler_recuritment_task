# Simple http fetcher for http

----
## Task to solve

Write a crawler (to be more precise a scraper, with ETL process) for a site bash.org.pl.

The crawler should download n latests pages and parse it to json with following format:

    {
      "id": Long,
      "points": Long,
      "content": String
    }


Example json item from url http://bash.org.pl/4862636/ should look like:

    {
      "id": 4862636,
      "points": -12,
      "content": "Backstory: Zapomniałem odpowiedzi na pytania zabezpieczające potrzebne do zarządzania
      kontem Apple\n<br>\n<br>\n<kazi> jaka może być moja wymarzona praca?\n<br>\n<ziomek> wpisz że zawsze
      chciałeś być operatorem maszynki do mięsa"
    }

The output should be saved to file, which path is specified in typesafe config. N should be input params.

(Optional*) After finishing crawling, display basic statistcs (number of items downloaded, average page download, average one item acquire time)


## Solution

Really overengineered, cause it is after all a recruitment task ;)

Used some cats, fs2.Stream, http4s, io.circe, pureconfig.

## How to run

Simply use something like

    sbt run
    sbt "run -n 2"
    sbt test
