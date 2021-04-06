package me.minetsh.imaging.rs;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;

import androidx.annotation.NonNull;

import me.minetsh.imaging.rs.ScriptC_Gray;

/**
 * @author dizzylay
 * @date 2021/4/6
 * @email liaoyanglay@outlook.com
 */
public class RSTool {
    /**
     * 将 bitmap 去色后返回一张新的 Bitmap。
     */
    public static Bitmap gray(@NonNull Context context, @NonNull Bitmap bitmap) {
        // 创建输出 bitamp
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 创建 RenderScript 对象
        RenderScript rs = RenderScript.create(context);

        // 创建输入、输出 Allocation
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        // 创建我们在上面定义的 script
        ScriptC_Gray script = new ScriptC_Gray(rs);

        // 对每一个像素执行 root 方法
        script.forEach_root(allIn, allOut);

        // 将执行结果复制到输出 bitmap 上
        allOut.copyTo(outBitmap);

        // 释放资源
        rs.destroy();
        return outBitmap;
    }
}
