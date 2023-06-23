package com.nahvicdev.colscr

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.shapes.Shape
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.constraintlayout.helper.widget.Carousel
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.color.utilities.MaterialDynamicColors.primaryFixedDarker
import com.google.android.material.color.utilities.MaterialDynamicColors.surface
import com.google.android.material.snackbar.Snackbar
import com.nahvicdev.colscr.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.lang.reflect.Modifier
import kotlin.random.Random

const val AD_UNIT_ID = "ca-app-pub-2768901988841601/6013951141"

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val API_KEY = "iCCYTzKySpjeJ5fPnmo2eZmzPPKaMAXxCQzCC2QXpF39qX6eeYMXutMz"
    private val BASE_URL = "https://api.pexels.com/v1/"
    private lateinit var fab: View
    lateinit var mAdView : AdView
    private var clickCount = 0
    private var interstitialAd: InterstitialAd? = null
    private var adIsLoading: Boolean = false
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val editTextQuery = findViewById<EditText>(R.id.editTextQuery)
        fab = findViewById(R.id.fab)
        recyclerView = findViewById(R.id.recyclerView)
        val buttonSearch = findViewById<Button>(R.id.buttonSearch)


        buttonSearch.setOnClickListener {
            val query = editTextQuery.text.toString()
            searchImagesFromPexels(query)
            clickCount++
            if (clickCount % 2 == 0) {
                // Ejecutar la función cada 5 clics
                doSomethingEvery5Clicks()

                // Reiniciar el contador a 0
                clickCount = 0

            }
            recyclerView.scrollToPosition(0)
        }

        fab.setOnClickListener {
            clickCount++
            if (clickCount % 5 == 0) {
                // Ejecutar la función cada 5 clics
                doSomethingEvery5Clicks()
                // Reiniciar el contador a 0
                clickCount = 0
            }
            getImagesFromPexels()
        }



        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        if (!adIsLoading && interstitialAd == null) {
            adIsLoading = true
            loadAd()
        }


        setupRecyclerView()
        getImagesFromPexels()

    }

    override fun onDestroy() {
        // Liberar recursos relacionados con los anuncios
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = null
            interstitialAd = null
        }

        mAdView.destroy()

        super.onDestroy()
    }


    private fun loadAd() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {

                    interstitialAd = null
                    adIsLoading = false
                    val error =
                        "domain: ${adError.domain}, code: ${adError.code}, " + "message: ${adError.message}"
                    Toast.makeText(
                        this@MainActivity,
                        "onAdFailedToLoad() with error $error",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    interstitialAd = ad
                    adIsLoading = false

                }
            }
        )
    }

    private fun searchImagesFromPexels(query: String)
    {
        val client = OkHttpClient()

        val url = "$BASE_URL/search?query=$query&per_page=50&page=1"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", API_KEY)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar error de conexión
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val images = parseImageData(responseData)
                    runOnUiThread {
                        imageAdapter.setItems(images)


                    }
                } else {
                    // Manejar respuesta de error
                }
            }
        })
    }



    private fun getImagesFromPexels() {
        val client = OkHttpClient()


        val url = "$BASE_URL/curated?per_page=20&page=1"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", API_KEY)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar error de conexión
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val images = parseImageData(responseData)
                    runOnUiThread {
                        imageAdapter.setItems(images)


                    }
                } else {
                    // Manejar respuesta de error
                }
            }
        })
    }



    private fun parseImageData(responseData: String?): List<String> {
        val images = mutableListOf<String>()
        responseData?.let {
            val jsonObject = JSONObject(it)
            val jsonArray: JSONArray = jsonObject.getJSONArray("photos")
            for (i in 0 until jsonArray.length()) {
                val photoObject: JSONObject = jsonArray.getJSONObject(i)
                val imageUrl: String = photoObject.getJSONObject("src").getString("large")
                images.add(imageUrl)
            }
        }
        return images
    }



    private fun doSomethingEvery5Clicks() {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad was dismissed.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        interstitialAd = null
                        loadAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d(TAG, "Ad failed to show.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        interstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen content.")
                        // Called when ad is dismissed.
                    }
                }
            interstitialAd?.show(this)
        } else {
            Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()

        }
    }

    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = imageAdapter
    }

    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

        private val images = mutableListOf<String>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val imageUrl = images[position]
            Picasso.get().load(imageUrl).into(holder.imageView)
            holder.cardView.setOnClickListener {
                val imageUrl = images[position]
                openImageActivity(imageUrl)
                clickCount++
                if (clickCount % 3 == 0) {
                    // Ejecutar la función cada 3 clics
                    doSomethingEvery5Clicks()
                    // Reiniciar el contador a 0
                    clickCount = 0
                }
            }
        }

        override fun getItemCount(): Int {
            return images.size
        }

        fun setItems(items: List<String>) {
            images.clear()
            images.addAll(items)
            notifyDataSetChanged()
        }

        inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
            val imageView: ImageView = itemView.findViewById(R.id.imageView)
            }

        private fun openImageActivity(imageUrl: String) {
            val intent = Intent(this@MainActivity, ImageActivity::class.java)
            intent.putExtra(ImageActivity.EXTRA_IMAGE_URL, imageUrl)
            startActivity(intent)
        }
        }


    }

