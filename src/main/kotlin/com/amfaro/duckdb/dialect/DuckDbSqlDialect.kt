package com.amfaro.duckdb.dialect

import com.amfaro.duckdb.dbms.DuckDbDbms
import com.intellij.database.Dbms
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.base.SqlLanguageDialectBase
import com.intellij.sql.dialects.base.TokensHelper
import com.intellij.sql.dialects.sql92.Sql92Dialect

// Must extend SqlLanguageDialectBase with a *base* dialect (2-arg ctor), not as a top-level
// Language (1-arg ctor). The 1-arg ctor registers DuckDB as Language("DuckDB") which causes
// SingleRootFileViewProvider to assert "Language: SQL != Language: DuckDB" at file creation.
// Public no-arg ctor lets the sql.dialect EP instantiate via ReflectionUtil.newInstance.
class DuckDbSqlDialect : SqlLanguageDialectBase(Sql92Dialect.INSTANCE, ID) {

    override fun getDbms(): Dbms = DuckDbDbms.INSTANCE
    override fun getDisplayName(): String = ID
    override fun getSystemVariables(): Set<String> = emptySet()
    override fun isOperatorSupported(operator: IElementType): Boolean = true
    override fun createTokensHelper(): TokensHelper = Sql92Dialect.INSTANCE.tokensHelper

    companion object {
        const val ID: String = "DuckDB"
    }
}
