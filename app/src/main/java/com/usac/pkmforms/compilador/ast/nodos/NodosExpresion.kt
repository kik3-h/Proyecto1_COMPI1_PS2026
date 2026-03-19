package com.usac.pkmforms.compilador.ast.nodos

import com.usac.pkmforms.compilador.interprete.Entorno
import kotlin.math.pow

private fun recolectarOperadoresLogicos(nodo: NodoAST, operadores: MutableSet<String>) {
    if (nodo is Logica) {
        if (nodo.derecha != null && (nodo.operador == "&&" || nodo.operador == "||")) {
            operadores.add(nodo.operador)
        }
        recolectarOperadoresLogicos(nodo.izquierda, operadores)
        nodo.derecha?.let { recolectarOperadoresLogicos(it, operadores) }
    }
}

data class Aritmetica(
    val operador: String,
    val izquierda: NodoAST,
    val derecha: NodoAST? = null
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        val tipoIzquierda = izquierda.validarSemantica(entorno)
        val tipoDerecha = derecha?.validarSemantica(entorno) ?: Entorno.TipoVariablePkm.NUMBER

        return when (operador) {
            "+" -> {
                if (tipoIzquierda.esNumero() && tipoDerecha.esNumero()) {
                    Entorno.TipoVariablePkm.NUMBER
                } else if (tipoIzquierda.esCadena() || tipoDerecha.esCadena()) {
                    Entorno.TipoVariablePkm.STRING
                } else {
                    entorno.registrarErrorSemantico(
                        lexema = operador,
                        descripcion = "La suma solo permite números o concatenación con cadenas."
                    )
                    Entorno.TipoVariablePkm.DESCONOCIDO
                }
            }

            "-", "*", "/", "^", "%", "NEG" -> {
                val tipoObjetivo = if (operador == "NEG") tipoDerecha else tipoIzquierda
                val tipoComparacion = if (operador == "NEG") tipoDerecha else tipoDerecha
                if (tipoObjetivo.esNumero() && tipoComparacion.esNumero()) {
                    Entorno.TipoVariablePkm.NUMBER
                } else {
                    entorno.registrarErrorSemantico(
                        lexema = operador,
                        descripcion = "La operación '$operador' requiere operandos numéricos."
                    )
                    Entorno.TipoVariablePkm.DESCONOCIDO
                }
            }

            else -> {
                entorno.registrarErrorSemantico(
                    lexema = operador,
                    descripcion = "Operador aritmético no reconocido."
                )
                Entorno.TipoVariablePkm.DESCONOCIDO
            }
        }
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val valorIzquierda = izquierda.ejecutar(entorno)
        val valorDerecha = derecha?.ejecutar(entorno)

        return when (operador) {
            "+" -> {
                if (valorIzquierda is String || valorDerecha is String) {
                    valorIzquierda.aCadena() + valorDerecha.aCadena()
                } else {
                    val numeroIzquierda = valorIzquierda.aNumero()
                    val numeroDerecha = valorDerecha.aNumero()
                    if (numeroIzquierda == null || numeroDerecha == null) {
                        entorno.registrarErrorSemantico(
                            lexema = operador,
                            descripcion = "No se pudo evaluar suma por tipos inválidos."
                        )
                        null
                    } else {
                        numeroIzquierda + numeroDerecha
                    }
                }
            }

            "-" -> {
                val numeroIzquierda = valorIzquierda.aNumero()
                val numeroDerecha = valorDerecha.aNumero()
                if (numeroIzquierda == null || numeroDerecha == null) {
                    entorno.registrarErrorSemantico(
                        lexema = operador,
                        descripcion = "No se pudo evaluar resta por tipos inválidos."
                    )
                    null
                } else {
                    numeroIzquierda - numeroDerecha
                }
            }

            "*" -> {
                val numeroIzquierda = valorIzquierda.aNumero()
                val numeroDerecha = valorDerecha.aNumero()
                if (numeroIzquierda == null || numeroDerecha == null) {
                    entorno.registrarErrorSemantico(
                        lexema = operador,
                        descripcion = "No se pudo evaluar multiplicación por tipos inválidos."
                    )
                    null
                } else {
                    numeroIzquierda * numeroDerecha
                }
            }

            "/" -> {
                val numeroIzquierda = valorIzquierda.aNumero()
                val numeroDerecha = valorDerecha.aNumero()
                if (numeroIzquierda == null || numeroDerecha == null) {
                    entorno.registrarErrorSemantico(
                        lexema = operador,
                        descripcion = "No se pudo evaluar división por tipos inválidos."
                    )
                    null
                } else if (numeroDerecha == 0.0) {
                    entorno.registrarErrorSemantico(
                        lexema = operador,
                        descripcion = "División entre cero."
                    )
                    null
                } else {
                    numeroIzquierda / numeroDerecha
                }
            }

            "^" -> {
                val numeroIzquierda = valorIzquierda.aNumero()
                val numeroDerecha = valorDerecha.aNumero()
                if (numeroIzquierda == null || numeroDerecha == null) {
                    entorno.registrarErrorSemantico(
                        lexema = operador,
                        descripcion = "No se pudo evaluar potencia por tipos inválidos."
                    )
                    null
                } else {
                    numeroIzquierda.pow(numeroDerecha)
                }
            }

            "%" -> {
                val numeroIzquierda = valorIzquierda.aNumero()
                val numeroDerecha = valorDerecha.aNumero()
                if (numeroIzquierda == null || numeroDerecha == null) {
                    entorno.registrarErrorSemantico(
                        lexema = operador,
                        descripcion = "No se pudo evaluar módulo por tipos inválidos."
                    )
                    null
                } else if (numeroDerecha == 0.0) {
                    entorno.registrarErrorSemantico(
                        lexema = operador,
                        descripcion = "Módulo entre cero."
                    )
                    null
                } else {
                    numeroIzquierda % numeroDerecha
                }
            }

            "NEG" -> {
                val numero = valorDerecha.aNumero()
                if (numero == null) {
                    entorno.registrarErrorSemantico(
                        lexema = operador,
                        descripcion = "Negación unaria inválida para valor no numérico."
                    )
                    null
                } else {
                    -numero
                }
            }

            else -> null
        }
    }
}

data class Relacional(
    val operador: String,
    val izquierda: NodoAST,
    val derecha: NodoAST
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        val tipoIzquierda = izquierda.validarSemantica(entorno)
        val tipoDerecha = derecha.validarSemantica(entorno)

        when (operador) {
            ">", "<", ">=", "<=" -> {
                if (!tipoIzquierda.esNumero() || !tipoDerecha.esNumero()) {
                    entorno.registrarErrorSemantico(
                        lexema = operador,
                        descripcion = "El operador '$operador' requiere dos expresiones numéricas."
                    )
                }
            }

            "==", "!!" -> Unit
            else -> entorno.registrarErrorSemantico(
                lexema = operador,
                descripcion = "Operador relacional no reconocido."
            )
        }

        return Entorno.TipoVariablePkm.BOOLEAN
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val valorIzquierda = izquierda.ejecutar(entorno)
        val valorDerecha = derecha.ejecutar(entorno)

        return when (operador) {
            ">" -> (valorIzquierda.aNumero() ?: Double.NaN) > (valorDerecha.aNumero() ?: Double.NaN)
            "<" -> (valorIzquierda.aNumero() ?: Double.NaN) < (valorDerecha.aNumero() ?: Double.NaN)
            ">=" -> (valorIzquierda.aNumero() ?: Double.NaN) >= (valorDerecha.aNumero() ?: Double.NaN)
            "<=" -> (valorIzquierda.aNumero() ?: Double.NaN) <= (valorDerecha.aNumero() ?: Double.NaN)
            "==" -> valorIzquierda == valorDerecha
            "!!" -> valorIzquierda != valorDerecha
            else -> false
        }
    }
}

data class Logica(
    val operador: String,
    val izquierda: NodoAST,
    val derecha: NodoAST? = null
) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        izquierda.validarSemantica(entorno)
        derecha?.validarSemantica(entorno)

        if (operador == "&&" || operador == "||") {
            val operadores = mutableSetOf<String>()
            recolectarOperadoresLogicos(this, operadores)
            if (operadores.size > 1) {
                entorno.registrarErrorSemantico(
                    lexema = operador,
                    descripcion = "No se permite combinar '&&' y '||' en la misma expresión lógica."
                )
            }
        }

        return Entorno.TipoVariablePkm.BOOLEAN
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val valorIzquierda = izquierda.ejecutar(entorno).aBooleanoPkm()
        val valorDerecha = derecha?.ejecutar(entorno)?.aBooleanoPkm() ?: false

        return when (operador) {
            "!" -> !valorIzquierda
            "&&" -> valorIzquierda && valorDerecha
            "||" -> valorIzquierda || valorDerecha
            else -> false
        }
    }
}

data class Literal(val valor: Any?) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        if (valor.esComodin()) {
            return Entorno.TipoVariablePkm.DESCONOCIDO
        }
        return entorno.tipoDeValor(valor)
    }

    override fun ejecutar(entorno: Entorno): Any? = valor
}

data class Identificador(val nombre: String) : NodoAST {
    override fun validarSemantica(entorno: Entorno): Entorno.TipoVariablePkm {
        val variable = entorno.obtenerVariable(nombre)
        if (variable == null) {
            entorno.registrarErrorSemantico(
                lexema = nombre,
                descripcion = "El identificador '$nombre' no ha sido declarado."
            )
            return Entorno.TipoVariablePkm.DESCONOCIDO
        }
        return variable.tipo
    }

    override fun ejecutar(entorno: Entorno): Any? {
        val variable = entorno.obtenerVariable(nombre)
        if (variable == null) {
            entorno.registrarErrorSemantico(
                lexema = nombre,
                descripcion = "El identificador '$nombre' no existe en ejecución."
            )
            return null
        }
        return variable.valor
    }
}
