package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonError

@Composable
fun MitmThreatDialog(
    threatMessage: String,
    onDismiss: () -> Unit,
    onViewSecurityLog: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.GppBad,
                contentDescription = "Threat Blocked",
                tint = CrimsonError,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "SSL Connection Intercepted (MITM Defended)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CrimsonError
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "The application detected an untrusted SSL certificate or potential Man-in-the-Middle network proxy attempting to inspect or alter traffic to fromeuropetoyou.com.",
                    fontSize = 13.sp
                )
                Text(
                    text = "Threat Diagnostic: $threatMessage",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Action: The unencrypted session was automatically terminated to protect your private credentials and security keys.",
                    fontSize = 12.sp,
                    color = CrimsonError
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                modifier = Modifier.testTag("dismiss_mitm_dialog_button")
            ) {
                Text("Acknowledge & Protect", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onViewSecurityLog,
                modifier = Modifier.testTag("view_mitm_log_button")
            ) {
                Text("View Security Logs")
            }
        }
    )
}
