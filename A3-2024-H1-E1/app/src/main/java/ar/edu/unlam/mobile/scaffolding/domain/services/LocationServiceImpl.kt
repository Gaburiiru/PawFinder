package ar.edu.unlam.mobile.scaffolding.domain.services

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import ar.edu.unlam.mobile.scaffolding.core.utils.hasLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LocationServiceImpl
    @Inject
    constructor(
        private val context: Context,
        private val locationClient: FusedLocationProviderClient,
    ) : LocationService {
        @SuppressLint("MissingPermission")
        override fun requestLocationUpdates(): Flow<LatLng?> =
            callbackFlow {
                if (!context.hasLocationPermission()) {
                    trySend(null)
                    return@callbackFlow
                }
                val request =
                    LocationRequest.Builder(10000L)
                        .setIntervalMillis(10000L)
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .build()
                val locationCallback =
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            locationResult.locations.lastOrNull()?.let {
                                trySend(LatLng(it.latitude, it.longitude))
                            }
                        }
                    }
                locationClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper(),
                )
                awaitClose {
                    locationClient.removeLocationUpdates(locationCallback)
                }
            }

        override fun requestCurrentLocation(): Flow<LatLng?> {
            TODO("Not yet implemented")
        }
    }
