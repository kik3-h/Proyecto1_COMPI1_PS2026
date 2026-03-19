import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val configuracionHerramientasCompilador by configurations.creating

val archivoLexerPkm = file("src/main/jflex/com/usac/pkmforms/compilador/lexer/LexerPkm.flex")
val archivoParserPkm = file("src/main/cup/com/usac/pkmforms/compilador/parser/ParserPkm.cup")

val directorioLexerGenerado = layout.buildDirectory.dir("generated/source/compilador/lexer")
val directorioParserGenerado = layout.buildDirectory.dir("generated/source/compilador/parser")

android {
    namespace = "com.usac.pkmforms"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.usac.pkmforms"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0-fase1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("main") {
            java.srcDir(directorioLexerGenerado)
            java.srcDir(directorioParserGenerado)
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

val generarLexerPkm by tasks.registering(JavaExec::class) {
    group = "compilador"
    description = "Genera el analizador léxico con JFlex para PKM_FORMS"

    classpath = configuracionHerramientasCompilador
    mainClass.set("jflex.Main")

    if (archivoLexerPkm.exists()) {
        args(
            "-d",
            directorioLexerGenerado.get().asFile.absolutePath,
            archivoLexerPkm.absolutePath
        )
    }

    inputs.file(archivoLexerPkm)
    outputs.dir(directorioLexerGenerado)
    onlyIf { archivoLexerPkm.exists() }
}

val generarParserPkm by tasks.registering(JavaExec::class) {
    group = "compilador"
    description = "Genera el analizador sintáctico con CUP para PKM_FORMS"

    classpath = configuracionHerramientasCompilador
    mainClass.set("java_cup.Main")

    if (archivoParserPkm.exists()) {
        args(
            "-destdir",
            directorioParserGenerado.get().asFile.absolutePath,
            "-parser",
            "ParserPkm",
            "-symbols",
            "SimbolosSintacticosPkm",
            archivoParserPkm.absolutePath
        )
    }

    inputs.file(archivoParserPkm)
    outputs.dir(directorioParserGenerado)
    onlyIf { archivoParserPkm.exists() }
}

val generarAnalizadoresPkm by tasks.registering {
    group = "compilador"
    description = "Genera lexer y parser de PKM_FORMS con JFlex + CUP"
    dependsOn(generarLexerPkm, generarParserPkm)
}

tasks.named("preBuild").configure {
    dependsOn(generarAnalizadoresPkm)
}

dependencies {
    val versionRoom = "2.7.0"
    val versionRetrofit = "2.11.0"
    val versionMoshi = "1.15.1"
    val versionGson = "2.11.0"
    val versionComposeBom = "2024.12.01"

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.0")

    implementation(platform("androidx.compose:compose-bom:$versionComposeBom"))
    androidTestImplementation(platform("androidx.compose:compose-bom:$versionComposeBom"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    implementation("androidx.room:room-runtime:$versionRoom")
    implementation("androidx.room:room-ktx:$versionRoom")
    ksp("androidx.room:room-compiler:$versionRoom")

    implementation("com.squareup.retrofit2:retrofit:$versionRetrofit")
    implementation("com.squareup.retrofit2:converter-moshi:$versionRetrofit")
    implementation("com.squareup.retrofit2:converter-gson:$versionRetrofit")

    implementation("com.squareup.moshi:moshi-kotlin:$versionMoshi")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$versionMoshi")
    implementation("com.google.code.gson:gson:$versionGson")

    implementation(files("libs/java-cup-11b-runtime.jar"))

    add(configuracionHerramientasCompilador.name, files("libs/jflex-full-1.9.1.jar"))
    add(configuracionHerramientasCompilador.name, files("libs/java-cup-11b.jar"))

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
