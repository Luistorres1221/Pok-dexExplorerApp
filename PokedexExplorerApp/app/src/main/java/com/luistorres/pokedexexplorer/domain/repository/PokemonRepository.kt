package com.luistorres.pokedexexplorer.domain.repository

import com.luistorres.pokedexexplorer.domain.model.Pokemon
import com.luistorres.pokedexexplorer.domain.model.PokemonSpeciesDetails

data class PokemonPage(
    val items: List<Pokemon>,
    val hasNext: Boolean,
    val totalCount: Int
)

interface PokemonRepository {
    suspend fun getPokemonPage(offset: Int, limit: Int = 24): PokemonPage
    suspend fun getPokemonByNameOrId(query: String): Pokemon
    suspend fun getPokemonByType(type: String, offset: Int = 0, limit: Int = 24): PokemonPage
    suspend fun getPokemonSpeciesDetails(id: Int): PokemonSpeciesDetails
    suspend fun getPokemonAbilityNamesInSpanish(abilities: List<String>): List<String>
}
