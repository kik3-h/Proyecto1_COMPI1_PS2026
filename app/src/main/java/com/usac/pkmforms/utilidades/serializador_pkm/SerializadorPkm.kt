package com.usac.pkmforms.utilidades.serializador_pkm

import com.usac.pkmforms.domain.modelo.formulario.BordeFormulario
import com.usac.pkmforms.domain.modelo.formulario.ComponenteFormulario
import com.usac.pkmforms.domain.modelo.formulario.EstiloFormulario
import com.usac.pkmforms.domain.modelo.formulario.FormularioCompilado
import com.usac.pkmforms.domain.modelo.formulario.TextoFormulario
import com.usac.pkmforms.domain.modelo.formulario.TipoBordeFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaAbiertaFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaDesplegableFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaSeleccionMultipleFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaSeleccionUnicaFormulario
import com.usac.pkmforms.domain.modelo.seccion.OrientacionSeccion
import com.usac.pkmforms.domain.modelo.seccion.SeccionFormulario
import com.usac.pkmforms.domain.modelo.tabla.TablaFormulario

object SerializadorPkm {

    fun generarStringPkm(
        formulario: FormularioCompilado,
        metadatos: Map<String, String>
    ): String {
        val builder = StringBuilder()
        val estadisticas = recolectarEstadisticas(formulario.componentes)

        builder.appendLine("###")
        builder.appendLine("Author: ${metadatos["Author"].orEmpty()}")
        builder.appendLine("Fecha: ${metadatos["Fecha"].orEmpty()}")
        builder.appendLine("Hora: ${metadatos["Hora"].orEmpty()}")
        builder.appendLine("Description: ${metadatos["Description"].orEmpty()}")
        builder.appendLine("Total de Secciones: ${estadisticas.totalSecciones}")
        builder.appendLine("Total de Preguntas: ${estadisticas.totalPreguntas}")
        builder.appendLine("Abiertas: ${estadisticas.preguntasAbiertas}")
        builder.appendLine("Desplegables: ${estadisticas.preguntasDesplegables}")
        builder.appendLine("Selección: ${estadisticas.preguntasSeleccionUnica}")
        builder.appendLine("Múltiples: ${estadisticas.preguntasSeleccionMultiple}")
        builder.appendLine("###")
        builder.appendLine()

        formulario.componentes.forEach { componente ->
            builder.append(serializarComponente(componente, 0))
            builder.appendLine()
        }

        return builder.toString().trimEnd()
    }

    private fun serializarComponente(
        componente: ComponenteFormulario,
        indent: Int
    ): String {
        val tabs = "    ".repeat(indent)
        return when (componente) {
            is SeccionFormulario -> serializarSeccion(componente, indent, tabs)
            is TablaFormulario -> serializarTabla(componente, indent, tabs)
            is TextoFormulario -> serializarTexto(componente, tabs)
            is PreguntaAbiertaFormulario -> serializarPreguntaAbierta(componente, tabs)
            is PreguntaDesplegableFormulario -> serializarPreguntaDesplegable(componente, tabs)
            is PreguntaSeleccionUnicaFormulario -> serializarPreguntaSeleccionUnica(componente, tabs)
            is PreguntaSeleccionMultipleFormulario -> serializarPreguntaSeleccionMultiple(componente, tabs)
            else -> "$tabs<!-- componente no soportado en serializador -->"
        }
    }

    private fun serializarSeccion(
        seccion: SeccionFormulario,
        indent: Int,
        tabs: String
    ): String {
        val orientation = when (seccion.orientation) {
            OrientacionSeccion.VERTICAL -> "VERTICAL"
            OrientacionSeccion.HORIZONTAL -> "HORIZONTAL"
        }
        val contenido = StringBuilder()
        contenido.append("$tabs<section=${fmt(seccion.width)},${fmt(seccion.height)},${fmt(seccion.pointX)},${fmt(seccion.pointY)},$orientation>")
        contenido.appendLine()

        serializarEstiloSiExiste(seccion.estilos, indent + 1)?.let {
            contenido.append(it).appendLine()
        }

        contenido.append("    ".repeat(indent + 1)).appendLine("<content>")
        seccion.elements.forEach { elemento ->
            contenido.append(serializarComponente(elemento, indent + 2)).appendLine()
        }
        contenido.append("    ".repeat(indent + 1)).appendLine("</content>")
        contenido.append("$tabs</section>")
        return contenido.toString()
    }

    private fun serializarTabla(
        tabla: TablaFormulario,
        indent: Int,
        tabs: String
    ): String {
        val contenido = StringBuilder()
        contenido.append("$tabs<table=${fmt(tabla.width)},${fmt(tabla.height)},${fmt(tabla.pointX)},${fmt(tabla.pointY)}>")
        contenido.appendLine()

        serializarEstiloSiExiste(tabla.estilos, indent + 1)?.let {
            contenido.append(it).appendLine()
        }

        contenido.append("    ".repeat(indent + 1)).appendLine("<content>")
        tabla.elements.forEach { elemento ->
            contenido.append("    ".repeat(indent + 2)).appendLine("<line>")
            contenido.append("    ".repeat(indent + 3)).appendLine("<element>")
            contenido.append(serializarComponente(elemento, indent + 4)).appendLine()
            contenido.append("    ".repeat(indent + 3)).appendLine("</element>")
            contenido.append("    ".repeat(indent + 2)).appendLine("</line>")
        }
        contenido.append("    ".repeat(indent + 1)).appendLine("</content>")
        contenido.append("$tabs</table>")
        return contenido.toString()
    }

    private fun serializarTexto(texto: TextoFormulario, tabs: String): String {
        val width = texto.width?.let { fmt(it) } ?: "0"
        val height = texto.height?.let { fmt(it) } ?: "0"
        val base = "$tabs<open=$width,$height,${quote(texto.content)}"
        return if (texto.estilos == null) {
            "$base/>"
        } else {
            buildString {
                appendLine("$base>")
                append(serializarEstilo(texto.estilos, 1))
                appendLine()
                append("$tabs</open>")
            }
        }
    }

    private fun serializarPreguntaAbierta(
        pregunta: PreguntaAbiertaFormulario,
        tabs: String
    ): String {
        val width = pregunta.width?.let { fmt(it) } ?: "0"
        val height = pregunta.height?.let { fmt(it) } ?: "0"
        val base = "$tabs<open=$width,$height,${quote(pregunta.label)}"
        return if (pregunta.estilos == null) {
            "$base/>"
        } else {
            buildString {
                appendLine("$base>")
                append(serializarEstilo(pregunta.estilos, 1))
                appendLine()
                append("$tabs</open>")
            }
        }
    }

    private fun serializarPreguntaDesplegable(
        pregunta: PreguntaDesplegableFormulario,
        tabs: String
    ): String {
        val width = pregunta.width?.let { fmt(it) } ?: "0"
        val height = pregunta.height?.let { fmt(it) } ?: "0"
        val options = pregunta.options.joinToString(prefix = "{", postfix = "}") { quote(it) }
        val correct = pregunta.correct ?: -1
        val base = "$tabs<drop=$width,$height,${quote(pregunta.label)},$options,$correct"
        return if (pregunta.estilos == null) {
            "$base/>"
        } else {
            buildString {
                appendLine("$base>")
                append(serializarEstilo(pregunta.estilos, 1))
                appendLine()
                append("$tabs</drop>")
            }
        }
    }

    private fun serializarPreguntaSeleccionUnica(
        pregunta: PreguntaSeleccionUnicaFormulario,
        tabs: String
    ): String {
        val width = pregunta.width?.let { fmt(it) } ?: "0"
        val height = pregunta.height?.let { fmt(it) } ?: "0"
        val options = pregunta.options.joinToString(prefix = "{", postfix = "}") { quote(it) }
        val correct = pregunta.correct ?: -1
        val base = "$tabs<select=$width,$height,${quote(pregunta.label)},$options,$correct"
        return if (pregunta.estilos == null) {
            "$base/>"
        } else {
            buildString {
                appendLine("$base>")
                append(serializarEstilo(pregunta.estilos, 1))
                appendLine()
                append("$tabs</select>")
            }
        }
    }

    private fun serializarPreguntaSeleccionMultiple(
        pregunta: PreguntaSeleccionMultipleFormulario,
        tabs: String
    ): String {
        val width = pregunta.width?.let { fmt(it) } ?: "0"
        val height = pregunta.height?.let { fmt(it) } ?: "0"
        val options = pregunta.options.joinToString(prefix = "{", postfix = "}") { quote(it) }
        val correct = pregunta.correct.joinToString(prefix = "{", postfix = "}")
        val base = "$tabs<multiple=$width,$height,${quote(pregunta.label)},$options,$correct"
        return if (pregunta.estilos == null) {
            "$base/>"
        } else {
            buildString {
                appendLine("$base>")
                append(serializarEstilo(pregunta.estilos, 1))
                appendLine()
                append("$tabs</multiple>")
            }
        }
    }

    private fun serializarEstiloSiExiste(estilo: EstiloFormulario?, indent: Int): String? {
        estilo ?: return null
        return serializarEstilo(estilo, indent)
    }

    private fun serializarEstilo(estilo: EstiloFormulario, indent: Int): String {
        val tabs = "    ".repeat(indent)
        val builder = StringBuilder()
        builder.append(tabs).appendLine("<style>")

        estilo.colorTexto?.let {
            builder.append(tabs).append("    ").appendLine("<color=$it/>")
        }
        estilo.colorFondo?.let {
            builder.append(tabs).append("    ").appendLine("<background color=$it/>")
        }
        estilo.familiaFuente?.let {
            builder.append(tabs).append("    ").appendLine("<font family=${it.name}/>")
        }
        estilo.tamanioTexto?.let {
            builder.append(tabs).append("    ").appendLine("<text size=${fmt(it)}/>")
        }
        estilo.borde?.let {
            builder.append(tabs).append("    ").appendLine(serializarBorde(it))
        }

        builder.append(tabs).append("</style>")
        return builder.toString()
    }

    private fun serializarBorde(borde: BordeFormulario): String {
        val tipo = when (borde.tipo) {
            TipoBordeFormulario.LINE -> "LINE"
            TipoBordeFormulario.DOTTED -> "DOTTED"
            TipoBordeFormulario.DOUBLE -> "DOUBLE"
        }
        return "<border,${fmt(borde.grosor)},$tipo,color=${borde.color}/>"
    }

    private fun quote(texto: String): String {
        return "\"${texto.replace("\"", "\\\"")}\""
    }

    private fun fmt(valor: Double): String {
        val intVal = valor.toLong()
        return if (valor == intVal.toDouble()) intVal.toString() else valor.toString()
    }

    private data class EstadisticasPkm(
        val totalSecciones: Int,
        val totalPreguntas: Int,
        val preguntasAbiertas: Int,
        val preguntasDesplegables: Int,
        val preguntasSeleccionUnica: Int,
        val preguntasSeleccionMultiple: Int
    )

    private fun recolectarEstadisticas(componentes: List<ComponenteFormulario>): EstadisticasPkm {
        var secciones = 0
        var abiertas = 0
        var desplegables = 0
        var unicas = 0
        var multiples = 0

        fun recorrer(lista: List<ComponenteFormulario>) {
            lista.forEach { componente ->
                when (componente) {
                    is SeccionFormulario -> {
                        secciones += 1
                        recorrer(componente.elements)
                    }

                    is TablaFormulario -> recorrer(componente.elements)
                    is PreguntaAbiertaFormulario -> abiertas += 1
                    is PreguntaDesplegableFormulario -> desplegables += 1
                    is PreguntaSeleccionUnicaFormulario -> unicas += 1
                    is PreguntaSeleccionMultipleFormulario -> multiples += 1
                    else -> Unit
                }
            }
        }

        recorrer(componentes)
        val totalPreguntas = abiertas + desplegables + unicas + multiples
        return EstadisticasPkm(
            totalSecciones = secciones,
            totalPreguntas = totalPreguntas,
            preguntasAbiertas = abiertas,
            preguntasDesplegables = desplegables,
            preguntasSeleccionUnica = unicas,
            preguntasSeleccionMultiple = multiples
        )
    }
}
