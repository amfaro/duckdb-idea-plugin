package com.amfaro.duckdb.dialect.lexer;

import com.amfaro.duckdb.dialect.DuckDbLexerUtil;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.sql.psi.SqlTokens;

%%

%public
%class _DuckDbLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

WHITE_SPACE=\s+
DIGIT=[0-9]
IDENT_START=[A-Za-z_]
IDENT_PART=[A-Za-z0-9_$]
IDENT={IDENT_START}{IDENT_PART}*
INTEGER={DIGIT}+
EXPONENT=[eE][+-]?{DIGIT}+
FLOAT=(({DIGIT}+"."{DIGIT}*)|({DIGIT}*"."{DIGIT}+))({EXPONENT})?|({DIGIT}+{EXPONENT})
SINGLE_QUOTED=\'([^\'\r\n]|\'\')*\'
DOUBLE_QUOTED=\"([^\"\r\n]|\"\")*\"
BACKTICK_QUOTED=\`([^`\r\n]|``)*\`
LINE_COMMENT=("--"|"#")[^\r\n]*
BLOCK_COMMENT=\/\*([^*]|\*+[^*/])*\*+\/

%%

{WHITE_SPACE}      { return TokenType.WHITE_SPACE; }
{LINE_COMMENT}     { return SqlTokens.SQL_LINE_COMMENT; }
{BLOCK_COMMENT}    { return SqlTokens.SQL_BLOCK_COMMENT; }
{SINGLE_QUOTED}    { return SqlTokens.SQL_STRING_TOKEN; }
{DOUBLE_QUOTED}    { return SqlTokens.SQL_IDENT_DELIMITED; }
{BACKTICK_QUOTED}  { return SqlTokens.SQL_IDENT_DELIMITED; }
{FLOAT}            { return SqlTokens.SQL_FLOAT_TOKEN; }
{INTEGER}          { return SqlTokens.SQL_INTEGER_TOKEN; }
"::"              { return DuckDbLexerUtil.symbol("::"); }
"->>"             { return DuckDbLexerUtil.symbol("->>"); }
"->"              { return DuckDbLexerUtil.symbol("->"); }
"=>"              { return DuckDbLexerUtil.symbol("=>"); }
">="              { return DuckDbLexerUtil.symbol(">="); }
"<="              { return DuckDbLexerUtil.symbol("<="); }
"<>"              { return DuckDbLexerUtil.symbol("<>"); }
"!="              { return DuckDbLexerUtil.symbol("!="); }
"||"              { return DuckDbLexerUtil.symbol("||"); }
"("               { return DuckDbLexerUtil.symbol("("); }
")"               { return DuckDbLexerUtil.symbol(")"); }
","               { return DuckDbLexerUtil.symbol(","); }
";"               { return DuckDbLexerUtil.symbol(";"); }
"."               { return DuckDbLexerUtil.symbol("."); }
":"               { return DuckDbLexerUtil.symbol(":"); }
"+"               { return DuckDbLexerUtil.symbol("+"); }
"-"               { return DuckDbLexerUtil.symbol("-"); }
"/"               { return DuckDbLexerUtil.symbol("/"); }
"*"               { return DuckDbLexerUtil.symbol("*"); }
"%"               { return DuckDbLexerUtil.symbol("%"); }
"="               { return DuckDbLexerUtil.symbol("="); }
">"               { return DuckDbLexerUtil.symbol(">"); }
"<"               { return DuckDbLexerUtil.symbol("<"); }
"["               { return DuckDbLexerUtil.symbol("["); }
"]"               { return DuckDbLexerUtil.symbol("]"); }
"{"               { return DuckDbLexerUtil.symbol("{"); }
"}"               { return DuckDbLexerUtil.symbol("}"); }
{IDENT}            { return DuckDbLexerUtil.lookupKeyword(yytext()); }

[^]                { return TokenType.BAD_CHARACTER; }
