package org.nativescript.samplejs

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.bonuspack.routing.RoadNode
import java.util.*

class OfflineNavigationService : Service() {
    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var roadManager: RoadManager
    private lateinit var routeCache: RouteCache

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation

    private val _currentRoute = MutableStateFlow<Road?>(null)
    val currentRoute: StateFlow<Road?> = _currentRoute

    private val _navigationInstructions = MutableStateFlow<List<RoadNode>>(emptyList())
    val navigationInstructions: StateFlow<List<RoadNode>> = _navigationInstructions

    private val _nextTurn = MutableStateFlow<String>("")
    val nextTurn: StateFlow<String> = _nextTurn

    private val _estimatedArrivalTime = MutableStateFlow<String>("")
    val estimatedArrivalTime: StateFlow<String> = _estimatedArrivalTime

    private var destination: GeoPoint? = null
    private var userPreferences: UserPreferences? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        roadManager = OSRMRoadManager(this, "MyRouteApp")
        (roadManager as OSRMRoadManager).setMean(OSRMRoadManager.MEAN_BY_CAR)
        routeCache = RouteCache(this)
    }

    fun setUserPreferences(preferences: UserPreferences) {
        userPreferences = preferences
        (roadManager as OSRMRoadManager).apply {
            setMean(when (preferences.vehicleType) {
                VehicleType.CAR -> OSRMRoadManager.MEAN_BY_CAR
                VehicleType.TRUCK -> OSRMRoadManager.MEAN_BY_TRUCK
                VehicleType.BICYCLE -> OSRMRoadManager.MEAN_BY_BICYCLE
            })
            setOptions(when {
                preferences.avoidTolls -> "avoid_tolls"
                preferences.avoidHighways -> "avoid_highways"
                else -> ""
            })
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newLocation = GeoPoint(location.latitude, location.longitude)
                    _currentLocation.value = newLocation
                    updateRouteIfNeeded(newLocation)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun updateRouteIfNeeded(currentLocation: GeoPoint) {
        val currentRoute = _currentRoute.value
        if (currentRoute != null && destination != null) {
            if (!isOnRoute(currentLocation, currentRoute)) {
                recalculateRoute(currentLocation, destination!!)
            } else {
                updateNavigationInstructions(currentLocation, currentRoute)
            }
        }
    }

    private fun isOnRoute(location: GeoPoint, road: Road): Boolean {
        val tolerance = 50.0 // 50 meters tolerance
        return road.mRouteHigh.any { routePoint ->
            location.distanceToAsDouble(GeoPoint(routePoint.latitude, routePoint.longitude)) < tolerance
        }
    }

    private fun recalculateRoute(start: GeoPoint, end: GeoPoint) {
        CoroutineScope(Dispatchers.Default).launch {
            val cachedRoute = routeCache.getRoute(start, end)
            if (cachedRoute != null) {
                _currentRoute.value = cachedRoute
                updateNavigationInstructions(start, cachedRoute)
            } else {
                val waypoints = ArrayList<GeoPoint>()
                waypoints.add(start)
                waypoints.add(end)
                val road = roadManager.getRoad(waypoints)
                _currentRoute.value = road
                updateNavigationInstructions(start, road)
                routeCache.saveRoute(start, end, road)
            }
        }
    }

    private fun updateNavigationInstructions(currentLocation: GeoPoint, road: Road) {
        val remainingNodes = road.mNodes.dropWhile { node ->
            currentLocation.distanceToAsDouble(GeoPoint(node.mLocation.latitude, node.mLocation.longitude)) > 50
        }
        _navigationInstructions.value = remainingNodes
        
        // Update next turn
        _nextTurn.value = remainingNodes.firstOrNull()?.mInstructions ?: "No turn information"
        
        // Update estimated arrival time
        val remainingDistance = remainingNodes.sumOf { it.mLength }
        val averageSpeed = 50.0 // km/h, you might want to adjust this or use actual speed
        val estimatedTimeInHours = remainingDistance / averageSpeed
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, estimatedTimeInHours.toInt())
        calendar.add(Calendar.MINUTE, ((estimatedTimeInHours % 1) * 60).toInt())
        _estimatedArrivalTime.value = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    fun setDestination(destination: GeoPoint) {
        this.destination = destination
        _currentLocation.value?.let { currentLocation ->
            recalculateRoute(currentLocation, destination)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    inner class LocalBinder : Binder() {
        fun getService(): OfflineNavigationService = this@OfflineNavigationService
    }
}

data class UserPreferences(
    val vehicleType: VehicleType,
    val avoidTolls: Boolean,
    val avoidHighways: Boolean
)

enum class VehicleType {
    CAR, TRUCK, BICYCLE
}