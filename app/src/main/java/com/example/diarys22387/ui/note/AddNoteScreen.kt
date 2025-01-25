package com.example.diarys22387.ui.note

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diarys22387.data.model.Note
import com.example.diarys22387.data.repository.NoteRepository
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.diarys22387.data.repository.MediaRepository
import com.example.diarys22387.util.LocationManager


@Composable
fun AddNoteScreen(
    onNoteSaved: () -> Unit,
    viewModel: AddNoteViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }
    var address by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content") })
        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            viewModel.setLocationIfPossible(
                onResult = { newLat, newLon, place ->
                    lat = newLat
                    lon = newLon
                    address = place
                }
            )
        }) {
            Text("Set Current Location")
        }

        address?.let { Text("Location: $it") }

        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            viewModel.addNote(title, content, lat, lon, address)
            onNoteSaved()
        }) {
            Text("Save Note")
        }
    }
}



@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    fun addNote(
        title: String,
        content: String,
        lat: Double?,
        lon: Double?,
        address: String?
    ) {
        viewModelScope.launch {
            val newNote = Note(
                title = title,
                content = content,
                latitude = lat,
                longitude = lon,
                address = address
            )
            noteRepository.addNote(newNote)
        }
    }

    fun setLocationIfPossible(onResult: (Double?, Double?, String?) -> Unit) {
        viewModelScope.launch {
            if (!locationManager.hasLocationPermission()) {
                onResult(null, null, "No location permission!")
                return@launch
            }
            val loc = locationManager.getLastKnownLocation()
            if (loc != null) {
                val place = locationManager.getAddressFromCoords(loc.latitude, loc.longitude)
                onResult(loc.latitude, loc.longitude, place)
            } else {
                onResult(null, null, "Could not get location")
            }
        }
    }
}

