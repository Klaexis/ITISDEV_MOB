package com.itisdev.application

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.maps.android.PolyUtil
import java.io.IOException

class evacuation : AppCompatActivity(), OnMapReadyCallback, OnDataFetchedListener2  {

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    private lateinit var mMap: GoogleMap
    private var isMapReady = false
    private lateinit var currentLocation: LatLng
    private lateinit var destination: LatLng
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var navigationPolyline: Polyline? = null

    private lateinit var spinner: Spinner
    private val sharedPreferences by lazy { getSharedPreferences("MyPrefs", MODE_PRIVATE) }
    private val firestore: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.evac)

        initMap()

        spinner = findViewById(R.id.spinner)
        // Retrieve the saved position
        val savedPosition = sharedPreferences.getInt("selectedPosition", 0)
        spinner.setSelection(savedPosition)

        val getEvacCenter = getEvac(this)
        getEvacCenter.execute()

        setupFooter() // Call the footer setup function
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestLocationPermission()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true

            // Get the current location using FusedLocationProviderClient
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    currentLocation = latLng
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                    // Draw navigation route
                    drawNavigationRoute()
                }
            }
        }
    }

    override fun onDataFetched(data: List<modelEvac>) {
        // Set up the spinner
        spinner = findViewById(R.id.spinner)
        val adapter = ArrayAdapter(this@evacuation, android.R.layout.simple_spinner_item, data.map { it.evacName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        //Retrieve the saved position
        val savedPosition = sharedPreferences.getInt("selectedPosition", 0)
        spinner.setSelection(savedPosition)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Clear previous navigation route and marker
                if (navigationPolyline != null) {
                    navigationPolyline!!.remove()
                }
                mMap.clear() // Clear the map

                val selectedEvacAddress = data[position].evacAddress

                sharedPreferences.edit().putInt("selectedPosition", position).apply()

                addMarkerFromAddress(selectedEvacAddress)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Remove the marker
                mMap.clear()
                destination = LatLng(0.0, 0.0) // Initialize destination with a default value
            }
        }
    }

    private fun addMarkerFromAddress(address: String) {
        val geocoder = Geocoder(this)
        try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses!!.isNotEmpty()) {
                val latLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(address))
                destination = latLng
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                // Draw navigation route
                drawNavigationRoute()
            } else {
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Error finding address", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawNavigationRoute() {
        if (navigationPolyline != null) {
            navigationPolyline!!.remove()
        }

        if (::destination.isInitialized && ::currentLocation.isInitialized) {
            val url = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=${currentLocation.latitude},${currentLocation.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&mode=driving" +
                    "AIzaSyCRpGom0CUtmivji58dzqwulOuhOy4ppHY" // Replace with your actual API key

            val request = JsonObjectRequest(Request.Method.GET, url, null, { response ->
                val routes = response.getJSONArray("routes")
                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val overviewPolyline = route.getJSONObject("overview_polyline")
                    val polylineString = overviewPolyline.getString("points")

                    val polylineList = PolyUtil.decode(polylineString)
                    val polylineOptions = PolylineOptions()
                    polylineOptions.color(Color.BLUE)
                    polylineOptions.width(10f)

                    for (point in polylineList) {
                        polylineOptions.add(point)
                    }

                    // Add the polyline to the map on the main thread
                    runOnUiThread {
                        navigationPolyline = mMap.addPolyline(polylineOptions)
                    }
                } else {
                    Toast.makeText(this, "No routes found", Toast.LENGTH_SHORT).show()
                }
            }, { error ->
                Toast.makeText(this, "Error drawing navigation route: ${error.message}", Toast.LENGTH_SHORT).show()
            })

            // Execute the request on a background thread
            val queue = Volley.newRequestQueue(this)
            queue.add(request)
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )!= PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val locationRequest = LocationRequest.Builder(10000L)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(10000L)
            .setMaxUpdateDelayMillis(20000L)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult.locations.isNotEmpty()) {
                        val location = locationResult.lastLocation
                        val latLng = LatLng(location!!.latitude, location!!.longitude)
                        currentLocation = latLng
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                        // Draw navigation route
                        drawNavigationRoute()
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isMapReady) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        mMap.isMyLocationEnabled = true
                    }
                    mMap.uiSettings.isMyLocationButtonEnabled = true

                    // Get the current location using FusedLocationProviderClient
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val latLng = LatLng(location.latitude, location.longitude)
                            currentLocation = latLng
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                            // Draw navigation route
                            drawNavigationRoute()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
    }
}