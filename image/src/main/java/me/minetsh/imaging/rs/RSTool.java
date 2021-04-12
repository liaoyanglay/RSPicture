package me.minetsh.imaging.rs;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import androidx.annotation.NonNull;

import me.minetsh.imaging.rs.ScriptC_Filter;

/**
 * @author dizzylay
 * @date 2021/4/6
 * @email liaoyanglay@outlook.com
 */
public class RSTool {

    public static Bitmap gray(@NonNull Context context, @NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 创建 RenderScript 对象
        RenderScript rs = RenderScript.create(context);

        // 创建输入、输出 Allocation
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        // 创建我们在上面定义的 script
        ScriptC_Filter script = new ScriptC_Filter(rs);

        script.forEach_gray(allIn, allOut);

        // 将执行结果复制到输出 bitmap 上
        allOut.copyTo(outBitmap);

        // 释放资源
        allOut.destroy();
        rs.destroy();
        return outBitmap;
    }

    public static Bitmap blackGold(@NonNull Context context, @NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);

        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        ScriptC_Filter script = new ScriptC_Filter(rs);

        script.forEach_blackGold(allIn, allOut);

        allOut.copyTo(outBitmap);

        allOut.destroy();
        rs.destroy();
        return outBitmap;
    }

    public static Bitmap invert(@NonNull Context context, @NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);

        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        ScriptC_Filter script = new ScriptC_Filter(rs);

        script.forEach_invert(allIn, allOut);

        allOut.copyTo(outBitmap);

        allOut.destroy();
        rs.destroy();
        return outBitmap;
    }

    public static Bitmap blur(@NonNull Context context, @NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);

        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(allIn);

        // Set the blur radius
        script.setRadius(8);

        // Start the ScriptIntrinsicBlur
        script.forEach(allOut);

        allOut.copyTo(outBitmap);

        allOut.destroy();
        rs.destroy();
        return outBitmap;
    }

    public static Bitmap nostalgia(@NonNull Context context, @NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);

        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        ScriptC_Filter script = new ScriptC_Filter(rs);

        script.forEach_nostalgia(allIn, allOut);

        allOut.copyTo(outBitmap);

        allOut.destroy();
        rs.destroy();
        return outBitmap;
    }

    public static Bitmap comic(@NonNull Context context, @NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);

        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        ScriptC_Filter script = new ScriptC_Filter(rs);

        script.forEach_comic(allIn, allOut);

        allOut.copyTo(outBitmap);

        allOut.destroy();
        rs.destroy();
        return outBitmap;
    }
}
