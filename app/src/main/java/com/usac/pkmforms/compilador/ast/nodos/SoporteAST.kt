package com.usac.pkmforms.compilador.ast.nodos

import com.usac.pkmforms.compilador.interprete.Entorno

internal fun Entorno.TipoVariablePkm.esNumero(): Boolean =
    this == Entorno.TipoVariablePkm.NUMBER

internal fun Entorno.TipoVariablePkm.esCadena(): Boolean =
    this == Entorno.TipoVariablePkm.STRING

internal fun Any?.aNumero(): Double? {
    return when (this) {
        null -> null
        is Double -> this
        is Float -> this.toDouble()
        is Int -> this.toDouble()
        is Long -> this.toDouble()
        is Short -> this.toDouble()
        is Byte -> this.toDouble()
        is String -> this.toDoubleOrNull()
        else -> null
    }
}

internal fun Any?.aBooleanoPkm(): Boolean {
    return when (this) {
        is Boolean -> this
        is Number -> this.toDouble() >= 1.0
        is String -> this.isNotBlank()
        else -> false
    }
}

internal fun Any?.aCadena(): String = this?.toString().orEmpty()

internal fun Any?.esComodin(): Boolean = this is String && this == "?"
