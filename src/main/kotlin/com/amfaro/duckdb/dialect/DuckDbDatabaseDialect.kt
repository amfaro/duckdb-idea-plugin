package com.amfaro.duckdb.dialect

import com.amfaro.duckdb.dbms.DuckDbDbms
import com.intellij.database.Dbms
import com.intellij.database.dialects.base.AbstractDatabaseDialect
import com.intellij.database.model.DasObject
import com.intellij.database.util.DasUtil
import com.intellij.database.util.DdlBuilder

class DuckDbDatabaseDialect private constructor() : AbstractDatabaseDialect(DuckDbTypeHelper) {

    override fun getDbms(): Dbms = DuckDbDbms.INSTANCE

    override fun getDisplayName(): String = "DuckDB"

    override fun supportsEmptyTables(): Boolean = true

    override fun supportsCommonTableExpression(): Boolean = true

    // Mirrors com.intellij.database.dialects.generic.GenericDialect.qualifiedIdentifier;
    // AbstractDatabaseDialect leaves the method abstract.
    override fun qualifiedIdentifier(
        builder: DdlBuilder,
        name: String,
        parent: DasObject?,
        target: DasObject,
    ): DdlBuilder {
        val schema = DasUtil.getSchemaObject(target)
        return builder.qualifiedRef(
            parent, name,
            schema, DasUtil.getName(schema),
            null, null,
            null, null,
        )
    }

    companion object {
        @JvmField
        val INSTANCE = DuckDbDatabaseDialect()
    }
}
