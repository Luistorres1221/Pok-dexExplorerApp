package com.luistorres.pokedexexplorer.data.repository

import com.luistorres.pokedexexplorer.data.remote.PokeApiService
import com.luistorres.pokedexexplorer.data.remote.PokemonDto
import com.luistorres.pokedexexplorer.data.remote.PokemonListItem
import com.luistorres.pokedexexplorer.domain.model.Pokemon
import com.luistorres.pokedexexplorer.domain.model.PokemonSpeciesDetails
import com.luistorres.pokedexexplorer.domain.model.PokemonStat
import com.luistorres.pokedexexplorer.domain.repository.PokemonPage
import com.luistorres.pokedexexplorer.domain.repository.PokemonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PokemonRepositoryImpl(private val api: PokeApiService) : PokemonRepository {
    private val namesMutex = Mutex()
    @Volatile private var cachedPokemonNames: List<PokemonListItem>? = null

    override suspend fun getPokemonPage(offset: Int, limit: Int): PokemonPage {
        val response = api.getPokemonList(limit, offset)
        val pokemon = coroutineScope {
            response.results.map { item -> async { mapPokemon(api.getPokemon(item.name)) } }.awaitAll()
        }
        return PokemonPage(pokemon, response.next != null, response.count)
    }

    override suspend fun searchPokemon(query: String, offset: Int, limit: Int): PokemonPage {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) return getPokemonPage(offset, limit)

        val allPokemon = namesMutex.withLock {
            cachedPokemonNames ?: api.getPokemonList(limit = 2000, offset = 0).results.also {
                cachedPokemonNames = it
            }
        }
        val matches = allPokemon.filter { entry ->
            val id = entry.url.trimEnd('/').substringAfterLast('/').toIntOrNull()
            entry.name.contains(normalizedQuery) || id?.toString()?.contains(normalizedQuery) == true
        }
        val pageEntries = matches.drop(offset).take(limit)
        val pokemon = coroutineScope {
            pageEntries.map { entry -> async { mapPokemon(api.getPokemon(entry.name)) } }.awaitAll()
        }
        return PokemonPage(
            items = pokemon,
            hasNext = offset + pageEntries.size < matches.size,
            totalCount = matches.size
        )
    }

    override suspend fun getPokemonByNameOrId(query: String): Pokemon {
        val normalized = query.trim().lowercase()
        require(normalized.isNotEmpty()) { "Escribe un nombre o número de Pokémon." }
        return mapPokemon(api.getPokemon(normalized))
    }

    override suspend fun getPokemonSpeciesDetails(id: Int): PokemonSpeciesDetails {
        val species = api.getSpecies(id)
        val description = species.flavorTextEntries
            .firstOrNull { it.language.name == "es" }
            ?.flavorText
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
        return PokemonSpeciesDetails(species.isLegendary, species.isMythical, description)
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

    override suspend fun getPokemonByType(type: String, offset: Int, limit: Int): PokemonPage {
        val response = api.getPokemonByType(type.lowercase())
        val totalCount = response.pokemon.size
        val pageEntries = response.pokemon.drop(offset).take(limit)
        return coroutineScope {
            val pokemon = pageEntries.map { entry ->
                async { mapPokemon(api.getPokemon(entry.pokemon.name)) }
            }.awaitAll()
            PokemonPage(
                items = pokemon,
                hasNext = offset + pageEntries.size < totalCount,
                totalCount = totalCount
            )
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
