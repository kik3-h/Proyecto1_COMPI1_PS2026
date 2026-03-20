package com.usac.pkmforms.compilador.ast.nodos

import com.usac.pkmforms.compilador.interprete.Entorno
import com.usac.pkmforms.domain.modelo.formulario.ComponenteFormulario
import com.usac.pkmforms.domain.modelo.formulario.TextoFormulario
import com.usac.pkmforms.domain.modelo.seccion.OrientacionSeccion
import com.usac.pkmforms.domain.modelo.seccion.SeccionFormulario
import com.usac.pkmforms.domain.modelo.tabla.TablaFormulario

private fun extraerElementos(atributos: Map<String, Any?>): List<NodoAST> {
    val elementos = atributos["elements"] as? List<*> ?: return emptyList()
    return elementos.mapNotNull { it as? NodoAST }
}

data class Seccion(val atributos: Map<String, Any?>) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        resolverNumeroAtributo(atributos, "width", entorno, obligatorio = true)
        resolverNumeroAtributo(atributos, "height", entorno, obligatorio = true)
        resolverNumeroAtributo(atributos, "pointX", entorno, obligatorio = true)
        resolverNumeroAtributo(atributos, "pointY", entorno, obligatorio = true)

        val orientacion = resolverValor(atributos["orientation"], entorno)?.toString()
        if (orientacion != null && orientacion !in listOf("VERTICAL", "HORIZONTAL")) {
            entorno.registrarErrorSemantico(
                lexema = orientacion,
                descripcion = "La orientación debe ser VERTICAL u HORIZONTAL."
            )
        }

        val entornoInterno = entorno.crearEntornoHijo()
        extraerElementos(atributos).forEach { it.validarSemantica(entornoInterno) }
        resolverEstilo(atributos, entorno)

        return Entorno.TipoVariablePkm.SPECIAL
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val width = resolverNumeroAtributo(atributos, "width", entorno, obligatorio = false, valorDefecto = 0.0) ?: 0.0
        val height = resolverNumeroAtributo(atributos, "height", entorno, obligatorio = false, valorDefecto = 0.0) ?: 0.0
        val pointX = resolverNumeroAtributo(atributos, "pointX", entorno, obligatorio = false, valorDefecto = 0.0) ?: 0.0
        val pointY = resolverNumeroAtributo(atributos, "pointY", entorno, obligatorio = false, valorDefecto = 0.0) ?: 0.0
        val orientacion = when (resolverValor(atributos["orientation"], entorno)?.toString()) {
            "HORIZONTAL" -> OrientacionSeccion.HORIZONTAL
            else -> OrientacionSeccion.VERTICAL
        }

        val estilos = resolverEstilo(atributos, entorno)
        val entornoInterno = entorno.crearEntornoHijo()
        val elementos = mutableListOf<ComponenteFormulario>()

        extraerElementos(atributos).forEach { nodo ->
            val resultado = nodo.ejecutar(entornoInterno)
            if (resultado is ComponenteFormulario) {
                elementos.add(resultado)
            }
        }

        return SeccionFormulario(
            width = width,
            height = height,
            pointX = pointX,
            pointY = pointY,
            orientation = orientacion,
            elements = elementos,
            estilos = estilos
        )
    }
}

data class Tabla(val atributos: Map<String, Any?>) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        resolverNumeroAtributo(atributos, "width", entorno, obligatorio = true)
        resolverNumeroAtributo(atributos, "height", entorno, obligatorio = true)
        resolverNumeroAtributo(atributos, "pointX", entorno, obligatorio = true)
        resolverNumeroAtributo(atributos, "pointY", entorno, obligatorio = true)
        val orientacion = resolverValor(atributos["orientation"], entorno)?.toString()
        if (orientacion != null && orientacion !in listOf("VERTICAL", "HORIZONTAL")) {
            entorno.registrarErrorSemantico(
                lexema = orientacion,
                descripcion = "La orientación debe ser VERTICAL u HORIZONTAL."
            )
        }

        val entornoInterno = entorno.crearEntornoHijo()
        extraerElementos(atributos).forEach { it.validarSemantica(entornoInterno) }
        resolverEstilo(atributos, entorno)

        return Entorno.TipoVariablePkm.SPECIAL
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val width = resolverNumeroAtributo(atributos, "width", entorno, obligatorio = false, valorDefecto = 0.0) ?: 0.0
        val height = resolverNumeroAtributo(atributos, "height", entorno, obligatorio = false, valorDefecto = 0.0) ?: 0.0
        val pointX = resolverNumeroAtributo(atributos, "pointX", entorno, obligatorio = false, valorDefecto = 0.0) ?: 0.0
        val pointY = resolverNumeroAtributo(atributos, "pointY", entorno, obligatorio = false, valorDefecto = 0.0) ?: 0.0
        val orientacion = when (resolverValor(atributos["orientation"], entorno)?.toString()) {
            "HORIZONTAL" -> OrientacionSeccion.HORIZONTAL
            else -> OrientacionSeccion.VERTICAL
        }
        val estilos = resolverEstilo(atributos, entorno)

        val entornoInterno = entorno.crearEntornoHijo()
        val elementos = mutableListOf<ComponenteFormulario>()
        extraerElementos(atributos).forEach { nodo ->
            val resultado = nodo.ejecutar(entornoInterno)
            if (resultado is ComponenteFormulario) {
                elementos.add(resultado)
            }
        }

        return TablaFormulario(
            width = width,
            height = height,
            pointX = pointX,
            pointY = pointY,
            orientation = orientacion,
            elements = elementos,
            estilos = estilos
        )
    }
}

data class Texto(val atributos: Map<String, Any?>) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        val tipoContenido = (atributos["content"] as? NodoAST)?.validarSemantica(entorno)
        if (tipoContenido != null && tipoContenido != Entorno.TipoVariablePkm.STRING) {
            entorno.registrarErrorSemantico(
                lexema = "content",
                descripcion = "TEXT.content debe evaluarse como string."
            )
        }
        resolverEstilo(atributos, entorno)
        return Entorno.TipoVariablePkm.SPECIAL
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val width = resolverNumeroAtributo(atributos, "width", entorno, obligatorio = false)
        val height = resolverNumeroAtributo(atributos, "height", entorno, obligatorio = false)
        val contenido = resolverTextoAtributo(atributos, "content", entorno, obligatorio = true).orEmpty()
        val estilos = resolverEstilo(atributos, entorno)

        return TextoFormulario(
            width = width,
            height = height,
            content = contenido,
            estilos = estilos
        )
    }
}
