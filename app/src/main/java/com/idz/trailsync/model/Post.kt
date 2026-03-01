package com.idz.trailsync.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Post(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val description: String = "",
    val location: Location? = null,
    val numberOfDays: Int = 0,
    val photos: List<String> = emptyList(),
    val price: Int = 0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val mapLink: String = ""
) {
    data class Location(
        val city: String = "",
        val country: String = "",
        val lat: Double = 0.0,
        val lng: Double = 0.0,
        val name: String = "",
        val placeId: String = "",
        val mapLink: String = ""
    )

    companion object {
        private const val ID_KEY = "id"
        private const val TITLE_KEY = "title"
        private const val AUTHOR_KEY = "author"
        private const val DESCRIPTION_KEY = "description"
        private const val LOCATION_KEY = "location"
        private const val NUMBER_OF_DAYS_KEY = "numberOfDays"
        private const val PHOTOS_KEY = "photos"
        private const val PRICE_KEY = "price"
        private const val CREATED_AT_KEY = "createdAt"
        private const val UPDATED_AT_KEY = "updatedAt"
        private const val MAP_LINK_KEY = "mapLink"

        fun fromJSON(json: Map<String, Any>): Post {
            val id = json[ID_KEY] as? String ?: UUID.randomUUID().toString()
            val title = json[TITLE_KEY] as? String ?: ""
            val author = json[AUTHOR_KEY] as? String ?: ""
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val locationMap = json[LOCATION_KEY] as? Map<String, Any>
            val location = locationMap?.let { map ->
                Location(
                    city = map["city"] as? String ?: "",
                    country = map["country"] as? String ?: "",
                    lat = (map["lat"] as? Number)?.toDouble() ?: 0.0,
                    lng = (map["lng"] as? Number)?.toDouble() ?: 0.0,
                    name = map["name"] as? String ?: "",
                    placeId = map["placeId"] as? String ?: "",
                    mapLink = map["mapLink"] as? String ?: ""
                )
            }
            val numberOfDays = (json[NUMBER_OF_DAYS_KEY] as? Number)?.toInt() ?: 0
            val photos = (json[PHOTOS_KEY] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val price = (json[PRICE_KEY] as? Number)?.toInt() ?: 0
            val createdAt = json[CREATED_AT_KEY] as? Date ?: Date()
            val updatedAt = json[UPDATED_AT_KEY] as? Date ?: Date()
            val mapLink = json[MAP_LINK_KEY] as? String ?: ""

            return Post(
                id = id,
                title = title,
                author = author,
                description = description,
                location = location,
                numberOfDays = numberOfDays,
                photos = photos,
                price = price,
                createdAt = createdAt,
                updatedAt = updatedAt,
                mapLink = mapLink
            )
        }
    }

    val json: Map<String, Any>
        get() {
            val locationMap = location?.let {
                mapOf(
                    "city" to it.city,
                    "country" to it.country,
                    "lat" to it.lat,
                    "lng" to it.lng,
                    "name" to it.name,
                    "placeId" to it.placeId,
                    "mapLink" to it.mapLink
                )
            }
            return hashMapOf(
                ID_KEY to id,
                TITLE_KEY to title,
                AUTHOR_KEY to author,
                DESCRIPTION_KEY to description,
                LOCATION_KEY to (locationMap ?: emptyMap<String, Any>()),
                NUMBER_OF_DAYS_KEY to numberOfDays,
                PHOTOS_KEY to photos,
                PRICE_KEY to price,
                CREATED_AT_KEY to createdAt,
                UPDATED_AT_KEY to updatedAt,
                MAP_LINK_KEY to mapLink
            )
        }
}