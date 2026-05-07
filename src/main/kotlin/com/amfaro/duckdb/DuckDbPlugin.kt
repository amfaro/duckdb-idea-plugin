package com.amfaro.duckdb

import com.amfaro.duckdb.dbms.DuckDbDbms
import com.intellij.openapi.components.Service

// Ensure Dbms.create("DUCKDB") runs before the platform resolves <sql.dialect dbms="DUCKDB"/>.
@Service(Service.Level.APP)
class DuckDbPlugin {
    init {
        DuckDbDbms.INSTANCE
    }
}
