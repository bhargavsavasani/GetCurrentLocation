package com.mycurrentlocation

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.currentlocation.LocationUtils
import com.mycurrentlocation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mLocationResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mLocationResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                Log.d("MainActivity", "Result :- ${result.resultCode}")
            }

        LocationUtils.fetchLocation(
            this,
            mLocationResultLauncher,
            object : LocationUtils.LocationListener {
                override fun onGetLocation(location: Location?) {
                    location?.apply {
                        Log.d("MainActivity", "onGetLocation: $location")
                        binding.tvLat.text = latitude.toString()
                        binding.tvLng.text = longitude.toString()
                    }
                }
                override fun onFailLocation(errorMessage: String) {

                }
            },
            true
        )
    }
}