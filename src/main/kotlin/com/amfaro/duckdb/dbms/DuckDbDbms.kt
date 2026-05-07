package com.amfaro.duckdb.dbms

import com.intellij.database.Dbms
import com.intellij.openapi.util.IconLoader

object DuckDbDbms {

    @JvmField
    val INSTANCE: Dbms = Dbms.create("DUCKDB", "DuckDB") {
        IconLoader.getIcon("/icons/duckdb.svg", DuckDbDbms::class.java)
    }
}
