package com.amfaro.duckdb.dialect

import com.amfaro.duckdb.dbms.DuckDbDbms
import com.intellij.database.Dbms
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.base.SqlLanguageDialectBase
import com.intellij.sql.dialects.base.TokensHelper
import com.intellij.sql.dialects.sql92.Sql92Dialect

/**
 * Tier-1 stub dialect: surfaces "DuckDB" in the SQL Dialects picker and groups under Dbms.DUCKDB.
 *
 * Lexing/parsing delegates to SQL-92 — this is the same approach Tarantool's plugin took at the
 * equivalent stage. Tier 2 (real lexer + DuckDB keyword/function tables) and Tier 3 (.bnf grammar)
 * are tracked as separate issues.
 */
class DuckDbSqlDialect private constructor() : SqlLanguageDialectBase(ID) {

    override fun getDbms(): Dbms = DuckDbDbms.INSTANCE
    override fun getDisplayName(): String = ID
    override fun getSystemVariables(): Set<String> = emptySet()
    override fun isOperatorSupported(operator: IElementType): Boolean = true
    override fun createTokensHelper(): TokensHelper = Sql92Dialect.INSTANCE.tokensHelper

    companion object {
        const val ID: String = "DuckDB"

        @JvmField
        val INSTANCE: DuckDbSqlDialect = DuckDbSqlDialect()
    }
}
