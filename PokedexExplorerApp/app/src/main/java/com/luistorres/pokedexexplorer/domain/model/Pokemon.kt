package com.luistorres.pokedexexplorer.domain.model

data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val height: Int,
    val weight: Int,
    val types: List<String>,
    val abilities: List<String>,
    val stats: List<PokemonStat>,
    val isLegendary: Boolean = false,
    val isMythical: Boolean = false
)

data class PokemonStat(val name: String, val value: Int)
