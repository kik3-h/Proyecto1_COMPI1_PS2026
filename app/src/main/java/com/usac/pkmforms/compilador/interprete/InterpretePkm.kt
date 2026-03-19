package com.usac.pkmforms.compilador.interprete

import com.usac.pkmforms.compilador.ast.nodos.NodoAST
import com.usac.pkmforms.domain.modelo.formulario.ComponenteFormulario
import com.usac.pkmforms.utilidades.manejador_errores.ErrorAnalisis

data class ResultadoInterpretacionPkm(
    val componentes: List<ComponenteFormulario>,
    val erroresLexicos: List<ErrorAnalisis>,
    val erroresSintacticos: List<ErrorAnalisis>,
    val erroresSemanticos: List<ErrorAnalisis>
) {
    val hayErroresCriticos: Boolean
        get() = erroresSintacticos.isNotEmpty() ||
            erroresSemanticos.any { !it.descripcion.startsWith("Advertencia") }
}

class InterpretePkm(
    private val nodosAst: List<NodoAST>
) {
    fun interpretar(
        erroresLexicos: List<ErrorAnalisis> = emptyList(),
        erroresSintacticos: List<ErrorAnalisis> = emptyList()
    ): ResultadoInterpretacionPkm {
        val entornoSemantico = Entorno()
        nodosAst.forEach { it.validarSemantica(entornoSemantico) }
        val erroresSemanticos = entornoSemantico.erroresSemanticos.toList()

        val hayErroresCriticos = erroresSintacticos.isNotEmpty() ||
            erroresSemanticos.any { !it.descripcion.startsWith("Advertencia") }

        if (hayErroresCriticos) {
            return ResultadoInterpretacionPkm(
                componentes = emptyList(),
                erroresLexicos = erroresLexicos,
                erroresSintacticos = erroresSintacticos,
                erroresSemanticos = erroresSemanticos
            )
        }

        val entornoEjecucion = Entorno()
        val componentesGenerados = mutableListOf<ComponenteFormulario>()
        nodosAst.forEach { nodo ->
            val resultado = nodo.ejecutar(entornoEjecucion)
            if (resultado is ComponenteFormulario) {
                componentesGenerados.add(resultado)
            }
        }

        val erroresFinales = erroresSemanticos + entornoEjecucion.erroresSemanticos

        return ResultadoInterpretacionPkm(
            componentes = componentesGenerados,
            erroresLexicos = erroresLexicos,
            erroresSintacticos = erroresSintacticos,
            erroresSemanticos = erroresFinales
        )
    }
}
