package com.amfaro.duckdb.dbms

import com.intellij.database.Dbms
import com.intellij.util.ui.EmptyIcon

/** Registers a custom Dbms constant so the platform can group DuckDB dialects, data sources, and consoles by it. */
object DuckDbDbms {

    @JvmField
    val INSTANCE: Dbms = Dbms.create("DUCKDB", "DuckDB") { EmptyIcon.ICON_16 }
}
