package com.platoon.app.model

data class Reminder(
    val id: Long = 0L,
    val type: String = TYPE_DAILY,
    val hour: Int = 8,
    val minute: Int = 0,
    val title: String = "",
    val message: String = "",
    val sound: String = "default",
    val days: List<Int> = emptyList(),
    val enabled: Boolean = true
) {
    companion object {
        const val TYPE_DAILY = "daily"
        const val TYPE_WEEKLY = "weekly"
        const val TYPE_MONTHLY = "monthly"

        fun fromMap(map: Map<String, Any>): Reminder {
            @Suppress("UNCHECKED_CAST")
            val days = (map["days"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
            return Reminder(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                type = map["type"] as? String ?: TYPE_DAILY,
                hour = (map["hour"] as? Number)?.toInt() ?: 8,
                minute = (map["minute"] as? Number)?.toInt() ?: 0,
                title = map["title"] as? String ?: "",
                message = map["message"] as? String ?: "",
                sound = map["sound"] as? String ?: "default",
                days = days,
                enabled = map["enabled"] as? Boolean ?: true
            )
        }
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "type" to type,
        "hour" to hour,
        "minute" to minute,
        "title" to title,
        "message" to message,
        "sound" to sound,
        "days" to days,
        "enabled" to enabled
    )
}
