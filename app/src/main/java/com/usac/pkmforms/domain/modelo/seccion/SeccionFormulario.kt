package com.usac.pkmforms.domain.modelo.seccion

import com.usac.pkmforms.domain.modelo.formulario.ComponenteFormulario
import com.usac.pkmforms.domain.modelo.formulario.EstiloFormulario

enum class OrientacionSeccion {
    VERTICAL,
    HORIZONTAL
}

data class SeccionFormulario(
    val width: Double,
    val height: Double,
    val pointX: Double,
    val pointY: Double,
    val orientation: OrientacionSeccion = OrientacionSeccion.VERTICAL,
    val elements: MutableList<ComponenteFormulario> = mutableListOf(),
    val estilos: EstiloFormulario? = null
) : ComponenteFormulario
