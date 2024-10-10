package org.nativescript.samplejs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MapDownloadActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MapDownloadScreen()
            }
        }
    }
}

@Composable
fun MapDownloadScreen(viewModel: MapDownloadViewModel = viewModel()) {
    var selectedRegion by remember { mutableStateOf("") }
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select a region to download:")
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenu(
            expanded = false,
            onDismissRequest = { },
        ) {
            viewModel.availableRegions.forEach { region ->
                DropdownMenuItem(onClick = {
                    selectedRegion = region
                }) {
                    Text(region)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.downloadMap(selectedRegion) },
            enabled = selectedRegion.isNotEmpty()
        ) {
            Text("Download Map")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (downloadProgress > 0) {
            LinearProgressIndicator(
                progress = downloadProgress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}