package com.usac.pkmforms.domain.modelo.preguntas

import com.usac.pkmforms.domain.modelo.formulario.ComponenteFormulario
import com.usac.pkmforms.domain.modelo.formulario.EstiloFormulario

sealed interface PreguntaFormulario : ComponenteFormulario {
    val width: Double?
    val height: Double?
    val label: String
    val estilos: EstiloFormulario?
}

data class PreguntaAbiertaFormulario(
    override val width: Double? = null,
    override val height: Double? = null,
    override val label: String,
    override val estilos: EstiloFormulario? = null
) : PreguntaFormulario

data class PreguntaDesplegableFormulario(
    override val width: Double? = null,
    override val height: Double? = null,
    override val label: String,
    val options: List<String>,
    val correct: Int? = null,
    override val estilos: EstiloFormulario? = null
) : PreguntaFormulario

data class PreguntaSeleccionUnicaFormulario(
    override val width: Double? = null,
    override val height: Double? = null,
    override val label: String,
    val options: List<String>,
    val correct: Int? = null,
    override val estilos: EstiloFormulario? = null
) : PreguntaFormulario

data class PreguntaSeleccionMultipleFormulario(
    override val width: Double? = null,
    override val height: Double? = null,
    override val label: String,
    val options: List<String>,
    val correct: List<Int> = emptyList(),
    override val estilos: EstiloFormulario? = null
) : PreguntaFormulario

data class InvocacionPokemonFormulario(
    val tipoDatoConsulta: String,
    val rangoInicio: Int,
    val rangoFin: Int
) : ComponenteFormulario
