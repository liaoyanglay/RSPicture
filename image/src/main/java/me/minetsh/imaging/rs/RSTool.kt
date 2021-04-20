package me.minetsh.imaging.rs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.renderscript.*
import com.dizzylay.rspicture.rs.ScriptC_BrightnessContrastFilter
import com.dizzylay.rspicture.rs.ScriptC_Filter
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author dizzylay
 * @date 2021/4/6
 * @email liaoyanglay@outlook.com
 */
class RSTool(context: Context, bitmap: Bitmap? = null) {

    private val rs: RenderScript = RenderScript.create(context)
    private val scriptFilter = ScriptC_Filter(rs)
    private val scriptBrightnessContrast = ScriptC_BrightnessContrastFilter(rs)
    private lateinit var mBitmap: Bitmap
    private lateinit var allocationIn: Allocation
    private lateinit var allocationOut: Allocation

    init {
        bitmap?.let { setBitmap(it) }
    }

    fun destroy() {
        scriptFilter.destroy()
        allocationIn.destroy()
        allocationOut.destroy()
        rs.destroy()
    }

    fun setBitmap(bitmap: Bitmap) {
        mBitmap = bitmap
        if (::allocationIn.isInitialized) {
            allocationIn.destroy()
        }
        if (::allocationOut.isInitialized) {
            allocationOut.destroy()
        }
        val temp = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        allocationIn = Allocation.createFromBitmap(rs, bitmap)
        allocationOut = Allocation.createFromBitmap(rs, temp)
    }

    @JvmOverloads
    fun grayscale(outBitmap: Bitmap? = null): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val retBitmap = outBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        scriptFilter.forEach_grayscale(allocationIn, allocationOut)
        allocationOut.copyTo(retBitmap)
        return retBitmap
    }

    @JvmOverloads
    fun blackGold(outBitmap: Bitmap? = null): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val retBitmap = outBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        scriptFilter.forEach_blackGold(allocationIn, allocationOut)
        allocationOut.copyTo(retBitmap)
        return retBitmap
    }

    @JvmOverloads
    fun invert(outBitmap: Bitmap? = null): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val retBitmap = outBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        scriptFilter.forEach_invert(allocationIn, allocationOut)
        allocationOut.copyTo(retBitmap)
        return retBitmap
    }

    @JvmOverloads
    fun blur(radius: Float, outBitmap: Bitmap? = null): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val retBitmap = outBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        if (radius == 0f) {
            val canvas = Canvas(retBitmap)
            canvas.drawBitmap(mBitmap, 0f, 0f, null)
            return retBitmap
        }
        val scriptBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        scriptBlur.setInput(allocationIn)
        scriptBlur.setRadius(radius)
        scriptBlur.forEach(allocationOut)
        allocationOut.copyTo(retBitmap)
        return retBitmap
    }

    @JvmOverloads
    fun nostalgia(outBitmap: Bitmap? = null): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val retBitmap = outBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        scriptFilter.forEach_nostalgia(allocationIn, allocationOut)
        allocationOut.copyTo(retBitmap)
        return retBitmap
    }

    @JvmOverloads
    fun comic(outBitmap: Bitmap? = null): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val retBitmap = outBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        scriptFilter.forEach_comic(allocationIn, allocationOut)
        allocationOut.copyTo(retBitmap)
        return retBitmap
    }

    @JvmOverloads
    fun saturation(value: Float, outBitmap: Bitmap? = null): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val retBitmap = outBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        scriptFilter._saturationValue = value
        scriptFilter.forEach_saturation(allocationIn, allocationOut)
        allocationOut.copyTo(retBitmap)
        return retBitmap
    }

    @JvmOverloads
    fun hue(value: Float, outBitmap: Bitmap? = null): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val retBitmap = outBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val scriptMatrix = ScriptIntrinsicColorMatrix.create(rs)

        // Set HUE rotation matrix
        // The matrix below performs a combined operation of,
        // RGB->HSV transform * HUE rotation * HSV->RGB transform
        val cos = cos(value.toDouble()).toFloat()
        val sin = sin(value.toDouble()).toFloat()
        val mat = Matrix3f()
        mat[0, 0] = (.299 + .701 * cos + .168 * sin).toFloat()
        mat[1, 0] = (.587 - .587 * cos + .330 * sin).toFloat()
        mat[2, 0] = (.114 - .114 * cos - .497 * sin).toFloat()
        mat[0, 1] = (.299 - .299 * cos - .328 * sin).toFloat()
        mat[1, 1] = (.587 + .413 * cos + .035 * sin).toFloat()
        mat[2, 1] = (.114 - .114 * cos + .292 * sin).toFloat()
        mat[0, 2] = (.299 - .3 * cos + 1.25 * sin).toFloat()
        mat[1, 2] = (.587 - .588 * cos - 1.05 * sin).toFloat()
        mat[2, 2] = (.114 + .886 * cos - .203 * sin).toFloat()
        scriptMatrix.setColorMatrix(mat)
        scriptMatrix.forEach(allocationIn, allocationOut)
        allocationOut.copyTo(retBitmap)
        return retBitmap
    }

    @JvmOverloads
    fun emboss(value: Float, outBitmap: Bitmap? = null): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val retBitmap = outBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val scriptConvolve = ScriptIntrinsicConvolve5x5.create(rs, Element.U8_4(rs))
        val f1 = value
        val f2 = 1.0f - value
        // Emboss filter kernel
        val coefficients = floatArrayOf(
            -f1 * 2, 0f, -f1, 0f, 0f,
            0f, -f2 * 2, -f2, 0f, 0f,
            -f1, -f2, 1f, f2, f1,
            0f, 0f, f2, f2 * 2, 0f,
            0f, 0f, f1, 0f, f1 * 2
        )
        scriptConvolve.setCoefficients(coefficients)
        scriptConvolve.setInput(allocationIn)
        scriptConvolve.forEach(allocationOut)
        allocationOut.copyTo(retBitmap)
        return retBitmap
    }

    @JvmOverloads
    fun brightnessContrast(brightness: Float, contrast: Float, outBitmap: Bitmap? = null): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val retBitmap = outBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        scriptBrightnessContrast._gBrightnessFactor = brightness
        scriptBrightnessContrast._gContrastFactor = contrast
        scriptBrightnessContrast._gScript = scriptBrightnessContrast
        scriptBrightnessContrast.invoke_filter(allocationIn, allocationOut)
        allocationOut.copyTo(retBitmap)
        return retBitmap
    }

}
