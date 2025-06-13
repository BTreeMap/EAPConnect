package org.joefang.wearos.eap.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.ui.focus.FocusRequester
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn

@Composable
fun WiFiConfigScreen(onSubmit: (String, String, String, String) -> Unit) {
    var ssid by remember { mutableStateOf("") }
    var identity by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedTextField(
                value = ssid,
                onValueChange = { ssid = it },
                label = { Text("SSID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = identity,
                onValueChange = { identity = it },
                label = { Text("Identity") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = domain,
                onValueChange = { domain = it },
                label = { Text("Domain") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            Button(
                onClick = { onSubmit(ssid, identity, password, domain) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.Filled.Wifi, contentDescription = null, Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Network")
            }
        }
    }
}