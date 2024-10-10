package org.nativescript.samplejs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class FavoritesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FavoritesScreen()
            }
        }
    }
}

@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel = viewModel()) {
    val favorites by viewModel.favorites.collectAsState()
    var newFavoriteName by remember { mutableStateOf("") }
    var newFavoriteAddress by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Favorites", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = newFavoriteName,
            onValueChange = { newFavoriteName = it },
            label = { Text("Favorite Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = newFavoriteAddress,
            onValueChange = { newFavoriteAddress = it },
            label = { Text("Address") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.addFavorite(newFavoriteName, newFavoriteAddress)
                newFavoriteName = ""
                newFavoriteAddress = ""
            }
        ) {
            Text("Add Favorite")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(favorites) { favorite ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(favorite.name, style = MaterialTheme.typography.subtitle1)
                        Text(favorite.address, style = MaterialTheme.typography.body2)
                    }
                    IconButton(onClick = { viewModel.removeFavorite(favorite) }) {
                        Text("🗑️")
                    }
                }
            }
        }
    }
}