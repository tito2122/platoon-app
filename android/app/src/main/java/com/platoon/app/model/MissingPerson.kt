package com.platoon.app.model

data class MissingPerson(
    val id: Long = 0L,
    val name: String = "",
    val reason: String = "",
    val soldierDataId: Long? = null,
    val timestamp: Long = 0L
) {
    companion object {
        fun fromMap(map: Map<String, Any>): MissingPerson {
            return MissingPerson(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                name = map["name"] as? String ?: "",
                reason = map["reason"] as? String ?: "",
                soldierDataId = (map["soldierDataId"] as? Number)?.toLong(),
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
            )
        }
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "reason" to reason,
        "soldierDataId" to soldierDataId,
        "timestamp" to timestamp
    )
}
