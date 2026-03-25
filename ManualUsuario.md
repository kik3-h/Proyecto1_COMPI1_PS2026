# Centro Universitario de Occidente - USAC | Primer Semestre 2026
## Documentación - Proyecto 1: PKM_FORMS_EH
## Organización de Lenguajes y Compiladores 1
### Ing. Moises Granados
**Nombre:** Enrique Alexander Tebalan Hernandez | **Carnet:** 202230026
---

## 1. Descripción de PKM_FORMS_EH

**PKM_FORMS_EH** es una aplicación Android que permite diseñar formularios a partir de código PKM (lenguaje del proyecto), procesar dicho código y renderizar automáticamente la interfaz resultante.

El flujo funcional para el usuario es:

1. Escribir código PKM en el editor.
2. Ejecutar análisis del contenido.
3. Revisar errores (si existen) con ubicación exacta.
4. Visualizar y contestar el formulario renderizado.
5. Guardar formularios para uso local posterior.

La aplicación incluye:

- Editor de código con resaltado de sintaxis.
- Módulo de análisis (léxico, sintáctico y semántico).
- Renderizador de componentes visuales.
- Persistencia local de formularios.
- Integración con PokeAPI para opciones dinámicas en preguntas.

---

## 2. Requisitos e Instalación

### 2.1 Requisitos mínimos

- Dispositivo con sistema operativo Android (API 26 o superior).
- Espacio de almacenamiento disponible para instalación del APK.
- Conexión a internet para funciones que consumen PokeAPI.

### 2.2 Permisos requeridos

| Permiso | Finalidad |
|---|---|
| `android.permission.INTERNET` | Permite consultas a PokeAPI para generar opciones dinámicas |

### 2.3 Procedimiento de instalación mediante APK

1. Obtener el archivo instalador `.apk` generado del proyecto.
2. Transferir el archivo al dispositivo Android.
3. Abrir el APK desde el explorador de archivos.
4. Autorizar la instalación de aplicaciones de origen desconocido si el sistema lo solicita.
5. Confirmar instalación y abrir la aplicación.

---

## 3. Guía de Uso de la Interfaz

### 3.1 Pantalla inicial

Al iniciar la aplicación se presentan dos accesos principales:

- **Abrir editor**: permite crear o modificar código PKM.
- **Ver formularios guardados localmente**: muestra formularios almacenados en el dispositivo.

### 3.2 Editor de código

El editor integra:

- área de escritura con tipografía monoespaciada,
- numeración de líneas,
- indicador de posición de cursor (línea y columna),
- resaltado de sintaxis para facilitar lectura.

#### 3.2.1 Función de los botones del editor

| Botón | Función principal | Resultado esperado |
|---|---|---|
| **Analizar** | Ejecuta análisis del código PKM | Si no hay errores, genera y abre el formulario renderizado |
| **Plantilla** | Inserta una plantilla base de código PKM | Acelera creación inicial del formulario |
| **Color** | Inserta un ejemplo de valor de color (`"#FFFFFF"`) | Facilita escribir estilos rápidamente |
| **Guardar** | Analiza, serializa y guarda formulario en almacenamiento local + base de datos | Registro exitoso del formulario para consultas posteriores |
| **Limpiar** | Elimina el contenido del editor | Reinicio del área de trabajo |

### 3.3 Reporte de errores

Cuando el análisis detecta inconsistencias, se presenta un cuadro de reporte con estructura tabular.

#### 3.3.1 Columnas del reporte de errores

| Columna | Significado |
|---|---|
| **Lexema** | Símbolo o fragmento donde ocurre el problema |
| **Línea** | Número de línea en el editor |
| **Columna** | Posición horizontal exacta del error |
| **Tipo** | Clasificación del error (`Léxico`, `Sintáctico`, `Semántico`) |
| **Descripción** | Explicación puntual del problema encontrado |

#### 3.3.2 Recomendación de lectura del reporte

1. Corregir primero errores léxicos, luego sintácticos y finalmente semánticos.
2. Priorizar los errores de líneas inferiores (primeros en aparecer), ya que uno puede causar efectos en cascada.
3. Reanalizar después de cada bloque de correcciones.

### 3.4 Renderizador de formularios

Cuando el código es válido, la pantalla de renderizado presenta los componentes definidos en PKM:

- Secciones (`SECTION`)
- Tablas (`TABLE`)
- Textos (`TEXT`)
- Preguntas abiertas
- Preguntas de selección única
- Preguntas de selección múltiple
- Preguntas desplegables

También se aplican estilos de color, fuente, borde y tamaño de texto cuando están definidos en el código.

#### 3.4.1 Interacción de usuario en el formulario renderizado

- **Pregunta abierta**: ingreso libre de texto.
- **Selección única**: elección de una opción.
- **Selección múltiple**: selección de varias opciones.
- **Desplegable**: selección desde lista expandible.

Al finalizar, el botón **Enviar formulario** muestra el resultado:

- puntuación (`correctas/total`) en preguntas evaluables, o
- confirmación de envío cuando no hay evaluación cuantitativa.

---

## 4. Observaciones de uso

- Utilizar **Plantilla** al iniciar para reducir errores de sintaxis.
- Ejecutar **Analizar** periódicamente durante la edición.
- Verificar el cuadro de errores antes de guardar.
- Guardar versiones funcionales de forma incremental.

