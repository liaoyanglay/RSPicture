package com.dizzylay.rspicture.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.dizzylay.rspicture.R
import me.minetsh.imaging.core.IMGMode
import me.minetsh.imaging.core.IMGPath
import me.minetsh.imaging.view.IMGColorGroup
import me.minetsh.imaging.view.IMGView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class DrawingActivity : AppCompatActivity() {

    private lateinit var mImgView: IMGView
    private lateinit var mColorGroup: IMGColorGroup
    private lateinit var mDoodleSeekBar: SeekBar

    private val imgPath by lazy { File(externalCacheDir, "draw.jpg").path }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)
        initViews()
        initBitmap()
    }

    private fun initViews() {
        mImgView = findViewById(R.id.image_canvas)
        mColorGroup = findViewById(R.id.cg_colors)
        mDoodleSeekBar = findViewById(R.id.skb_doodle)

        mColorGroup.setOnCheckedChangeListener { _, _ ->
            mImgView.setPenColor(mColorGroup.checkColor)
        }
        mDoodleSeekBar.progress = (mImgView.penWidth / (IMGPath.MAX_WIDTH - IMGPath.MIN_WIDTH) * 100).toInt()
        mDoodleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mImgView.penWidth = progress / 100f * (IMGPath.MAX_WIDTH - IMGPath.MIN_WIDTH) + IMGPath.MIN_WIDTH
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        val group: View = findViewById(R.id.group_drawing)
        mImgView.setMode(IMGMode.DOODLE, false)
        mImgView.setOnClickListener {
            group.visibility = if (group.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun initBitmap() {
        mImgView.post {
            val width = mImgView.width / resources.displayMetrics.density
            val height = mImgView.height / resources.displayMetrics.density
            val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.parseColor("#FFFFFF"))
            mImgView.setImageBitmap(bitmap, false)
        }
    }

    fun onCancelClick(view: View) {
        finish()
    }

    fun onDoneClick(view: View) {
        val bitmap = mImgView.saveBitmap()
        if (bitmap != null) {
            var fout: FileOutputStream? = null
            try {
                fout = FileOutputStream(imgPath)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } finally {
                if (fout != null) {
                    try {
                        fout.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            ShareActivity.start(this, imgPath)
            finish()
        }
    }

    fun onUndoClick(view: View) {
        mImgView.undoDoodle()
    }
}