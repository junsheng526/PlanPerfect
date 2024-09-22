package com.example.planperfect.view.planning

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.planperfect.R
import com.example.planperfect.data.api.OpenRouteServiceApi
import com.example.planperfect.data.model.DirectionsResponse
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.ActivityViewRouteBinding
import com.example.planperfect.viewmodel.TripViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ViewRouteActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var openRouteServiceApi: OpenRouteServiceApi
    private val tripViewModel: TripViewModel by viewModels()
    private lateinit var binding: ActivityViewRouteBinding

    private val polylines = mutableListOf<PolylineOptions>()
    private val markers = mutableListOf<MarkerOptions>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Initialize the map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize Retrofit for OpenRouteService API
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        openRouteServiceApi = retrofit.create(OpenRouteServiceApi::class.java)

        // Fetch the trip details
        val tripId = "a42009c2-3553-4dd4-a042-9e3dbbd01aa6"
        val dayId = "a42009c2-3553-4dd4-a042-9e3dbbd01aa6-Day-4"
        fetchTripPlaces(tripId, dayId)

        binding.optimizedRouteButton.setOnClickListener {
            lifecycleScope.launch {
                val places = tripViewModel.getPlacesForDay(tripId, dayId)
                if (places.isNotEmpty()) {
                    val optimizedPlaces = floydWarshall(places)
                    drawRoutes(optimizedPlaces)
                    addMarkers(optimizedPlaces)
                } else {
                    Log.e("ViewRouteActivity", "No places found for trip: $tripId, day: $dayId")
                }
            }
        }
    }

    private fun fetchTripPlaces(tripId: String, dayId: String) {
        lifecycleScope.launch {
            val places = tripViewModel.getPlacesForDay(tripId, dayId)
            if (places.isNotEmpty()) {
                drawRoutes(places)
                addMarkers(places)
            } else {
                Log.e("ViewRouteActivity", "No places found for trip: $tripId, day: $dayId")
            }
        }
    }

    private fun drawRoutes(places: List<TouristPlace>) {
        // Clear existing polylines
        polylines.forEach { mMap.clear() }
        polylines.clear()

        // Loop through places and draw routes between each consecutive pair
        for (i in 0 until places.size - 1) {
            val start = "${places[i].longitude},${places[i].latitude}"
            val end = "${places[i + 1].longitude},${places[i + 1].latitude}"
            drawRoute(start, end)
        }
    }

    private fun drawRoute(start: String, end: String) {
        val call = openRouteServiceApi.getDirections(
            apiKey = "5b3ce3597851110001cf62484dce9b1de6da4acb9229465f7ca42db5",
            start = start,
            end = end
        )

        call.enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if (response.isSuccessful) {
                    val directionsResponse = response.body()
                    Log.d("DirectionsResponse", directionsResponse.toString())

                    if (directionsResponse != null && directionsResponse.features.isNotEmpty()) {
                        val geometry = directionsResponse.features[0].geometry.coordinates
                        val polylinePoints = decodePolyline(geometry)
                        val polylineOptions = PolylineOptions().addAll(polylinePoints).width(5f).color(R.color.purple_200)
                        mMap.addPolyline(polylineOptions)
                        polylines.add(polylineOptions) // Store the polyline

                        // Fit the map to the polyline
                        val boundsBuilder = LatLngBounds.Builder()
                        polylinePoints.forEach { boundsBuilder.include(it) }
                        val bounds = boundsBuilder.build()
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    } else {
                        Log.e("ViewRouteActivity", "No routes found in response")
                    }
                } else {
                    Log.e("ViewRouteActivity", "Failed to get directions: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Log.e("ViewRouteActivity", "API call failed: ${t.message}")
            }
        })
    }

    private fun addMarkers(places: List<TouristPlace>) {
        // Clear existing markers
        markers.forEach { mMap.clear() }
        markers.clear()

        for (place in places) {
            val position = LatLng(place.latitude ?: 0.0, place.longitude ?: 0.0)
            val markerOptions = MarkerOptions().position(position).title(place.name).snippet(place.description)
            mMap.addMarker(markerOptions)
            markers.add(markerOptions) // Store the marker
        }
    }

    private fun decodePolyline(coordinates: List<List<Double>>): List<LatLng> {
        val poly = ArrayList<LatLng>()
        coordinates.forEach { coord ->
            poly.add(LatLng(coord[1], coord[0])) // LatLng expects (lat, lng)
        }
        return poly
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Example: Set initial map location
        val initialLocation = LatLng(37.7749, -122.4194) // Default location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }

        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun floydWarshall(places: List<TouristPlace>): List<TouristPlace> {
        val n = places.size
        val dist = Array(n) { DoubleArray(n) { Double.MAX_VALUE } }
        val next = Array(n) { IntArray(n) { -1 } }

        // Initialize distances and next
        for (i in places.indices) {
            for (j in places.indices) {
                if (i == j) dist[i][j] = 0.0
                else {
                    dist[i][j] = calculateDistance(places[i], places[j])
                    if (dist[i][j] < Double.MAX_VALUE) {
                        next[i][j] = j
                    }
                }
            }
        }

        // Floyd-Warshall Algorithm
        for (k in 0 until n) {
            for (i in 0 until n) {
                for (j in 0 until n) {
                    if (dist[i][j] > dist[i][k] + dist[k][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j]
                        next[i][j] = next[i][k]
                    }
                }
            }
        }

        // Reconstruct the optimized route
        val optimizedRoute = mutableListOf<TouristPlace>()
        val visited = BooleanArray(n)
        var currentIndex = 0

        while (optimizedRoute.size < n) {
            optimizedRoute.add(places[currentIndex])
            visited[currentIndex] = true

            var nextIndex = -1
            var minDistance = Double.MAX_VALUE

            for (j in 0 until n) {
                if (!visited[j] && dist[currentIndex][j] < minDistance) {
                    minDistance = dist[currentIndex][j]
                    nextIndex = j
                }
            }

            if (nextIndex == -1) break // No unvisited nodes
            currentIndex = nextIndex
        }

        return optimizedRoute
    }

    private fun calculateDistance(placeA: TouristPlace, placeB: TouristPlace): Double {
        val latA = placeA.latitude ?: 0.0
        val lonA = placeA.longitude ?: 0.0
        val latB = placeB.latitude ?: 0.0
        val lonB = placeB.longitude ?: 0.0

        // Haversine formula to calculate distance
        val earthRadius = 6371.0 // in kilometers
        val dLat = Math.toRadians(latB - latA)
        val dLon = Math.toRadians(lonB - lonA)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latA)) * Math.cos(Math.toRadians(latB)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c // Distance in kilometers
    }
}
