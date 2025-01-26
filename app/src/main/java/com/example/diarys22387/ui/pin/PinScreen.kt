package com.example.diarys22387.ui.pin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PinScreen(
    onPinSuccess: () -> Unit,
    viewModel: PinViewModel = viewModel()
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter PIN code:",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = pin,
            onValueChange = { 
                if (it.length <= 4) {
                    pin = it
                    error = false
                }
            },
            label = { Text("PIN") },
            singleLine = true,
            isError = error,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
            ),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
        )
        
        if (error) {
            Text(
                text = "Invalid PIN",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (viewModel.validatePin(pin)) {
                    onPinSuccess()
                } else {
                    error = true
                }
            },
            enabled = pin.length == 4
        ) {
            Text("Validate PIN")
        }
    }
}
