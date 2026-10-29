# Informe del proyecto: Explorador Pokédex

## Descripción de la aplicación

Explorador Pokédex es una aplicación Android desarrollada en Kotlin y Jetpack Compose. Permite consultar Pokémon mediante la API pública PokéAPI, recorrer el catálogo con paginación por desplazamiento, buscar por nombre o número de Pokédex, filtrar por tipo y abrir una pantalla de detalle. La interfaz presenta la información en español e indica el estado de conexión.

La aplicación organiza el código en presentación, dominio y datos. La interfaz Compose se comunica con `PokemonViewModel`, que administra el estado y las operaciones asíncronas; el contrato `PokemonRepository` separa el dominio de `PokemonRepositoryImpl`, que consulta los servicios REST con Retrofit y convierte las respuestas JSON en modelos Kotlin.

## Descripción de la API

Se seleccionó PokéAPI, una API REST pública de datos de Pokémon, disponible en `https://pokeapi.co/api/v2/`. La aplicación realiza solicitudes HTTP **GET** mediante Retrofit; no requiere cuenta ni clave de acceso. Los datos de respuesta son objetos JSON, convertidos por Gson a modelos Kotlin. Para presentar una ficha, el repositorio combina la respuesta principal del Pokémon con datos de especie y nombres localizados de sus habilidades.

| Método | Endpoint | Parámetros | Uso y respuesta |
| --- | --- | --- | --- |
| `GET` | `/pokemon?limit={limit}&offset={offset}` | `limit` indica la cantidad de resultados (24 por defecto); `offset` indica desde qué elemento continuar (0 al comenzar). | Devuelve un objeto JSON con el total (`count`), enlaces de paginación (`next`, `previous`) y una lista (`results`) con nombres y URL de recursos. Se usa para el catálogo y el scroll infinito. |
| `GET` | `/pokemon/{nameOrId}` | `nameOrId` es el nombre del Pokémon o su número de Pokédex, enviado en la ruta. | Devuelve un objeto JSON con identificador, nombre, sprites, tipos, habilidades, altura, peso y estadísticas base. Se usa para la búsqueda y para obtener los datos detallados de cada elemento. |
| `GET` | `/type/{type}` | `type` es el identificador del tipo, por ejemplo `fire`, `water` o `grass`. | Devuelve un objeto JSON con la colección `pokemon` asociada al tipo. La aplicación usa sus nombres para solicitar los datos de cada Pokémon y mostrar los resultados del filtro. |
| `GET` | `/pokemon-species/{id}` | `id` es el número de Pokédex del Pokémon. | Devuelve un objeto JSON de especie con indicadores como `is_legendary` e `is_mythical`, y entradas de texto (`flavor_text_entries`) en distintos idiomas. Se selecciona la descripción cuya lengua es español (`es`). |
| `GET` | `/ability/{name}` | `name` es el identificador de la habilidad, enviado en la ruta. | Devuelve un objeto JSON con nombres localizados (`names`). La app busca el nombre en español para mostrarlo en la ficha. |

En todas las rutas se reciben respuestas JSON. Retrofit y Gson las convierten en DTO de Kotlin; el repositorio mapea los datos necesarios al modelo de dominio `Pokemon` y a los modelos de especie y estadísticas. La paginación utiliza `next` para determinar si hay más resultados.

## Funcionalidades implementadas

- Catálogo en cuadrícula y carga de páginas al acercarse al final de la lista.
- Búsqueda exacta por nombre o número mediante `/pokemon/{nameOrId}`.
- Filtros por tipo mediante `/type/{type}`.
- Navegación a una pantalla independiente de detalle.
- Ficha con ilustración, descripción en español cuando está disponible, tipos, altura, peso, habilidades y estadísticas base.
- Indicadores de Pokémon legendarios y míticos, obtenidos desde el endpoint de especie.
- Indicador de conexión, estados de carga, mensajes de error y opción para volver a intentar.

## Uso de inteligencia artificial

Durante el desarrollo se utilizó **ChatGPT con el agente de programación Codex** como apoyo conversacional y de edición de código. Se le proporcionaron los requisitos de la actividad y el contexto del proyecto para revisar la estructura existente, contrastar las funciones con la rúbrica y ayudar a implementar la navegación a una pantalla de detalle, el manejo del estado y los mensajes de error. También se empleó para revisar la compilación, resolver errores detectados por Gradle y preparar documentación en español.

El agente trabajó sobre los archivos del proyecto y recibió retroalimentación del compilador durante el flujo de desarrollo. Para validar la aplicación se utilizaron herramientas de línea de comandos del entorno: Gradle para generar el APK de depuración y ADB para instalarlo, abrirlo y comprobar en el emulador el catálogo y la navegación al detalle. La IA se utilizó como asistente; las decisiones sobre requisitos y los resultados generados se revisaron en el proyecto y se contrastaron mediante compilación y ejecución.

No se utilizaron otros agentes autónomos ni servicios de IA externos para obtener los datos de Pokémon: el contenido de la aplicación proviene de PokéAPI.

## Validación

La compilación de depuración se completó correctamente con `:app:assembleDebug`. Después se instaló la aplicación en el emulador Android. Se comprobó visualmente que el catálogo cargara datos de PokéAPI y que al seleccionar un Pokémon se mostrara una ficha independiente con su descripción y atributos.

## Tecnologías principales

- Kotlin y Jetpack Compose con Material 3.
- Retrofit y Gson para solicitudes HTTP y conversión de JSON.
- Kotlin Coroutines para operaciones asíncronas.
- Arquitectura por capas con presentación, dominio y datos.
- Coil para cargar las ilustraciones.
- PokéAPI como fuente de datos.