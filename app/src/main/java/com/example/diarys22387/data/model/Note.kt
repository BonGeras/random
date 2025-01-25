package com.example.diarys22387.data.model

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null
) {
    fun hasLocation() = latitude != null && longitude != null
}
