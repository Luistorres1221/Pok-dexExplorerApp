# Explorador Pokédex

Aplicación Android para explorar Pokémon, buscar por nombre o número y consultar información detallada. La interfaz está en español y obtiene los datos de la API pública [PokéAPI](https://pokeapi.co/).

[Repositorio en GitHub](https://github.com/Luistorres1221/Pok-dexExplorerApp)

## Funcionalidades

- Catálogo paginado con carga de más Pokémon al desplazarse.
- Búsqueda exacta por nombre o número de Pokédex.
- Filtros por tipo.
- Ficha con descripción en español, ilustración, tipos, altura, peso, habilidades y estadísticas base.
- Indicadores para Pokémon legendarios y míticos.
- Estado de conexión, indicador de carga y mensajes de error en español.
- Traducción al español de los nombres de tipos, estadísticas y habilidades cuando PokéAPI ofrece esa traducción.

## Tecnologías

- Kotlin
- Jetpack Compose y Material 3
- Arquitectura MVVM, separada en presentación, dominio y datos
- Retrofit y Gson para las solicitudes HTTP
- Kotlin Coroutines para las operaciones asíncronas
- Coil para cargar ilustraciones
- PokéAPI como fuente de datos

## Requisitos

- Android Studio con soporte para proyectos Kotlin y Jetpack Compose.
- JDK 17.
- Android SDK Platform 35.
- Emulador o dispositivo con Android 7.0 (API 24) o posterior.
- Conexión a Internet para consultar PokéAPI y cargar las ilustraciones.

## Abrir y ejecutar

1. Clona o descarga este repositorio.
2. En Android Studio, selecciona **Open** y abre la carpeta `PokedexExplorerApp` que contiene `settings.gradle.kts`.
3. Espera a que termine la sincronización de Gradle y permite la instalación de los componentes del SDK que Android Studio solicite.
4. Inicia un emulador o conecta un dispositivo Android.
5. Selecciona la configuración `app` y pulsa **Run**.

La primera carga consulta PokéAPI para obtener los datos del catálogo y puede tardar unos segundos. Mientras tanto, la aplicación muestra el mensaje **Cargando Pokémon…**.

## Compilar desde la terminal

El repositorio no incluye Gradle Wrapper. Si Gradle está instalado y configurado en el equipo, ejecuta desde la carpeta del proyecto:

```bash
gradle :app:assembleDebug
```

El APK de depuración se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

También puedes compilar e instalar desde Android Studio con **Run**.

## API

La aplicación utiliza la base URL `https://pokeapi.co/api/v2/` y estos recursos:

| Método | Recurso | Uso |
| --- | --- | --- |
| `GET` | `/pokemon?limit=24&offset={offset}` | Listado y paginación |
| `GET` | `/pokemon/{nameOrId}` | Datos del Pokémon y búsqueda exacta |
| `GET` | `/type/{type}` | Pokémon de un tipo seleccionado |
| `GET` | `/pokemon-species/{id}` | Descripción en español y datos de especie (legendario/mítico) |
| `GET` | `/ability/{name}` | Nombre de la habilidad en español |

No requiere cuenta, clave de API, servidor propio ni base de datos externa.

## Estructura del proyecto

```text
app/src/main/java/com/luistorres/pokedexexplorer/
├── data/
│   ├── network/       # Observación de conectividad
│   ├── remote/        # Servicios Retrofit y modelos JSON
│   └── repository/    # Consultas a PokéAPI y mapeo de datos
├── domain/
│   ├── model/         # Modelos de dominio
│   └── repository/    # Contrato del repositorio
├── presentation/     # Interfaz Compose y PokemonViewModel
└── MainActivity.kt
```

## Referencias

- [Documentación de PokéAPI](https://pokeapi.co/docs/v2)
- [Arquitectura de apps Android](https://developer.android.com/topic/architecture)
- [Jetpack Compose](https://developer.android.com/develop/ui/compose)
