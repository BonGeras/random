package com.example.diarys22387.data.model

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun hasLocation(): Boolean = latitude != null && longitude != null
}
