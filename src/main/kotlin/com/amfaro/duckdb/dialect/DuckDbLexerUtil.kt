package com.amfaro.duckdb.dialect

import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.sql92.Sql92Tokens
import com.intellij.sql.psi.SqlTokens
import com.intellij.sql.util.SqlTokenRegistry
import java.util.Locale

object DuckDbLexerUtil {
    private val keywordTokens: Map<String, IElementType> = buildMap {
        register(Sql92Tokens::class.java, overwrite = true)
        register(DuckDbReservedKeywords::class.java, overwrite = false)
        register(DuckDbOptionalKeywords::class.java, overwrite = false)
    }

    @JvmStatic
    fun lookupKeyword(text: CharSequence): IElementType =
        keywordTokens[text.toString().uppercase(Locale.ROOT)] ?: SqlTokens.SQL_IDENT

    @JvmStatic
    fun symbol(text: String): IElementType = SqlTokenRegistry.getType(text)

    private fun MutableMap<String, IElementType>.register(type: Class<*>, overwrite: Boolean) {
        for (field in type.fields) {
            val token = field.get(null) as IElementType
            if (overwrite) {
                put(field.name, token)
            } else {
                putIfAbsent(field.name, token)
            }
        }
    }
}
