package com.usac.pkmforms.compilador.ast.nodos

import com.usac.pkmforms.compilador.interprete.Entorno

private const val MAXIMO_ITERACIONES_SEGURIDAD = 10_000

private fun validarCondicion(condicion: NodoAST, entorno: Entorno, contexto: String) {
    val tipoCondicion = condicion.validarSemantica(entorno)
    if (tipoCondicion != Entorno.TipoVariablePkm.BOOLEAN &&
        tipoCondicion != Entorno.TipoVariablePkm.NUMBER &&
        tipoCondicion != Entorno.TipoVariablePkm.DESCONOCIDO
    ) {
        entorno.registrarErrorSemantico(
            lexema = contexto,
            descripcion = "La condición en $contexto debe ser booleana o numérica."
        )
    }
}

data class IfElse(
    val condicion: NodoAST,
    val bloqueVerdadero: List<NodoAST>,
    val bloqueFalso: List<NodoAST> = emptyList()
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        validarCondicion(condicion, entorno, "IF")
        val entornoVerdadero = entorno.crearEntornoHijo()
        bloqueVerdadero.forEach { it.validarSemantica(entornoVerdadero) }
        val entornoFalso = entorno.crearEntornoHijo()
        bloqueFalso.forEach { it.validarSemantica(entornoFalso) }
        return Entorno.TipoVariablePkm.NULO
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val condicionCumplida = condicion.ejecutar(entorno).aBooleanoPkm()
        if (condicionCumplida) {
            val entornoVerdadero = entorno.crearEntornoHijo()
            bloqueVerdadero.forEach { it.ejecutar(entornoVerdadero) }
        } else {
            val entornoFalso = entorno.crearEntornoHijo()
            bloqueFalso.forEach { it.ejecutar(entornoFalso) }
        }
        return null
    }
}

data class While(
    val condicion: NodoAST,
    val bloque: List<NodoAST>
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        validarCondicion(condicion, entorno, "WHILE")
        val entornoIteracion = entorno.crearEntornoHijo()
        bloque.forEach { it.validarSemantica(entornoIteracion) }
        return Entorno.TipoVariablePkm.NULO
    }

    override fun ejecutar(entorno: Entorno): Any? {
        var iteraciones = 0
        while (condicion.ejecutar(entorno).aBooleanoPkm()) {
            iteraciones++
            if (iteraciones > MAXIMO_ITERACIONES_SEGURIDAD) {
                entorno.registrarErrorSemantico(
                    lexema = "WHILE",
                    descripcion = "Se alcanzó el límite de iteraciones de seguridad en WHILE."
                )
                break
            }
            val entornoIteracion = entorno.crearEntornoHijo()
            bloque.forEach { it.ejecutar(entornoIteracion) }
        }
        return null
    }
}

data class DoWhile(
    val bloque: List<NodoAST>,
    val condicion: NodoAST
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        val entornoIteracion = entorno.crearEntornoHijo()
        bloque.forEach { it.validarSemantica(entornoIteracion) }
        validarCondicion(condicion, entorno, "DO-WHILE")
        return Entorno.TipoVariablePkm.NULO
    }

    override fun ejecutar(entorno: Entorno): Any? {
        var iteraciones = 0
        do {
            iteraciones++
            if (iteraciones > MAXIMO_ITERACIONES_SEGURIDAD) {
                entorno.registrarErrorSemantico(
                    lexema = "DO-WHILE",
                    descripcion = "Se alcanzó el límite de iteraciones de seguridad en DO-WHILE."
                )
                break
            }
            val entornoIteracion = entorno.crearEntornoHijo()
            bloque.forEach { it.ejecutar(entornoIteracion) }
        } while (condicion.ejecutar(entorno).aBooleanoPkm())
        return null
    }
}

data class ForClasico(
    val inicializacion: NodoAST,
    val condicion: NodoAST,
    val actualizacion: NodoAST,
    val bloque: List<NodoAST>
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        val entornoFor = entorno.crearEntornoHijo()

        if (inicializacion is AsignacionVar) {
            val variableExistente = entornoFor.obtenerVariable(inicializacion.nombre)
            if (variableExistente == null) {
                entornoFor.declararVariable(
                    id = inicializacion.nombre,
                    tipo = Entorno.TipoVariablePkm.NUMBER,
                    valor = 0.0,
                    mutabilidad = true
                )
            } else if (variableExistente.tipo != Entorno.TipoVariablePkm.NUMBER) {
                entornoFor.registrarErrorSemantico(
                    lexema = inicializacion.nombre,
                    descripcion = "La variable de FOR clásico debe ser de tipo number."
                )
            }
        }

        inicializacion.validarSemantica(entornoFor)
        validarCondicion(condicion, entornoFor, "FOR")
        actualizacion.validarSemantica(entornoFor)
        val entornoBloque = entornoFor.crearEntornoHijo()
        bloque.forEach { it.validarSemantica(entornoBloque) }
        return Entorno.TipoVariablePkm.NULO
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val entornoFor = entorno.crearEntornoHijo()

        if (inicializacion is AsignacionVar) {
            val variableExistente = entornoFor.obtenerVariable(inicializacion.nombre)
            if (variableExistente == null) {
                entornoFor.declararVariable(
                    id = inicializacion.nombre,
                    tipo = Entorno.TipoVariablePkm.NUMBER,
                    valor = 0.0,
                    mutabilidad = true
                )
            }
        }

        inicializacion.ejecutar(entornoFor)

        var iteraciones = 0
        while (condicion.ejecutar(entornoFor).aBooleanoPkm()) {
            iteraciones++
            if (iteraciones > MAXIMO_ITERACIONES_SEGURIDAD) {
                entorno.registrarErrorSemantico(
                    lexema = "FOR",
                    descripcion = "Se alcanzó el límite de iteraciones de seguridad en FOR clásico."
                )
                break
            }
            val entornoBloque = entornoFor.crearEntornoHijo()
            bloque.forEach { it.ejecutar(entornoBloque) }
            actualizacion.ejecutar(entornoFor)
        }
        return null
    }
}

data class ForRango(
    val nombreVariable: String,
    val inicio: NodoAST,
    val fin: NodoAST,
    val bloque: List<NodoAST>
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        val entornoFor = entorno.crearEntornoHijo()
        val variableExistente = entornoFor.obtenerVariable(nombreVariable)
        if (variableExistente != null && variableExistente.tipo != Entorno.TipoVariablePkm.NUMBER) {
            entornoFor.registrarErrorSemantico(
                lexema = nombreVariable,
                descripcion = "La variable de FOR rango debe ser number."
            )
        } else if (variableExistente == null) {
            entornoFor.declararVariable(
                id = nombreVariable,
                tipo = Entorno.TipoVariablePkm.NUMBER,
                valor = 0.0,
                mutabilidad = true
            )
        }

        val tipoInicio = inicio.validarSemantica(entornoFor)
        val tipoFin = fin.validarSemantica(entornoFor)
        if (!tipoInicio.esNumero() || !tipoFin.esNumero()) {
            entornoFor.registrarErrorSemantico(
                lexema = "FOR",
                descripcion = "El rango de FOR (in ..) debe ser numérico."
            )
        }

        val entornoBloque = entornoFor.crearEntornoHijo()
        bloque.forEach { it.validarSemantica(entornoBloque) }
        return Entorno.TipoVariablePkm.NULO
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val entornoFor = entorno.crearEntornoHijo()
        if (entornoFor.obtenerVariable(nombreVariable) == null) {
            entornoFor.declararVariable(
                id = nombreVariable,
                tipo = Entorno.TipoVariablePkm.NUMBER,
                valor = 0.0,
                mutabilidad = true
            )
        }

        val inicioNumero = (inicio.ejecutar(entornoFor).aNumero() ?: 0.0).toInt()
        val finNumero = (fin.ejecutar(entornoFor).aNumero() ?: inicioNumero.toDouble()).toInt()
        val paso = if (inicioNumero <= finNumero) 1 else -1

        var actual = inicioNumero
        var iteraciones = 0
        while ((paso > 0 && actual <= finNumero) || (paso < 0 && actual >= finNumero)) {
            iteraciones++
            if (iteraciones > MAXIMO_ITERACIONES_SEGURIDAD) {
                entorno.registrarErrorSemantico(
                    lexema = "FOR",
                    descripcion = "Se alcanzó el límite de iteraciones de seguridad en FOR rango."
                )
                break
            }

            entornoFor.reasignarVariable(nombreVariable, actual.toDouble())
            val entornoBloque = entornoFor.crearEntornoHijo()
            bloque.forEach { it.ejecutar(entornoBloque) }
            actual += paso
        }

        return null
    }
}
