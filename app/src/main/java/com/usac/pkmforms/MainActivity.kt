package com.usac.pkmforms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

@Composable
private fun NavegadorPantallas() {
    var pantallaActual by remember {
        mutableStateOf(PantallaActual.INICIO)
    }
    var componentesRender by remember {
        mutableStateOf<List<ComponenteFormulario>>(emptyList())
    }

    when (pantallaActual) {
        PantallaActual.INICIO -> PantallaInicio(
            onIrEditor = { pantallaActual = PantallaActual.EDITOR },
            onIrLista = { pantallaActual = PantallaActual.LISTA_FORMULARIOS }
        )

        PantallaActual.EDITOR -> EditorPantalla(
            onFormularioGenerado = { componentes ->
                componentesRender = componentes
                pantallaActual = PantallaActual.RENDER
            }
        )

        PantallaActual.LISTA_FORMULARIOS -> ListaFormulariosPantalla(
            onVolver = { pantallaActual = PantallaActual.INICIO },
            onAbrirFormulario = { componentes ->
                componentesRender = componentes
                pantallaActual = PantallaActual.RENDER
            }
        )

        PantallaActual.RENDER -> RenderizadorPantalla(
            componentes = componentesRender,
            onVolverEditor = { pantallaActual = PantallaActual.EDITOR }
        )
    }
}

@Composable
private fun PantallaInicio(
    onIrEditor: () -> Unit,
    onIrLista: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PKM_FORMS")
        Button(onClick = onIrEditor) {
            Text("Abrir editor")
        }
        Button(onClick = onIrLista) {
            Text("Ver formularios guardados localmente")
        }
    }
}

private enum class PantallaActual {
    INICIO,
    EDITOR,
    LISTA_FORMULARIOS,
    RENDER
}
