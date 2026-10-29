package com.luistorres.pokedexexplorer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.luistorres.pokedexexplorer.domain.model.Pokemon
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

private val PokeballRed = Color(0xFFE53935)
private val Ink = Color(0xFF202334)
private val SoftBackground = Color(0xFFF6F7FB)
private val TypeColors = mapOf(
    "grass" to Color(0xFF55B86A), "fire" to Color(0xFFFF8A50), "water" to Color(0xFF5797F4),
    "bug" to Color(0xFF91B83E), "normal" to Color(0xFF9AA0A6), "poison" to Color(0xFFAB70C6),
    "electric" to Color(0xFFF4C542), "ground" to Color(0xFFD0A05D), "fairy" to Color(0xFFEA91B7),
    "fighting" to Color(0xFFD96A5F), "psychic" to Color(0xFFED6B91), "rock" to Color(0xFFB9A779),
    "ghost" to Color(0xFF8270B8), "ice" to Color(0xFF72CFCB), "dragon" to Color(0xFF7770D8),
    "dark" to Color(0xFF69636C), "steel" to Color(0xFF8B9AA8), "flying" to Color(0xFF8CACE8)
)
private val Types = listOf("all", "grass", "fire", "water", "electric", "bug", "normal", "poison", "fairy", "dragon", "fighting", "psychic", "rock", "ghost", "ice", "dark", "steel", "flying", "ground")
private val SpanishTypes = mapOf(
    "grass" to "Planta", "fire" to "Fuego", "water" to "Agua", "electric" to "Eléctrico",
    "bug" to "Bicho", "normal" to "Normal", "poison" to "Veneno", "fairy" to "Hada",
    "dragon" to "Dragón", "fighting" to "Lucha", "psychic" to "Psíquico", "rock" to "Roca",
    "ghost" to "Fantasma", "ice" to "Hielo", "dark" to "Siniestro", "steel" to "Acero",
    "flying" to "Volador", "ground" to "Tierra"
)
private val SpanishStats = mapOf(
    "hp" to "PS", "attack" to "Ataque", "defense" to "Defensa",
    "special attack" to "Ataque especial", "special defense" to "Defensa especial", "speed" to "Velocidad"
)
private fun typeName(type: String) = SpanishTypes[type] ?: type.replaceFirstChar { it.titlecase(Locale.ROOT) }
private fun statName(stat: String) = SpanishStats[stat] ?: stat.replace('-', ' ').replaceFirstChar { it.titlecase(Locale.ROOT) }

@Composable
fun PokedexApp(vm: PokemonViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "pokedex") {
        composable("pokedex") {
            PokedexListScreen(state, vm) { pokemon -> navController.navigate("pokemon/${pokemon.id}") }
        }
        composable(
            route = "pokemon/{pokemonId}",
            arguments = listOf(navArgument("pokemonId") { type = NavType.IntType })
        ) { entry ->
            val pokemonId = entry.arguments?.getInt("pokemonId") ?: return@composable
            LaunchedEffect(pokemonId) { vm.loadPokemonDetail(pokemonId) }
            PokemonDetailScreen(
                pokemon = state.selectedPokemon?.takeIf { it.id == pokemonId },
                isLoading = state.isDetailLoading,
                error = state.detailError,
                isOnline = state.isOnline,
                onBack = { navController.popBackStack() },
                onRetry = { vm.loadPokemonDetail(pokemonId, retry = true) }
            )
        }
    }
}

@Composable
private fun PokedexListScreen(state: PokemonUiState, vm: PokemonViewModel, onOpenPokemon: (Pokemon) -> Unit) {
    val gridState = rememberLazyGridState()
    val displayedPokemon = state.pokemon
    LaunchedEffect(gridState, state.pokemon.size, state.hasNext, state.searchText, state.selectedType) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collectLatest { index ->
                if (state.hasNext && index >= state.pokemon.size - 5 && state.pokemon.isNotEmpty()) vm.loadNextPage()
            }
    }
    Scaffold(containerColor = SoftBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Header(isOnline = state.isOnline)
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.searchText,
                    onValueChange = vm::onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Busca por nombre o número…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = { IconButton(onClick = vm::searchExact) { Icon(Icons.Default.CatchingPokemon, contentDescription = "Buscar") } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { vm.searchExact() }),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                )
                Spacer(Modifier.height(12.dp))
                Text("Explora por tipo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Types) { type ->
                        val selected = if (type == "all") state.selectedType == null else state.selectedType == type
                        FilterChip(
                            selected = selected,
                            onClick = { vm.filterByType(if (type == "all") null else type) },
                            label = { Text(if (type == "all") "Todos" else typeName(type)) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(state.selectedType?.let { "Tipo: ${typeName(it)}" } ?: "Pokédex", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(Modifier.weight(1f))
                    Text("${state.totalCount} resultados", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator(color = PokeballRed)
                            Text("Cargando Pokémon…", color = Color.Gray)
                        }
                    }
                    state.error != null && state.pokemon.isEmpty() -> ErrorPanel(state.error!!, onRetry = { if (state.selectedType != null) vm.filterByType(state.selectedType) else if (state.searchText.isNotBlank()) vm.searchExact() else vm.loadFirstPage() })
                    displayedPokemon.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No se encontraron Pokémon.", color = Color.Gray) }
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2), state = gridState,
                        contentPadding = PaddingValues(bottom = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayedPokemon, key = { it.id }) { pokemon -> PokemonCard(pokemon) { onOpenPokemon(pokemon) } }
                        if (state.isLoadingMore) item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                        if (state.error != null) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(state.error!!, modifier = Modifier.weight(1f), color = PokeballRed)
                                    TextButton(onClick = vm::loadNextPage) { Text("Reintentar") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(isOnline: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().background(PokeballRed).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(46.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.CatchingPokemon, contentDescription = null, tint = PokeballRed, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("POKÉDEX", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Tu aventura comienza aquí", color = Color.White.copy(alpha = .88f), style = MaterialTheme.typography.bodySmall)
        }
        Surface(color = if (isOnline) Color(0xFFB9F6CA) else Color(0xFFFFCDD2), shape = RoundedCornerShape(50)) {
            Row(Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isOnline) Color(0xFF176B36) else Color(0xFF9F2020))
                Spacer(Modifier.width(4.dp))
                Text(if (isOnline) "En línea" else "Sin conexión", style = MaterialTheme.typography.labelSmall, color = if (isOnline) Color(0xFF176B36) else Color(0xFF9F2020), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PokemonCard(pokemon: Pokemon, onClick: () -> Unit) {
    val tint = TypeColors[pokemon.types.firstOrNull()] ?: Color(0xFFB7C4D8)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxWidth().background(tint.copy(alpha = .15f))) {
            Text("#${pokemon.id.toString().padStart(3, '0')}", modifier = Modifier.align(Alignment.TopEnd).padding(10.dp), color = Color.DarkGray.copy(alpha = .7f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            AsyncImage(model = pokemon.imageUrl, contentDescription = pokemon.name, modifier = Modifier.fillMaxWidth().height(135.dp).padding(top = 14.dp, start = 12.dp, end = 12.dp))
        }
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(pokemon.name.replaceFirstChar { it.titlecase(Locale.ROOT) }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                pokemon.types.take(2).forEach { type ->
                    Surface(color = (TypeColors[type] ?: Color.Gray).copy(alpha = .17f), shape = RoundedCornerShape(7.dp)) {
                        Text(typeName(type), modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), color = TypeColors[type] ?: Color.DarkGray, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorPanel(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("¡Ups!", style = MaterialTheme.typography.headlineMedium, color = PokeballRed, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, textAlign = TextAlign.Center, color = Ink)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = PokeballRed)) { Text("Intentar de nuevo") }
    }
}

@Composable
private fun PokemonDetailScreen(
    pokemon: Pokemon?,
    isLoading: Boolean,
    error: String?,
    isOnline: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(SoftBackground)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().background(PokeballRed).padding(end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    pokemon?.name?.replaceFirstChar { it.titlecase(Locale.ROOT) } ?: "Detalle del Pokémon",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("Información completa", color = Color.White.copy(alpha = .88f), style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = if (isOnline) "En línea" else "Sin conexión",
                tint = Color.White
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isOnline) "En línea" else "Sin conexión", color = Color.White, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(8.dp))
        }

        when {
            pokemon == null && isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PokeballRed)
            }
            pokemon == null && error != null -> ErrorPanel(error, onRetry)
            pokemon == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay información para mostrar.", color = Color.Gray)
            }
            else -> {
                val details = requireNotNull(pokemon)
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val tint = TypeColors[details.types.firstOrNull()] ?: Color(0xFFB7C4D8)
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = .16f))) {
                        AsyncImage(
                            model = details.imageUrl,
                            contentDescription = details.name,
                            modifier = Modifier.fillMaxWidth().height(240.dp).padding(12.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("N.º ${details.id.toString().padStart(3, '0')}", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
                    Text(details.name.replaceFirstChar { it.titlecase(Locale.ROOT) }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        details.types.forEach { type ->
                            Surface(color = (TypeColors[type] ?: Color.Gray).copy(alpha = .18f), shape = RoundedCornerShape(10.dp)) {
                                Text(typeName(type), Modifier.padding(horizontal = 12.dp, vertical = 7.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))

                    DetailSection(title = "Descripción") {
                        Text(details.description ?: if (isLoading) "Cargando descripción…" else "Descripción en español no disponible.", color = Color.DarkGray)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailSection("Altura", Modifier.weight(1f)) {
                            Text("${details.height / 10.0} m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Ink)
                        }
                        DetailSection("Peso", Modifier.weight(1f)) {
                            Text("${details.weight / 10.0} kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Ink)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    DetailSection(title = "Habilidades") {
                        Text(details.abilities.joinToString { it.replaceFirstChar { c -> c.titlecase(Locale.ROOT) } }, color = Color.DarkGray)
                    }
                    Spacer(Modifier.height(12.dp))
                    DetailSection(title = "Estadísticas base") {
                        details.stats.forEach { stat ->
                            Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(statName(stat.name), style = MaterialTheme.typography.bodyMedium, color = Ink)
                                    Text(stat.value.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Ink)
                                }
                                LinearProgressIndicator(
                                    progress = { (stat.value / 180f).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                                    color = PokeballRed,
                                    trackColor = Color.LightGray.copy(alpha = .35f)
                                )
                            }
                        }
                    }
                    if (details.isLegendary || details.isMythical) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (details.isLegendary) "Pokémon legendario ✨" else "Pokémon mítico ✨",
                            color = PokeballRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (error != null) {
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(error, modifier = Modifier.weight(1f), color = PokeballRed)
                            TextButton(onClick = onRetry) { Text("Reintentar") }
                        }
                    }
                    if (isLoading) {
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator(color = PokeballRed, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Ink)
            content()
        }
    }
}

