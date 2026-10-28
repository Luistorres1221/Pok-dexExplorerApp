package com.luistorres.pokedexexplorer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
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
import androidx.lifecycle.viewmodel.compose.viewModel
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
private val Types = listOf("all", "grass", "fire", "water", "electric", "bug", "normal", "poison", "fairy", "dragon")

@Composable
fun PokedexApp(vm: PokemonViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    val displayedPokemon = remember(state.pokemon, state.searchText) {
        val query = state.searchText.trim().lowercase()
        if (query.isBlank()) state.pokemon else state.pokemon.filter {
            it.name.contains(query) || it.id.toString().contains(query)
        }
    }
    LaunchedEffect(gridState, state.pokemon.size, state.hasNext, state.searchText, state.selectedType) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collectLatest { index ->
                if (state.selectedType == null && state.searchText.isBlank() && state.hasNext && index >= state.pokemon.size - 5 && state.pokemon.isNotEmpty()) vm.loadNextPage()
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
                            label = { Text(if (type == "all") "Todos" else type.replaceFirstChar { it.titlecase(Locale.ROOT) }) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.selectedType != null) "Tipo: ${state.selectedType}" else "Pokédex", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(Modifier.weight(1f))
                    Text("${displayedPokemon.size} resultados", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PokeballRed) }
                    state.error != null && state.pokemon.isEmpty() -> ErrorPanel(state.error!!, onRetry = { if (state.selectedType != null) vm.filterByType(state.selectedType) else if (state.searchText.isNotBlank()) vm.searchExact() else vm.loadFirstPage() })
                    displayedPokemon.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No se encontraron Pokémon.", color = Color.Gray) }
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2), state = gridState,
                        contentPadding = PaddingValues(bottom = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayedPokemon, key = { it.id }) { pokemon -> PokemonCard(pokemon) { vm.openDetail(pokemon) } }
                        if (state.isLoadingMore) item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                    }
                }
            }
        }
    }
    state.selectedPokemon?.let { pokemon -> PokemonDetailDialog(pokemon, state.isDetailLoading) { vm.closeDetail() } }
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
                Text(if (isOnline) "Online" else "Offline", style = MaterialTheme.typography.labelSmall, color = if (isOnline) Color(0xFF176B36) else Color(0xFF9F2020), fontWeight = FontWeight.Bold)
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
                        Text(type.replaceFirstChar { it.titlecase(Locale.ROOT) }, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), color = TypeColors[type] ?: Color.DarkGray, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
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
private fun PokemonDetailDialog(pokemon: Pokemon, loadingSpecies: Boolean, onClose: () -> Unit) {
    val tint = TypeColors[pokemon.types.firstOrNull()] ?: Color(0xFFB7C4D8)
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = onClose) { Text("Cerrar", color = PokeballRed) } },
        title = { Text("${pokemon.name.replaceFirstChar { it.titlecase(Locale.ROOT) }}  #${pokemon.id.toString().padStart(3, '0')}", fontWeight = FontWeight.ExtraBold, color = Ink) },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(tint.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                    AsyncImage(model = pokemon.imageUrl, contentDescription = pokemon.name, modifier = Modifier.size(190.dp).padding(8.dp))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pokemon.types.forEach { Surface(color = (TypeColors[it] ?: Color.Gray).copy(alpha = .18f), shape = RoundedCornerShape(8.dp)) { Text(it.replaceFirstChar { c -> c.titlecase(Locale.ROOT) }, Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontWeight = FontWeight.Bold) } }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DetailMetric("Altura", "${pokemon.height / 10.0} m")
                    DetailMetric("Peso", "${pokemon.weight / 10.0} kg")
                }
                Spacer(Modifier.height(14.dp))
                Text("Habilidades", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, color = Ink)
                Text(pokemon.abilities.joinToString { it.replaceFirstChar { c -> c.titlecase(Locale.ROOT) } }, modifier = Modifier.fillMaxWidth(), color = Color.DarkGray)
                Spacer(Modifier.height(12.dp))
                Text("Estadísticas base", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, color = Ink)
                pokemon.stats.forEach { stat ->
                    Column(Modifier.fillMaxWidth().padding(top = 7.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stat.name.replaceFirstChar { it.titlecase(Locale.ROOT) }, style = MaterialTheme.typography.bodySmall)
                            Text(stat.value.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(progress = { (stat.value / 180f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = PokeballRed, trackColor = Color.LightGray.copy(alpha = .35f))
                    }
                }
                Spacer(Modifier.height(12.dp))
                when {
                    loadingSpecies -> CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    pokemon.isLegendary -> Text("Pokémon legendario ✨", color = PokeballRed, fontWeight = FontWeight.Bold)
                    pokemon.isMythical -> Text("Pokémon mítico ✨", color = PokeballRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color.White
    )
}

@Composable
private fun DetailMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Ink)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
    }
}
