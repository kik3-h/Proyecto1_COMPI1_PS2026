package com.usac.pkmforms.compilador.ast.nodos

import com.usac.pkmforms.compilador.interprete.Entorno
import com.usac.pkmforms.domain.modelo.formulario.ComponenteFormulario
import com.usac.pkmforms.domain.modelo.preguntas.InvocacionPokemonFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaAbiertaFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaDesplegableFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaSeleccionMultipleFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaSeleccionUnicaFormulario

interface NodoPreguntaEspecial : NodoAST {
    fun contarComodinesDeclarados(): Int
    fun ejecutarConComodines(entorno: Entorno, parametros: List<Any?>): ComponenteFormulario?
}

data class PlantillaPreguntaEspecial(
    val nodoPregunta: NodoPreguntaEspecial,
    val totalComodines: Int
)

private fun resolverConComodines(
    atributos: Map<String, Any?>,
    clave: String,
    entorno: Entorno,
    parametros: List<Any?>,
    indiceParametro: IntArray
): Any? {
    return resolverValorConComodines(atributos[clave], entorno, parametros, indiceParametro)
}

private fun validarAtributosBasePregunta(atributos: Map<String, Any?>, entorno: Entorno, requiereLabel: Boolean) {
    resolverNumeroAtributo(atributos, "width", entorno, obligatorio = false)
    resolverNumeroAtributo(atributos, "height", entorno, obligatorio = false)

    if (requiereLabel) {
        val tipoLabel = (atributos["label"] as? NodoAST)?.validarSemantica(entorno)
        if (tipoLabel != null && tipoLabel != Entorno.TipoVariablePkm.STRING) {
            entorno.registrarErrorSemantico(
                lexema = "label",
                descripcion = "El atributo 'label' debe ser string."
            )
        }
    }
}

data class PreguntaAbierta(val atributos: Map<String, Any?>) : NodoPreguntaEspecial {
    override fun contarComodinesDeclarados(): Int = contarComodines(atributos)

    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        validarAtributosBasePregunta(atributos, entorno, requiereLabel = true)
        resolverEstilo(atributos, entorno)
        return Entorno.TipoVariablePkm.SPECIAL
    }

    override fun ejecutar(entorno: Entorno): Any? = ejecutarConComodines(entorno, emptyList())

    override fun ejecutarConComodines(entorno: Entorno, parametros: List<Any?>): ComponenteFormulario {
        val indiceParametro = intArrayOf(0)
        val width = if (parametros.isEmpty()) {
            resolverNumeroAtributo(atributos, "width", entorno, obligatorio = false)
        } else {
            resolverConComodines(atributos, "width", entorno, parametros, indiceParametro).aNumero()
        }
        val height = if (parametros.isEmpty()) {
            resolverNumeroAtributo(atributos, "height", entorno, obligatorio = false)
        } else {
            resolverConComodines(atributos, "height", entorno, parametros, indiceParametro).aNumero()
        }
        val label = if (parametros.isEmpty()) {
            resolverTextoAtributo(atributos, "label", entorno, obligatorio = true).orEmpty()
        } else {
            resolverConComodines(atributos, "label", entorno, parametros, indiceParametro).toString()
        }

        return PreguntaAbiertaFormulario(
            width = width,
            height = height,
            label = label,
            estilos = resolverEstilo(atributos, entorno)
        )
    }
}

data class PreguntaDesplegable(val atributos: Map<String, Any?>) : NodoPreguntaEspecial {
    override fun contarComodinesDeclarados(): Int = contarComodines(atributos)

    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        validarAtributosBasePregunta(atributos, entorno, requiereLabel = true)
        val opciones = resolverListaTextoAtributo(atributos, "options", entorno)
        if (opciones.isEmpty()) {
            entorno.registrarErrorSemantico(
                lexema = "options",
                descripcion = "DROP_QUESTION requiere al menos una opción."
            )
        }

        val correcto = resolverValor(atributos["correct"], entorno).aNumero()?.toInt()
        if (correcto != null && opciones.isNotEmpty() && (correcto < 0 || correcto >= opciones.size)) {
            entorno.registrarErrorSemantico(
                lexema = "correct",
                descripcion = "El índice 'correct' está fuera del rango de opciones."
            )
        }
        resolverEstilo(atributos, entorno)
        return Entorno.TipoVariablePkm.SPECIAL
    }

    override fun ejecutar(entorno: Entorno): Any? = ejecutarConComodines(entorno, emptyList())

    override fun ejecutarConComodines(entorno: Entorno, parametros: List<Any?>): ComponenteFormulario {
        val indiceParametro = intArrayOf(0)
        val width = if (parametros.isEmpty()) resolverNumeroAtributo(atributos, "width", entorno, false) else resolverConComodines(atributos, "width", entorno, parametros, indiceParametro).aNumero()
        val height = if (parametros.isEmpty()) resolverNumeroAtributo(atributos, "height", entorno, false) else resolverConComodines(atributos, "height", entorno, parametros, indiceParametro).aNumero()
        val label = if (parametros.isEmpty()) resolverTextoAtributo(atributos, "label", entorno, true).orEmpty() else resolverConComodines(atributos, "label", entorno, parametros, indiceParametro).toString()
        val options = if (parametros.isEmpty()) {
            resolverListaTextoAtributo(atributos, "options", entorno)
        } else {
            (resolverConComodines(atributos, "options", entorno, parametros, indiceParametro) as? List<*>)?.mapNotNull { it?.toString() }
                ?: emptyList()
        }
        val correct = if (parametros.isEmpty()) {
            resolverValor(atributos["correct"], entorno).aNumero()?.toInt()
        } else {
            resolverConComodines(atributos, "correct", entorno, parametros, indiceParametro).aNumero()?.toInt()
        }

        return PreguntaDesplegableFormulario(
            width = width,
            height = height,
            label = label,
            options = options,
            correct = correct,
            estilos = resolverEstilo(atributos, entorno)
        )
    }
}

data class PreguntaSeleccionUnica(val atributos: Map<String, Any?>) : NodoPreguntaEspecial {
    override fun contarComodinesDeclarados(): Int = contarComodines(atributos)

    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        validarAtributosBasePregunta(atributos, entorno, requiereLabel = true)
        val opciones = resolverListaTextoAtributo(atributos, "options", entorno)
        if (opciones.isEmpty()) {
            entorno.registrarErrorSemantico(
                lexema = "options",
                descripcion = "SELECT_QUESTION requiere al menos una opción."
            )
        }
        if (opciones.size > 5) {
            entorno.registrarErrorSemantico(
                lexema = "options",
                descripcion = "Advertencia: se ingresaron más de 5 opciones en SELECT_QUESTION."
            )
        }

        val correcto = resolverValor(atributos["correct"], entorno).aNumero()?.toInt()
        if (correcto != null && opciones.isNotEmpty() && (correcto < 0 || correcto >= opciones.size)) {
            entorno.registrarErrorSemantico(
                lexema = "correct",
                descripcion = "El índice 'correct' está fuera del rango de opciones."
            )
        }
        resolverEstilo(atributos, entorno)
        return Entorno.TipoVariablePkm.SPECIAL
    }

    override fun ejecutar(entorno: Entorno): Any? = ejecutarConComodines(entorno, emptyList())

    override fun ejecutarConComodines(entorno: Entorno, parametros: List<Any?>): ComponenteFormulario {
        val indiceParametro = intArrayOf(0)
        val width = if (parametros.isEmpty()) resolverNumeroAtributo(atributos, "width", entorno, false) else resolverConComodines(atributos, "width", entorno, parametros, indiceParametro).aNumero()
        val height = if (parametros.isEmpty()) resolverNumeroAtributo(atributos, "height", entorno, false) else resolverConComodines(atributos, "height", entorno, parametros, indiceParametro).aNumero()
        val label = if (parametros.isEmpty()) resolverTextoAtributo(atributos, "label", entorno, true).orEmpty() else resolverConComodines(atributos, "label", entorno, parametros, indiceParametro).toString()
        val options = if (parametros.isEmpty()) {
            resolverListaTextoAtributo(atributos, "options", entorno)
        } else {
            (resolverConComodines(atributos, "options", entorno, parametros, indiceParametro) as? List<*>)?.mapNotNull { it?.toString() }
                ?: emptyList()
        }
        val correct = if (parametros.isEmpty()) {
            resolverValor(atributos["correct"], entorno).aNumero()?.toInt()
        } else {
            resolverConComodines(atributos, "correct", entorno, parametros, indiceParametro).aNumero()?.toInt()
        }

        return PreguntaSeleccionUnicaFormulario(
            width = width,
            height = height,
            label = label,
            options = options,
            correct = correct,
            estilos = resolverEstilo(atributos, entorno)
        )
    }
}

data class PreguntaSeleccionMultiple(val atributos: Map<String, Any?>) : NodoPreguntaEspecial {
    override fun contarComodinesDeclarados(): Int = contarComodines(atributos)

    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        validarAtributosBasePregunta(atributos, entorno, requiereLabel = true)
        val opciones = resolverListaTextoAtributo(atributos, "options", entorno)
        if (opciones.isEmpty()) {
            entorno.registrarErrorSemantico(
                lexema = "options",
                descripcion = "MULTIPLE_QUESTION requiere al menos una opción."
            )
        }

        val indicesCorrectos = resolverListaEnterosAtributo(atributos, "correct", entorno)
        val fueraRango = indicesCorrectos.any { it < 0 || it >= opciones.size }
        if (fueraRango) {
            entorno.registrarErrorSemantico(
                lexema = "correct",
                descripcion = "Uno o más índices de respuestas correctas están fuera de rango."
            )
        }
        resolverEstilo(atributos, entorno)
        return Entorno.TipoVariablePkm.SPECIAL
    }

    override fun ejecutar(entorno: Entorno): Any? = ejecutarConComodines(entorno, emptyList())

    override fun ejecutarConComodines(entorno: Entorno, parametros: List<Any?>): ComponenteFormulario {
        val indiceParametro = intArrayOf(0)
        val width = if (parametros.isEmpty()) resolverNumeroAtributo(atributos, "width", entorno, false) else resolverConComodines(atributos, "width", entorno, parametros, indiceParametro).aNumero()
        val height = if (parametros.isEmpty()) resolverNumeroAtributo(atributos, "height", entorno, false) else resolverConComodines(atributos, "height", entorno, parametros, indiceParametro).aNumero()
        val label = if (parametros.isEmpty()) resolverTextoAtributo(atributos, "label", entorno, true).orEmpty() else resolverConComodines(atributos, "label", entorno, parametros, indiceParametro).toString()
        val options = if (parametros.isEmpty()) {
            resolverListaTextoAtributo(atributos, "options", entorno)
        } else {
            (resolverConComodines(atributos, "options", entorno, parametros, indiceParametro) as? List<*>)?.mapNotNull { it?.toString() }
                ?: emptyList()
        }
        val correct = if (parametros.isEmpty()) {
            resolverListaEnterosAtributo(atributos, "correct", entorno)
        } else {
            (resolverConComodines(atributos, "correct", entorno, parametros, indiceParametro) as? List<*>)?.mapNotNull {
                when (it) {
                    is Int -> it
                    is Number -> it.toInt()
                    is String -> it.toIntOrNull()
                    else -> null
                }
            } ?: emptyList()
        }

        return PreguntaSeleccionMultipleFormulario(
            width = width,
            height = height,
            label = label,
            options = options,
            correct = correct,
            estilos = resolverEstilo(atributos, entorno)
        )
    }
}

data class InvocacionPokemon(
    val tipoDatoConsulta: NodoAST,
    val rangoInicio: NodoAST,
    val rangoFin: NodoAST
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        val tipoConsulta = tipoDatoConsulta.validarSemantica(entorno)
        rangoInicio.validarSemantica(entorno)
        rangoFin.validarSemantica(entorno)

        if (tipoConsulta != Entorno.TipoVariablePkm.STRING &&
            tipoConsulta != Entorno.TipoVariablePkm.NUMBER &&
            tipoConsulta != Entorno.TipoVariablePkm.DESCONOCIDO
        ) {
            entorno.registrarErrorSemantico(
                lexema = "who_is_that_pokemon",
                descripcion = "El primer parámetro de who_is_that_pokemon debe ser string o number."
            )
        }

        return Entorno.TipoVariablePkm.SPECIAL
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val tipo = tipoDatoConsulta.ejecutar(entorno).toString()
        val inicio = (rangoInicio.ejecutar(entorno).aNumero() ?: 1.0).toInt()
        val fin = (rangoFin.ejecutar(entorno).aNumero() ?: inicio.toDouble()).toInt()

        return InvocacionPokemonFormulario(
            tipoDatoConsulta = tipo,
            rangoInicio = inicio,
            rangoFin = fin
        )
    }
}

data class InvocacionDrawPreguntaEspecial(
    val identificadorVariable: String,
    val parametros: List<NodoAST>
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        parametros.forEach { it.validarSemantica(entorno) }

        val variable = entorno.obtenerVariable(identificadorVariable)
        if (variable == null) {
            entorno.registrarErrorSemantico(
                lexema = identificadorVariable,
                descripcion = "La variable especial '$identificadorVariable' no existe."
            )
            return Entorno.TipoVariablePkm.DESCONOCIDO
        }

        if (variable.tipo != Entorno.TipoVariablePkm.SPECIAL) {
            entorno.registrarErrorSemantico(
                lexema = identificadorVariable,
                descripcion = "La invocación draw() solo aplica a variables de tipo special."
            )
            return Entorno.TipoVariablePkm.DESCONOCIDO
        }

        val plantilla = variable.valor as? PlantillaPreguntaEspecial
        if (plantilla == null) {
            entorno.registrarErrorSemantico(
                lexema = identificadorVariable,
                descripcion = "La variable special no contiene una plantilla de pregunta válida."
            )
            return Entorno.TipoVariablePkm.DESCONOCIDO
        }

        if (parametros.size != plantilla.totalComodines) {
            entorno.registrarErrorSemantico(
                lexema = "draw",
                descripcion = "Cantidad de parámetros inválida para draw(). Se esperaban ${plantilla.totalComodines}."
            )
        }

        return Entorno.TipoVariablePkm.SPECIAL
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val variable = entorno.obtenerVariable(identificadorVariable) ?: return null
        val plantilla = variable.valor as? PlantillaPreguntaEspecial ?: return null
        val valoresParametros = parametros.map { it.ejecutar(entorno) }

        if (valoresParametros.size != plantilla.totalComodines) {
            entorno.registrarErrorSemantico(
                lexema = "draw",
                descripcion = "La cantidad de parámetros en draw() no coincide con los comodines definidos."
            )
            return null
        }

        return plantilla.nodoPregunta.ejecutarConComodines(entorno, valoresParametros)
    }
}
