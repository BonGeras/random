package com.example.diarys22387.ui.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.diarys22387.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@Composable
fun DrawingScreen(
    imageUrl: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: DrawingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    var currentPath by remember { mutableStateOf<Path?>(null) }
    val paths = remember { mutableStateListOf<Path>() }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is DrawingUiState.Success) {
            val newUrl = (uiState as DrawingUiState.Success).newUrl
            onSave(newUrl)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = Path().apply {
                                moveTo(offset.x, offset.y)
                            }
                        },
                        onDrag = { change, _ ->
                            currentPath?.lineTo(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            currentPath?.let { paths.add(it) }
                            currentPath = null
                        }
                    )
                }
        ) {
            paths.forEach { path ->
                drawPath(path, Color.Red, style = Stroke(width = 5f))
            }
            currentPath?.let { path ->
                drawPath(path, Color.Red, style = Stroke(width = 5f))
            }
        }

        Row(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(
                onClick = {
                    viewModel.saveDrawingWithBackground(
                        context = context,
                        imageUrl = imageUrl,
                        userPaths = paths
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<DrawingUiState>(DrawingUiState.Idle)
    val uiState: StateFlow<DrawingUiState> = _uiState

    fun saveDrawingWithBackground(
        context: Context,
        imageUrl: String,
        userPaths: List<Path>
    ) {
        viewModelScope.launch {
            _uiState.value = DrawingUiState.Loading

            try {
                val backgroundBitmap = loadBitmapFromUrl(context, imageUrl)
                    ?: throw Exception("Failed to load background")

                val resultBitmap = backgroundBitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(resultBitmap)

                val paint = Paint().apply {
                    color = android.graphics.Color.RED
                    strokeWidth = 5f
                    style = Paint.Style.STROKE
                    isAntiAlias = true
                }
                userPaths.forEach { path ->
                    val androidPath = android.graphics.Path()
                    path.asAndroidPath().let { p -> androidPath.set(p) }
                    canvas.drawPath(androidPath, paint)
                }

                val outFile = File.createTempFile("drawn_image_", ".jpg", context.cacheDir)
                FileOutputStream(outFile).use { out ->
                    resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }

                val finalUrl = mediaRepository.uploadImage(android.net.Uri.fromFile(outFile))

                outFile.delete()
                backgroundBitmap.recycle()
                resultBitmap.recycle()

                _uiState.value = DrawingUiState.Success(finalUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = DrawingUiState.Error(e.message ?: "Error saving drawing")
            }
        }
    }

    private suspend fun loadBitmapFromUrl(context: Context, imageUrl: String): Bitmap? {
        if (imageUrl.isBlank()) return null

        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()

        val result = Coil.imageLoader(context).execute(request)
        return if (result is SuccessResult) {
            val bmpDrawable = result.drawable as? android.graphics.drawable.BitmapDrawable
            bmpDrawable?.bitmap
        } else null
    }
}

sealed class DrawingUiState {
    object Idle : DrawingUiState()
    object Loading : DrawingUiState()
    data class Success(val newUrl: String) : DrawingUiState()
    data class Error(val message: String) : DrawingUiState()
}
