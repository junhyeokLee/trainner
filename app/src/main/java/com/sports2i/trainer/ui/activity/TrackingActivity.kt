package com.sports2i.trainer.ui.activity

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrackingData
import com.sports2i.trainer.data.model.TrainingGroupStatus
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.data.model.TrainingSubDetailInsert
import com.sports2i.trainer.data.model.TrainingSubResponse
import com.sports2i.trainer.data.model.WeatherData
import com.sports2i.trainer.databinding.ActivityTrackingBinding
import com.sports2i.trainer.ui.dialog.CancelTrackingDialog
import com.sports2i.trainer.utils.ACTION_PAUSE_SERVICE
import com.sports2i.trainer.utils.ACTION_START_OR_RESUME_SERVICE
import com.sports2i.trainer.utils.ACTION_STOP_SERVICE
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.FileUtil.getBitmapFromVector
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.MAP_ZOOM
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.POLYLINE_WIDTH
import com.sports2i.trainer.utils.PolyLine
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.utils.TrackingService
import com.sports2i.trainer.utils.TrackingUtility
import com.sports2i.trainer.viewmodel.TrackingViewModel
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Calendar
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG_TAG = "CancelDialog"

@AndroidEntryPoint
class TrackingActivity : BaseActivity<ActivityTrackingBinding>({ ActivityTrackingBinding.inflate(it)}) {

    private val trackingViewModel: TrackingViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<PolyLine>()
    private var map: GoogleMap? = null
    private var currentTimeInMillis = 0L
    private var currentMarker: Marker? = null
    private var previousMarkerAnimator: Animator? = null // 이전 마커 애니메이터를 추적하기 위한 변수 추가

    private var exerciseInfo:  TrainingGroupStatus.TrainingGroupStatusExercise? = null
    private var exerciseId:String = ""
    private var userId:String = ""
    private var selectedDate:String = ""
    private var trainingTime:String = ""

    private var trainingStartTime: String = ""
    private var trainingEndTime: String = ""

    private var recordingStartTime: Long? = null
    private var recordingEndTime: Long? = null

    private var startLongitude: Double? = 0.0
    private var startLatitude: Double? = 0.0
    private var endLongitude: Double? = 0.0
    private var endLatitude: Double? = 0.0
    private var weatherData: WeatherData? = null
    private var weather : String = ""
    private var weatherDetail: String = ""
    private var temp: Double = 0.0
    private var humidity: Int = 0
    private var wind: Double = 0.0
    private var videoUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Preferences.init(this, Preferences.DB_USER_INFO)
        binding.mapView.onCreate(savedInstanceState)

        init()
        setFunction()

        this@TrackingActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun init(){
        exerciseId = intent.getStringExtra("exerciseId")?:""
        userId = intent.getStringExtra("userId")?:""
        trainingTime = intent.getStringExtra("trainingTime")?:""
        selectedDate = intent.getStringExtra("selectedDate")?:""
        exerciseInfo = intent.getParcelableExtra("exerciseInfo")

        recordingStartTime = null // 초기화

        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
            addFirstMarker() // 첫 번째 마커 추가
        }
        subscribeToObservers()

    }

    private fun setFunction(){
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        binding.btnCurrentLocation.setOnClickListener {
            moveCameraToCurrentLocation()
        }

    }


    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(this, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(this, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(this, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
            binding.tvTimer.text = formattedTime

            var distanceInMeters = 0
            for (polyLine in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolyLineLength(polyLine).toInt()
            }
            val distanceInKM = "${distanceInMeters / 1000f}"
            binding.tvDistance.text = distanceInKM.toString()

            val avgSpeed = round((distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            binding.tvSpeed.text = avgSpeed.toString()

            val caloriesBurned = ((distanceInMeters / 1000f) * 80).toInt()
            binding.tvCalories.text = caloriesBurned.toString()

            // 수영에서의 칼로리 소비량 계산을 위한 상수
            val CALORIES_PER_HOUR_PER_KILOGRAM = 8 // 1시간 당 1kg 당 소비되는 칼로리
            // 기존 코드에서 칼로리 소비량 계산 부분 수정
            val swimCaloriesBurned = ((currentTimeInMillis / (1000f * 60 * 60)) * 80 * CALORIES_PER_HOUR_PER_KILOGRAM).toInt()
        })

        this.trainingViewModel.trainingSubInsertState.observe(this) {
            when (it) {
                is NetworkState.Success -> {
                    handlerTrainingSubInsertSuccess(it.data.data)
                    val intent = Intent()
                    intent.putExtra("trainingSub",it.data.data)
                    intent.putExtra("trackingSuccess", true)
                    setResult(Activity.RESULT_OK, intent)
                    stopRun()
                    Global.showBottomSnackBar(binding.root, "운동종료")
                }
                is NetworkState.Error -> {
                    Global.showBottomSnackBar(binding.root, "등록 실패하였습니다")
                    finish()
                }
                is NetworkState.Loading -> {
                }
            }
        }
    }

    private fun handlerTrainingSubInsertSuccess(trainingSubResponse: TrainingSubResponse) {
        if(trainingSubResponse == null) {
            Global.showBottomSnackBar(binding.root, "등록 실패하였습니다")
            finish()
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            if (recordingStartTime == null) {
                recordingStartTime = System.currentTimeMillis()
            }
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun showSaveTrackingDialog(trackingData:TrackingData,startLatitude: Double,startLongitude: Double,endLatitude: Double,endLongitude: Double) {
        CancelTrackingDialog().apply {
            setYesListener {
                trackingViewModel.saveTrackingData(trackingData)
                mapAndWeatherInsert(startLatitude,startLongitude,endLatitude,endLongitude)
            }
        }.show(supportFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        binding.tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        finish()
        back()
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis > 0L) {
            binding.btnToggleRun.icon = getDrawable(R.drawable.ic_tracking_play)
            binding.btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            binding.btnToggleRun.icon = getDrawable(R.drawable.ic_tracking_pause)
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (poluline in pathPoints) {
            for (pos in poluline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp -> // bmp은 현재위치를 비트맵 이미지로 만드는 변수
            var distanceInMeters = 0
            for (polyLine in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolyLineLength(polyLine).toInt()
            }

//            val distanceInKM = distanceInMeters / 1000f
//            val avgSpeed = round((distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
//            val caloriesBurned = ((distanceInMeters / 1000f) * 80).toInt()

            // 운동을 종료할 때만 recordingEndTime을 설정.
            recordingEndTime = System.currentTimeMillis()

            val startTimeFormattedInsert = DateTimeUtil.formatTimeTrainingSubDetail(recordingStartTime!!)
            val endTimeFormattedInsert = DateTimeUtil.formatTimeTrainingSubDetail(recordingEndTime!!)

            trainingStartTime = startTimeFormattedInsert
            trainingEndTime = endTimeFormattedInsert

            endLatitude = pathPoints.last().last().latitude
            endLongitude = pathPoints.last().last().longitude

            val trackingData = TrackingData(pathPoints,exerciseId,userId,trainingTime,selectedDate)
            showSaveTrackingDialog(trackingData,startLatitude!!,startLongitude!!,endLatitude!!,endLongitude!!)
        }
    }

    private fun mapAndWeatherInsert(startLatitude : Double,startLongitude:Double,endLatitude:Double,endLongitude:Double) {
        val apiKey = "4f424c8f88afd109f42c3a4c89832f7b"
        val apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=$endLatitude&lon=$endLongitude&units=metric&appid=$apiKey"
        val client = OkHttpClient()
        val request = Request.Builder().url(apiUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val weatherGson = Gson().fromJson(responseBody, WeatherData::class.java)
                    weatherData = weatherGson
                    weather = weatherData!!.weather[0].main
                    weatherDetail = weatherData!!.weather[0].description
                    temp = weatherData!!.main.temp
                    humidity = weatherData!!.main.humidity
                    wind = weatherData!!.wind.speed

                val trainingSubInsert = TrainingSubDetailInsert(
                    exerciseId!!, userId!!, trainingTime!!, selectedDate!!, trainingStartTime, trainingEndTime, videoUrl,
                    startLongitude, startLatitude, endLongitude, endLatitude, weather, weatherDetail, temp, humidity.toDouble(), wind)

                    trainingViewModel.requestTrainingSubInsert(trainingSubInsert) // 트레이닝 서브 데이터 저장
                }
                else {
                    Global.showBottomSnackBar(binding.root, "등록 실패하였습니다")
                    finish()
                }

            }
        })
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polyLineOptions = PolylineOptions()
                .color(getColor(R.color.primary))
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polyLineOptions)
        }
    }
    @SuppressLint("MissingPermission")
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polyLineOptions = PolylineOptions()
                .color(getColor(R.color.primary))
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
                map?.addPolyline(polyLineOptions)

            // 마커의 애니메이터를 생성하고 시작
            val markerIcon = BitmapDescriptorFactory.fromBitmap(getBitmapFromVector(this, R.drawable.ic_circle))
            val markerOptions = MarkerOptions()
                .position(lastLatLng)
                .anchor(0.5f, 0.5f)
                .icon(markerIcon)
            val newMarker = map?.addMarker(markerOptions)
            val markerAnimator = ValueAnimator.ofFloat(0.6f, 1.0f)
            markerAnimator.repeatMode = ValueAnimator.REVERSE
            markerAnimator.repeatCount = ValueAnimator.INFINITE
            markerAnimator.interpolator = AccelerateDecelerateInterpolator()
            markerAnimator.duration = 1000
            markerAnimator.addUpdateListener { animator ->
                val alpha = animator.animatedValue as Float
                newMarker?.alpha = alpha
            }
            markerAnimator.start()

            // 이전 마커 애니메이터를 중지하고 삭제합니다.
            previousMarkerAnimator?.cancel()
            previousMarkerAnimator = markerAnimator

            // 이전 마커를 새로운 마커로 업데이트합니다.
            currentMarker?.remove()
            currentMarker = newMarker

            // 카메라 이동
            moveCameraToUser()
        }
    }
    @SuppressLint("MissingPermission")
    private fun addFirstMarker() {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    startLatitude = it.latitude
                    startLongitude = it.longitude
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    val markerIcon = BitmapDescriptorFactory.fromBitmap(getBitmapFromVector(this, R.drawable.ic_circle_hollow))
                    val markerOptions = MarkerOptions()
                        .position(currentLatLng)
//                        .anchor(0.5f, 0.5f)
                        .icon(markerIcon)
                    map?.addMarker(markerOptions)
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
    }

    // 현재 위치로 이동하는 함수 추가
    @SuppressLint("MissingPermission")
    private fun moveCameraToCurrentLocation() {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
    }

    private fun sendCommandToService(action: String) =
        Intent(this, TrackingService::class.java).also {
            it.action = action
            this.startService(it)
        }



    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(!isTracking && currentTimeInMillis == 0L) {
                back()
                finish()
            } else if(isTracking) {
                sendCommandToService(ACTION_PAUSE_SERVICE)
            } else {
                stopRun()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }

    /*override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }  */

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}

