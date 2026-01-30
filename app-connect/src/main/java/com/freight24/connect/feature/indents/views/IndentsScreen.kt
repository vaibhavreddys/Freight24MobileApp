package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun IndentsScreen(modifier: Modifier = Modifier) {
    IndentsList(
        modifier = modifier,
    )
}

@Composable
fun IndentsList(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Text("List of indents will be here.")
    }
}

@Composable
@Preview(showBackground = true)
fun IndentsScreenPreview() {
    IndentsScreen()
}
