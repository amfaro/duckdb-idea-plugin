package com.amfaro.duckdb.dialect

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.sql.dialects.sql92.Sql92ParserDefinition
import com.intellij.sql.psi.stubs.SqlFileElementType

class DuckDbParserDefinition : ParserDefinition {
    private val delegate = Sql92ParserDefinition()

    override fun createLexer(project: Project?): Lexer = DuckDbLexer()

    override fun createParser(project: Project?): PsiParser = delegate.createParser(project)

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = delegate.commentTokens

    override fun getStringLiteralElements(): TokenSet = delegate.stringLiteralElements

    override fun createElement(node: ASTNode): PsiElement = delegate.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = delegate.createFile(viewProvider)

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode): ParserDefinition.SpaceRequirements =
        delegate.spaceExistenceTypeBetweenTokens(left, right)

    companion object {
        private val FILE = SqlFileElementType("DUCKDB_SQL_FILE", DuckDbSqlDialect.INSTANCE)
    }
}
