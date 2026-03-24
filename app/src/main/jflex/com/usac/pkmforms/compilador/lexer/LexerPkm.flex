package com.usac.pkmforms.compilador.lexer;

import com.usac.pkmforms.compilador.parser.SimbolosSintacticosPkm;
import java_cup.runtime.Symbol;
import com.usac.pkmforms.utilidades.manejador_errores.ErrorAnalisis;

%%

%public
%class LexerPkm
%unicode
%line
%column
%cupsym SimbolosSintacticosPkm
%cup

%{
    public static final java.util.List<ErrorAnalisis> listaErroresLexicos = new java.util.ArrayList<>();

    public static void limpiarErroresLexicos() {
        listaErroresLexicos.clear();
    }

    private Symbol simbolo(int tipo) {
        return new Symbol(tipo, yyline + 1, yycolumn + 1, yytext());
    }

    private Symbol simbolo(int tipo, Object valor) {
        return new Symbol(tipo, yyline + 1, yycolumn + 1, valor);
    }
%}

Espacios = [ \t\r\n\f]+
ComentarioLinea = "$"[^\r\n]*
ComentarioBloque = "/*"([^*]|\*+[^*/])*"*/"

Entero = [0-9]+
Decimal = [0-9]+"."[0-9]+
Cadena = \"([^\"\\\r\n]|\\.)*\"
Identificador = [a-zA-Z_][a-zA-Z0-9_]*
SeparadorAtributo = [ \t]+

EmojiFeliz = "@[:)]"
EmojiTriste = "@[:(]"
EmojiSerio = "@[:]]"
EmojiCorazon = "@[<3]"
EmojiEstrella = "@[:star:]"
EmojiEstrellasN = "@[:star:"{Entero}":]"
EmojiGatoAscii = "@[:^^:]"
EmojiGatoTexto = "@[:cat:]"

ColorHex = "#"[0-9a-fA-F]{6}
NumeroColor = [0-9]{1,3}
ColorRgb = "("{NumeroColor}","{NumeroColor}","{NumeroColor}")"
ColorHsl = "<"{NumeroColor}","{NumeroColor}","{NumeroColor}">"

%%

{ComentarioLinea}                             { /* Ignorar comentario de una línea */ }
{ComentarioBloque}                            { /* Ignorar comentario de varias líneas */ }
{Espacios}                                    { /* Ignorar espacios, tabulaciones y saltos */ }

"number"                                      { return simbolo(SimbolosSintacticosPkm.TIPO_NUMBER); }
"string"                                      { return simbolo(SimbolosSintacticosPkm.TIPO_STRING); }
"special"                                     { return simbolo(SimbolosSintacticosPkm.TIPO_SPECIAL); }
"SECTION"                                     { return simbolo(SimbolosSintacticosPkm.SECTION); }
"TABLE"                                       { return simbolo(SimbolosSintacticosPkm.TABLE); }
"TEXT"                                        { return simbolo(SimbolosSintacticosPkm.TEXT); }
"OPEN_QUESTION"                               { return simbolo(SimbolosSintacticosPkm.OPEN_QUESTION); }
"DROP_QUESTION"                               { return simbolo(SimbolosSintacticosPkm.DROP_QUESTION); }
"SELECT_QUESTION"                             { return simbolo(SimbolosSintacticosPkm.SELECT_QUESTION); }
"MULTIPLE_QUESTION"                           { return simbolo(SimbolosSintacticosPkm.MULTIPLE_QUESTION); }
"IF"                                          { return simbolo(SimbolosSintacticosPkm.IF); }
"ELSE"                                        { return simbolo(SimbolosSintacticosPkm.ELSE); }
"WHILE"                                       { return simbolo(SimbolosSintacticosPkm.WHILE); }
"DO"                                          { return simbolo(SimbolosSintacticosPkm.DO); }
"FOR"                                         { return simbolo(SimbolosSintacticosPkm.FOR); }
"in"                                          { return simbolo(SimbolosSintacticosPkm.IN); }
"draw"                                        { return simbolo(SimbolosSintacticosPkm.DRAW); }
"who_is_that_pokemon"                         { return simbolo(SimbolosSintacticosPkm.WHO_IS_THAT_POKEMON); }

"width"                                       { return simbolo(SimbolosSintacticosPkm.WIDTH); }
"height"                                      { return simbolo(SimbolosSintacticosPkm.HEIGHT); }
"pointX"                                      { return simbolo(SimbolosSintacticosPkm.POINT_X); }
"pointY"                                      { return simbolo(SimbolosSintacticosPkm.POINT_Y); }
"orientation"                                 { return simbolo(SimbolosSintacticosPkm.ORIENTATION); }
"VERTICAL"                                    { return simbolo(SimbolosSintacticosPkm.VERTICAL); }
"HORIZONTAL"                                  { return simbolo(SimbolosSintacticosPkm.HORIZONTAL); }
"elements"                                    { return simbolo(SimbolosSintacticosPkm.ELEMENTS); }
"styles"                                      { return simbolo(SimbolosSintacticosPkm.STYLES); }
"content"                                     { return simbolo(SimbolosSintacticosPkm.CONTENT); }
"label"                                       { return simbolo(SimbolosSintacticosPkm.LABEL); }
"options"                                     { return simbolo(SimbolosSintacticosPkm.OPTIONS); }
"OPTIONS"                                     { return simbolo(SimbolosSintacticosPkm.OPTIONS); }
"correct"                                     { return simbolo(SimbolosSintacticosPkm.CORRECT); }
"CORRECT"                                     { return simbolo(SimbolosSintacticosPkm.CORRECT); }
"MONO"                                        { return simbolo(SimbolosSintacticosPkm.MONO); }
"SANS_SERIF"                                  { return simbolo(SimbolosSintacticosPkm.SANS_SERIF); }
"CURSIVE"                                     { return simbolo(SimbolosSintacticosPkm.CURSIVE); }
"LINE"                                        { return simbolo(SimbolosSintacticosPkm.LINE); }
"DOTTED"                                      { return simbolo(SimbolosSintacticosPkm.DOTTED); }
"DOUBLE"                                      { return simbolo(SimbolosSintacticosPkm.DOUBLE); }

"+"                                           { return simbolo(SimbolosSintacticosPkm.MAS); }
"-"                                           { return simbolo(SimbolosSintacticosPkm.MENOS); }
"*"                                           { return simbolo(SimbolosSintacticosPkm.POR); }
"/"                                           { return simbolo(SimbolosSintacticosPkm.DIV); }
"^"                                           { return simbolo(SimbolosSintacticosPkm.POTENCIA); }
"%"                                           { return simbolo(SimbolosSintacticosPkm.MODULO); }
">="                                          { return simbolo(SimbolosSintacticosPkm.MAYOR_IGUAL); }
"<="                                          { return simbolo(SimbolosSintacticosPkm.MENOR_IGUAL); }
"=="                                          { return simbolo(SimbolosSintacticosPkm.IGUAL_IGUAL); }
"!!"                                          { return simbolo(SimbolosSintacticosPkm.DIFERENTE); }
"||"                                          { return simbolo(SimbolosSintacticosPkm.OR); }
"&&"                                          { return simbolo(SimbolosSintacticosPkm.AND); }
">"                                           { return simbolo(SimbolosSintacticosPkm.MAYOR); }
"<"                                           { return simbolo(SimbolosSintacticosPkm.MENOR); }
"!"                                           { return simbolo(SimbolosSintacticosPkm.NOT); }

"("                                           { return simbolo(SimbolosSintacticosPkm.PAREN_ABRE); }
")"                                           { return simbolo(SimbolosSintacticosPkm.PAREN_CIERRA); }
"{"                                           { return simbolo(SimbolosSintacticosPkm.LLAVE_ABRE); }
"}"                                           { return simbolo(SimbolosSintacticosPkm.LLAVE_CIERRA); }
"["                                           { return simbolo(SimbolosSintacticosPkm.CORCHETE_ABRE); }
"]"                                           { return simbolo(SimbolosSintacticosPkm.CORCHETE_CIERRA); }
","                                           { return simbolo(SimbolosSintacticosPkm.COMA); }
";"                                           { return simbolo(SimbolosSintacticosPkm.PUNTO_COMA); }
".."                                          { return simbolo(SimbolosSintacticosPkm.RANGO); }
"."                                           { return simbolo(SimbolosSintacticosPkm.PUNTO); }
"="                                           { return simbolo(SimbolosSintacticosPkm.IGUAL); }
"?"                                           { return simbolo(SimbolosSintacticosPkm.COMODIN); }
":"                                           { return simbolo(SimbolosSintacticosPkm.DOS_PUNTOS); }

{Decimal}                                     { return simbolo(SimbolosSintacticosPkm.DECIMAL, Double.valueOf(yytext())); }
{Entero}                                      { return simbolo(SimbolosSintacticosPkm.ENTERO, Integer.valueOf(yytext())); }
"\"color\""                                   { return simbolo(SimbolosSintacticosPkm.COLOR); }
"\"background color\""                        { return simbolo(SimbolosSintacticosPkm.BACKGROUND_COLOR); }
"\"font family\""                             { return simbolo(SimbolosSintacticosPkm.FONT_FAMILY); }
"\"text size\""                               { return simbolo(SimbolosSintacticosPkm.TEXT_SIZE); }
"\"border\""                                  { return simbolo(SimbolosSintacticosPkm.BORDER); }
{Cadena}                                      { return simbolo(SimbolosSintacticosPkm.CADENA, yytext()); }

{EmojiFeliz}                                  { return simbolo(SimbolosSintacticosPkm.EMOJI, yytext()); }
{EmojiTriste}                                 { return simbolo(SimbolosSintacticosPkm.EMOJI, yytext()); }
{EmojiSerio}                                  { return simbolo(SimbolosSintacticosPkm.EMOJI, yytext()); }
{EmojiCorazon}                                { return simbolo(SimbolosSintacticosPkm.EMOJI, yytext()); }
{EmojiEstrella}                               { return simbolo(SimbolosSintacticosPkm.EMOJI, yytext()); }
{EmojiEstrellasN}                             { return simbolo(SimbolosSintacticosPkm.EMOJI, yytext()); }
{EmojiGatoAscii}                              { return simbolo(SimbolosSintacticosPkm.EMOJI, yytext()); }
{EmojiGatoTexto}                              { return simbolo(SimbolosSintacticosPkm.EMOJI, yytext()); }

{ColorHex}                                    { return simbolo(SimbolosSintacticosPkm.COLOR_HEX, yytext()); }
{ColorRgb}                                    { return simbolo(SimbolosSintacticosPkm.COLOR_RGB, yytext()); }
{ColorHsl}                                    { return simbolo(SimbolosSintacticosPkm.COLOR_HSL, yytext()); }
"RED"                                         { return simbolo(SimbolosSintacticosPkm.COLOR_BASE, yytext()); }
"BLUE"                                        { return simbolo(SimbolosSintacticosPkm.COLOR_BASE, yytext()); }
"GREEN"                                       { return simbolo(SimbolosSintacticosPkm.COLOR_BASE, yytext()); }
"PURPLE"                                      { return simbolo(SimbolosSintacticosPkm.COLOR_BASE, yytext()); }
"SKY"                                         { return simbolo(SimbolosSintacticosPkm.COLOR_BASE, yytext()); }
"YELLOW"                                      { return simbolo(SimbolosSintacticosPkm.COLOR_BASE, yytext()); }
"BLACK"                                       { return simbolo(SimbolosSintacticosPkm.COLOR_BASE, yytext()); }
"WHITE"                                       { return simbolo(SimbolosSintacticosPkm.COLOR_BASE, yytext()); }

{Identificador}                               { return simbolo(SimbolosSintacticosPkm.IDENTIFICADOR, yytext()); }

[^] {
    listaErroresLexicos.add(
        new ErrorAnalisis(
            yytext(),
            yyline + 1,
            yycolumn + 1,
            "Léxico",
            "Símbolo no existe"
        )
    );
}
