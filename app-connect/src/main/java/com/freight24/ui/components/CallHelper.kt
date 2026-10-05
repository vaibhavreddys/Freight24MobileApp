package com.freight24.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

/**
 * A helper to handle phone call logic with permissions and fallback to dialer.
 */
@Composable
fun rememberCallAction(phone: String): () -> Unit {
    val context = LocalContext.current
    val callIntent =
        remember(phone) {
            Intent(Intent.ACTION_CALL).apply {
                data = "tel:$phone".toUri()
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                try {
                    context.startActivity(callIntent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot make call: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } else {
                try {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply { data = "tel:$phone".toUri() }
                    context.startActivity(dialIntent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open dialer: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    return {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                context.startActivity(callIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot make call: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }
}
