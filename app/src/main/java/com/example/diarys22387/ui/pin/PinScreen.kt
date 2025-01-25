package com.example.diarys22387.ui.pin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PinScreen(onPinSuccess: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    val correctPin = "1234" // TODO: хранить в шифр. prefs

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter PIN code:")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("PIN") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (pin == correctPin) {
                onPinSuccess()
            }
        }) {
            Text("Validate PIN")
        }
    }
}
