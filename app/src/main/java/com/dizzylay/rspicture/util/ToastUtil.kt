package com.dizzylay.rspicture.util

import android.content.Context
import android.widget.Toast

/**
 * @author dizzylay
 * @date 2021/3/17
 * @email liaoyanglay@outlook.com
 */

object ToastUtil {
    fun makeShort(context: Context, text: CharSequence) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun makeLong(context: Context, text: CharSequence) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}