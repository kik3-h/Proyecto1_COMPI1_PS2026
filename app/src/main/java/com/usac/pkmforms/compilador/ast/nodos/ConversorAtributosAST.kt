package com.usac.pkmforms.compilador.ast.nodos

import com.usac.pkmforms.compilador.interprete.Entorno
import com.usac.pkmforms.domain.modelo.formulario.BordeFormulario
import com.usac.pkmforms.domain.modelo.formulario.EstiloFormulario
import com.usac.pkmforms.domain.modelo.formulario.FamiliaFuenteFormulario
import com.usac.pkmforms.domain.modelo.formulario.TipoBordeFormulario

internal fun resolverValor(entrada: Any?, entorno: Entorno): Any? {
    return when (entrada) {
        is NodoAST -> entrada.ejecutar(entorno)
        is List<*> -> entrada.map { resolverValor(it, entorno) }
        is Map<*, *> -> entrada.entries.associate { (clave, valor) ->
            clave to resolverValor(valor, entorno)
        }

        else -> entrada
    }
}

internal fun evaluarNodoConComodines(
    nodo: NodoAST,
    entorno: Entorno,
    parametros: List<Any?>,
    indiceParametro: IntArray
): Any? {
    return when (nodo) {
        is Literal -> {
            if (nodo.valor.esComodin()) {
                val indice = indiceParametro[0]
                indiceParametro[0] = indice + 1
                parametros.getOrNull(indice)
            } else {
                nodo.valor
            }
        }

        is Aritmetica -> {
            val izquierda = evaluarNodoConComodines(nodo.izquierda, entorno, parametros, indiceParametro)
            val derecha = nodo.derecha?.let { evaluarNodoConComodines(it, entorno, parametros, indiceParametro) }
            Aritmetica(nodo.operador, Literal(izquierda), Literal(derecha)).ejecutar(entorno)
        }

        is Relacional -> {
            val izquierda = evaluarNodoConComodines(nodo.izquierda, entorno, parametros, indiceParametro)
            val derecha = evaluarNodoConComodines(nodo.derecha, entorno, parametros, indiceParametro)
            Relacional(nodo.operador, Literal(izquierda), Literal(derecha)).ejecutar(entorno)
        }

        is Logica -> {
            val izquierda = evaluarNodoConComodines(nodo.izquierda, entorno, parametros, indiceParametro)
            val derecha = nodo.derecha?.let { evaluarNodoConComodines(it, entorno, parametros, indiceParametro) }
            Logica(nodo.operador, Literal(izquierda), derecha?.let { Literal(it) }).ejecutar(entorno)
        }

        else -> nodo.ejecutar(entorno)
    }
}

internal fun resolverValorConComodines(
    entrada: Any?,
    entorno: Entorno,
    parametros: List<Any?>,
    indiceParametro: IntArray
): Any? {
    return when (entrada) {
        is NodoAST -> evaluarNodoConComodines(entrada, entorno, parametros, indiceParametro)
        is List<*> -> entrada.map { resolverValorConComodines(it, entorno, parametros, indiceParametro) }
        is Map<*, *> -> entrada.entries.associate { (clave, valor) ->
            clave to resolverValorConComodines(valor, entorno, parametros, indiceParametro)
        }

        else -> entrada
    }
}

internal fun contarComodines(entrada: Any?): Int {
    return when (entrada) {
        is Literal -> if (entrada.valor.esComodin()) 1 else 0
        is Aritmetica -> contarComodines(entrada.izquierda) + contarComodines(entrada.derecha)
        is Relacional -> contarComodines(entrada.izquierda) + contarComodines(entrada.derecha)
        is Logica -> contarComodines(entrada.izquierda) + contarComodines(entrada.derecha)
        is List<*> -> entrada.sumOf { contarComodines(it) }
        is Map<*, *> -> entrada.values.sumOf { contarComodines(it) }
        else -> 0
    }
}

internal fun resolverEstilo(atributos: Map<String, Any?>, entorno: Entorno): EstiloFormulario? {
    val estilos = atributos["styles"] as? Map<*, *> ?: return null

    val colorTexto = resolverValor(estilos["color"], entorno)?.toString()
    val colorFondo = resolverValor(estilos["background color"], entorno)?.toString()
    val tamanioTexto = resolverValor(estilos["text size"], entorno).aNumero()

    val familiaFuenteRaw = resolverValor(estilos["font family"], entorno)?.toString()
    val familiaFuente = when (familiaFuenteRaw) {
        "MONO" -> FamiliaFuenteFormulario.MONO
        "SANS_SERIF" -> FamiliaFuenteFormulario.SANS_SERIF
        "CURSIVE" -> FamiliaFuenteFormulario.CURSIVE
        else -> null
    }

    val bordeRaw = resolverValor(estilos["border"], entorno) as? List<*>
    val borde = if (bordeRaw != null && bordeRaw.size == 3) {
        val grosor = bordeRaw[0].aNumero() ?: 0.0
        val tipo = when (bordeRaw[1]?.toString()) {
            "LINE" -> TipoBordeFormulario.LINE
            "DOTTED" -> TipoBordeFormulario.DOTTED
            "DOUBLE" -> TipoBordeFormulario.DOUBLE
            else -> TipoBordeFormulario.LINE
        }
        val color = bordeRaw[2]?.toString().orEmpty()
        BordeFormulario(grosor = grosor, tipo = tipo, color = color)
    } else {
        null
    }

    return EstiloFormulario(
        colorTexto = colorTexto,
        colorFondo = colorFondo,
        familiaFuente = familiaFuente,
        tamanioTexto = tamanioTexto,
        borde = borde
    )
}

internal fun resolverNumeroAtributo(
    atributos: Map<String, Any?>,
    clave: String,
    entorno: Entorno,
    obligatorio: Boolean,
    valorDefecto: Double? = null
): Double? {
    val valor = resolverValor(atributos[clave], entorno).aNumero()
    return valor ?: valorDefecto ?: 0.0
}

internal fun resolverTextoAtributo(
    atributos: Map<String, Any?>,
    clave: String,
    entorno: Entorno,
    obligatorio: Boolean
): String? {
    val valor = resolverValor(atributos[clave], entorno)?.toString()
    return valor?.takeIf { it.isNotBlank() } ?: ""
}

internal fun resolverListaTextoAtributo(
    atributos: Map<String, Any?>,
    clave: String,
    entorno: Entorno
): List<String> {
    val valorAtributo = atributos[clave]
    if (clave.equals("options", ignoreCase = true) && valorAtributo is InvocacionPokemon) {
        return listOf("Opción API 1", "Opción API 2")
    }

    val valor = resolverValor(valorAtributo, entorno)
    if (clave.equals("options", ignoreCase = true)) {
        return when (valor) {
            is List<*> -> {
                val opciones = valor.mapNotNull { it?.toString() }.take(5)
                if (opciones.isEmpty()) listOf("Opción API 1", "Opción API 2") else opciones
            }

            else -> listOf("Opción API 1", "Opción API 2")
        }
    }

    return when (valor) {
        is List<*> -> valor.mapNotNull { it?.toString() }
        else -> emptyList()
    }
}

internal fun resolverListaEnterosAtributo(
    atributos: Map<String, Any?>,
    clave: String,
    entorno: Entorno
): List<Int> {
    val valor = resolverValor(atributos[clave], entorno)
    return when (valor) {
        is List<*> -> valor.mapNotNull { item ->
            when (item) {
                is Int -> item
                is Number -> item.toInt()
                is String -> if (item.trim() == "?") 0 else item.toIntOrNull()
                is Literal -> if (item.valor.esComodin()) 0 else item.valor.aNumero()?.toInt()
                else -> item.aNumero()?.toInt()
            }
        }.ifEmpty { listOf(0) }

        is String -> listOf(if (valor.trim() == "?") 0 else valor.toIntOrNull() ?: 0)
        is Literal -> listOf(if (valor.valor.esComodin()) 0 else valor.valor.aNumero()?.toInt() ?: 0)
        else -> listOf(valor.aNumero()?.toInt() ?: 0)
    }
}
