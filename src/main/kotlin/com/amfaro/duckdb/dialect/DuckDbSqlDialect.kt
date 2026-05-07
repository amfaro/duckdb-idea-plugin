package com.amfaro.duckdb.dialect

import com.amfaro.duckdb.dbms.DuckDbDbms
import com.intellij.database.Dbms
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.base.SqlLanguageDialectBase
import com.intellij.sql.dialects.base.TokensHelper
import com.intellij.sql.dialects.sql92.Sql92Dialect

// The sql.dialect EP resolves dialect instances via a static INSTANCE field (not newInstance()).
// The 2-arg ctor registers DuckDB as a dialect OF SQL-92, not a standalone Language. Using the
// 1-arg ctor caused Language("DuckDB") != Language("SQL") assertion in SqlParserDefinitionBase.
class DuckDbSqlDialect private constructor() : SqlLanguageDialectBase(Sql92Dialect.INSTANCE, ID) {

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
