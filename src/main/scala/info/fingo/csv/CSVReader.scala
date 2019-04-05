package info.fingo.csv

import info.fingo.csv.parser.{ParsingFailure, RawRow, RowParser}

import scala.collection.Iterator
import scala.io.Source

class CSVReader(source: Source, separator: Char) {

  private val parser = RowParser.builder(source).fieldDelimiter(separator).build()
  private var lineNum = 1
  private var rowNum = 0

  // caption -> position
  implicit private val headerIndex: Map[String,Int] = parser.next() match {
    case RawRow(captions, _) => captions.zipWithIndex.toMap
    case ParsingFailure(code, message, _) => throw new CSVException(message, code) // TODO: add better info
  }

  val iterator: Iterator[CSVRow] = new Iterator[CSVRow] {
    def hasNext: Boolean = parser.hasNext
    def next: CSVRow = {
      wrapRow()
    }
  }

  private def wrapRow() = parser.next() match {
    case RawRow(fields, counters) =>
      rowNum += 1
      lineNum += counters.newLines
      new CSVRow(fields, lineNum, rowNum)
    case ParsingFailure(code, message, counters) =>
      throw new CSVException(message, code, Some(lineNum + counters.newLines), Some(counters.position), Some(rowNum + 1), None)
  }
}
