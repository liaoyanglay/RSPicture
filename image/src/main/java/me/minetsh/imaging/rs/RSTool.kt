package me.minetsh.imaging.rs

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.dizzylay.rspicture.rs.ScriptC_Filter

/**
 * @author dizzylay
 * @date 2021/4/6
 * @email liaoyanglay@outlook.com
 */
class RSTool(context: Context, bitmap: Bitmap? = null) {

    private val rs: RenderScript = RenderScript.create(context)
    private val scriptFilter: ScriptC_Filter = ScriptC_Filter(rs)
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
    fun blur(radius: Float = 8f, outBitmap: Bitmap? = null): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val retBitmap = outBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setInput(allocationIn)
        script.setRadius(radius)
        script.forEach(allocationOut)
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

}