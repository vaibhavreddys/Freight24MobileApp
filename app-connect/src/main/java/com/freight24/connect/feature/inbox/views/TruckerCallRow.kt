package com.freight24.connect.feature.inbox.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.common.util.DateTimeUtils
import com.freight24.ui.components.rememberCallAction

private val GreenBrand = Color(0xFF10B981)
private val RedClose = Color(0xFFEF4444)
private val TextDark = Color(0xFF0F172A)
private val TextGrey = Color(0xFF64748B)

@Composable
fun TruckerCallRow(
    name: String,
    phone: String,
    expressedAt: String,
    onReject: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val makeCall = rememberCallAction(phone)
    val relativeTime = remember(expressedAt) { DateTimeUtils.formatRelativeTime(expressedAt) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle with first letter
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFE2E8F0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Interested $relativeTime",
                style = MaterialTheme.typography.labelSmall,
                color = TextGrey
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Call button
        Button(
            onClick = { makeCall() },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenBrand),
            modifier = Modifier.height(36.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(
                Icons.Outlined.Call,
                contentDescription = "Call",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Call", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (onReject != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                onClick = onReject,
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFEF2F2),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Reject",
                        tint = RedClose,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
