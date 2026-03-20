package com.usac.pkmforms.ui.pantallas.editor_codigo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.usac.pkmforms.compilador.ast.nodos.NodoAST
import com.usac.pkmforms.compilador.interprete.InterpretePkm
import com.usac.pkmforms.compilador.lexer.LexerPkm
import com.usac.pkmforms.compilador.parser.ParserPkm
import com.usac.pkmforms.data.base_datos.AppDatabase
import com.usac.pkmforms.data.base_datos.entidades.FormularioGuardadoEntity
import com.usac.pkmforms.data.repositorios.FormularioRepository
import com.usac.pkmforms.domain.modelo.formulario.ComponenteFormulario
import com.usac.pkmforms.domain.modelo.formulario.FormularioCompilado
import com.usac.pkmforms.utilidades.GestorArchivosLocal
import com.usac.pkmforms.utilidades.manejador_errores.ErrorAnalisis
import com.usac.pkmforms.utilidades.serializador_pkm.SerializadorPkm
import java.io.StringReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java_cup.runtime.Symbol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorPantalla(
    navController: NavController,
    onFormularioGenerado: (List<ComponenteFormulario>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var codigoFuente by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var erroresAnalisis by remember { mutableStateOf<List<ErrorAnalisis>>(emptyList()) }
    var mostrarErrores by remember { mutableStateOf(false) }

    val estadoSnackBar = remember { SnackbarHostState() }
    val alcanceCoroutine = rememberCoroutineScope()
    val formularioRepository = remember(context) {
        FormularioRepository(AppDatabase.obtenerInstancia(context).formularioDao())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("PKM_FORMS_EH - Editor") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = estadoSnackBar) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val resultado = ejecutarAnalisis(codigoFuente.text)
                        if (resultado.errores.isNotEmpty()) {
                            erroresAnalisis = resultado.errores
                            mostrarErrores = true
                            return@Button
                        }

                        onFormularioGenerado(resultado.componentes)
                        alcanceCoroutine.launch {
                            estadoSnackBar.showSnackbar("Análisis exitoso. Formulario generado.")
                        }
                    }
                ) {
                    Text("Analizar")
                }

                OutlinedButton(
                    onClick = {
                        val codigoPlantilla = plantillaBasePkm()
                        val nuevoContenido = if (codigoFuente.text.isBlank()) {
                            codigoPlantilla
                        } else {
                            "${codigoFuente.text.trimEnd()}\n\n$codigoPlantilla"
                        }
                        codigoFuente = TextFieldValue(
                            text = nuevoContenido,
                            selection = TextRange(nuevoContenido.length)
                        )
                    }
                ) {
                    Text("Insertar Plantilla")
                }

                OutlinedButton(
                    onClick = {
                        val nuevoContenido = if (codigoFuente.text.isBlank()) {
                            "\"#FFFFFF\""
                        } else {
                            "${codigoFuente.text.trimEnd()}\n\"#FFFFFF\""
                        }
                        codigoFuente = TextFieldValue(
                            text = nuevoContenido,
                            selection = TextRange(nuevoContenido.length)
                        )
                    }
                ) {
                    Text("Insertar Color")
                }

                OutlinedButton(
                    onClick = {
                        val resultado = ejecutarAnalisis(codigoFuente.text)
                        if (resultado.errores.isNotEmpty()) {
                            erroresAnalisis = resultado.errores
                            mostrarErrores = true
                            return@OutlinedButton
                        }
                        if (resultado.componentes.isEmpty()) {
                            alcanceCoroutine.launch {
                                estadoSnackBar.showSnackbar("No hay componentes válidos para guardar.")
                            }
                            return@OutlinedButton
                        }

                        alcanceCoroutine.launch(Dispatchers.IO) {
                            try {
                                val fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                                val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                                val nombreArchivoBase = "formulario_$stamp"

                                val metadatos = linkedMapOf(
                                    "Author" to "Usuario",
                                    "Fecha" to fecha,
                                    "Hora" to hora,
                                    "Description" to "Formulario guardado desde editor"
                                )

                                val formularioCompilado = FormularioCompilado(
                                    componentes = resultado.componentes
                                )
                                val contenidoPkm = SerializadorPkm.generarStringPkm(
                                    formulario = formularioCompilado,
                                    metadatos = metadatos
                                )
                                val rutaArchivo = GestorArchivosLocal.guardarArchivoPkm(
                                    context = context,
                                    nombreArchivo = nombreArchivoBase,
                                    contenido = contenidoPkm
                                )

                                formularioRepository.insertar(
                                    FormularioGuardadoEntity(
                                        nombre = "$nombreArchivoBase.pkm",
                                        fechaCreacion = "$fecha $hora",
                                        rutaArchivo = rutaArchivo
                                    )
                                )

                                launch(Dispatchers.Main) {
                                    estadoSnackBar.showSnackbar("Guardado exitoso: $nombreArchivoBase.pkm")
                                }
                            } catch (exception: Exception) {
                                launch(Dispatchers.Main) {
                                    estadoSnackBar.showSnackbar("Error al guardar: ${exception.message.orEmpty()}")
                                }
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }

                OutlinedButton(
                    onClick = {
                        codigoFuente = TextFieldValue("")
                    }
                ) {
                    Text("Limpiar")
                }
            }

            val scrollSincronizado = rememberScrollState()
            val totalLineas = maxOf(1, codigoFuente.text.count { it == '\n' } + 1)
            val estiloCodigo = TextStyle(
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF151515), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF3A3A3A), RoundedCornerShape(12.dp))
                    .padding(10.dp)
                    .verticalScroll(scrollSincronizado)
            ) {
                Column(
                    modifier = Modifier
                        .width(52.dp)
                        .background(Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    for (linea in 1..totalLineas) {
                        Text(
                            text = linea.toString(),
                            color = Color(0xFF9E9E9E),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .fillMaxWidth()
                ) {
                    if (codigoFuente.text.isBlank()) {
                        Text(
                            text = "Escribe aquí el código PKM_FORMS...",
                            color = Color(0xFF9E9E9E),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = codigoFuente,
                        onValueChange = { codigoFuente = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = estiloCodigo,
                        cursorBrush = SolidColor(Color.White),
                        visualTransformation = remember { ResaltadorSintaxisPkm() }
                    )
                }
            }

            val (lineaCursor, colCursor) = obtenerPosicionCursor(codigoFuente)
            Text(
                text = "Línea: $lineaCursor, Col: $colCursor",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFBDBDBD),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (mostrarErrores) {
        DialogoErroresAnalisis(
            errores = erroresAnalisis,
            onCerrar = { mostrarErrores = false }
        )
    }
}

private fun obtenerPosicionCursor(valor: TextFieldValue): Pair<Int, Int> {
    val texto = valor.text
    val indiceCursor = valor.selection.start.coerceIn(0, texto.length)
    var linea = 1
    var ultimoSalto = -1

    for (indice in 0 until indiceCursor) {
        if (texto[indice] == '\n') {
            linea += 1
            ultimoSalto = indice
        }
    }

    val columna = indiceCursor - ultimoSalto
    return linea to columna
}

private data class ResultadoAnalisisUi(
    val componentes: List<ComponenteFormulario>,
    val errores: List<ErrorAnalisis>
)

private fun ejecutarAnalisis(codigoFuente: String): ResultadoAnalisisUi {
    LexerPkm.limpiarErroresLexicos()
    ParserPkm.listaErroresSintacticos.clear()

    val lexer = LexerPkm(StringReader(codigoFuente))
    val parser = ParserPkm(lexer)

    val simboloRaiz: Symbol = parser.parse()

    val erroresLexicos = LexerPkm.listaErroresLexicos.toList()
    val erroresSintacticos = ParserPkm.listaErroresSintacticos.toList()
    if (erroresLexicos.isNotEmpty() || erroresSintacticos.isNotEmpty()) {
        return ResultadoAnalisisUi(
            componentes = emptyList(),
            errores = erroresLexicos + erroresSintacticos
        )
    }

    val nodosAst = (simboloRaiz.value as? List<*>)?.mapNotNull { it as? NodoAST }.orEmpty()
    if (codigoFuente.isNotBlank() && nodosAst.isEmpty()) {
        return ResultadoAnalisisUi(
            componentes = emptyList(),
            errores = listOf(
                ErrorAnalisis(
                    lexema = "EOF",
                    linea = 0,
                    columna = 0,
                    tipo = "Sintáctico",
                    descripcion = "No se logró construir el AST del código ingresado."
                )
            )
        )
    }

    val resultadoInterpretacion = InterpretePkm(nodosAst).interpretar(
        erroresLexicos = erroresLexicos,
        erroresSintacticos = erroresSintacticos
    )

    if (resultadoInterpretacion.erroresSemanticos.isNotEmpty()) {
        return ResultadoAnalisisUi(
            componentes = emptyList(),
            errores = resultadoInterpretacion.erroresSemanticos
        )
    }

    return ResultadoAnalisisUi(
        componentes = resultadoInterpretacion.componentes,
        errores = emptyList()
    )
}

@Composable
private fun DialogoErroresAnalisis(
    errores: List<ErrorAnalisis>,
    onCerrar: () -> Unit
) {
    val scrollHorizontal = rememberScrollState()
    val scrollVertical = rememberScrollState()

    AlertDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text("Cerrar")
            }
        },
        title = {
            Text("Reporte de Errores (${errores.size})")
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(scrollVertical)
                    .horizontalScroll(scrollHorizontal)
            ) {
                Column(
                    modifier = Modifier.widthIn(min = 760.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilaCabeceraErrores()
                    errores.forEach { error ->
                        FilaError(error = error)
                    }
                }
            }
        }
    )
}

@Composable
private fun FilaCabeceraErrores() {
    FilaTabla(
        lexema = "Lexema",
        linea = "Línea",
        columna = "Columna",
        tipo = "Tipo",
        descripcion = "Descripción",
        estilo = MaterialTheme.typography.labelMedium.copy(color = Color.White)
    )
}

@Composable
private fun FilaError(error: ErrorAnalisis) {
    FilaTabla(
        lexema = error.lexema,
        linea = error.linea.toString(),
        columna = error.columna.toString(),
        tipo = error.tipo,
        descripcion = error.descripcion,
        estilo = MaterialTheme.typography.bodySmall.copy(color = Color.White)
    )
}

@Composable
private fun FilaTabla(
    lexema: String,
    linea: String,
    columna: String,
    tipo: String,
    descripcion: String,
    estilo: TextStyle
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2A2A), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = lexema, modifier = Modifier.width(120.dp), style = estilo)
        Text(text = linea, modifier = Modifier.width(70.dp), style = estilo)
        Text(text = columna, modifier = Modifier.width(70.dp), style = estilo)
        Text(text = tipo, modifier = Modifier.width(100.dp), style = estilo)
        Text(text = descripcion, modifier = Modifier.weight(1f), style = estilo)
    }
}

private data class ReglaColor(
    val regex: Regex,
    val color: Color,
    val prioridad: Int
)

private class ResaltadorSintaxisPkm : VisualTransformation {
    private val colorOperadores = Color(0xFF4CAF50)
    private val colorVariables = Color.White
    private val colorStrings = Color(0xFFFF9800)
    private val colorNumeros = Color(0xFF4FC3F7)
    private val colorReservadas = Color(0xFFAB47BC)
    private val colorLlaves = Color(0xFF42A5F5)
    private val colorEmojis = Color(0xFFFFEB3B)

    private val reglas: List<ReglaColor> = listOf(
        ReglaColor(
            regex = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b"),
            color = colorVariables,
            prioridad = 1
        ),
        ReglaColor(
            regex = Regex("(\\|\\||&&|==|!!|>=|<=|\\.\\.|\\+|-|\\*|/|\\^|%|>|<|=|~|!)"),
            color = colorOperadores,
            prioridad = 2
        ),
        ReglaColor(
            regex = Regex("\\b\\d+(?:\\.\\d+)?\\b"),
            color = colorNumeros,
            prioridad = 3
        ),
        ReglaColor(
            regex = Regex(
                "\\b(?:number|string|special|SECTION|TABLE|TEXT|OPEN_QUESTION|DROP_QUESTION|SELECT_QUESTION|MULTIPLE_QUESTION|IF|ELSE|WHILE|DO|FOR|in|draw|who_is_that_pokemon|width|height|pointX|pointY|orientation|VERTICAL|HORIZONTAL|elements|styles|content|label|options|correct|color|background|font|family|text|size|border|MONO|SANS_SERIF|CURSIVE|LINE|DOTTED|DOUBLE)\\b"
            ),
            color = colorReservadas,
            prioridad = 4
        ),
        ReglaColor(
            regex = Regex("[\\{\\}\\[\\]\\(\\)]"),
            color = colorLlaves,
            prioridad = 5
        ),
        ReglaColor(
            regex = Regex("\"([^\"\\\\]|\\\\.)*\""),
            color = colorStrings,
            prioridad = 6
        ),
        ReglaColor(
            regex = Regex("@\\[[^\\]\\n]+\\]"),
            color = colorEmojis,
            prioridad = 7
        )
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val contenido = text.text
        val resaltado = AnnotatedString.Builder(contenido).apply {
            if (contenido.isNotEmpty()) {
                addStyle(SpanStyle(color = Color.White), 0, contenido.length)
            }

            reglas.sortedBy { it.prioridad }.forEach { regla ->
                regla.regex.findAll(contenido).forEach { coincidencia ->
                    val inicio = coincidencia.range.first
                    val fin = coincidencia.range.last + 1
                    if (inicio in 0 until fin && fin <= contenido.length) {
                        addStyle(SpanStyle(color = regla.color), inicio, fin)
                    }
                }
            }
        }.toAnnotatedString()

        return TransformedText(
            text = resaltado,
            offsetMapping = OffsetMapping.Identity
        )
    }
}

private fun plantillaBasePkm(): String {
    return """
SECTION [
    width: 360,
    height: 640,
    pointX: 0,
    pointY: 0,
    orientation: VERTICAL,
    elements: {
        TEXT [
            content: "Formulario @[:star:2:]"
        ],
        OPEN_QUESTION [
            label: "¿Cuál es tu nombre?"
        ],
        SELECT_QUESTION [
            label: "Elige una opción",
            options: {"Primera", "Segunda"},
            correct: 0
        ],
        MULTIPLE_QUESTION [
            label: "Selecciona varias",
            options: {"A", "B", "C"},
            correct: {0, 2}
        ],
        DROP_QUESTION [
            label: "Selecciona en desplegable",
            options: {"Bulbasaur", "Charmander", "Squirtle"},
            correct: 1
        ]
    }
]
""".trimIndent()
}
