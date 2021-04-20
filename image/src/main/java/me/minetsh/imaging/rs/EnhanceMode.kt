package me.minetsh.imaging.rs

import kotlin.math.PI

/**
 * @author dizzylay
 * @date 2021/4/18
 * @email liaoyanglay@outlook.com
 */
enum class EnhanceMode(private val MIN: Float, private val MAX: Float, private val default: Float) {
    BLUR(0f, 25f, 0f),
    BRIGHTNESS(-0.25f, 0.25f, 0f),
    CONTRAST(-0.25f, 0.25f, 0f),
    SATURATION(0f, 2f, 1f),
    HUE((-PI).toFloat(), PI.toFloat(), 0f),
    EMBOSS(0f, 2f, 0f);

    var param: Float = default
        private set

    fun updateParamFromProgress(progress: Int) {
        if (progress in 0..100) {
            param = (MAX - MIN) * (progress / 100f) + MIN
        }
    }

    fun getProgressFromParam(): Int {
        return ((param - MIN) / (MAX - MIN) * 100).toInt()
    }

    fun resetParam() {
        param = default
    }
}
