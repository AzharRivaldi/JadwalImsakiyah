package com.azhar.jadwalimsakiyah.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.azhar.jadwalimsakiyah.R
import com.azhar.jadwalimsakiyah.adapter.MainAdapter
import com.azhar.jadwalimsakiyah.adapter.MainAdapter.onSelectData
import com.azhar.jadwalimsakiyah.model.ModelMain
import com.azhar.jadwalimsakiyah.service.GetAddressIntentService
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_date_pray.*
import kotlinx.android.synthetic.main.layout_time_location.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Created by Azhar Rivaldi on 22-12-2019.
 */

class MainActivity : AppCompatActivity(), onSelectData {

    var modelMainList: MutableList<ModelMain> = ArrayList()

    //show location
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var addressResultReceiver: LocationAddressResultReceiver? = null
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //transparent statusbar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        addressResultReceiver = LocationAddressResultReceiver(Handler())

        rvList.setHasFixedSize(true)
        rvList.setLayoutManager(LinearLayoutManager(this))

        //get Methods
        imageGreet
        textJSON
        dataJSON

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.locations[0]
                address
            }
        }
        startLocationUpdates()
    }

    private val textJSON: Unit
        get() {
            val progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.setMessage("Mohon tunggu...")
            progressDialog.show()
            AndroidNetworking.get("http://api.aladhan.com/v1/timingsByCity?city=Jakarta&country=Indonesia&method=5")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            try {
                                progressDialog.dismiss()
                                val playerArray = response.getJSONObject("data")
                                for (i in 0 until playerArray.length()) {

                                    val jsonObject1 = playerArray.getJSONObject("timings")
                                    txtTimeSubuh!!.text = jsonObject1.getString("Fajr")
                                    txtTimeDhuhur!!.text = jsonObject1.getString("Dhuhr")
                                    txtTimeAshar!!.text = jsonObject1.getString("Asr")
                                    txtTimeMagrib!!.text = jsonObject1.getString("Maghrib")
                                    txtTimeIsya!!.text = jsonObject1.getString("Isha")
                                    txtTimeImsak!!.text = jsonObject1.getString("Imsak")

                                    val jsonObject2 = playerArray.getJSONObject("date")
                                    txtDateMasehi!!.text = jsonObject2.getString("readable")

                                    val jsonObject3 = jsonObject2.getJSONObject("hijri")
                                    val strYear = jsonObject3.getString("year")
                                    txtDateHijri!!.text = jsonObject3.getString("day") + " Ramadhan " + strYear + " H"
                                }

                            } catch (e: JSONException) {
                                e.printStackTrace()
                                Toast.makeText(this@MainActivity, "Gagal menampilkan data!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onError(anError: ANError) {
                            progressDialog.dismiss()
                            Toast.makeText(this@MainActivity, "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                        }
                    })
        }

    private val dataJSON: Unit
        get() {
            val progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.setMessage("Mohon tunggu...")
            progressDialog.show()
            AndroidNetworking.get("http://api.aladhan.com/v1/hijriCalendarByCity?city=Jakarta&country=Indonesia&method=5&month=09&year=1441")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            try {
                                progressDialog.dismiss()
                                val playerArray = response.getJSONArray("data")
                                for (i in 0 until playerArray.length()) {

                                    val temp = playerArray.getJSONObject(i)
                                    val dataApi = ModelMain()

                                    val jsonObject1 = temp.getJSONObject("timings")
                                    val jsonObject2 = temp.getJSONObject("date")
                                    val strDate = jsonObject2.getString("readable")
                                    val jsonObject3 = jsonObject2.getJSONObject("hijri")
                                    val strDay = jsonObject3.getString("day")
                                    val strYear = jsonObject3.getString("year")
                                    val jsonObject4 = jsonObject2.getJSONObject("gregorian")
                                    val jsonObject5 = jsonObject4.getJSONObject("weekday")
                                    val strWeekDay = jsonObject5.getString("en")

                                    dataApi.txtFajr = jsonObject1.getString("Fajr")
                                    dataApi.txtDhuhr = jsonObject1.getString("Dhuhr")
                                    dataApi.txtAsr = jsonObject1.getString("Asr")
                                    dataApi.txtMaghrib = jsonObject1.getString("Maghrib")
                                    dataApi.txtIsha = jsonObject1.getString("Isha")
                                    dataApi.txtImsak = jsonObject1.getString("Imsak")
                                    dataApi.txtDate = strDate
                                    dataApi.txtYear = strYear
                                    dataApi.txtDay = "$strDay Ramadhan $strYear H"
                                    dataApi.txtWeekDay = strWeekDay

                                    modelMainList.add(dataApi)
                                    showNews()
                                }

                            } catch (e: JSONException) {
                                e.printStackTrace()
                                Toast.makeText(this@MainActivity, "Gagal menampilkan data!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onError(anError: ANError) {
                            progressDialog.dismiss()
                            Toast.makeText(this@MainActivity, "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                        }
                    })
        }

    //greeting image
    private val imageGreet: Unit
        get() {
            val calendar = Calendar.getInstance()
            val timeOfDay = calendar[Calendar.HOUR_OF_DAY]
            if (timeOfDay >= 0 && timeOfDay < 6) {
                greeting_img!!.setImageResource(R.drawable.bg_header_dawn)
            } else if (timeOfDay >= 6 && timeOfDay < 12) {
                greeting_img!!.setImageResource(R.drawable.bg_header_sunrise)
            } else if (timeOfDay >= 12 && timeOfDay < 16) {
                greeting_img!!.setImageResource(R.drawable.bg_header_daylight)
            } else if (timeOfDay >= 16 && timeOfDay < 18) {
                greeting_img!!.setImageResource(R.drawable.bg_header_evening)
            } else if (timeOfDay >= 18 && timeOfDay < 24) {
                greeting_img!!.setImageResource(R.drawable.bg_header_night)
            }
        }

    private fun showNews() {
        val mainAdapter = MainAdapter(this@MainActivity, modelMainList, this)
        rvList!!.adapter = mainAdapter
    }

    override fun onSelected(modelMain: ModelMain) {
        val intent = Intent(this@MainActivity, DetailActivity::class.java)
        intent.putExtra("paramDtl", modelMain)
        startActivity(intent)
    }

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
            fusedLocationClient!!.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null)
        }
    }

    private val address: Unit
        get() {
            if (!Geocoder.isPresent()) {
                Toast.makeText(this@MainActivity, "Can't find current address, ", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(this, GetAddressIntentService::class.java)
            intent.putExtra("add_receiver", addressResultReceiver)
            intent.putExtra("add_location", currentLocation)
            startService(intent)
        }

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
                Toast.makeText(this@MainActivity,
                        "Address not found, ",
                        Toast.LENGTH_SHORT).show()
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