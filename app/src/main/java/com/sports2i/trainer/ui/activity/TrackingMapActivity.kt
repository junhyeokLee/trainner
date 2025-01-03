package com.sports2i.trainer.ui.activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.sports2i.trainer.R
import com.sports2i.trainer.databinding.ActivityTrackingMapBinding
import com.sports2i.trainer.utils.FileUtil
import com.sports2i.trainer.utils.FileUtil.getBitmapFromVector
import com.sports2i.trainer.utils.FileUtil.resizeBitmap
import com.sports2i.trainer.utils.POLYLINE_WIDTH
import com.sports2i.trainer.viewmodel.TrackingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingMapActivity: BaseActivity<ActivityTrackingMapBinding>({ ActivityTrackingMapBinding.inflate(it)}),
    OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val trackingViewModel: TrackingViewModel by viewModels()

    private var exerciseId = ""
    private var userId = ""
    private var selectedDate = ""
    private var trainingTime: String = ""
    private var startLatitude: Double = 0.0
    private var startLongitude: Double = 0.0
    private var endLatitude: Double = 0.0
    private var endLongitude: Double = 0.0
    private var distance: Double = 0.0
    private var caloriesBurned: Double = 0.0
    private var speed: Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()

        exerciseId = intent.getStringExtra("exerciseId")?:""
        userId = intent.getStringExtra("userId")?:""
        trainingTime = intent.getStringExtra("trainingTime")?:""
        selectedDate = intent.getStringExtra("selectedDate")?:""
        startLatitude = intent.getDoubleExtra("startLatitude",0.0)
        startLongitude = intent.getDoubleExtra("startLongitude",0.0)
        endLatitude = intent.getDoubleExtra("endLatitude",0.0)
        endLongitude = intent.getDoubleExtra("endLongitude",0.0)
        distance = intent.getDoubleExtra("distance",0.0)
        caloriesBurned = intent.getDoubleExtra("caloriesBurned",0.0)
        speed = intent.getDoubleExtra("speed",0.0)

        trackingViewModel.getTrackingData(exerciseId, userId, trainingTime, selectedDate)

        // SupportMapFragment 가져오기
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        binding.btnCurrentLocation.setOnClickListener {
            moveCameraToCurrentLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // UI 설정 변경하여 전체화면 모드로 지도를 표시
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = false
        drawPath()
    }

    private fun drawPath() {
        // 경로를 나타내는 PolylineOptions를 생성하고 구글 맵에 추가
    trackingViewModel.trackingData.observe(this) {
        if (it != null) {
            if (it.pathPoints.isNotEmpty()) {
                val polylineOptions = PolylineOptions()
                    .color(ContextCompat.getColor(this, R.color.primary))
                    .width(POLYLINE_WIDTH)
                    .addAll(it.pathPoints?.flatten() ?: emptyList()) // 경로 좌표 추가
                mMap.addPolyline(polylineOptions)

                // 경로의 첫 번째와 마지막 좌표를 가져와서 마커 추가
                val firstPoint = it.pathPoints.first().firstOrNull()
                val lastPoint = it.pathPoints.last().lastOrNull()
                if (firstPoint != null && lastPoint != null) {
                    addFirstMarker(firstPoint, R.drawable.ic_circle_hollow)
                    addFinishMarker(lastPoint, R.drawable.ic_tracking_finish)
                }

                // 경로가 보이도록 카메라 이동
                val boundsBuilder = LatLngBounds.builder()
                for (polyLine in it.pathPoints?.flatten() ?: emptyList()) {
                    boundsBuilder.include(polyLine)
                }
                val bounds = boundsBuilder.build()
                val padding = resources.getDimensionPixelSize(R.dimen.polyline_padding)
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                mMap.moveCamera(cameraUpdate)

            }
        } else {
            val startBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
                resizeBitmap(
                    getBitmapFromVector(this, R.drawable.ic_circle_hollow), 50, 50
                )
            )
            val endBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
                resizeBitmap(getBitmapFromVector(this, R.drawable.ic_tracking_finish), 50, 50
                )
            )

            val startMarkerOptions = MarkerOptions().position(LatLng(startLatitude, startLongitude)).title("시작점")
            val endMarkerOptions = MarkerOptions().position(LatLng(endLatitude, endLongitude)).title("도착점")
            startMarkerOptions.icon(startBitmapDescriptor)
            endMarkerOptions.icon(endBitmapDescriptor)

            mMap?.addMarker(startMarkerOptions)
            mMap?.addMarker(endMarkerOptions)
            mMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        startLatitude,
                        startLongitude
                    ), 17f
                )
            )
            mMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(endLatitude, endLongitude),
                    17f
                )
            )
        }

        binding.tvDistance.text = distance.toString()
        binding.tvSpeed.text = speed.toString()
        binding.tvCalories.text = caloriesBurned.toString()
    }
}

    private fun addFirstMarker(position: LatLng, @DrawableRes iconRes: Int) {
        val markerIcon = BitmapDescriptorFactory.fromBitmap(resizeBitmap(getBitmapFromVector(this, iconRes), 50, 50))
        val markerOptions = MarkerOptions()
            .position(position)
            .anchor(0.5f, 0.5f)
            .icon(markerIcon)
        mMap?.addMarker(markerOptions)
    }

    private fun addFinishMarker(position: LatLng, @DrawableRes iconRes: Int) {
        val markerIcon = BitmapDescriptorFactory.fromBitmap(resizeBitmap(getBitmapFromVector(this, iconRes), 80, 80))
        val markerOptions = MarkerOptions()
            .position(position)
            .anchor(0.5f, 0.5f)
            .icon(markerIcon)
        mMap?.addMarker(markerOptions)
    }

    @SuppressLint("MissingPermission")
    private fun moveCameraToCurrentLocation() {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }
    }