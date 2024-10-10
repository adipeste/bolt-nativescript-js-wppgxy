package org.nativescript.samplejs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.compose.setContent

class LicensesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LicensesScreen()
            }
        }
    }
}

@Composable
fun LicensesScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        item {
            Text(
                "OpenStreetMap Attribution",
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Map data © OpenStreetMap contributors, licensed under ODbL.")
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                "Third-Party Licenses",
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val licenses = listOf(
            "osmdroid - Version 6.1.13 - Apache License 2.0",
            "Retrofit - Version 2.9.0 - Apache License 2.0",
            "OkHttp - Version 4.10.0 - Apache License 2.0",
            "Kotlin - Version 1.8.0 - Apache License 2.0",
            "AndroidX Core - Version 1.9.0 - Apache License 2.0",
            "AndroidX AppCompat - Version 1.6.1 - Apache License 2.0",
            "Google Material Design - Version 1.8.0 - Apache License 2.0",
            "Mapsforge - Version 0.15.0 - GNU Lesser General Public License",
            "Google Play Services Location - Version 21.0.1 - Android Software Development Kit License",
            "Kotlinx Coroutines - Version 1.6.4 - Apache License 2.0",
            "AndroidX Room - Version 2.5.0 - Apache License 2.0",
            "AndroidX Lifecycle - Version 2.5.1 - Apache License 2.0",
            "AndroidX Compose - Version 1.3.3 - Apache License 2.0"
        )

        items(licenses) { license ->
            Text(license)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}