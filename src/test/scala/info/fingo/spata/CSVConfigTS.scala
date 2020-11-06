/*
 * Copyright 2020 FINGO sp. z o.o.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package info.fingo.spata

import org.scalatest.funsuite.AnyFunSuite
import scala.io.Source
import cats.effect.IO
import info.fingo.spata.io.reader

class CSVConfigTS extends AnyFunSuite {

  test("Config should be build correctly") {
    val config = CSVConfig().fieldDelimiter(';').noHeader().fieldSizeLimit(100)
    val expected = CSVConfig(';', '\n', '"', hasHeader = false, NoHeaderMap, Some(100))
    assert(config == expected)
  }

  test("Config should allow parser creation with proper settings") {
    val rs = 0x1E.toChar
    val content = s"'value 1A'|'value ''1B'$rs'value 2A'|'value ''2B'"
    val config = CSVConfig().fieldDelimiter('|').quoteMark('\'').recordDelimiter(rs).noHeader()
    val data = reader[IO]().read(Source.fromString(content))
    val parser = config.get[IO]()
    val result = parser.get(data).unsafeRunSync()
    assert(result.length == 2)
    assert(result.head.size == 2)
    assert(result.head(1).contains("value '1B"))
  }

  test("Config should clearly present its composition through toString") {
    val c1 = CSVConfig(',', '\n', '"')
    assert(c1.toString == """CSVConfig(',', '\n', '"', header, no mapping)""")
    val c2 = CSVConfig('\t', '\r', '\'', hasHeader = false, Map("x" -> "y"), Some(100))
    assert(c2.toString == """CSVConfig('\t', '\r', ''', no header, header mapping, 100)""")
    val c3 = CSVConfig(';', ' ', '\"')
    assert(c3.toString == """CSVConfig(';', ' ', '"', header, no mapping)""")
    val c4 = CSVConfig('\u001F', '\u001E', '|', fieldSizeLimit = Some(256))
    assert(c4.toString == """CSVConfig('␣', '␣', '|', header, no mapping, 256)""")
  }
}
