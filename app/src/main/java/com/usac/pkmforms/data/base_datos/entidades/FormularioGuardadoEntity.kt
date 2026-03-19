package com.usac.pkmforms.data.base_datos.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "formularios_guardados")
data class FormularioGuardadoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    val fechaCreacion: String,
    val rutaArchivo: String
)
