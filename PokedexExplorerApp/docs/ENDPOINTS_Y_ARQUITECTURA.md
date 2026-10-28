# Documentación técnica — Pokédex Explorer

## Objetivo
Construir una aplicación móvil Android que permita consultar, buscar y explorar Pokémon usando una sola API REST pública: PokéAPI v2.

## Endpoints
| Método | Endpoint | Uso |
|---|---|---|
| GET | `/pokemon?limit=20&offset=0` | Listado inicial y paginación |
| GET | `/pokemon/{nameOrId}` | Detalle por nombre o ID |
| GET | `/type/{type}` | Filtro por tipo |
| GET | `/pokemon-species/{idOrName}` | Datos complementarios de especie |

Los listados responden con `count`, `next`, `previous` y `results`. Los recursos individuales responden con objetos JSON que Retrofit/Gson transforma en modelos Kotlin.

## MVVM
- UI: Jetpack Compose dibuja pantallas y componentes.
- ViewModel: expone `StateFlow` y administra las solicitudes, búsquedas y estados.
- Repository: abstrae las consultas y transforma DTOs a modelos de dominio.
- Remote data source: Retrofit define y ejecuta las solicitudes HTTP.
- Domain: contiene los modelos usados por la aplicación y el contrato del repositorio.

## Paginación
Se solicitan 20 elementos por página. El desplazamiento `offset` aumenta con el número de elementos recibidos. La UI observa el final de la lista y solicita la siguiente página.

## Estado de red
Android `ConnectivityManager` y `NetworkCapabilities` determinan si existe una conexión validada. Un error de API se presenta de forma independiente del estado de conectividad.

## IA
ChatGPT se utilizó para apoyar el diseño técnico, generar código inicial, explicar MVVM/Retrofit y estructurar la documentación. La validación, ejecución y captura de evidencias deben realizarse en Android Studio.
