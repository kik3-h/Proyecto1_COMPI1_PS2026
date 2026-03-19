package com.usac.pkmforms.compilador.interprete

import com.usac.pkmforms.utilidades.manejador_errores.ErrorAnalisis

class Entorno(private val padre: Entorno? = null) {

    enum class TipoVariablePkm(val alias: String) {
        NUMBER("number"),
        STRING("string"),
        SPECIAL("special"),
        BOOLEAN("boolean"),
        NULO("nulo"),
        DESCONOCIDO("desconocido");

        companion object {
            fun desdeAlias(alias: String?): TipoVariablePkm {
                return entries.firstOrNull { it.alias.equals(alias, ignoreCase = true) }
                    ?: DESCONOCIDO
            }
        }
    }

    data class SimboloVariable(
        val id: String,
        val tipo: TipoVariablePkm,
        var valor: Any?,
        val mutabilidad: Boolean
    )

    private val tablaSimbolos: MutableMap<String, SimboloVariable> = mutableMapOf()

    val erroresSemanticos: MutableList<ErrorAnalisis> =
        padre?.erroresSemanticos ?: mutableListOf()

    fun crearEntornoHijo(): Entorno = Entorno(this)

    fun declararVariable(
        id: String,
        tipo: TipoVariablePkm,
        valor: Any? = valorPorDefecto(tipo),
        mutabilidad: Boolean = true
    ): Boolean {
        if (existeEnEntornoActual(id)) {
            reportarErrorSemantico(
                ErrorAnalisis(
                    lexema = id,
                    linea = 0,
                    columna = 0,
                    tipo = "Semántico",
                    descripcion = "La variable '$id' ya fue declarada en este ámbito."
                )
            )
            return false
        }

        val valorAjustado = valor ?: valorPorDefecto(tipo)
        if (!tipoEsCompatible(tipo, valorAjustado)) {
            reportarErrorSemantico(
                ErrorAnalisis(
                    lexema = id,
                    linea = 0,
                    columna = 0,
                    tipo = "Semántico",
                    descripcion = "El valor inicial no es compatible con el tipo '${tipo.alias}'."
                )
            )
            return false
        }

        tablaSimbolos[id] = SimboloVariable(
            id = id,
            tipo = tipo,
            valor = valorAjustado,
            mutabilidad = mutabilidad
        )
        return true
    }

    fun declararVariable(
        id: String,
        tipo: String,
        valor: Any? = null,
        mutabilidad: Boolean = true
    ): Boolean {
        val tipoNormalizado = TipoVariablePkm.desdeAlias(tipo)
        return declararVariable(id, tipoNormalizado, valor, mutabilidad)
    }

    fun obtenerVariable(id: String): SimboloVariable? = buscarVariable(id)

    fun reasignarVariable(id: String, valor: Any?): Boolean {
        val simboloExistente = buscarVariable(id)
        if (simboloExistente == null) {
            reportarErrorSemantico(
                ErrorAnalisis(
                    lexema = id,
                    linea = 0,
                    columna = 0,
                    tipo = "Semántico",
                    descripcion = "La variable '$id' no existe para reasignación."
                )
            )
            return false
        }

        if (!simboloExistente.mutabilidad) {
            reportarErrorSemantico(
                ErrorAnalisis(
                    lexema = id,
                    linea = 0,
                    columna = 0,
                    tipo = "Semántico",
                    descripcion = "La variable '$id' es inmutable."
                )
            )
            return false
        }

        if (!tipoEsCompatible(simboloExistente.tipo, valor)) {
            reportarErrorSemantico(
                ErrorAnalisis(
                    lexema = id,
                    linea = 0,
                    columna = 0,
                    tipo = "Semántico",
                    descripcion = "No se puede asignar un valor incompatible a '${simboloExistente.tipo.alias}'."
                )
            )
            return false
        }

        simboloExistente.valor = valor
        return true
    }

    fun reportarErrorSemantico(error: ErrorAnalisis) {
        erroresSemanticos.add(error)
    }

    fun registrarErrorSemantico(
        lexema: String,
        descripcion: String,
        linea: Int = 0,
        columna: Int = 0
    ) {
        reportarErrorSemantico(
            ErrorAnalisis(
                lexema = lexema,
                linea = linea,
                columna = columna,
                tipo = "Semántico",
                descripcion = descripcion
            )
        )
    }

    fun obtenerTipoVariable(id: String): TipoVariablePkm? = buscarVariable(id)?.tipo

    fun tipoDeValor(valor: Any?): TipoVariablePkm = inferirTipo(valor)

    private fun existeEnEntornoActual(id: String): Boolean = tablaSimbolos.containsKey(id)

    private fun buscarVariable(id: String): SimboloVariable? {
        if (tablaSimbolos.containsKey(id)) {
            return tablaSimbolos[id]
        }
        return padre?.buscarVariable(id)
    }

    private fun tipoEsCompatible(tipoEsperado: TipoVariablePkm, valor: Any?): Boolean {
        if (valor == null) {
            return tipoEsperado == TipoVariablePkm.SPECIAL || tipoEsperado == TipoVariablePkm.NULO
        }

        val tipoValor = inferirTipo(valor)
        return when (tipoEsperado) {
            TipoVariablePkm.NUMBER -> tipoValor == TipoVariablePkm.NUMBER
            TipoVariablePkm.STRING -> tipoValor == TipoVariablePkm.STRING
            TipoVariablePkm.SPECIAL -> tipoValor == TipoVariablePkm.SPECIAL || tipoValor == TipoVariablePkm.DESCONOCIDO
            TipoVariablePkm.BOOLEAN -> tipoValor == TipoVariablePkm.BOOLEAN
            TipoVariablePkm.NULO -> valor == null
            TipoVariablePkm.DESCONOCIDO -> true
        }
    }

    private fun inferirTipo(valor: Any?): TipoVariablePkm {
        return when (valor) {
            null -> TipoVariablePkm.NULO
            is Byte, is Short, is Int, is Long, is Float, is Double -> TipoVariablePkm.NUMBER
            is String -> TipoVariablePkm.STRING
            is Boolean -> TipoVariablePkm.BOOLEAN
            else -> {
                val nombreSimple = valor::class.simpleName.orEmpty()
                if (nombreSimple.contains("Pregunta") || nombreSimple.contains("Seccion") || nombreSimple.contains("Tabla")) {
                    TipoVariablePkm.SPECIAL
                } else {
                    TipoVariablePkm.DESCONOCIDO
                }
            }
        }
    }

    private fun valorPorDefecto(tipo: TipoVariablePkm): Any? {
        return when (tipo) {
            TipoVariablePkm.NUMBER -> 0.0
            TipoVariablePkm.STRING -> ""
            TipoVariablePkm.SPECIAL -> null
            TipoVariablePkm.BOOLEAN -> false
            TipoVariablePkm.NULO,
            TipoVariablePkm.DESCONOCIDO -> null
        }
    }
}
