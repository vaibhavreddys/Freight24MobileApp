package com.freight24.connect.feature.clients.views

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.connect.domain.model.ClientAddress
import com.freight24.connect.domain.model.ClientBranch
import com.freight24.connect.domain.model.ClientContact
import com.freight24.connect.domain.model.ClientDetails
import com.freight24.connect.feature.clients.domain.viewmodel.ClientProfileViewModel
import com.freight24.connect.domain.model.VerificationDocument
import com.freight24.ui.UiState
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.launchPdfPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ClientProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { snackbar.showSnackbar(it, duration = SnackbarDuration.Short) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            FreightTopAppBar(
                title = { Text("Client Profile", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when (val current = state) {
                UiState.Idle, UiState.Loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                is UiState.Error ->
                    Box(
                        Modifier.fillMaxSize().clickable(onClick = viewModel::retry),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(current.message)
                    }
                is UiState.Success -> ClientProfileContent(current.data, viewModel)
            }
        }
    }
}

@Composable
private fun ClientProfileContent(client: ClientDetails, viewModel: ClientProfileViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(client.company.displayName ?: client.company.legalName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (client.company.displayName != null) Text(client.company.legalName, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                client.tagCode?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                client.status?.let { AssistChip(onClick = {}, label = { Text(it.replaceFirstChar(Char::uppercase)) }) }
                client.profileVerificationStatus?.let { AssistChip(onClick = {}, label = { Text("Documents: $it") }) }
            }
        }

        InfoSection("Company", Icons.Default.Business) {
            DetailRow("Client type", client.clientType)
            DetailRow("Company type", client.company.companyType)
            DetailRow("PAN", client.company.panNumber)
            DetailRow("GSTIN", client.company.gstNumber)
            DetailRow("Email", client.company.email)
            DetailRow("Phone", client.company.phone, dial = true)
            DetailRow("Alternate phone", client.company.alternatePhone, dial = true)
            DetailRow("Website", client.company.website)
        }

        InfoSection("Addresses", Icons.Default.LocationOn) {
            AddressBlock("Registered", client.registeredAddress)
            AddressBlock("Billing", client.billingAddress)
            AddressBlock("POD", client.podAddress)
        }

        InfoSection("Contacts (${client.contacts.size})", Icons.Default.Person) {
            client.contacts.ifEmpty { null }?.forEachIndexed { index, contact ->
                ContactBlock(contact)
                if (index < client.contacts.lastIndex) HorizontalDivider(Modifier.padding(vertical = 10.dp))
            } ?: Text("No contacts available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        InfoSection("Branches (${client.branches.size})", Icons.Default.LocationOn) {
            client.branches.ifEmpty { null }?.forEach { branch -> BranchBlock(branch, viewModel) }
                ?: Text("No branches available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        InfoSection("Documents", Icons.Default.Description) {
            client.documents.forEach { document ->
                DocumentRow(document) {
                    val id = document.documentId ?: return@DocumentRow
                    viewModel.getDocumentSignedUrl(id) { url ->
                        launchPdfPreview(url, context)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun InfoSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider()
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String?, dial: Boolean = false) {
    if (value.isNullOrBlank()) return
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().let { if (dial) it.clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$value"))) } else it },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.4f))
        Text(value, modifier = Modifier.weight(0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (dial) Icon(Icons.Default.Phone, contentDescription = "Call $value", modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AddressBlock(label: String, address: ClientAddress?) {
    val formatted = address?.formatted()
    if (formatted.isNullOrBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(formatted, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ContactBlock(contact: ClientContact) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(contact.fullName, fontWeight = FontWeight.Medium)
        contact.designation?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        DetailRow("Phone", contact.phone, dial = true)
        DetailRow("Alternate", contact.alternatePhone, dial = true)
        contact.email?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(it)
            }
        }
        val roles = contact.clientRoles.mapNotNull { it.name ?: it.code }
        if (roles.isNotEmpty()) Text(roles.joinToString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun BranchBlock(branch: ClientBranch, viewModel: ClientProfileViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(branch.branchName, fontWeight = FontWeight.Medium)
                    Text(listOfNotNull(branch.branchCode, branch.branchTypeName, branch.status).joinToString(" | "), style = MaterialTheme.typography.bodySmall)
                }
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
            if (expanded) {
                AddressBlock("Branch address", branch.address)
                AddressBlock("Billing address", branch.billingAddress)
                branch.gst?.let { DetailRow("GSTIN", it.gstin) }
                AddressBlock("POD address", branch.podAddress)
                branch.contacts.forEach { ContactBlock(it) }
                branch.documents.forEach { document ->
                    DocumentRow(document) {
                        val id = document.documentId ?: return@DocumentRow
                        viewModel.getDocumentSignedUrl(id, branch.branchId) { url ->
                            launchPdfPreview(url, context)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentRow(document: VerificationDocument, onClick: () -> Unit) {
    val available = document.documentId != null && !document.url.isNullOrBlank()
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = available, onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(document.type.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase), fontWeight = FontWeight.Medium)
            Text(document.status.replace('_', ' ').replaceFirstChar(Char::uppercase), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (available) Text("View", color = MaterialTheme.colorScheme.primary)
    }
}

private fun ClientAddress.formatted(): String =
    listOfNotNull(line1, line2, area?.name, city?.name, state?.name, pinCode)
        .filter { it.isNotBlank() }
        .joinToString(", ")
