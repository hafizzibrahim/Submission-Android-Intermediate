package com.example.storyapp.view.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.storyapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.LatLngBounds
import com.example.storyapp.data.repository.Result
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.view.ViewModelFactory
import com.example.storyapp.view.detail.DetailActivity
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapsViewModel: MapsViewModel
    private val boundBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupViewModel() {
        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        mapsViewModel = ViewModelProvider(this, factory)[MapsViewModel::class.java]
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        setupViewModel()
        setupStory()
        getMyLocation()
    }

    private fun setupStory() {
        mapsViewModel.getSession().observe(this){
            val token = "Bearer " +it.token
            mapsViewModel.getStoryLocation(token).observe(this){
                when(it){
                    is Result.Loading -> {}
                    is Result.Success -> showMarker(it.data.listStory)
                    is Result.Error -> Toast.makeText(this, it.error, Toast.LENGTH_SHORT).show()
                    else -> {}
                }
            }
        }
    }

    private fun showMarker(data: List<ListStoryItem>) {
        val builder = LatLngBounds.builder() // Buat builder baru

        data.forEach {
            val latLng = it.lat?.let { it1 -> it.lon?.let { it2 -> LatLng(it1, it2) } }
            val marker = latLng?.let { it1 ->
                MarkerOptions()
                    .position(it1)
                    .title(getString(R.string.story_from) + it.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .alpha(0.7f)
                    .snippet(it.description)
            }?.let { it2 ->
                mMap.addMarker(it2)
            }

            if (latLng != null) {
                builder.include(latLng) // Tambahkan marker ke builder
            }

            marker?.tag = it
            mMap.setOnInfoWindowClickListener {
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_DETAIL, it.tag as ListStoryItem)
                }
                startActivity(intent)
            }
        }
        // Setelah menambahkan semua marker ke builder, atur tampilan peta agar mencakup semua marker
        val bounds = builder.build()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 0)
        mMap.animateCamera(cu)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}