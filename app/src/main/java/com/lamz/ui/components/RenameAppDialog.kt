package com.lamz.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.lamz.data.model.InstalledApp

@Composable
fun RenameAppDialog(
    app: InstalledApp,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(app.displayLabel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Rename App", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Default name: ${app.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    label = { Text("App Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_app_input")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                modifier = Modifier.testTag("rename_app_confirm_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("rename_app_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
