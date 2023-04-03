package com.currentlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

object LocationUtils {

    private lateinit var mLocationResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationListener: LocationListener
    private var mLocationRequest: LocationRequest? = null
    private var mLocationCallback: LocationCallback? = null
    private var isOneTimeLocationFetch: Boolean = true

    fun fetchLocation(
        context: Context,
        locationResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        locationListener: LocationListener,
        isOneTimeLocationFetch: Boolean
    ) {
        this.mLocationResultLauncher = locationResultLauncher
        this.mLocationListener = locationListener
        this.isOneTimeLocationFetch = isOneTimeLocationFetch
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        requestLocation(context)
    }

    private fun requestLocation(context: Context) {
        val permissions = arrayListOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).toTypedArray()
        TedPermission.create()
            .setDeniedMessage(getLocationDeniedMessage(context))
            .setPermissions(*permissions)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    initLocationCallback()
                    setLocationSetting(context)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    mLocationListener.onFailLocation(deniedPermissions.toString())
                }
            })
            .check()
    }

    private fun initLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { currentLocation ->
                    mLocationListener.onGetLocation(currentLocation)
                    if (isOneTimeLocationFetch) stopLocationRequest()
                }
            }
        }
    }

    private fun setLocationSetting(context: Context) {
        mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdateDelayMillis(10000)
            .build()

        val locationSettingBuilder =
            LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest!!)
        val settingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> =
            settingsClient.checkLocationSettings(locationSettingBuilder.build())

        task.addOnSuccessListener { startLocationUpdate() }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    mLocationResultLauncher.launch(
                        IntentSenderRequest.Builder(exception.resolution).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    mLocationListener.onFailLocation(sendEx.message.orEmpty())
                }
            } else {
                mLocationListener.onFailLocation(exception.message.orEmpty())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        /*mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            mLocationListener.onGetLocation(location)
        }*/

        mLocationRequest?.let { request ->
            mLocationCallback?.let { callback ->
                mFusedLocationClient.requestLocationUpdates(
                    request,
                    callback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    private fun stopLocationRequest() {
        mLocationCallback?.let {
            mFusedLocationClient.removeLocationUpdates(it)
        }
    }

    interface LocationListener {
        fun onGetLocation(location: Location?)
        fun onFailLocation(errorMessage: String)
    }

    private fun getLocationDeniedMessage(context: Context?): String? {
        return context?.getString(R.string.msg_no_permission_location)
            ?.replace("#", context.getString(R.string.app_name))
    }
}