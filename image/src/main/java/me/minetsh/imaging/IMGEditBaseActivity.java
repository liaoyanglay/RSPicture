package me.minetsh.imaging;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.ViewSwitcher;

import me.minetsh.imaging.core.IMGMode;
import me.minetsh.imaging.core.IMGText;
import me.minetsh.imaging.rs.RSTool;
import me.minetsh.imaging.view.IMGColorGroup;
import me.minetsh.imaging.view.IMGView;

abstract class IMGEditBaseActivity extends Activity implements View.OnClickListener,
        IMGTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener,
        DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    protected IMGView mImgView;
    protected Bitmap mOriginalBitmap;
    protected Bitmap mCurrBitmap;

    private RSTool mRSTool;

    private RadioGroup mModeGroup;
    private IMGColorGroup mColorGroup;
    private IMGTextEditDialog mTextDialog;
    private View mLayoutOpSub;
    private ViewSwitcher mOpSwitcher, mOpSubSwitcher;
    private View mLayoutFilter;
    private View mLayoutEnhance;
    private RadioGroup mFilterGroup;
    private RadioGroup mEnhanceGroup;
    private SeekBar mFilterSeekBar;
    private SeekBar mEnhanceSeekBar;

    public static final int OP_HIDE = -1;
    public static final int OP_NORMAL = 0;
    public static final int OP_CLIP = 1;

    public static final int OP_SUB_DOODLE = 0;
    public static final int OP_SUB_MOSAIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOriginalBitmap = getBitmap();
        if (mOriginalBitmap != null) {
            setContentView(R.layout.image_edit_activity);
            initViews();
            mImgView.setImageBitmap(mOriginalBitmap);
            mCurrBitmap = mOriginalBitmap;
            mRSTool = new RSTool(this, mCurrBitmap);
        } else finish();
    }

    @Override
    protected void onDestroy() {
        mRSTool.destroy();
        super.onDestroy();
    }

    private void initViews() {
        mImgView = findViewById(R.id.image_canvas);
        mModeGroup = findViewById(R.id.rg_modes);
        mOpSwitcher = findViewById(R.id.vs_op);
        mOpSubSwitcher = findViewById(R.id.vs_op_sub);
        mColorGroup = findViewById(R.id.cg_colors);
        mLayoutOpSub = findViewById(R.id.layout_op_sub);
        mLayoutFilter = findViewById(R.id.layout_filter);
        mFilterGroup = findViewById(R.id.rg_filters);
        mLayoutEnhance = findViewById(R.id.layout_enhance);
        mEnhanceGroup = findViewById(R.id.rg_enhance);
        mFilterSeekBar = findViewById(R.id.skb_filter);
        mEnhanceSeekBar = findViewById(R.id.skb_enhance);

        mColorGroup.setOnCheckedChangeListener(this);
        mFilterGroup.setOnCheckedChangeListener(mFilterChangeListener);
        mEnhanceGroup.setOnCheckedChangeListener(mEnhanceChangeListener);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.rb_doodle) {
            onModeClick(IMGMode.DOODLE);
        } else if (vid == R.id.btn_text) {
            onTextModeClick();
        } else if (vid == R.id.rb_mosaic) {
            onModeClick(IMGMode.MOSAIC);
        } else if (vid == R.id.btn_clip) {
            onModeClick(IMGMode.CLIP);
        } else if (vid == R.id.btn_undo) {
            onUndoClick();
        } else if (vid == R.id.tv_done) {
            onDoneClick();
        } else if (vid == R.id.tv_cancel) {
            onCancelClick();
        } else if (vid == R.id.ib_clip_cancel) {
            onCancelClipClick();
        } else if (vid == R.id.ib_clip_done) {
            onDoneClipClick();
        } else if (vid == R.id.tv_clip_reset) {
            onResetClipClick();
        } else if (vid == R.id.ib_clip_rotate) {
            onRotateClipClick();
        } else if (vid == R.id.btn_filter) {
            onModeClick(IMGMode.FILTER);
        } else if (vid == R.id.btn_enhance) {
            onModeClick(IMGMode.ENHANCE);
        } else if (vid == R.id.btn_filter_done) {
            onModeClick(IMGMode.FILTER);
        } else if (vid == R.id.btn_filter_cancel) {
            mFilterGroup.check(R.id.btn_origin);
            onModeClick(IMGMode.FILTER);
        } else if (vid == R.id.btn_enhance_done) {
            onModeClick(IMGMode.ENHANCE);
        } else if (vid == R.id.btn_enhance_cancel) {
            mEnhanceGroup.check(R.id.btn_brightness);
            onModeClick(IMGMode.ENHANCE);
        } else if (vid == R.id.tv_enhance_reset) {

        }
    }

    public void updateModeUI() {
        IMGMode mode = mImgView.getMode();
        switch (mode) {
            case DOODLE:
                mModeGroup.check(R.id.rb_doodle);
                setOpSubDisplay(OP_SUB_DOODLE);
                break;
            case MOSAIC:
                mModeGroup.check(R.id.rb_mosaic);
                setOpSubDisplay(OP_SUB_MOSAIC);
                break;
            case FILTER:
                mModeGroup.clearCheck();
                setOpSubDisplay(OP_HIDE);
                setFilterDisplay(true);
                break;
            case ENHANCE:
                mModeGroup.clearCheck();
                setOpSubDisplay(OP_HIDE);
                setEnhanceDisplay(true);
                break;
            case NONE:
                mModeGroup.clearCheck();
                setOpSubDisplay(OP_HIDE);
                setFilterDisplay(false);
                setEnhanceDisplay(false);
                break;
        }
    }

    public void onTextModeClick() {
        if (mTextDialog == null) {
            mTextDialog = new IMGTextEditDialog(this, this);
            mTextDialog.setOnShowListener(this);
            mTextDialog.setOnDismissListener(this);
        }
        mTextDialog.show();
    }

    @Override
    public final void onCheckedChanged(RadioGroup group, int checkedId) {
        onColorChanged(mColorGroup.getCheckColor());
    }

    private final RadioGroup.OnCheckedChangeListener mFilterChangeListener = (group, checkedId) -> {
        if (mCurrBitmap != mOriginalBitmap) {
            mCurrBitmap.recycle();
        }

        if (checkedId == R.id.btn_blur) {
            mFilterSeekBar.setVisibility(View.VISIBLE);
        } else {
            mFilterSeekBar.setVisibility(View.GONE);
        }

        if (checkedId == R.id.btn_origin) {
            mCurrBitmap = mOriginalBitmap;
        } else if (checkedId == R.id.btn_grayscale) {
            mCurrBitmap = mRSTool.grayscale();
        } else if (checkedId == R.id.btn_black_gold) {
            mCurrBitmap = mRSTool.blackGold();
        } else if (checkedId == R.id.btn_invert) {
            mCurrBitmap = mRSTool.invert();
        } else if (checkedId == R.id.btn_blur) {
            mCurrBitmap = mRSTool.blur();
        } else if (checkedId == R.id.btn_nostalgia) {
            mCurrBitmap = mRSTool.nostalgia();
        } else if (checkedId == R.id.btn_comic) {
            mCurrBitmap = mRSTool.comic();
        }
        mImgView.setImageBitmap(mCurrBitmap);
    };

    private final RadioGroup.OnCheckedChangeListener mEnhanceChangeListener = (group, checkedId) -> {
        if (checkedId == R.id.btn_brightness) {

        } else if (checkedId == R.id.btn_contrast) {

        } else if (checkedId == R.id.btn_saturation) {

        } else if (checkedId == R.id.btn_hue) {

        }
    };

    public void setOpDisplay(int op) {
        if (op >= 0) {
            mOpSwitcher.setDisplayedChild(op);
        }
    }

    public void setOpSubDisplay(int opSub) {
        if (opSub < 0) {
            mLayoutOpSub.setVisibility(View.GONE);
        } else {
            mOpSubSwitcher.setDisplayedChild(opSub);
            mLayoutOpSub.setVisibility(View.VISIBLE);
        }
    }

    public void setFilterDisplay(boolean display) {
        if (display) {
            mModeGroup.setVisibility(View.GONE);
            mLayoutFilter.setVisibility(View.VISIBLE);
        } else {
            mModeGroup.setVisibility(View.VISIBLE);
            mLayoutFilter.setVisibility(View.GONE);
        }
    }

    public void setEnhanceDisplay(boolean display) {
        if (display) {
            mModeGroup.setVisibility(View.GONE);
            mLayoutEnhance.setVisibility(View.VISIBLE);
        } else {
            mModeGroup.setVisibility(View.VISIBLE);
            mLayoutEnhance.setVisibility(View.GONE);
        }
    }

    @Override
    public void onShow(DialogInterface dialog) {
        mOpSwitcher.setVisibility(View.GONE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mOpSwitcher.setVisibility(View.VISIBLE);
    }

    public abstract Bitmap getBitmap();

    public abstract void onModeClick(IMGMode mode);

    public abstract void onUndoClick();

    public abstract void onCancelClick();

    public abstract void onDoneClick();

    public abstract void onCancelClipClick();

    public abstract void onDoneClipClick();

    public abstract void onResetClipClick();

    public abstract void onRotateClipClick();

    public abstract void onColorChanged(int checkedColor);

    @Override
    public abstract void onText(IMGText text);

    @Override
    public void onBackPressed() {
        if (mImgView.getMode() == IMGMode.CLIP) {
            onCancelClipClick();
            return;
        }
        if (mImgView.getMode() == IMGMode.FILTER || mImgView.getMode() == IMGMode.ENHANCE) {
            onModeClick(mImgView.getMode());
            return;
        }
        super.onBackPressed();
    }
}
