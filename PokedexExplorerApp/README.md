# Pokédex Explorer App

Aplicación móvil Android desarrollada en Kotlin y Jetpack Compose que consume exclusivamente la API REST pública **PokeAPI**. La arquitectura separa presentación, dominio y datos siguiendo MVVM.

## Funcionalidades
- Listado paginado de Pokémon con scroll infinito.
- Búsqueda exacta por nombre o número.
- Filtro por tipo consumiendo `/type/{type}`.
- Detalle con imagen, tipos, altura, peso, habilidades y estadísticas.
- Consulta de especie para identificar Pokémon legendarios y míticos.
- Indicador de conectividad online/offline.
- Estados de carga y mensajes de error.

## Endpoints utilizados
| Método | Endpoint | Uso |
|---|---|---|
| GET | `/pokemon?limit=24&offset=0` | Listado y paginación |
| GET | `/pokemon/{nameOrId}` | Búsqueda exacta y detalle del Pokémon |
| GET | `/type/{type}` | Filtro por tipo |
| GET | `/pokemon-species/{id}` | Datos de especie (legendario/mítico) |

Base URL: `https://pokeapi.co/api/v2/`

No se utilizan otras APIs, backend propio ni base de datos externa.

## Arquitectura
- `presentation`: interfaz Compose y `PokemonViewModel`.
- `domain`: modelo `Pokemon` y contrato de repositorio.
- `data/remote`: Retrofit y modelos de respuesta JSON.
- `data/repository`: implementación de acceso a la API y mapeo DTO a dominio.
- `data/network`: observador de conectividad Android.

## Requisitos
- Android Studio reciente.
- JDK 17.
- Android SDK 35.
- Conexión a Internet para descargar dependencias y consultar PokeAPI.

## Cómo abrir
1. Descomprime el ZIP.
2. En Android Studio, selecciona **Open** y elige la carpeta `PokedexExplorerApp`.
3. Espera la sincronización de Gradle y acepta la instalación de componentes del SDK si se solicita.
4. Ejecuta la app en un emulador o dispositivo Android (Android 7.0/API 24 o superior).

## Nota sobre el estado del proyecto
Este paquete contiene los archivos fuente y la configuración de Gradle. La compilación final debe verificarse en Android Studio con el SDK local disponible. No se incluye una APK precompilada.

## Referencias
- PokeAPI: https://pokeapi.co/docs/v2
- Android ViewModel: https://developer.android.com/topic/libraries/architecture/viewmodel
- Compose UI architecture: https://developer.android.com/develop/ui/compose/architecture
