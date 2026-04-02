package com.platoon.app.model

data class PlatoonList(
    val id: String = "",
    val name: String = "",
    val items: List<String> = emptyList(),
    val createdAt: Long = 0L
) {
    companion object {
        fun fromMap(map: Map<String, Any>): PlatoonList {
            @Suppress("UNCHECKED_CAST")
            val items = map["items"] as? List<String> ?: emptyList()
            return PlatoonList(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                items = items,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "items" to items,
        "createdAt" to createdAt
    )
}
