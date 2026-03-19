package com.usac.pkmforms.domain.modelo.formulario

data class TextoFormulario(
    val width: Double? = null,
    val height: Double? = null,
    val content: String,
    val estilos: EstiloFormulario? = null
) : ComponenteFormulario
