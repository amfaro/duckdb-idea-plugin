package com.amfaro.duckdb.dialect

import com.amfaro.duckdb.dbms.DuckDbDbms
import com.intellij.database.Dbms
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.base.SqlLanguageDialectBase
import com.intellij.sql.dialects.base.TokensHelper
import com.intellij.sql.dialects.sql92.Sql92Dialect

// The sql.dialect EP resolves dialect instances via a static INSTANCE field (not newInstance()).
// Keep DuckDB layered on SQL-92 for now: Tier 2 replaces token metadata and adds a DuckDB lexer
// source, but Tier 3 still owns parser-definition work for a standalone DuckDB language.
class DuckDbSqlDialect private constructor() : SqlLanguageDialectBase(Sql92Dialect.INSTANCE, ID) {

    override fun getDbms(): Dbms = DuckDbDbms.INSTANCE
    override fun getDisplayName(): String = ID
    override fun getSystemVariables(): Set<String> = emptySet()
    override fun isOperatorSupported(operator: IElementType): Boolean = true
    override fun createTokensHelper(): TokensHelper = createTokensHelper(DuckDbTokens::class.java)

    companion object {
        const val ID: String = "DuckDB"

        @JvmField
        val INSTANCE: DuckDbSqlDialect = DuckDbSqlDialect()
    }
}
