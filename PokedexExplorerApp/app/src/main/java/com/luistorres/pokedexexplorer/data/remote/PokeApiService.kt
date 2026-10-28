package com.luistorres.pokedexexplorer.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 24,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    @GET("pokemon/{nameOrId}")
    suspend fun getPokemon(@Path("nameOrId") nameOrId: String): PokemonDto

    @GET("type/{type}")
    suspend fun getPokemonByType(@Path("type") type: String): PokemonTypeResponse

    @GET("pokemon-species/{id}")
    suspend fun getSpecies(@Path("id") id: Int): PokemonSpeciesDto

    @GET("ability/{name}")
    suspend fun getAbility(@Path("name") name: String): PokemonAbilityDto
}
