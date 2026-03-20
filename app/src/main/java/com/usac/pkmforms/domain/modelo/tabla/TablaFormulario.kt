package com.usac.pkmforms.domain.modelo.tabla

import com.usac.pkmforms.domain.modelo.formulario.ComponenteFormulario
import com.usac.pkmforms.domain.modelo.formulario.EstiloFormulario
import com.usac.pkmforms.domain.modelo.seccion.OrientacionSeccion

data class TablaFormulario(
    val width: Double,
    val height: Double,
    val pointX: Double,
    val pointY: Double,
    val orientation: OrientacionSeccion = OrientacionSeccion.VERTICAL,
    val elements: MutableList<ComponenteFormulario> = mutableListOf(),
    val estilos: EstiloFormulario? = null
) : ComponenteFormulario
