package com.amfaro.duckdb

import com.amfaro.duckdb.dbms.DuckDbDbms
import com.amfaro.duckdb.dialect.DuckDbDatabaseDialect
import com.amfaro.duckdb.dialect.DuckDbLexer
import com.amfaro.duckdb.dialect.DuckDbParserDefinition
import com.amfaro.duckdb.dialect.DuckDbReservedKeywords
import com.amfaro.duckdb.dialect.DuckDbSqlDialect
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.SqlTokens
import com.intellij.sql.util.SqlTokenRegistry
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DuckDbDialectTest {

    @Test
    fun `dialect ID is DuckDB`() {
        assertEquals("DuckDB", DuckDbSqlDialect.ID)
    }

    @Test
    fun `database dialect reports DuckDB dbms and capabilities`() {
        val dialect = DuckDbDatabaseDialect.INSTANCE

        assertSame(DuckDbDbms.INSTANCE, dialect.dbms)
        assertEquals("DuckDB", dialect.displayName)
        assertTrue(dialect.supportsCommonTableExpression())
        assertTrue(dialect.supportsEmptyTables())
    }

    @Test
    fun `parser definition uses DuckDB lexer`() {
        val lexer = DuckDbParserDefinition().createLexer(null)

        assertTrue(lexer is DuckDbLexer)
    }

    @Test
    fun `generated keyword metadata exposes DuckDB reserved words`() {
        assertEquals(DuckDbReservedKeywords.SUMMARIZE, keyword("SUMMARIZE"))
        assertEquals(SqlTokens.SQL_IDENT, keyword("not_a_keyword"))
    }

    @Test
    fun `lexer tokenizes DuckDB specific comments operators and identifiers`() {
        val tokens = lex("# comment\nSUMMARIZE::INT, payload->>'field', `tbl`")

        assertTrue(tokens.contains(SqlTokens.SQL_LINE_COMMENT))
        assertTrue(tokens.contains(keyword("SUMMARIZE")))
        assertTrue(tokens.contains(SqlTokenRegistry.getType("::")))
        assertTrue(tokens.contains(SqlTokenRegistry.getType("->>")))
        assertTrue(tokens.contains(SqlTokens.SQL_IDENT_DELIMITED))
    }

    @Test
    fun `generator uses duckdb_functions when available`() {
        val root = Files.createTempDirectory("duckdb-metadata-primary")
        val duckdb = fakeDuckDb(
            """
            case "${'$'}sql" in
              *duckdb_keywords* )
                printf 'keyword_name,keyword_category\nSUMMARIZE,reserved\nATTACH,unreserved\n'
                ;;
              *duckdb_functions* )
                printf 'function_name,function_type,arity\nread_csv,scalar,1\nsum,aggregate,1\n'
                ;;
              * )
                echo "unexpected sql: ${'$'}sql" >&2
                exit 1
                ;;
            esac
            """.trimIndent(),
            root,
        )

        runGenerator(root, duckdb)

        val functions = root.resolve("src/main/resources/com/amfaro/duckdb/dialect/duckdb-functions.txt").readText()
        val functionsXml = root.resolve("src/main/resources/com/amfaro/duckdb/dialect/functions.xml").readText()

        assertTrue(functions.contains("read_csv\n"))
        assertTrue(functionsXml.contains("<name>sum</name>"))
        assertTrue(functionsXml.contains("aggregate='true'"))
    }

    @Test
    fun `generator falls back to pragma functions when duckdb_functions is unavailable`() {
        val root = Files.createTempDirectory("duckdb-metadata-fallback")
        val duckdb = fakeDuckDb(
            """
            case "${'$'}sql" in
              *duckdb_keywords* )
                printf 'keyword_name,keyword_category\nSUMMARIZE,reserved\nATTACH,unreserved\n'
                ;;
              *duckdb_functions* )
                echo 'Catalog Error: Table Function with name duckdb_functions does not exist!' >&2
                exit 1
                ;;
              'PRAGMA functions' )
                printf 'name,type,parameters\nlegacy_fn,SCALAR,"[INTEGER, VARCHAR]"\nlegacy_sum,AGGREGATE,"[INTEGER]"\n'
                ;;
              * )
                echo "unexpected sql: ${'$'}sql" >&2
                exit 1
                ;;
            esac
            """.trimIndent(),
            root,
        )

        runGenerator(root, duckdb)

        val functions = root.resolve("src/main/resources/com/amfaro/duckdb/dialect/duckdb-functions.txt").readText()
        val functionsXml = root.resolve("src/main/resources/com/amfaro/duckdb/dialect/functions.xml").readText()

        assertTrue(functions.contains("legacy_fn\n"))
        assertTrue(functionsXml.contains("<name>legacy_sum</name>"))
        assertTrue(functionsXml.contains("(arg1:ANY, arg2:ANY):ANY"))
        assertTrue(functionsXml.contains("aggregate='true'"))
    }

    private fun lex(sql: String): List<IElementType> {
        val lexer = DuckDbParserDefinition().createLexer(null)
        val tokens = mutableListOf<IElementType>()

        lexer.start(sql)
        while (lexer.tokenType != null) {
            val tokenType = lexer.tokenType!!
            if (tokenType != TokenType.WHITE_SPACE) {
                tokens += tokenType
            }
            lexer.advance()
        }

        return tokens
    }

    private fun keyword(text: String): IElementType =
        com.amfaro.duckdb.dialect.DuckDbLexerUtil.lookupKeyword(text)

    private fun fakeDuckDb(caseBody: String, root: Path): Path {
        val script = root.resolve("duckdb")
        script.writeText(
            """
            |#!/bin/sh
            |sql=""
            |while [ "$#" -gt 0 ]; do
            |  if [ "$1" = "-c" ]; then
            |    sql="$2"
            |    shift 2
            |  else
            |    shift
            |  fi
            |done
            |
            |$caseBody
            """.trimMargin(),
        )
        script.toFile().setExecutable(true)
        return script
    }

    private fun runGenerator(root: Path, duckdb: Path) {
        root.resolve("src/main/java/com/amfaro/duckdb/dialect").createDirectories()
        root.resolve("src/main/resources/com/amfaro/duckdb/dialect").createDirectories()

        val process = ProcessBuilder("python3", "tools/generate_duckdb_metadata.py")
            .directory(Path.of(".").toFile())
            .redirectErrorStream(true)
            .apply {
                environment()["DUCKDB_METADATA_ROOT"] = root.toString()
                environment()["DUCKDB_BIN"] = duckdb.toString()
            }
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        assertEquals(output, 0, exitCode)
    }
}
