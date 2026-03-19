package com.usac.pkmforms.compilador.ast.nodos

import com.usac.pkmforms.compilador.interprete.Entorno

interface NodoAST {
    fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm
    fun ejecutar(entorno: Entorno): Any?
}
