package org.nativescript.samplejs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FavoritesRepository
    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites: StateFlow<List<Favorite>> = _favorites

    init {
        val favoritesDao = FavoritesDatabase.getDatabase(application).favoritesDao()
        repository = FavoritesRepository(favoritesDao)
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _favorites.value = repository.getAllFavorites()
        }
    }

    fun addFavorite(name: String, address: String) {
        viewModelScope.launch {
            repository.insertFavorite(Favorite(name = name, address = address))
            loadFavorites()
        }
    }

    fun removeFavorite(favorite: Favorite) {
        viewModelScope.launch {
            repository.deleteFavorite(favorite)
            loadFavorites()
        }
    }
}