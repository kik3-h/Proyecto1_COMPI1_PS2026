package com.usac.pkmforms.data.base_datos.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.usac.pkmforms.data.base_datos.entidades.FormularioGuardadoEntity

@Dao
interface FormularioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(formulario: FormularioGuardadoEntity): Long

    @Query("SELECT * FROM formularios_guardados ORDER BY id DESC")
    suspend fun obtenerTodos(): List<FormularioGuardadoEntity>

    @Delete
    suspend fun eliminar(formulario: FormularioGuardadoEntity)
}
