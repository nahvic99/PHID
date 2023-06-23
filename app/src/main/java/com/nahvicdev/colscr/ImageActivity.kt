package com.nahvicdev.colscr

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nahvicdev.colscr.databinding.ActivityImageBinding
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException


class ImageActivity : AppCompatActivity() {
    private lateinit var fabShare: FloatingActionButton

    private lateinit var imageUrl: String
    private lateinit var binding: ActivityImageBinding
    private lateinit var imageView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR)
        setContentView(binding.root)
        supportActionBar?.hide()

        imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL).toString()
        imageView = findViewById<ImageView>(R.id.imageView)
        fabShare = findViewById(R.id.btnShare)

        // Carga la imagen en el ImageView usando Picasso o cualquier otra biblioteca de tu elección
        Picasso.get().load(imageUrl).into(imageView)

        fabShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, imageUrl)
            startActivity(Intent.createChooser(shareIntent, "Compartir con"))
        }


    }



    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"

    }
}