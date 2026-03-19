package com.usac.pkmforms.domain.modelo.formulario

data class FormularioCompilado(
    val componentes: List<ComponenteFormulario>,
    val metadatos: Map<String, Any?> = emptyMap()
)
