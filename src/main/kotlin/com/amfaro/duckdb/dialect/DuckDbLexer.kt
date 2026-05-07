package com.amfaro.duckdb.dialect

import com.amfaro.duckdb.dialect.lexer._DuckDbLexer
import com.intellij.lexer.FlexAdapter

class DuckDbLexer : FlexAdapter(_DuckDbLexer(null))
