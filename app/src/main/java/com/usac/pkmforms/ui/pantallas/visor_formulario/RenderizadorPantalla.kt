package com.usac.pkmforms.ui.pantallas.visor_formulario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.usac.pkmforms.domain.modelo.formulario.BordeFormulario
import com.usac.pkmforms.domain.modelo.formulario.ComponenteFormulario
import com.usac.pkmforms.domain.modelo.formulario.EstiloFormulario
import com.usac.pkmforms.domain.modelo.formulario.FamiliaFuenteFormulario
import com.usac.pkmforms.domain.modelo.formulario.TextoFormulario
import com.usac.pkmforms.domain.modelo.formulario.TipoBordeFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaAbiertaFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaDesplegableFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaSeleccionMultipleFormulario
import com.usac.pkmforms.domain.modelo.preguntas.PreguntaSeleccionUnicaFormulario
import com.usac.pkmforms.domain.modelo.seccion.OrientacionSeccion
import com.usac.pkmforms.domain.modelo.seccion.SeccionFormulario
import com.usac.pkmforms.domain.modelo.tabla.TablaFormulario
import com.usac.pkmforms.servicios.cliente_api_pokemon.PokeApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

fun String.procesarEmojis(): String {
    var txt = this.replace("@[:smile:]", "😊").replace("@[:heart:]", "❤️").replace("@[:cat:]", "🐱")
    txt = txt.replace("@[:)]", "🙂").replace("@[:(]", "☹️").replace("@[:serious:]", "😐")
    txt = txt.replace("@[<3]", "❤️").replace("@[<<<333]", "💖").replace("@[:||||]", "📊")
    txt = txt.replace(Regex("@\\[:star:(\\d+):\\]")) { match -> "⭐".repeat(match.groupValues[1].toIntOrNull() ?: 1) }
    return txt
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenderizadorPantalla(
    navController: NavController,
    componentes: List<ComponenteFormulario>,
    modifier: Modifier = Modifier
) {
    val respuestasAbiertas = remember { mutableStateMapOf<String, String>() }
    val respuestasUnica = remember { mutableStateMapOf<String, Int>() }
    val respuestasMultiple = remember { mutableStateMapOf<String, Set<Int>>() }
    val respuestasDrop = remember { mutableStateMapOf<String, Int>() }

    var resultadoEnvio by remember { mutableStateOf<ResultadoEnvio?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("PKM_FORMS_EH") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (componentes.isEmpty()) {
                Text(
                    text = "No hay componentes para renderizar.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                componentes.forEachIndexed { indice, componente ->
                    RenderComponente(
                        componente = componente,
                        clave = "root-$indice",
                        respuestasAbiertas = respuestasAbiertas,
                        respuestasUnica = respuestasUnica,
                        respuestasMultiple = respuestasMultiple,
                        respuestasDrop = respuestasDrop
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    resultadoEnvio = evaluarFormulario(
                        componentes = componentes,
                        respuestasUnica = respuestasUnica,
                        respuestasMultiple = respuestasMultiple,
                        respuestasDrop = respuestasDrop
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Enviar formulario")
            }
        }
    }

    val resultadoActual = resultadoEnvio
    if (resultadoActual != null) {
        AlertDialog(
            onDismissRequest = { resultadoEnvio = null },
            confirmButton = {
                TextButton(onClick = { resultadoEnvio = null }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Resultado del formulario") },
            text = {
                Text(
                    if (resultadoActual.totalEvaluables == 0) {
                        "Formulario enviado correctamente"
                    } else {
                        "Puntuación: ${resultadoActual.correctas}/${resultadoActual.totalEvaluables}"
                    }
                )
            }
        )
    }
}

@Composable
private fun RenderComponente(
    componente: ComponenteFormulario,
    clave: String,
    respuestasAbiertas: MutableMap<String, String>,
    respuestasUnica: MutableMap<String, Int>,
    respuestasMultiple: MutableMap<String, Set<Int>>,
    respuestasDrop: MutableMap<String, Int>,
    estiloHeredado: EstiloFormulario? = null
) {
    when (componente) {
        is SeccionFormulario -> RenderSeccion(
            seccion = componente,
            clave = clave,
            estiloHeredado = estiloHeredado,
            respuestasAbiertas = respuestasAbiertas,
            respuestasUnica = respuestasUnica,
            respuestasMultiple = respuestasMultiple,
            respuestasDrop = respuestasDrop
        )

        is TextoFormulario -> RenderTexto(
            texto = componente,
            estiloHeredado = estiloHeredado
        )

        is PreguntaAbiertaFormulario -> RenderPreguntaAbierta(
            pregunta = componente,
            clave = clave,
            estiloHeredado = estiloHeredado,
            respuestasAbiertas = respuestasAbiertas
        )

        is PreguntaSeleccionUnicaFormulario -> RenderPreguntaSeleccionUnica(
            pregunta = componente,
            clave = clave,
            estiloHeredado = estiloHeredado,
            respuestasUnica = respuestasUnica
        )

        is PreguntaSeleccionMultipleFormulario -> RenderPreguntaSeleccionMultiple(
            pregunta = componente,
            clave = clave,
            estiloHeredado = estiloHeredado,
            respuestasMultiple = respuestasMultiple
        )

        is PreguntaDesplegableFormulario -> RenderPreguntaDesplegable(
            pregunta = componente,
            clave = clave,
            estiloHeredado = estiloHeredado,
            respuestasDrop = respuestasDrop
        )

        is TablaFormulario -> RenderTabla(
            tabla = componente,
            clave = clave,
            estiloHeredado = estiloHeredado,
            respuestasAbiertas = respuestasAbiertas,
            respuestasUnica = respuestasUnica,
            respuestasMultiple = respuestasMultiple,
            respuestasDrop = respuestasDrop
        )

        else -> Text(
            text = "Componente no soportado: ${componente::class.simpleName}",
            color = Color.Red
        )
    }
}

@Composable
private fun RenderSeccion(
    seccion: SeccionFormulario,
    clave: String,
    estiloHeredado: EstiloFormulario?,
    respuestasAbiertas: MutableMap<String, String>,
    respuestasUnica: MutableMap<String, Int>,
    respuestasMultiple: MutableMap<String, Set<Int>>,
    respuestasDrop: MutableMap<String, Int>
) {
    val estiloFinal = fusionarEstilos(estiloHeredado, seccion.estilos)
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .then(aplicarDimensiones(width = seccion.width, height = seccion.height)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 2.dp,
            color = colorDesdeCadena(estiloFinal.colorTexto) ?: MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = colorDesdeCadena(estiloFinal.colorFondo)
                ?: MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (seccion.orientation == OrientacionSeccion.HORIZONTAL) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(aplicarEstilosContenedor(estiloFinal))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                seccion.elements.forEachIndexed { indice, componenteHijo ->
                    Box(modifier = Modifier.weight(1f)) {
                        RenderComponente(
                            componente = componenteHijo,
                            clave = "$clave-h-$indice",
                            estiloHeredado = estiloFinal,
                            respuestasAbiertas = respuestasAbiertas,
                            respuestasUnica = respuestasUnica,
                            respuestasMultiple = respuestasMultiple,
                            respuestasDrop = respuestasDrop
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(aplicarEstilosContenedor(estiloFinal))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                seccion.elements.forEachIndexed { indice, componenteHijo ->
                    RenderComponente(
                        componente = componenteHijo,
                        clave = "$clave-v-$indice",
                        estiloHeredado = estiloFinal,
                        respuestasAbiertas = respuestasAbiertas,
                        respuestasUnica = respuestasUnica,
                        respuestasMultiple = respuestasMultiple,
                        respuestasDrop = respuestasDrop
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderTabla(
    tabla: TablaFormulario,
    clave: String,
    estiloHeredado: EstiloFormulario?,
    respuestasAbiertas: MutableMap<String, String>,
    respuestasUnica: MutableMap<String, Int>,
    respuestasMultiple: MutableMap<String, Set<Int>>,
    respuestasDrop: MutableMap<String, Int>
) {
    val estiloFinal = fusionarEstilos(estiloHeredado, tabla.estilos)
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .then(aplicarDimensiones(width = tabla.width, height = tabla.height)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 2.dp,
            color = colorDesdeCadena(estiloFinal.colorTexto) ?: MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = colorDesdeCadena(estiloFinal.colorFondo)
                ?: MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (tabla.orientation == OrientacionSeccion.HORIZONTAL) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(aplicarEstilosContenedor(estiloFinal))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabla.elements.forEachIndexed { indice, componente ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, Color(0x33555555), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        RenderComponente(
                            componente = componente,
                            clave = "$clave-table-h-$indice",
                            estiloHeredado = estiloFinal,
                            respuestasAbiertas = respuestasAbiertas,
                            respuestasUnica = respuestasUnica,
                            respuestasMultiple = respuestasMultiple,
                            respuestasDrop = respuestasDrop
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(aplicarEstilosContenedor(estiloFinal))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabla.elements.forEachIndexed { indice, componente ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, Color(0x33555555), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        RenderComponente(
                            componente = componente,
                            clave = "$clave-table-v-$indice",
                            estiloHeredado = estiloFinal,
                            respuestasAbiertas = respuestasAbiertas,
                            respuestasUnica = respuestasUnica,
                            respuestasMultiple = respuestasMultiple,
                            respuestasDrop = respuestasDrop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderTexto(
    texto: TextoFormulario,
    estiloHeredado: EstiloFormulario?
) {
    val estiloFinal = fusionarEstilos(estiloHeredado, texto.estilos)
    Text(
        text = texto.content.procesarEmojis(),
        modifier = Modifier
            .fillMaxWidth()
            .then(aplicarDimensiones(width = texto.width, height = texto.height))
            .then(aplicarEstilosContenedor(estiloFinal))
            .padding(8.dp),
        style = textStyleDesdeEstilo(estiloFinal)
    )
}

@Composable
private fun RenderPreguntaAbierta(
    pregunta: PreguntaAbiertaFormulario,
    clave: String,
    estiloHeredado: EstiloFormulario?,
    respuestasAbiertas: MutableMap<String, String>
) {
    var estadoTexto by remember(clave) { mutableStateOf(respuestasAbiertas[clave].orEmpty()) }
    val estiloFinal = fusionarEstilos(estiloHeredado, pregunta.estilos)
    val colorTexto = colorDesdeCadena(estiloFinal.colorTexto) ?: MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .then(aplicarDimensiones(width = pregunta.width, height = pregunta.height))
            .then(aplicarEstilosContenedor(estiloFinal))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = pregunta.label.procesarEmojis(),
            style = textStyleDesdeEstilo(estiloFinal)
        )
        OutlinedTextField(
            value = estadoTexto,
            onValueChange = {
                estadoTexto = it
                respuestasAbiertas[clave] = it
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = colorTexto, fontSize = textSizeDesdeEstilo(estiloFinal))
        )
    }
}

@Composable
private fun RenderPreguntaSeleccionUnica(
    pregunta: PreguntaSeleccionUnicaFormulario,
    clave: String,
    estiloHeredado: EstiloFormulario?,
    respuestasUnica: MutableMap<String, Int>
) {
    val estiloFinal = fusionarEstilos(estiloHeredado, pregunta.estilos)
    val colorTexto = colorDesdeCadena(estiloFinal.colorTexto) ?: MaterialTheme.colorScheme.onSurface

    var opcionesDynamic by remember(clave) { mutableStateOf(pregunta.options) }
    LaunchedEffect(clave, pregunta.options) {
        if (pregunta.options.isEmpty()) {
            val desdeCorrect = ((pregunta.correct ?: 0) + 1).coerceAtLeast(1)
            val hasta = (desdeCorrect + 4).coerceAtLeast(desdeCorrect)
            val opcionesApi = withContext(Dispatchers.IO) {
                PokeApiClient.who_is_that_pokemon(desdeCorrect, hasta)
            }
            if (opcionesApi.isNotEmpty()) {
                opcionesDynamic = opcionesApi
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .then(aplicarDimensiones(width = pregunta.width, height = pregunta.height))
            .then(aplicarEstilosContenedor(estiloFinal))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = pregunta.label.procesarEmojis(), style = textStyleDesdeEstilo(estiloFinal))
        opcionesDynamic.forEachIndexed { indice, opcion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { respuestasUnica[clave] = indice },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = respuestasUnica[clave] == indice,
                    onClick = { respuestasUnica[clave] = indice }
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = opcion,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = TextStyle(color = colorTexto, fontSize = textSizeDesdeEstilo(estiloFinal))
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderPreguntaSeleccionMultiple(
    pregunta: PreguntaSeleccionMultipleFormulario,
    clave: String,
    estiloHeredado: EstiloFormulario?,
    respuestasMultiple: MutableMap<String, Set<Int>>
) {
    val estiloFinal = fusionarEstilos(estiloHeredado, pregunta.estilos)
    val colorTexto = colorDesdeCadena(estiloFinal.colorTexto) ?: MaterialTheme.colorScheme.onSurface

    var opcionesDynamic by remember(clave) { mutableStateOf(pregunta.options) }
    LaunchedEffect(clave, pregunta.options) {
        if (pregunta.options.isEmpty()) {
            val desdeCorrect = (pregunta.correct.minOrNull()?.plus(1) ?: 1).coerceAtLeast(1)
            val hasta = (desdeCorrect + 4).coerceAtLeast(desdeCorrect)
            val opcionesApi = withContext(Dispatchers.IO) {
                PokeApiClient.who_is_that_pokemon(desdeCorrect, hasta)
            }
            if (opcionesApi.isNotEmpty()) {
                opcionesDynamic = opcionesApi
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .then(aplicarDimensiones(width = pregunta.width, height = pregunta.height))
            .then(aplicarEstilosContenedor(estiloFinal))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = pregunta.label.procesarEmojis(), style = textStyleDesdeEstilo(estiloFinal))
        opcionesDynamic.forEachIndexed { indice, opcion ->
            val seleccionados = respuestasMultiple[clave].orEmpty()
            val marcado = seleccionados.contains(indice)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        respuestasMultiple[clave] = if (marcado) {
                            seleccionados - indice
                        } else {
                            seleccionados + indice
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = marcado,
                    onCheckedChange = { isChecked ->
                        respuestasMultiple[clave] = if (isChecked) {
                            seleccionados + indice
                        } else {
                            seleccionados - indice
                        }
                    }
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = opcion,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = TextStyle(color = colorTexto, fontSize = textSizeDesdeEstilo(estiloFinal))
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderPreguntaDesplegable(
    pregunta: PreguntaDesplegableFormulario,
    clave: String,
    estiloHeredado: EstiloFormulario?,
    respuestasDrop: MutableMap<String, Int>
) {
    var expandido by remember(clave) { mutableStateOf(false) }
    val estiloFinal = fusionarEstilos(estiloHeredado, pregunta.estilos)
    val colorTexto = colorDesdeCadena(estiloFinal.colorTexto) ?: MaterialTheme.colorScheme.onSurface

    var opcionesDynamic by remember(clave) { mutableStateOf(pregunta.options) }
    LaunchedEffect(clave, pregunta.options) {
        if (pregunta.options.isEmpty()) {
            val desdeCorrect = ((pregunta.correct ?: 0) + 1).coerceAtLeast(1)
            val hasta = (desdeCorrect + 4).coerceAtLeast(desdeCorrect)
            val opcionesApi = withContext(Dispatchers.IO) {
                PokeApiClient.who_is_that_pokemon(desdeCorrect, hasta)
            }
            if (opcionesApi.isNotEmpty()) {
                opcionesDynamic = opcionesApi
            }
        }
    }

    val seleccionIdx = respuestasDrop[clave]
    val seleccionado = seleccionIdx?.let { opcionesDynamic.getOrNull(it) }.orEmpty()

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .then(aplicarDimensiones(width = pregunta.width, height = pregunta.height))
            .then(aplicarEstilosContenedor(estiloFinal))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = pregunta.label.procesarEmojis(), style = textStyleDesdeEstilo(estiloFinal))
        Box {
            OutlinedTextField(
                value = seleccionado,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = true },
                textStyle = TextStyle(color = colorTexto, fontSize = textSizeDesdeEstilo(estiloFinal)),
                placeholder = { Text("Selecciona una opción") }
            )
            DropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false }
            ) {
                opcionesDynamic.forEachIndexed { idx, opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            respuestasDrop[clave] = idx
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}

private data class ResultadoEnvio(
    val totalEvaluables: Int,
    val correctas: Int
)

private fun evaluarFormulario(
    componentes: List<ComponenteFormulario>,
    respuestasUnica: Map<String, Int>,
    respuestasMultiple: Map<String, Set<Int>>,
    respuestasDrop: Map<String, Int>
): ResultadoEnvio {
    var total = 0
    var correctas = 0

    val respuestasUnicaNormalizadas = respuestasUnica.mapKeys { it.key.substringAfter("root-") }
    val respuestasDropNormalizadas = respuestasDrop.mapKeys { it.key.substringAfter("root-") }
    val respuestasMultipleNormalizadas = respuestasMultiple.mapKeys { it.key.substringAfter("root-") }

    fun recorrer(lista: List<ComponenteFormulario>, path: String) {
        lista.forEachIndexed { index, componente ->
            val key = "$path-$index"
            when (componente) {
                is SeccionFormulario -> recorrer(componente.elements, "$key-sec")
                is TablaFormulario -> recorrer(componente.elements, "$key-tab")
                is PreguntaSeleccionUnicaFormulario -> {
                    val correct = componente.correct
                    if (correct != null && correct >= 0) {
                        total += 1
                        if (respuestasUnica[key] == correct || respuestasUnicaNormalizadas[key] == correct) {
                            correctas += 1
                        }
                    }
                }

                is PreguntaDesplegableFormulario -> {
                    val correct = componente.correct
                    if (correct != null && correct >= 0) {
                        total += 1
                        if (respuestasDrop[key] == correct || respuestasDropNormalizadas[key] == correct) {
                            correctas += 1
                        }
                    }
                }

                is PreguntaSeleccionMultipleFormulario -> {
                    val correctSet = componente.correct.toSet()
                    if (correctSet.isNotEmpty()) {
                        total += 1
                        val resp = respuestasMultiple[key].orEmpty()
                        val respNorm = respuestasMultipleNormalizadas[key].orEmpty()
                        if (resp == correctSet || respNorm == correctSet) {
                            correctas += 1
                        }
                    }
                }

                else -> Unit
            }
        }
    }

    recorrer(componentes, "root")
    return ResultadoEnvio(totalEvaluables = total, correctas = correctas)
}

private fun fusionarEstilos(
    heredado: EstiloFormulario?,
    propio: EstiloFormulario?
): EstiloFormulario {
    return EstiloFormulario(
        colorTexto = propio?.colorTexto ?: heredado?.colorTexto,
        colorFondo = propio?.colorFondo ?: heredado?.colorFondo,
        familiaFuente = propio?.familiaFuente ?: heredado?.familiaFuente,
        tamanioTexto = propio?.tamanioTexto ?: heredado?.tamanioTexto,
        borde = propio?.borde ?: heredado?.borde
    )
}

private fun aplicarDimensiones(width: Double?, height: Double?): Modifier {
    var modifier: Modifier = Modifier
    width?.takeIf { it > 0 }?.let { modifier = modifier.width(max(1.0, it).dp) }
    height?.takeIf { it > 0 }?.let { modifier = modifier.height(max(1.0, it).dp) }
    return modifier
}

private fun aplicarEstilosContenedor(estilo: EstiloFormulario?): Modifier {
    var modifier: Modifier = Modifier

    colorDesdeCadena(estilo?.colorFondo)?.let { color ->
        modifier = modifier.background(color, RoundedCornerShape(8.dp))
    }

    bordeDesdeEstilo(estilo?.borde)?.let { (grosor, color, forma) ->
        modifier = modifier.border(grosor, color, forma)
    }

    return modifier
}

private fun textStyleDesdeEstilo(estilo: EstiloFormulario?): TextStyle {
    val colorTexto = colorDesdeCadena(estilo?.colorTexto) ?: Color.Black
    return TextStyle(
        color = colorTexto,
        fontSize = textSizeDesdeEstilo(estilo),
        fontFamily = when (estilo?.familiaFuente) {
            FamiliaFuenteFormulario.MONO -> FontFamily.Monospace
            FamiliaFuenteFormulario.SANS_SERIF -> FontFamily.SansSerif
            FamiliaFuenteFormulario.CURSIVE -> FontFamily.Cursive
            null -> FontFamily.Default
        }
    )
}

private fun textSizeDesdeEstilo(estilo: EstiloFormulario?): TextUnit {
    val valor = estilo?.tamanioTexto?.toFloat()?.takeIf { it > 0f } ?: 14f
    return valor.sp
}

private fun bordeDesdeEstilo(
    borde: BordeFormulario?
): Triple<androidx.compose.ui.unit.Dp, Color, androidx.compose.ui.graphics.Shape>? {
    borde ?: return null
    val grosor = borde.grosor.toFloat().coerceAtLeast(0.5f).dp
    val color = colorDesdeCadena(borde.color) ?: Color.DarkGray
    val shape = when (borde.tipo) {
        TipoBordeFormulario.LINE -> RoundedCornerShape(8.dp)
        TipoBordeFormulario.DOTTED -> RoundedCornerShape(8.dp)
        TipoBordeFormulario.DOUBLE -> RectangleShape
    }
    return Triple(grosor, color, shape)
}

private fun colorDesdeCadena(raw: String?): Color? {
    val value = raw?.trim().orEmpty()
    if (value.isBlank()) return null

    val colorBase = when (value.uppercase()) {
        "RED" -> Color.Red
        "BLUE" -> Color.Blue
        "GREEN" -> Color(0xFF2E7D32)
        "PURPLE" -> Color(0xFF7B1FA2)
        "SKY" -> Color(0xFF03A9F4)
        "YELLOW" -> Color(0xFFF9A825)
        "BLACK" -> Color.Black
        "WHITE" -> Color.White
        else -> null
    }
    if (colorBase != null) return colorBase

    if (value.startsWith("#")) {
        return try {
            Color(android.graphics.Color.parseColor(value))
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    val rgbRegex = Regex("^\\((\\d{1,3}),(\\d{1,3}),(\\d{1,3})\\)$")
    val hslRegex = Regex("^<(\\d{1,3}),(\\d{1,3}),(\\d{1,3})>$")

    rgbRegex.find(value)?.let { match ->
        val (r, g, b) = match.destructured
        return Color(
            red = r.toInt().coerceIn(0, 255),
            green = g.toInt().coerceIn(0, 255),
            blue = b.toInt().coerceIn(0, 255)
        )
    }

    hslRegex.find(value)?.let { match ->
        val (h, s, l) = match.destructured
        val hsv = FloatArray(3)
        hsv[0] = h.toFloat().coerceIn(0f, 360f)
        hsv[1] = (s.toFloat().coerceIn(0f, 100f) / 100f)
        hsv[2] = (l.toFloat().coerceIn(0f, 100f) / 100f)
        return Color(android.graphics.Color.HSVToColor(hsv))
    }

    return null
}
