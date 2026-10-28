package com.luistorres.pokedexexplorer.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.luistorres.pokedexexplorer.data.network.ConnectivityObserver
import com.luistorres.pokedexexplorer.data.remote.RetrofitProvider
import com.luistorres.pokedexexplorer.data.repository.PokemonRepositoryImpl
import com.luistorres.pokedexexplorer.domain.model.Pokemon
import com.luistorres.pokedexexplorer.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

 data class PokemonUiState(
    val pokemon: List<Pokemon> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val searchText: String = "",
    val selectedType: String? = null,
    val isOnline: Boolean = true,
    val hasNext: Boolean = true,
    val offset: Int = 0,
    val selectedPokemon: Pokemon? = null,
    val isDetailLoading: Boolean = false
)

class PokemonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PokemonRepository = PokemonRepositoryImpl(RetrofitProvider.service)
    private val connectivity = ConnectivityObserver(application)
    private val _uiState = MutableStateFlow(PokemonUiState())
    val uiState: StateFlow<PokemonUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            connectivity.status.catch { emit(false) }.collect { online -> _uiState.value = _uiState.value.copy(isOnline = online) }
        }
        loadFirstPage()
    }

    fun loadFirstPage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, offset = 0, hasNext = true, selectedType = null)
            try {
                val page = repository.getPokemonPage(0)
                _uiState.value = _uiState.value.copy(pokemon = page.items, isLoading = false, offset = page.items.size, hasNext = page.hasNext)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = messageFor(e))
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasNext || state.selectedType != null || state.searchText.isNotBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
            try {
                val page = repository.getPokemonPage(_uiState.value.offset)
                val current = _uiState.value.pokemon
                _uiState.value = _uiState.value.copy(pokemon = current + page.items, offset = current.size + page.items.size, hasNext = page.hasNext, isLoadingMore = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false, error = messageFor(e))
            }
        }
    }

    fun onSearchChange(value: String) {
        _uiState.value = _uiState.value.copy(searchText = value, selectedType = null, error = null)
    }

    fun searchExact() {
        val query = _uiState.value.searchText.trim()
        if (query.isEmpty()) { loadFirstPage(); return }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedType = null)
            try {
                val pokemon = repository.getPokemonByNameOrId(query)
                _uiState.value = _uiState.value.copy(pokemon = listOf(pokemon), isLoading = false, hasNext = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, pokemon = emptyList(), error = "No se encontró ese Pokémon. Comprueba el nombre o número.")
            }
        }
    }

    fun filterByType(type: String?) {
        if (type == null) { loadFirstPage(); _uiState.value = _uiState.value.copy(searchText = ""); return }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedType = type, searchText = "")
            try {
                val items = repository.getPokemonByType(type)
                _uiState.value = _uiState.value.copy(pokemon = items, isLoading = false, hasNext = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, pokemon = emptyList(), error = "No fue posible cargar el tipo seleccionado.")
            }
        }
    }

    fun openDetail(pokemon: Pokemon) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDetailLoading = true, selectedPokemon = pokemon)
            try {
                val species = repository.getPokemonSpeciesDetails(pokemon.id)
                val abilities = runCatching { repository.getPokemonAbilityNamesInSpanish(pokemon.abilities) }
                    .getOrDefault(pokemon.abilities)
                _uiState.value = _uiState.value.copy(
                    selectedPokemon = pokemon.copy(
                        isLegendary = species.isLegendary,
                        isMythical = species.isMythical,
                        abilities = abilities,
                        description = species.description
                    ),
                    isDetailLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isDetailLoading = false)
            }
        }
    }

    fun closeDetail() { _uiState.value = _uiState.value.copy(selectedPokemon = null, isDetailLoading = false) }

    private fun messageFor(e: Exception): String = when (e) {
        is java.net.UnknownHostException -> "Sin conexión a Internet. Revisa tu red e inténtalo de nuevo."
        is java.net.SocketTimeoutException -> "La solicitud tardó demasiado. Inténtalo de nuevo."
        else -> "Ocurrió un error al consultar PokeAPI. Inténtalo de nuevo."
    }
}
