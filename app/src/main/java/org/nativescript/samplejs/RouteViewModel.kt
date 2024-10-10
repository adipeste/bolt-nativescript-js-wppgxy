package org.nativescript.samplejs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadNode
import java.text.SimpleDateFormat
import java.util.*

class RouteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RouteRepository
    private val _route = MutableStateFlow<Road?>(null)
    val route: StateFlow<Road?> = _route
    private val _destination = MutableStateFlow<GeoPoint?>(null)
    val destination: StateFlow<GeoPoint?> = _destination
    private val _navigationInstructions = MutableStateFlow<List<RoadNode>>(emptyList())
    val navigationInstructions: StateFlow<List<RoadNode>> = _navigationInstructions
    private val _nextTurn = MutableStateFlow<String>("")
    val nextTurn: StateFlow<String> = _nextTurn
    private val _estimatedArrivalTime = MutableStateFlow<String>("")
    val estimatedArrivalTime: StateFlow<String> = _estimatedArrivalTime

    private lateinit var offlineNavigationService: OfflineNavigationService

    init {
        val routeDao = RouteDatabase.getDatabase(application).routeDao()
        repository = RouteRepository(routeDao)
    }

    fun setOfflineNavigationService(service: OfflineNavigationService) {
        offlineNavigationService = service
        viewModelScope.launch {
            offlineNavigationService.currentRoute.collect { road ->
                _route.value = road
                updateNavigationInfo(road)
            }
        }
    }

    fun searchAndSetDestination(query: String) {
        viewModelScope.launch {
            val result = repository.searchLocation(query)
            result?.let {
                _destination.value = it
                offlineNavigationService.setDestination(it)
            }
        }
    }

    fun cancelRoute() {
        _route.value = null
        _destination.value = null
        _navigationInstructions.value = emptyList()
        _nextTurn.value = ""
        _estimatedArrivalTime.value = ""
        offlineNavigationService.setDestination(GeoPoint(0.0, 0.0)) // Reset destination
    }

    fun setUserPreferences(preferences: UserPreferences) {
        offlineNavigationService.setUserPreferences(preferences)
    }

    private fun updateNavigationInfo(road: Road?) {
        road?.let {
            _navigationInstructions.value = it.mNodes
            _nextTurn.value = it.mNodes.firstOrNull()?.mInstructions ?: "No turn information"
            val eta = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() + (it.mDuration * 1000).toLong()))
            _estimatedArrivalTime.value = eta
        }
    }

    fun openFavoritesScreen() {
        // This would typically launch a new activity or fragment
        // For now, we'll just print a message
        println("Opening Favorites Screen")
    }
}