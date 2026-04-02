package com.platoon.app.model

data class Mission(
    val id: Long = 0L,
    val name: String = "",
    val soldiers: List<Long> = emptyList(),
    val createdAt: Long = 0L
) {
    companion object {
        fun fromMap(map: Map<String, Any>): Mission {
            @Suppress("UNCHECKED_CAST")
            val soldiers = (map["soldiers"] as? List<*>)?.mapNotNull { (it as? Number)?.toLong() } ?: emptyList()
            return Mission(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                name = map["name"] as? String ?: "",
                soldiers = soldiers,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "soldiers" to soldiers,
        "createdAt" to createdAt
    )
}
