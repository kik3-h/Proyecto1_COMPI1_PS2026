package com.usac.pkmforms.utilidades.manejador_errores

data class ErrorAnalisis(
    val lexema: String,
    val linea: Int,
    val columna: Int,
    val tipo: String,
    val descripcion: String
)
