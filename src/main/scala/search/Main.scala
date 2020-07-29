package search

import java.io.File

import scala.io.Source
import scala.io.StdIn.readLine
import scala.util.Try

object Main extends App {
  Program.readFile(args)
    .fold(
      println,
      file => Program.iterate(new SearchService(Program.index(file)))
    )
}

object Program {
  sealed trait ReadFileError
  case object MissingPathArg extends ReadFileError
  final case class NotDirectory(error: String) extends ReadFileError
  final case class FileNotFound(t: Throwable) extends ReadFileError

  def readFile(args: Array[String]): Either[ReadFileError, File] = {
    for {
      path <- args.headOption.toRight(MissingPathArg)
      file <- Try(new java.io.File(path))
        .fold(
          throwable => Left(FileNotFound(throwable)),
          file => if (file.isDirectory) Right(file) else Left(NotDirectory(s"Path [$path] is not a directory"))
        )
    } yield file
  }

  def index(dir: File): SearchService.Index = {
    def readLines(file: File) = {
      val src = Source.fromFile(file)
      val lines = Try(src.getLines.toList).toOption.getOrElse {
        println(s"Unable to parse file: ${ file.getName }")
        List.empty[String]
      }
      src.close
      lines
    }

    val files = dir.listFiles.toList.filter(_.isFile)
    val parsedFiles = files.map(file => SearchService.parseFile(file.getName, readLines(file)))

    println(s"${ files.size } file(s) read in directory: ${ dir.getName }")

    SearchService.Index(parsedFiles)
  }

  def iterate(searchService: SearchService): Unit = {
    readLine("search> ") match {
      case ":quit" =>
        println("done.")

      case searchString if searchString.trim.isEmpty =>
        println("search string cannot be empty")
        iterate(searchService)

      case searchString =>
        val result = searchService.search(searchString)

        if (result.nonEmpty)
          println(result.map { case SearchService.SearchResult(fileName, rank) => s"  $fileName : $rank%" }.mkString("\n"))
        else
          println("no matches found")

        iterate(searchService)
    }
  }
}