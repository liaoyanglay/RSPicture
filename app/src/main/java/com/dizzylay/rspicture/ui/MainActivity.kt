package com.dizzylay.rspicture.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dizzylay.rspicture.R
import com.dizzylay.rspicture.databinding.ActivityMainBinding
import com.dizzylay.rspicture.util.FileUtil
import com.dizzylay.rspicture.util.ToastUtil
import me.minetsh.imaging.IMGEditActivity
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var photoPath: String
    private lateinit var photoUri: Uri
    private lateinit var photoOutputPath: String
    private lateinit var mBinding: ActivityMainBinding

    private val imgSavePath by lazy { File(externalCacheDir, "result.jpg").path }

    private companion object {
        // Used to load the 'native-lib' library on application startup.
//        init {
//            System.loadLibrary("native-lib")
//        }

        const val REQ_PERMISSION_TAKE_PHOTO: Int = 0
        const val REQ_PERMISSION_WRITE_SDCARD: Int = 1

        const val REQ_IMAGE_SELECT: Int = 0
        const val REQ_TAKE_PHOTO: Int = 1
        const val REQ_IMAGE_CROP: Int = 2
        const val REQ_IMAGE_EDIT: Int = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Example of a call to a native method
//        mBinding.sampleText.text = stringFromJNI()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 申请读写内存卡内容的权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQ_PERMISSION_WRITE_SDCARD
            )
        }

        mBinding.cardViewAlbum.setOnClickListener(albumClickListener)
        mBinding.cardViewShoot.setOnClickListener(shootClickListener)
        mBinding.cardViewDraw.setOnClickListener(drawClickListener)
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    private external fun stringFromJNI(): String

    private val albumClickListener = View.OnClickListener {
        val choiceFromAlbumIntent = Intent(Intent.ACTION_GET_CONTENT)
        choiceFromAlbumIntent.type = "image/*"
        startActivityForResult(choiceFromAlbumIntent, REQ_IMAGE_SELECT)
    }

    private val shootClickListener = View.OnClickListener {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA), REQ_PERMISSION_TAKE_PHOTO
            )
        } else {
            startCamera()
        }
    }

    private val drawClickListener = View.OnClickListener {
        startDraw()
    }

    private fun startCamera() {
        val file = FileUtil.createFile(externalCacheDir, "_tmp_image.jpg")
        photoPath = file.path

        photoUri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(this, getString(R.string.provider_name), file)
        } else {
            Uri.fromFile(file)
        }
        // 打开系统相机的 Action
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 设置拍照所得照片的输出目录
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(takePhotoIntent, REQ_TAKE_PHOTO)
    }

    private fun startDraw() {
        startActivity(Intent(this, DrawingActivity::class.java))
    }

    private fun cropPhoto(inputUri: Uri) {
        val file = File(Environment.getExternalStorageDirectory().path, "_crop_image.jpg")
        photoOutputPath = file.path
        if (file.exists()) {
            file.delete()
        }

        val outputUri = Uri.fromFile(file)
        val cropPhotoIntent = Intent("com.android.camera.action.CROP")
        cropPhotoIntent.setDataAndType(inputUri, "image/*")
        cropPhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        cropPhotoIntent.putExtra("crop", "true")
        cropPhotoIntent.putExtra("scale", true)
        cropPhotoIntent.putExtra("scaleUpIfNeeded", true)
        cropPhotoIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        cropPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        startActivityForResult(cropPhotoIntent, REQ_IMAGE_CROP)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQ_PERMISSION_TAKE_PHOTO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                } else {
                    ToastUtil.makeShort(this, "拍照权限被拒绝")
                }
            }
            REQ_PERMISSION_WRITE_SDCARD -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    ToastUtil.makeShort(this, "读写内存卡内容权限被拒绝")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        var imagePath: String?
        when (requestCode) {
            REQ_IMAGE_SELECT -> {
                data?.data?.let {
//                    cropPhoto(it)
                    imagePath = FileUtil.getPathFromUri(this, it)
                    startActivityForResult(
                        Intent(this, IMGEditActivity::class.java)
                            .putExtra(IMGEditActivity.EXTRA_IMAGE_PATH, imagePath)
                            .putExtra(
                                IMGEditActivity.EXTRA_IMAGE_SAVE_PATH,
                                imgSavePath
                            ),
                        REQ_IMAGE_EDIT
                    )
                }
            }
            REQ_TAKE_PHOTO -> {
//                cropPhoto(photoUri)
                imagePath = photoPath
                startActivityForResult(
                    Intent(this, IMGEditActivity::class.java)
                        .putExtra(IMGEditActivity.EXTRA_IMAGE_PATH, photoPath)
                        .putExtra(
                            IMGEditActivity.EXTRA_IMAGE_SAVE_PATH,
                            imgSavePath
                        ),
                    REQ_IMAGE_EDIT
                )
            }
            REQ_IMAGE_CROP -> {
                val file = File(photoOutputPath)
                if (file.exists()) {
                    imagePath = file.path
                    PictureActivity.start(this, imagePath!!)
                } else {
                    ToastUtil.makeShort(this, "找不到照片")
                }
            }
            REQ_IMAGE_EDIT -> {
                ShareActivity.start(this, imgSavePath)
            }
        }
    }
}