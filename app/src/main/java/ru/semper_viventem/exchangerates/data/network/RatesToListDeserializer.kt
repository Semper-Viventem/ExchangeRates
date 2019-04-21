package ru.semper_viventem.exchangerates.data.network

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import ru.semper_viventem.exchangerates.data.network.response.AllRatesResponse
import java.lang.reflect.Type

class RatesToListDeserializer : JsonDeserializer<AllRatesResponse> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): AllRatesResponse? {
        if (json == null) return null

        val currencies = mutableListOf<Pair<String, Double>>()
        json.asJsonObject.entrySet().forEach { (key, value) ->
            currencies.add(key to value.asDouble)
        }

        return AllRatesResponse(currencies)
    }
}