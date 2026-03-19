package com.usac.pkmforms.compilador.ast.nodos

import com.usac.pkmforms.compilador.interprete.Entorno

private fun tipoDeclarado(tipo: String): Entorno.TipoVariablePkm =
    Entorno.TipoVariablePkm.desdeAlias(tipo)

private fun tipoCompatible(
    tipoEsperado: Entorno.TipoVariablePkm,
    tipoRecibido: Entorno.TipoVariablePkm
): Boolean {
    return when (tipoEsperado) {
        Entorno.TipoVariablePkm.NUMBER -> tipoRecibido == Entorno.TipoVariablePkm.NUMBER
        Entorno.TipoVariablePkm.STRING -> tipoRecibido == Entorno.TipoVariablePkm.STRING
        Entorno.TipoVariablePkm.SPECIAL -> tipoRecibido == Entorno.TipoVariablePkm.SPECIAL || tipoRecibido == Entorno.TipoVariablePkm.DESCONOCIDO
        Entorno.TipoVariablePkm.BOOLEAN -> tipoRecibido == Entorno.TipoVariablePkm.BOOLEAN
        Entorno.TipoVariablePkm.NULO -> tipoRecibido == Entorno.TipoVariablePkm.NULO
        Entorno.TipoVariablePkm.DESCONOCIDO -> true
    }
}

private fun valorPorDefecto(tipo: Entorno.TipoVariablePkm): Any? {
    return when (tipo) {
        Entorno.TipoVariablePkm.NUMBER -> 0.0
        Entorno.TipoVariablePkm.STRING -> ""
        Entorno.TipoVariablePkm.SPECIAL -> null
        Entorno.TipoVariablePkm.BOOLEAN -> false
        Entorno.TipoVariablePkm.NULO,
        Entorno.TipoVariablePkm.DESCONOCIDO -> null
    }
}

data class DeclaracionVar(
    val tipo: String,
    val nombre: String,
    val valorInicial: NodoAST? = null
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        val tipoVariable = tipoDeclarado(tipo)
        if (tipoVariable == Entorno.TipoVariablePkm.DESCONOCIDO) {
            entorno.registrarErrorSemantico(
                lexema = tipo,
                descripcion = "Tipo de variable no reconocido."
            )
            return Entorno.TipoVariablePkm.DESCONOCIDO
        }

        if (tipoVariable == Entorno.TipoVariablePkm.SPECIAL && valorInicial == null) {
            entorno.registrarErrorSemantico(
                lexema = nombre,
                descripcion = "Las variables special deben inicializarse obligatoriamente."
            )
        }

        val declarada = entorno.declararVariable(
            id = nombre,
            tipo = tipoVariable,
            valor = valorPorDefecto(tipoVariable),
            mutabilidad = true
        )
        if (!declarada) {
            return tipoVariable
        }

        val tipoInicial = valorInicial?.validarSemantica(entorno)
        if (tipoInicial != null && !tipoCompatible(tipoVariable, tipoInicial)) {
            entorno.registrarErrorSemantico(
                lexema = nombre,
                descripcion = "No se puede asignar un valor de tipo ${tipoInicial.alias} a '$tipo'."
            )
        }

        if (tipoVariable == Entorno.TipoVariablePkm.SPECIAL && valorInicial != null && valorInicial !is NodoPreguntaEspecial) {
            entorno.registrarErrorSemantico(
                lexema = nombre,
                descripcion = "Una variable special debe almacenar una pregunta especial."
            )
        }

        if (tipoVariable == Entorno.TipoVariablePkm.SPECIAL && valorInicial is NodoPreguntaEspecial) {
            val plantilla = PlantillaPreguntaEspecial(
                nodoPregunta = valorInicial,
                totalComodines = valorInicial.contarComodinesDeclarados()
            )
            entorno.reasignarVariable(nombre, plantilla)
        }

        return tipoVariable
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val tipoVariable = tipoDeclarado(tipo)
        val valorEvaluado = when {
            tipoVariable == Entorno.TipoVariablePkm.SPECIAL && valorInicial is NodoPreguntaEspecial -> {
                PlantillaPreguntaEspecial(
                    nodoPregunta = valorInicial,
                    totalComodines = valorInicial.contarComodinesDeclarados()
                )
            }

            valorInicial != null -> valorInicial.ejecutar(entorno)
            else -> valorPorDefecto(tipoVariable)
        }

        val declarada = entorno.declararVariable(
            id = nombre,
            tipo = tipoVariable,
            valor = valorEvaluado,
            mutabilidad = true
        )
        if (!declarada) {
            return null
        }
        return valorEvaluado
    }
}

data class AsignacionVar(
    val nombre: String,
    val valor: NodoAST
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        val variable = entorno.obtenerVariable(nombre)
        val tipoValor = valor.validarSemantica(entorno)

        if (variable == null) {
            entorno.registrarErrorSemantico(
                lexema = nombre,
                descripcion = "No se puede asignar a '$nombre' porque no existe."
            )
            return Entorno.TipoVariablePkm.DESCONOCIDO
        }

        if (!tipoCompatible(variable.tipo, tipoValor)) {
            entorno.registrarErrorSemantico(
                lexema = nombre,
                descripcion = "Asignación incompatible: ${tipoValor.alias} no puede asignarse a ${variable.tipo.alias}."
            )
        }

        return variable.tipo
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val valorEvaluado = valor.ejecutar(entorno)
        val exito = entorno.reasignarVariable(nombre, valorEvaluado)
        return if (exito) valorEvaluado else null
    }
}
