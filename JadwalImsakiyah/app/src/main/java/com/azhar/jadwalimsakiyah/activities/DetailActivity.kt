package com.azhar.jadwalimsakiyah.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.azhar.jadwalimsakiyah.R
import com.azhar.jadwalimsakiyah.model.ModelMain
import com.azhar.jadwalimsakiyah.service.GetAddressIntentService
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_detail.*

/**
 * Created by Azhar Rivaldi on 22-12-2019.
 */

class DetailActivity : AppCompatActivity() {

    var modelMain: ModelMain? = null

    //show location
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var addressResultReceiver: LocationAddressResultReceiver? = null
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        //transparent statusbar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        addressResultReceiver = LocationAddressResultReceiver(Handler())

        val tbDetailPuskesmas = findViewById<Toolbar>(R.id.tbDetail)
        setSupportActionBar(tbDetailPuskesmas)
        assert(supportActionBar != null)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //model to show data
        modelMain = intent.getSerializableExtra("paramDtl") as ModelMain
        if (modelMain != null) {

            val strWaktuShubuh = modelMain!!.txtFajr
            val strWaktuDzuhur = modelMain!!.txtDhuhr
            val strWaktuAshar = modelMain!!.txtAsr
            val strWaktuMaghrib = modelMain!!.txtMaghrib
            val strWaktuIsya = modelMain!!.txtIsha
            val strImsak = modelMain!!.txtImsak
            val strScheduleDay = modelMain!!.txtWeekDay
            val strScheduleDate = modelMain!!.txtDate

            tv_pray_time_fajr.setText(strWaktuShubuh)
            tv_pray_time_imsak.setText(strImsak)
            tv_pray_time_dhuhr.setText(strWaktuDzuhur)
            tv_pray_time_asr.setText(strWaktuAshar)
            tv_pray_time_maghrib.setText(strWaktuMaghrib)
            tv_pray_time_isha.setText(strWaktuIsya)
            tv_schedule_day.setText(strScheduleDay)
            tv_schedule_date.setText(strScheduleDate)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.locations[0]
                address
            }
        }
        startLocationUpdates()

    }

    //permission location
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            val locationRequest = LocationRequest()
            locationRequest.interval = 2000
            locationRequest.fastestInterval = 1000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    //get address
    private val address: Unit
        get() {
            if (!Geocoder.isPresent()) {
                Toast.makeText(this@DetailActivity, "Can't find current address!", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(this, GetAddressIntentService::class.java)
            intent.putExtra("add_receiver", addressResultReceiver)
            intent.putExtra("add_location", currentLocation)
            startService(intent)
        }

    //request permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class LocationAddressResultReceiver internal constructor(handler: Handler?) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            if (resultCode == 0) {
                address
            }
            if (resultCode == 1) {
                Toast.makeText(this@DetailActivity, "Address not found!", Toast.LENGTH_SHORT).show()
            }
            val currentAdd = resultData.getString("address_result")
            showResults(currentAdd)
        }
    }

    private fun showResults(currentAdd: String?) {
        txtLocation!!.text = currentAdd
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val winParams = window.attributes
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            window.attributes = winParams
        }
    }
}