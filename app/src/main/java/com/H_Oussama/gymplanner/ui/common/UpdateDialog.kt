package com.H_Oussama.gymplanner.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.H_Oussama.gymplanner.data.model.GitHubRelease

@Composable
fun UpdateDialog(
    release: GitHubRelease,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit,
    isForceUpdate: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Available") },
        text = { Text("A new version (${release.name}) is available. Do you want to update now?\n\nRelease notes:\n${release.body}") },
        confirmButton = {
            TextButton(onClick = onUpdateClick) {
                Text("Update")
            }
        },
        dismissButton = {
            if (!isForceUpdate) {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        }
    )
} 