package com.usac.pkmforms.data.repositorios

import com.usac.pkmforms.data.base_datos.dao.FormularioDao
import com.usac.pkmforms.data.base_datos.entidades.FormularioGuardadoEntity

class FormularioRepository(
    private val formularioDao: FormularioDao
) {
    suspend fun insertar(formulario: FormularioGuardadoEntity): Long =
        formularioDao.insertar(formulario)

    suspend fun obtenerTodos(): List<FormularioGuardadoEntity> =
        formularioDao.obtenerTodos()

    suspend fun eliminar(formulario: FormularioGuardadoEntity) =
        formularioDao.eliminar(formulario)
}
