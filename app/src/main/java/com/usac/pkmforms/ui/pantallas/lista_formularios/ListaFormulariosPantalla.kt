package com.usac.pkmforms.ui.pantallas.lista_formularios

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.usac.pkmforms.data.base_datos.AppDatabase
import com.usac.pkmforms.data.base_datos.entidades.FormularioGuardadoEntity
import com.usac.pkmforms.data.repositorios.FormularioRepository
import com.usac.pkmforms.domain.modelo.formulario.ComponenteFormulario
import com.usac.pkmforms.domain.modelo.formulario.TextoFormulario
import com.usac.pkmforms.utilidades.GestorArchivosLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaFormulariosPantalla(
    onVolver: () -> Unit,
    onAbrirFormulario: (List<ComponenteFormulario>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember(context) {
        FormularioRepository(AppDatabase.obtenerInstancia(context).formularioDao())
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var formularios by remember { mutableStateOf<List<FormularioGuardadoEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        formularios = withContext(Dispatchers.IO) {
            repository.obtenerTodos()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Formularios guardados localmente") },
                actions = {
                    Button(
                        onClick = onVolver,
                        modifier = Modifier.padding(end = 12.dp)
                    ) { Text("Volver") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (formularios.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("No hay formularios guardados.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(formularios, key = { it.id }) { formulario ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    val componentes = withContext(Dispatchers.IO) {
                                        abrirFormularioLocal(context, formulario)
                                    }
                                    if (componentes.isEmpty()) {
                                        snackbarHostState.showSnackbar(
                                            "No se pudo interpretar el .pkm, mostrando contenido básico."
                                        )
                                    }
                                    onAbrirFormulario(componentes)
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = formulario.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = formulario.fechaCreacion,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun abrirFormularioLocal(
    context: Context,
    formulario: FormularioGuardadoEntity
): List<ComponenteFormulario> {
    val contenido = leerContenidoPorRuta(context, formulario.rutaArchivo)
    if (contenido.isBlank()) return emptyList()

    // Fallback seguro: hasta implementar parser completo de .pkm, mostramos el texto guardado.
    return listOf(
        TextoFormulario(
            content = contenido,
            width = null,
            height = null,
            estilos = null
        )
    )
}

private fun leerContenidoPorRuta(
    context: Context,
    rutaArchivo: String
): String {
    return try {
        val archivo = File(rutaArchivo)
        if (archivo.exists()) {
            archivo.readText(Charsets.UTF_8)
        } else {
            GestorArchivosLocal.leerArchivoPkm(context, archivo.nameWithoutExtension)
        }
    } catch (_: Exception) {
        ""
    }
}
