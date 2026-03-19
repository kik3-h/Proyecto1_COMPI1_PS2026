package com.usac.pkmforms.servicios.cliente_api_pokemon

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PokeApiClient {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://pokeapi.co/api/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: PokeApiService = retrofit.create(PokeApiService::class.java)

    suspend fun who_is_that_pokemon(n: Int, m: Int): List<String> {
        if (n <= 0 || m < n) {
            return emptyList()
        }
        val limit = (m - n) + 1
        val offset = n - 1
        val response = service.getPokemons(limit = limit, offset = offset)
        if (!response.isSuccessful) {
            return emptyList()
        }
        return response.body()
            ?.results
            ?.map { it.name }
            .orEmpty()
    }
}
