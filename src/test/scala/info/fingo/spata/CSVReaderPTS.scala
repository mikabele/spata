/*
 * Copyright 2020 FINGO sp. z o.o.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package info.fingo.spata

import scala.io.Source
import org.scalatest.funsuite.AnyFunSuite

class CSVReaderPTS extends AnyFunSuite {
  val amount = 5_000
  test("Reader should handle large data streams") {
    val separator = ','
    val source = new TestSource(separator)
    val reader = CSVReader.config.fieldDelimiter(separator).get
    var count = 0
    reader.process(source) { _ =>
      count += 1
      true
    }
    assert(count == amount)
  }

  class TestSource(separator: Char) extends Source {
    def csvStream(sep: Char, lines: Int): LazyList[Char] = {
      val cols = 10
      val header = ((1 to cols).mkString("" + sep) + "\n").to(LazyList)
      val rows = LazyList.fill(lines)(s"lorem ipsum$sep" * (cols - 1) + "lorem ipsum\n").flatMap(_.toCharArray)
      header #::: rows
    }
    override val iter: Iterator[Char] = csvStream(separator, amount).iterator
  }
}
