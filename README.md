# Explorador Pokédex

Aplicación Android desarrollada con Kotlin y Jetpack Compose para explorar Pokémon mediante la API pública [PokéAPI](https://pokeapi.co/). La interfaz está en español.

## Funcionalidades

- Catálogo con paginación por desplazamiento.
- Búsqueda dinámica por nombre o número mientras se escribe, con resultados de todo el catálogo.
- Filtros por los 18 tipos con el total de resultados de cada tipo.
- Ficha con descripción, ilustración, tipos, medidas, habilidades y estadísticas.
- Indicadores de conexión y mensajes de carga o error.
- Traducción al español de nombres de tipos, estadísticas y habilidades cuando la API ofrece esos datos.

## Tecnologías y arquitectura

- Kotlin, Jetpack Compose y Material 3.
- MVVM con capas de presentación, dominio y datos.
- Retrofit y Gson para consumir PokéAPI; Coroutines para las operaciones asíncronas.
- Coil para cargar ilustraciones.

```text
app/src/main/java/com/luistorres/pokedexexplorer/
├── data/        # Retrofit, modelos remotos, conectividad y repositorio
├── domain/      # Modelos de dominio y contrato del repositorio
├── presentation/# Pantallas Compose y PokemonViewModel
└── MainActivity.kt
docs/            # Notas de endpoints y arquitectura
```

## Requisitos

- Android Studio, JDK 17 y Android SDK Platform 35.
- Emulador o dispositivo con Android 7.0 (API 24) o posterior.
- Conexión a Internet para consultar PokéAPI y cargar ilustraciones.

## Abrir y compilar

Abre **la carpeta raíz del repositorio** en Android Studio, espera la sincronización de Gradle y ejecuta la configuración `app` en un emulador o dispositivo.

Con Gradle instalado también puedes compilar desde la raíz:

```bash
gradle :app:assembleDebug
```

El APK de depuración queda en `app/build/outputs/apk/debug/app-debug.apk`.

## API utilizada

Todas las peticiones son `GET` a `https://pokeapi.co/api/v2/`:

| Recurso | Uso |
| --- | --- |
| `/pokemon?limit=24&offset={offset}` | Catálogo y paginación |
| `/pokemon?limit=2000&offset=0` | Índice de nombres para la búsqueda dinámica |
| `/pokemon/{nameOrId}` | Información del Pokémon y búsqueda por identificador |
| `/type/{type}` | Pokémon y cantidad asociados a un tipo |
| `/pokemon-species/{id}` | Descripción y clasificación de especie |
| `/ability/{name}` | Nombre de habilidad en español |

## Documentación adicional

- [Endpoints y arquitectura](docs/ENDPOINTS_Y_ARQUITECTURA.md)
