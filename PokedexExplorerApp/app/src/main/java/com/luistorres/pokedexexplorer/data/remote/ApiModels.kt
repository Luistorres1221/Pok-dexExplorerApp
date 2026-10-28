package com.luistorres.pokedexexplorer.data.remote

import com.google.gson.annotations.SerializedName

data class PokemonListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonListItem>
)

data class PokemonListItem(val name: String, val url: String)

data class PokemonDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: PokemonSprites,
    val types: List<PokemonTypeSlot>,
    val abilities: List<PokemonAbilitySlot>,
    val stats: List<PokemonStatSlot>
)

data class PokemonSprites(
    @SerializedName("front_default") val frontDefault: String?,
    @SerializedName("other") val other: PokemonOtherSprites?
)
data class PokemonOtherSprites(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtwork?
)
data class OfficialArtwork(@SerializedName("front_default") val frontDefault: String?)
data class PokemonTypeSlot(val slot: Int, val type: NamedResource)
data class PokemonAbilitySlot(val ability: NamedResource, @SerializedName("is_hidden") val isHidden: Boolean)
data class PokemonStatSlot(val base_stat: Int, val stat: NamedResource)
data class NamedResource(val name: String, val url: String)

data class PokemonTypeResponse(val pokemon: List<TypePokemonEntry>)
data class TypePokemonEntry(val pokemon: PokemonListItem, val slot: Int)

data class PokemonSpeciesDto(
    val id: Int,
    @SerializedName("is_legendary") val isLegendary: Boolean,
    @SerializedName("is_mythical") val isMythical: Boolean,
    @SerializedName("capture_rate") val captureRate: Int,
    @SerializedName("base_happiness") val baseHappiness: Int?
)

data class PokemonAbilityDto(val names: List<LocalizedName>)
data class LocalizedName(val name: String, val language: NamedResource)
