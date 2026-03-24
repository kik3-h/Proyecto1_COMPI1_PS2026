package com.usac.pkmforms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.usac.pkmforms.domain.modelo.formulario.ComponenteFormulario
import com.usac.pkmforms.ui.pantallas.editor_codigo.EditorPantalla
import com.usac.pkmforms.ui.pantallas.lista_formularios.ListaFormulariosPantalla
import com.usac.pkmforms.ui.pantallas.visor_formulario.RenderizadorPantalla

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavegadorPantallas()
                }
            }
        }
    }
}

private object RutasPantallas {
    const val INICIO = "inicio"
    const val EDITOR = "editor"
    const val LISTA_FORMULARIOS = "lista_formularios"
    const val RENDER = "render"
}

@Composable
private fun NavegadorPantallas() {
    val navController = rememberNavController()
    var componentesRender by remember {
        mutableStateOf<List<ComponenteFormulario>>(emptyList())
    }

    NavHost(
        navController = navController,
        startDestination = RutasPantallas.INICIO
    ) {
        composable(RutasPantallas.INICIO) {
            PantallaInicio(
                onIrEditor = { navController.navigate(RutasPantallas.EDITOR) },
                onIrLista = { navController.navigate(RutasPantallas.LISTA_FORMULARIOS) }
            )
        }

        composable(RutasPantallas.EDITOR) {
            EditorPantalla(
                navController = navController,
                onVolver = { navController.popBackStack(RutasPantallas.INICIO, false) },
                onFormularioGenerado = { componentes ->
                    componentesRender = componentes
                    navController.navigate(RutasPantallas.RENDER)
                }
            )
        }

        composable(RutasPantallas.LISTA_FORMULARIOS) {
            ListaFormulariosPantalla(
                navController = navController,
                onVolver = { navController.popBackStack(RutasPantallas.INICIO, false) },
                onAbrirFormulario = { componentes ->
                    componentesRender = componentes
                    navController.navigate(RutasPantallas.RENDER)
                }
            )
        }

        composable(RutasPantallas.RENDER) {
            RenderizadorPantalla(
                navController = navController,
                componentes = componentesRender
            )
        }
    }
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class) //para que funione agregue esto pq es experimental
private fun PantallaInicio(
    onIrEditor: () -> Unit,
    onIrLista: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PKM_FORMS_EH", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onIrEditor) {
                Text("Abrir editor")
            }
            Button(onClick = onIrLista) {
                Text("Ver formularios guardados localmente")
            }
        }
    }
}
