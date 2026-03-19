package com.usac.pkmforms.data.base_datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.usac.pkmforms.data.base_datos.dao.FormularioDao
import com.usac.pkmforms.data.base_datos.entidades.FormularioGuardadoEntity

@Database(
    entities = [FormularioGuardadoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun formularioDao(): FormularioDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtenerInstancia(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pkm_forms_db"
                ).build().also { instancia = it }
            }
        }
    }
}
