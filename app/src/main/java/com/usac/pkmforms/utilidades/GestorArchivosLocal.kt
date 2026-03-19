package com.usac.pkmforms.utilidades

import android.content.Context
import java.io.File

object GestorArchivosLocal {

    fun guardarArchivoPkm(
        context: Context,
        nombreArchivo: String,
        contenido: String
    ): String {
        val nombreNormalizado = if (nombreArchivo.endsWith(".pkm", ignoreCase = true)) {
            nombreArchivo
        } else {
            "$nombreArchivo.pkm"
        }
        val archivo = File(context.filesDir, nombreNormalizado)
        archivo.writeText(contenido, Charsets.UTF_8)
        return archivo.absolutePath
    }

    fun leerArchivoPkm(
        context: Context,
        nombreArchivo: String
    ): String {
        val nombreNormalizado = if (nombreArchivo.endsWith(".pkm", ignoreCase = true)) {
            nombreArchivo
        } else {
            "$nombreArchivo.pkm"
        }
        val archivo = File(context.filesDir, nombreNormalizado)
        return archivo.readText(Charsets.UTF_8)
    }
}
