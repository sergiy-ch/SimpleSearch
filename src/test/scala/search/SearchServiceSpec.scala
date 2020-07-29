package search

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import search.SearchService._

class SearchServiceSpec extends AnyWordSpec with Matchers {

  "SearchService" should {

    "return 0% match if a word was not found [single word]" in new Scope {
      service.search("zzz") shouldEqual Nil
    }

    "return 0% match if no words were found [multiple words]" in new Scope {
      service.search("zzz zzzz zzzzz") shouldEqual Nil
    }

    "return 33% match (1 of 3 words found)" in new Scope {
      service.search("zzz fox zzzz") shouldEqual List(SearchResult(fileName, 33))
      service.search("fox zzz zzzz") shouldEqual List(SearchResult(fileName, 33))
      service.search("zzz zzzz fox") shouldEqual List(SearchResult(fileName, 33))
    }

    "return 50% match (2 of 4 words found)" in new Scope {
      service.search("zzz fox jumps zzzz") shouldEqual List(SearchResult(fileName, 50))
      service.search("fox jumps zzz zzzz") shouldEqual List(SearchResult(fileName, 50))
      service.search("zzz zzzz fox jumps") shouldEqual List(SearchResult(fileName, 50))
    }

    "return 66% match (2 of 3 words found)" in new Scope {
      service.search("fox jumps zzzz") shouldEqual List(SearchResult(fileName, 66))
      service.search("zzz fox jumps") shouldEqual List(SearchResult(fileName, 66))
    }

    "return 33% match (out-of-order, 1 of 3 words found)" in new Scope {
      // although file contains all 3 words, they are in different order, therefore searched separately
      service.search("fox dog jumps") shouldEqual List(SearchResult(fileName, 33))
    }

    "return 100% match if a word was found [single word]" in new Scope {
      service.search("fox") shouldEqual List(SearchResult(fileName, 100))
    }

    "return 100% match if all words were found [multiple words]" in new Scope {
      service.search("fox jumps over") shouldEqual List(SearchResult(fileName, 100))
    }
  }

  trait Scope {
    val fileName = ""
    val file: File = SearchService.parseFile(fileName, List("The quick brown fox jumps over the lazy dog"))
    val service = new SearchService(SearchService.Index(List(file)))
  }
}
