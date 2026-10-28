package com.luistorres.pokedexexplorer.domain.repository

import com.luistorres.pokedexexplorer.domain.model.Pokemon

data class PokemonPage(val items: List<Pokemon>, val hasNext: Boolean)

interface PokemonRepository {
    suspend fun getPokemonPage(offset: Int, limit: Int = 24): PokemonPage
    suspend fun getPokemonByNameOrId(query: String): Pokemon
    suspend fun getPokemonByType(type: String, limit: Int = 40): List<Pokemon>
    suspend fun getPokemonSpeciesFlags(id: Int): Pair<Boolean, Boolean>
    suspend fun getPokemonAbilityNamesInSpanish(abilities: List<String>): List<String>
}
