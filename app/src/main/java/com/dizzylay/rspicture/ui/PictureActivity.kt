package com.dizzylay.rspicture.ui

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.dizzylay.rspicture.R


class PictureActivity : AppCompatActivity() {
    private lateinit var fullscreenContent: ImageView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    private lateinit var imagePath: String

    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls.visibility = View.VISIBLE
    }

    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    private val delayHideClickListener = View.OnClickListener {
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_picture)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        fullscreenContent = findViewById(R.id.fullscreen_content)
        fullscreenContent.setOnClickListener { toggle() }
        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)
        findViewById<Button>(R.id.dummy_button).setOnClickListener(delayHideClickListener)

        imagePath = intent.getStringExtra(IMAGE_PATH)!!
        if (imagePath.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            fullscreenContent.setImageBitmap(bitmap)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        isFullscreen = true
        delayedHide(100)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideHandler.removeCallbacksAndMessages(null)
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        private const val AUTO_HIDE = true

        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 0

        private const val IMAGE_PATH = "image_path"

        fun start(context: Context, imagePath: String) {
            val intent = Intent(context, PictureActivity::class.java)
            intent.putExtra(IMAGE_PATH, imagePath)
            context.startActivity(intent)
        }
    }
}