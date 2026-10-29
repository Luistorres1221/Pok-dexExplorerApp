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
    val totalCount: Int = 0,
    val selectedPokemon: Pokemon? = null,
    val isDetailLoading: Boolean = false,
    val detailError: String? = null,
    val detailLoaded: Boolean = false
)

class PokemonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PokemonRepository = PokemonRepositoryImpl(RetrofitProvider.service)
    private val connectivity = ConnectivityObserver(application)
    private val _uiState = MutableStateFlow(PokemonUiState())
    val uiState: StateFlow<PokemonUiState> = _uiState.asStateFlow()
    private var resultsRequestVersion = 0

    init {
        viewModelScope.launch {
            connectivity.status.catch { emit(false) }.collect { online -> _uiState.value = _uiState.value.copy(isOnline = online) }
        }
        loadFirstPage()
    }

    fun loadFirstPage() {
        val requestVersion = ++resultsRequestVersion
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isLoadingMore = false, error = null, offset = 0, totalCount = 0, hasNext = true, selectedType = null)
            try {
                val page = repository.getPokemonPage(0)
                if (requestVersion == resultsRequestVersion) _uiState.value = _uiState.value.copy(pokemon = page.items, isLoading = false, offset = page.items.size, hasNext = page.hasNext, totalCount = page.totalCount)
            } catch (e: Exception) {
                if (requestVersion == resultsRequestVersion) _uiState.value = _uiState.value.copy(isLoading = false, error = messageFor(e))
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasNext || state.searchText.isNotBlank()) return
        val requestVersion = resultsRequestVersion
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
            try {
                val currentState = _uiState.value
                val page = currentState.selectedType?.let { repository.getPokemonByType(it, currentState.offset) }
                    ?: repository.getPokemonPage(currentState.offset)
                if (requestVersion != resultsRequestVersion) return@launch
                val current = _uiState.value.pokemon
                _uiState.value = _uiState.value.copy(pokemon = current + page.items, offset = currentState.offset + page.items.size, hasNext = page.hasNext, totalCount = page.totalCount, isLoadingMore = false)
            } catch (e: Exception) {
                if (requestVersion == resultsRequestVersion) _uiState.value = _uiState.value.copy(isLoadingMore = false, error = messageFor(e))
            }
        }
    }

    fun onSearchChange(value: String) {
        val previous = _uiState.value
        _uiState.value = previous.copy(searchText = value, selectedType = null, error = null)
        if (previous.selectedType != null || (value.isBlank() && !previous.hasNext)) loadFirstPage()
    }

    fun searchExact() {
        val query = _uiState.value.searchText.trim()
        if (query.isEmpty()) { loadFirstPage(); return }
        val requestVersion = ++resultsRequestVersion
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isLoadingMore = false, totalCount = 0, error = null, selectedType = null)
            try {
                val pokemon = repository.getPokemonByNameOrId(query)
                if (requestVersion == resultsRequestVersion) _uiState.value = _uiState.value.copy(pokemon = listOf(pokemon), isLoading = false, hasNext = false, totalCount = 1)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, pokemon = emptyList(), hasNext = false, error = "No se encontró ese Pokémon. Comprueba el nombre o número.")
            }
        }
    }

    fun filterByType(type: String?) {
        if (type == null) { _uiState.value = _uiState.value.copy(searchText = ""); loadFirstPage(); return }
        val requestVersion = ++resultsRequestVersion
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isLoadingMore = false, error = null, selectedType = type, searchText = "", offset = 0, totalCount = 0)
            try {
                val page = repository.getPokemonByType(type, 0)
                if (requestVersion == resultsRequestVersion) _uiState.value = _uiState.value.copy(pokemon = page.items, isLoading = false, offset = page.items.size, hasNext = page.hasNext, totalCount = page.totalCount)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, pokemon = emptyList(), error = "No fue posible cargar el tipo seleccionado.")
            }
        }
    }

    fun loadPokemonDetail(id: Int, retry: Boolean = false) {
        val current = _uiState.value
        if (!retry && current.selectedPokemon?.id == id && current.detailLoaded) return
        viewModelScope.launch {
            val initial = _uiState.value.pokemon.firstOrNull { it.id == id }
            _uiState.value = _uiState.value.copy(
                isDetailLoading = true,
                detailError = null,
                detailLoaded = false,
                selectedPokemon = initial
            )
            try {
                val pokemon = repository.getPokemonByNameOrId(id.toString())
                val species = repository.getPokemonSpeciesDetails(id)
                val abilities = repository.getPokemonAbilityNamesInSpanish(pokemon.abilities)
                _uiState.value = _uiState.value.copy(
                    selectedPokemon = pokemon.copy(
                        isLegendary = species.isLegendary,
                        isMythical = species.isMythical,
                        abilities = abilities,
                        description = species.description
                    ),
                    isDetailLoading = false,
                    detailLoaded = true
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDetailLoading = false,
                    detailError = "No se pudo cargar el detalle. Revisa tu conexión e inténtalo de nuevo."
                )
            }
        }
    }

    private fun messageFor(e: Exception): String = when (e) {
        is java.net.UnknownHostException -> "Sin conexión a Internet. Revisa tu red e inténtalo de nuevo."
        is java.net.SocketTimeoutException -> "La solicitud tardó demasiado. Inténtalo de nuevo."
        else -> "Ocurrió un error al consultar PokeAPI. Inténtalo de nuevo."
    }
}
