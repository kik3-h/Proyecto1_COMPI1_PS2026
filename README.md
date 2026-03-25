# Proyecto1_COMPI1_PS2026
Proyecto 1 del curso de organizacion de lenguajes y compiladores 1 del primer semestre 2026

# PKM_FORMS_EH - Proyecto 1

**Organización de Lenguajes y Compiladores 1** **Centro Universitario de Occidente (CUNOC) - USAC** 
**Primer Semestre 2026**

---

##  Descripción del Proyecto
**PKM_FORMS_EH** es una aplicación móvil nativa para Android que implementa un compilador completo (Analizador Léxico, Sintáctico y Semántico) para un Lenguaje de Dominio Específico (DSL). Su objetivo es permitir a los usuarios escribir código en el dispositivo para diseñar, generar y responder formularios dinámicos e interactivos en tiempo real.

En lugar de usar interfaces de "arrastrar y soltar", el usuario programa la interfaz. El motor del compilador lee el código, genera un Árbol de Sintaxis Abstracta (AST) y renderiza los componentes visuales al instante utilizando Jetpack Compose.

##  Características Principales
* **Editor de Código Integrado:** Área de texto con numeración de líneas, control de posición del cursor y botones de acceso rápido (plantillas, inserción de colores).
* **Compilación en Tiempo Real:** Análisis léxico y sintáctico mediante JFlex y CUP. Incluye recuperación de errores (Modo Pánico) para mostrar un reporte detallado (línea, columna, lexema y descripción) sin que la aplicación colapse.
* **Renderizado Dinámico (UI):** Conversión del AST a componentes visuales nativos usando Jetpack Compose (Secciones, Tablas, Preguntas abiertas, de selección única/múltiple y desplegables).
* **Soporte de Estilos y Emojis:** Renderización de colores (HEX, RGB, HSL, Nombres base), bordes, tipografías y traducción de text-emojis (ej. `@[:smile:]` ➡️ 😊).
* **Integración con PokeAPI:** Soporte para la función especial `who_is_that_pokemon(NUMBER, min, max)`, la cual hace peticiones HTTP asíncronas para llenar preguntas desplegables con nombres reales de Pokémon.
* **Persistencia Local:** Guardado de archivos `.pkm` con metadatos utilizando Room Database (SQLite) y almacenamiento en la memoria interna del dispositivo.

##  Tecnologías y Herramientas Utilizadas
* **Lenguaje:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material Design 3)
* **Generadores de Analizadores:** JFlex (Léxico) y CUP (Sintáctico)
* **Base de Datos:** Room Database
* **Red:** Retrofit2 & Gson (Consumo de PokeAPI)
* **Asincronismo:** Kotlin Coroutines (Dispatchers.IO)

##  Instalación y Ejecución
1. Clona este repositorio:
   ```bash
   git clone https://github.com/kik3-h/Proyecto1_COMPI1_PS2026.git
