package com.usac.pkmforms.domain.modelo.formulario

enum class FamiliaFuenteFormulario {
    MONO,
    SANS_SERIF,
    CURSIVE
}

enum class TipoBordeFormulario {
    LINE,
    DOTTED,
    DOUBLE
}

data class BordeFormulario(
    val grosor: Double,
    val tipo: TipoBordeFormulario,
    val color: String
)

data class EstiloFormulario(
    val colorTexto: String? = null,
    val colorFondo: String? = null,
    val familiaFuente: FamiliaFuenteFormulario? = null,
    val tamanioTexto: Double? = null,
    val borde: BordeFormulario? = null
)
