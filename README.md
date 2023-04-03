# CurrentLocation


**Features**
=============
* Get Current location
* No need to ask permission or setting location dialog direct call this method an get current location
* Get accurate location
* Fetch location one time or continues 
* ✨Fast Working ✨



**Usage**
=============
```sh
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
```