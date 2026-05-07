package com.amfaro.duckdb.dialect

import com.intellij.sql.dialects.base.SqlSyntaxHighlighterFactory

class DuckDbSyntaxHighlighterFactory : SqlSyntaxHighlighterFactory.Base(DuckDbSqlDialect.INSTANCE)
