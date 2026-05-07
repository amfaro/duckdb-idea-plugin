package com.amfaro.duckdb

import com.amfaro.duckdb.dbms.DuckDbDbms
import com.intellij.ide.AppLifecycleListener

// Ensure Dbms.create("DUCKDB") runs before the platform resolves <sql.dialect dbms="DUCKDB"/>.
class DuckDbPlugin : AppLifecycleListener {
    override fun appFrameCreated(commandLineArgs: List<String>) {
        DuckDbDbms.INSTANCE
    }
}
