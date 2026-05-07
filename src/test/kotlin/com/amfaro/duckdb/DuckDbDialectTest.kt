package com.amfaro.duckdb

import com.amfaro.duckdb.dialect.DuckDbSqlDialect
import org.junit.Assert.assertEquals
import org.junit.Test

class DuckDbDialectTest {

    @Test
    fun `dialect ID is DuckDB`() {
        assertEquals("DuckDB", DuckDbSqlDialect.ID)
    }
}
