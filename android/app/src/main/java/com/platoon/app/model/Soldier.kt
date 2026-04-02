package com.platoon.app.model

data class Leave(
    val from: String = "",
    val to: String = ""
)

data class PendingLeave(
    val id: Long = 0L,
    val from: String = "",
    val to: String = "",
    val status: String = "pending"
)

data class Soldier(
    val id: Long = 0L,
    val firstName: String = "",
    val lastName: String = "",
    val personalId: String = "",
    val photoData: String = "",
    val status: String = STATUS_PRESENT,
    val roles: List<String> = emptyList(),
    val phone: String = "",
    val residence: String = "",
    val residenceLat: Double? = null,
    val residenceLng: Double? = null,
    val leaves: List<Leave> = emptyList(),
    val nextFrom: String = "",
    val nextTo: String = "",
    val nextApproved: Boolean = false,
    val notes: String = "",
    val hasWife: Boolean = false,
    val wifeName: String = "",
    val wifePhone: String = "",
    val hasKids: Boolean = false,
    val kids: List<String> = emptyList(),
    val nextNotes: String = "",
    val hofpa: Boolean = false,
    val pendingLeaves: List<PendingLeave> = emptyList(),
    val extra1: Boolean = false,
    val shams: List<String> = emptyList()
) {
    val fullName: String get() = "$firstName $lastName".trim()

    companion object {
        const val STATUS_PRESENT = "present"
        const val STATUS_ABSENT = "absent"

        fun fromMap(map: Map<String, Any>): Soldier {
            @Suppress("UNCHECKED_CAST")
            val leavesRaw = map["leaves"] as? List<Map<String, Any>> ?: emptyList()
            val leaves = leavesRaw.map {
                Leave(
                    from = it["from"] as? String ?: "",
                    to = it["to"] as? String ?: ""
                )
            }

            @Suppress("UNCHECKED_CAST")
            val pendingRaw = map["pendingLeaves"] as? List<Map<String, Any>> ?: emptyList()
            val pending = pendingRaw.map {
                PendingLeave(
                    id = (it["id"] as? Number)?.toLong() ?: 0L,
                    from = it["from"] as? String ?: "",
                    to = it["to"] as? String ?: "",
                    status = it["status"] as? String ?: "pending"
                )
            }

            @Suppress("UNCHECKED_CAST")
            val roles = map["roles"] as? List<String> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val kids = map["kids"] as? List<String> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val shams = map["shams"] as? List<String> ?: emptyList()

            return Soldier(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                firstName = map["firstName"] as? String ?: map["name"] as? String ?: "",
                lastName = map["lastName"] as? String ?: "",
                personalId = map["personalId"] as? String ?: "",
                photoData = map["photoData"] as? String ?: "",
                status = map["status"] as? String ?: STATUS_PRESENT,
                roles = roles,
                phone = map["phone"] as? String ?: "",
                residence = map["residence"] as? String ?: "",
                residenceLat = (map["residenceLat"] as? Number)?.toDouble(),
                residenceLng = (map["residenceLng"] as? Number)?.toDouble(),
                leaves = leaves,
                nextFrom = map["nextFrom"] as? String ?: "",
                nextTo = map["nextTo"] as? String ?: "",
                nextApproved = map["nextApproved"] as? Boolean ?: false,
                notes = map["notes"] as? String ?: "",
                hasWife = map["hasWife"] as? Boolean ?: false,
                wifeName = map["wifeName"] as? String ?: "",
                wifePhone = map["wifePhone"] as? String ?: "",
                hasKids = map["hasKids"] as? Boolean ?: false,
                kids = kids,
                nextNotes = map["nextNotes"] as? String ?: "",
                hofpa = map["hofpa"] as? Boolean ?: false,
                pendingLeaves = pending,
                extra1 = map["extra1"] as? Boolean ?: false,
                shams = shams
            )
        }
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "firstName" to firstName,
        "lastName" to lastName,
        "name" to fullName,
        "personalId" to personalId,
        "photoData" to photoData,
        "status" to status,
        "roles" to roles,
        "phone" to phone,
        "residence" to residence,
        "residenceLat" to residenceLat,
        "residenceLng" to residenceLng,
        "leaves" to leaves.map { mapOf("from" to it.from, "to" to it.to) },
        "nextFrom" to nextFrom,
        "nextTo" to nextTo,
        "nextApproved" to nextApproved,
        "notes" to notes,
        "hasWife" to hasWife,
        "wifeName" to wifeName,
        "wifePhone" to wifePhone,
        "hasKids" to hasKids,
        "kids" to kids,
        "nextNotes" to nextNotes,
        "hofpa" to hofpa,
        "pendingLeaves" to pendingLeaves.map {
            mapOf("id" to it.id, "from" to it.from, "to" to it.to, "status" to it.status)
        },
        "extra1" to extra1,
        "shams" to shams
    )
}
