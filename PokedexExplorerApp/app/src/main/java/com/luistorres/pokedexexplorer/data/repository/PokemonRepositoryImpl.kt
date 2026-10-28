package com.luistorres.pokedexexplorer.data.repository

import com.luistorres.pokedexexplorer.data.remote.PokeApiService
import com.luistorres.pokedexexplorer.data.remote.PokemonDto
import com.luistorres.pokedexexplorer.domain.model.Pokemon
import com.luistorres.pokedexexplorer.domain.model.PokemonStat
import com.luistorres.pokedexexplorer.domain.repository.PokemonPage
import com.luistorres.pokedexexplorer.domain.repository.PokemonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class PokemonRepositoryImpl(private val api: PokeApiService) : PokemonRepository {
    override suspend fun getPokemonPage(offset: Int, limit: Int): PokemonPage {
        val response = api.getPokemonList(limit, offset)
        val pokemon = coroutineScope {
            response.results.map { item -> async { mapPokemon(api.getPokemon(item.name)) } }.awaitAll()
        }
        return PokemonPage(pokemon, response.next != null)
    }

    override suspend fun getPokemonByNameOrId(query: String): Pokemon {
        val normalized = query.trim().lowercase()
        require(normalized.isNotEmpty()) { "Escribe un nombre o número de Pokémon." }
        return mapPokemon(api.getPokemon(normalized))
    }

    override suspend fun getPokemonSpeciesFlags(id: Int): Pair<Boolean, Boolean> {
        val species = api.getSpecies(id)
        return species.isLegendary to species.isMythical
    }

    override suspend fun getPokemonAbilityNamesInSpanish(abilities: List<String>): List<String> = coroutineScope {
        abilities.map { ability ->
            async {
                runCatching {
                    api.getAbility(ability).names.firstOrNull { it.language.name == "es" }?.name
                }.getOrNull() ?: "Traducción no disponible"
            }
        }.awaitAll()
    }

    override suspend fun getPokemonByType(type: String, limit: Int): List<Pokemon> {
        val response = api.getPokemonByType(type.lowercase())
        return coroutineScope {
            response.pokemon.take(limit).map { entry ->
                async { mapPokemon(api.getPokemon(entry.pokemon.name)) }
            }.awaitAll()
        }
    }

    private suspend fun mapPokemon(dto: PokemonDto): Pokemon {
        return Pokemon(
            id = dto.id,
            name = dto.name,
            imageUrl = dto.sprites.other?.officialArtwork?.frontDefault ?: dto.sprites.frontDefault,
            height = dto.height,
            weight = dto.weight,
            types = dto.types.map { it.type.name },
            abilities = dto.abilities.map { it.ability.name },
            stats = dto.stats.map { PokemonStat(it.stat.name.replace('-', ' '), it.base_stat) }
        )
    }
}
