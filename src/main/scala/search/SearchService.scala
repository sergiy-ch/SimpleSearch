package search

import search.SearchService._

import scala.annotation.tailrec


class SearchService(index: Index) {

  def search(searchString: String): List[SearchResult] = {
    val wordsToSearch = parseString(searchString)
    val wordsToSearchCount = wordsToSearch.size

    @tailrec
    def search(offsets: List[List[Offset]], acc: List[Int]): List[Int] = {
      offsets match {
        case Nil                    => acc
        case currentOffsets :: tail =>
          val matchedWordsMax = currentOffsets.map { offset =>
            tail.zipWithIndex.takeWhile { case (nextOffsets, idx) => nextOffsets.contains(offset + 1 + idx) }.size + 1
          }.maxOption.getOrElse(0)

          val next = if (matchedWordsMax == offsets.size) Nil else tail // optimization
          search(next, matchedWordsMax :: acc)
      }
    }

    @tailrec
    def run(files: List[File], exactMatchesCount: Int, results: List[SearchResult]): List[SearchResult] = {
      files match {
        case Nil          => results
        case file :: tail =>
          val offsets = wordsToSearch.map(file.words.getOrElse(_, List.empty))
          val matchedWordsMax = search(offsets, List.empty).maxOption.getOrElse(0)
          val rank = matchedWordsMax * 100 / wordsToSearchCount

          val newExactMatchesCount = if (rank == 100) exactMatchesCount + 1 else exactMatchesCount
          // optimization (stop search once we have found SearchResultsMax 100% matches)
          val next = if (newExactMatchesCount == SearchResultsMax) Nil else tail
          run(next, newExactMatchesCount, SearchResult(file.fileName, rank) :: results)
      }
    }

    if (wordsToSearchCount > 0)
      run(index.files, 0, List.empty)
        .filter(_.rank > 0)
        .sortWith { case (a, b) => a.rank > b.rank }
        .take(SearchResultsMax)
    else
      Nil
  }
}

object SearchService {
  type Word = String
  type Offset = Long

  final case class WordOffset(word: Word, offset: Offset)
  final case class File(fileName: String, words: Map[Word, List[Offset]])
  final case class Index(files: List[File])

  final case class SearchResult(fileName: String, rank: Int)

  val SearchResultsMax = 10

  def parseFile(fileName: String, content: List[String]): File = {

    @tailrec
    def parseLines(lines: List[String], lineOffset: Offset, acc: List[WordOffset]): List[WordOffset] = {
      lines match {
        case Nil          => acc
        case line :: tail =>
          val words = parseString(line)
          val wordsWithOffsets = words.zipWithIndex.map {
            case (word, wordIdx) => WordOffset(word, lineOffset + wordIdx)
          }
          parseLines(tail, lineOffset + words.size, wordsWithOffsets ++ acc)
      }
    }

    File(fileName, parseLines(content, 0L, List.empty).groupMap(_.word)(_.offset))
  }

  private def parseString(s: String): List[Word] = s.trim.split("\\s+").toList
}
