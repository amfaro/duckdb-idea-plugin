package com.amfaro.duckdb

import com.amfaro.duckdb.dbms.DuckDbDbms
import com.intellij.openapi.components.Service

// preload=true ensures Dbms.DUCKDB is registered before the sql.dialect EP resolves dbms="DUCKDB".
// AppLifecycleListener fires too late (after EP resolution). See issue #1 for context.
@Service(Service.Level.APP)
class DuckDbPlugin {
    init {
        DuckDbDbms.INSTANCE
    }
}
