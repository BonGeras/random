package com.example.diarys22387.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.diarys22387.ui.drawing.DrawingScreen
import com.example.diarys22387.ui.pin.PinScreen
import com.example.diarys22387.ui.note.NotesListScreen
import com.example.diarys22387.ui.note.AddNoteScreen
import com.example.diarys22387.ui.note.EditNoteScreen
import com.example.diarys22387.ui.map.MapScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diarys22387.ui.note.NotesViewModel
import com.example.diarys22387.ui.map.MapViewModel
import com.example.diarys22387.ui.note.NotePreviewScreen
import com.example.diarys22387.ui.note.AddNoteViewModel

@Composable
fun DiaryNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "pin") {
        composable("pin") {
            PinScreen(onPinSuccess = {
                navController.navigate("notes") {
                    popUpTo("pin") { inclusive = true }
                }
            })
        }
        composable("notes") {
            val viewModel: NotesViewModel = hiltViewModel()
            NotesListScreen(
                onAddClick = { navController.navigate("addNote") },
                onNoteClick = { noteId -> navController.navigate("preview/$noteId") },
                onMapClick = { navController.navigate("map") },
                viewModel = viewModel
            )
        }
        composable("addNote") {
            AddNoteScreen(
                onNoteSaved = { navController.popBackStack() },
                onOpenMap = { navController.navigate("selectLocation") }
            )
        }
        composable("editNote/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            EditNoteScreen(
                noteId = noteId,
                navController = navController
            )
        }
        composable("map") {
            val viewModel = hiltViewModel<MapViewModel>()
            MapScreen(viewModel = viewModel)
        }
        composable("selectLocation") {
            val viewModel = hiltViewModel<MapViewModel>()
            MapScreen(
                viewModel = viewModel,
                onLocationSelected = { latitude, longitude ->
                    val addNoteViewModel = navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<AddNoteViewModel>("addNoteViewModel")
                    addNoteViewModel?.setLocation(latitude, longitude)
                    navController.popBackStack()
                }
            )
        }
        composable("drawing/{imageUrl}") { backStackEntry ->
            val imgUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            DrawingScreen(
                imageUrl = imgUrl,
                onSave = { _ ->
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
        composable("preview/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            NotePreviewScreen(
                noteId = noteId,
                onEditClick = { navController.navigate("editNote/$noteId") }
            )
        }
    }
}
