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
            NotesListScreen(
                onAddClick = { navController.navigate("addNote") },
                onNoteClick = { noteId -> navController.navigate("editNote/$noteId") },
                onMapClick = { navController.navigate("map") }
            )
        }
        composable("addNote") {
            AddNoteScreen(
                onNoteSaved = { navController.popBackStack() }
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
            MapScreen()
        }
        composable("drawing/{imageUrl}") { backStackEntry ->
            val imgUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            DrawingScreen(
                imageUrl = imgUrl,
                onSave = { newUrl ->
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
    }
}
