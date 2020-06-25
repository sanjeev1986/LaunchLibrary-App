package com.sample.rockets.launch.details

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import android.view.View
import com.sample.rockets.R
import com.sample.rockets.common.BaseActivity
import com.sample.rockets.utils.ui.getMarker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_launch_details.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity displays Launch details such as coordinates on the map, Agency name & mission description
 */
class LaunchDetailsActivity : BaseActivity(), OnMapReadyCallback {
    override fun onMapReady(googleMap: GoogleMap?) {//its been a few yrs since i worked on maps, can't say it GoogleMap will be null, so default kotlin nullable
        launchCoordinates?.apply {
            if (latitude == 0.0 && longitude == 0.0) {
                veil.visibility = View.VISIBLE
            } else {
                val ll = this
                googleMap?.run {
                    val cameraPosition = CameraPosition.Builder()
                        .target(ll)
                        .zoom(8f)
                        .build()
                    animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    addMarker(
                        MarkerOptions()
                            .position(ll)
                            .title(launchTitle)
                            .icon(getMarker(R.drawable.ic_launch_marker))
                    )
                }
            }
        }
    }

    companion object {
        /**
         * Rocket launch name
         */
        const val EXTRA_LAUNCH_NAME = "com.sample.assessment.launch.details.EXTRA_LAUNCH_NAME"
        /**
         * Agency name
         */
        const val EXTRA_LAUNCH_AGENCY = "com.sample.assessment.launch.details.EXTRA_LAUNCH_AGENCY"
        /**
         * Count down time if started
         */
        const val EXTRA_LAUNCH_TIME = "com.sample.assessment.launch.details.EXTRA_LAUNCH_TIME"
        /**
         * Date of laucnh
         */
        const val EXTRA_LAUNCH_DATE = "com.sample.assessment.launch.details.EXTRA_LAUNCH_DATE"
        /**
         * Coordinates of the launch site
         */
        const val EXTRA_LAUNCH_LAT_LNG = "com.sample.assessment.launch.details.EXTRA_LAUNCH_LAT_LNG"
        /**
         * Mission description
         */
        const val EXTRA_LAUNCH_DESC = "com.sample.assessment.launch.details.EXTRA_LAUNCH_DESC"
    }

    private lateinit var launchTitle: String
    private var launchCountDown: Long = 0L
    private var launchCoordinates: LatLng? = null
    private var launchDescription: String? = null
    private var launchAgencyName: String? = null
    private var launchDate: String? = null
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch_details)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        (mapFragment as SupportMapFragment).getMapAsync(this)
        if (savedInstanceState == null) {
            launchTitle = intent.getStringExtra(EXTRA_LAUNCH_NAME)
            launchCountDown = intent.getLongExtra(EXTRA_LAUNCH_TIME, 0L)
            launchCoordinates = intent.getParcelableExtra(EXTRA_LAUNCH_LAT_LNG)
            launchDescription = intent.getStringExtra(EXTRA_LAUNCH_DESC)
            launchAgencyName = intent.getStringExtra(EXTRA_LAUNCH_AGENCY)
            launchDate = intent.getStringExtra(EXTRA_LAUNCH_DATE)
        } else {
            launchTitle = savedInstanceState.getString(EXTRA_LAUNCH_NAME)
                ?: throw IllegalStateException("Launch name cannot be empty")
            launchCountDown = savedInstanceState.getLong(EXTRA_LAUNCH_TIME)
            launchCoordinates = savedInstanceState.getParcelable(EXTRA_LAUNCH_LAT_LNG)
            launchDescription = savedInstanceState.getString(EXTRA_LAUNCH_DESC)
            launchAgencyName = savedInstanceState.getString(EXTRA_LAUNCH_AGENCY)
            launchDate = savedInstanceState.getString(EXTRA_LAUNCH_DATE)
        }
        supportActionBar?.title = launchTitle
        missionDescription.text = launchDescription
        agencyTitle.text = launchAgencyName
        if (launchCountDown > 0) {//check if count down has began;count down is represented by "wsstamp" with a non zero value
            launchCountDown -= System.currentTimeMillis()
            if (launchCountDown > 0) {//ensure the event is in the future
                timer = object : CountDownTimer(launchCountDown, 1000) {

                    private val calendar = Calendar.getInstance()
                    var simpleDateFormat = SimpleDateFormat("dd:HH:mm:ss", Locale.getDefault())

                    @SuppressLint("SetTextI18n")
                    override fun onTick(millisUntilFinished: Long) {
                        calendar.timeInMillis = millisUntilFinished
                        timerTxt.text =
                            "T - " + simpleDateFormat.format(calendar.time)
                    }

                    override fun onFinish() {
                        timerTxt.text = getString(R.string.mission_concluded)
                    }
                }
                timer?.start()
            } else {
                timerTxt.text = launchDate//display launch date if count down has'nt begun
            }
        } else {
            timerTxt.text = launchDate//display launch date if count down has'nt begun
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (android.R.id.home == item.itemId) {
            if (timerTxt.text == getString(R.string.mission_concluded) || launchCountDown < 0) {
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EXTRA_LAUNCH_NAME, launchTitle)
        outState.putString(EXTRA_LAUNCH_DESC, launchDescription)
        outState.putString(EXTRA_LAUNCH_AGENCY, launchAgencyName)
        outState.putLong(EXTRA_LAUNCH_TIME, launchCountDown)
        outState.putParcelable(EXTRA_LAUNCH_LAT_LNG, launchCoordinates)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    override fun onBackPressed() {

    }

}