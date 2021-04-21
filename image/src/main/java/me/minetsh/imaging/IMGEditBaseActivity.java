package me.minetsh.imaging;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.ViewSwitcher;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import me.minetsh.imaging.core.IMGMode;
import me.minetsh.imaging.core.IMGText;
import me.minetsh.imaging.rs.EnhanceMode;
import me.minetsh.imaging.rs.RSTool;
import me.minetsh.imaging.view.IMGColorGroup;
import me.minetsh.imaging.view.IMGView;

abstract class IMGEditBaseActivity extends Activity implements View.OnClickListener,
        IMGTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener,
        DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    protected IMGView mImgView;
    protected Bitmap mOriginalBitmap;
    protected Bitmap mCurrBitmap;
    private Bitmap mEnhanceBitmap;

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
    private SeekBar mEnhanceSeekBar;
    private HorizontalScrollView mEnhanceScrollView;

    private EnhanceMode mEnhanceMode;

    private Disposable mImageUpdateDisposable;

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
            mRSTool = new RSTool(this, mOriginalBitmap);
            mCurrBitmap = mOriginalBitmap.copy(mOriginalBitmap.getConfig(), true);
            mEnhanceBitmap = Bitmap.createBitmap(mCurrBitmap.getWidth(), mCurrBitmap.getHeight(), mCurrBitmap.getConfig());
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
        mEnhanceSeekBar = findViewById(R.id.skb_enhance);
        mEnhanceScrollView = findViewById(R.id.scroll_enhance);

        mColorGroup.setOnCheckedChangeListener(this);
        mFilterGroup.setOnCheckedChangeListener(mFilterChangeListener);
        mEnhanceGroup.setOnCheckedChangeListener(mEnhanceChangeListener);
        mEnhanceSeekBar.setOnSeekBarChangeListener(this);
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
            if (mCurrBitmap != mOriginalBitmap) {
                mCurrBitmap.recycle();
            }
            mCurrBitmap = mImgView.getImageBitmap();
            mRSTool.setBitmap(mCurrBitmap);
        } else if (vid == R.id.btn_filter_cancel) {
            mFilterGroup.check(R.id.btn_origin);
            onModeClick(IMGMode.FILTER);
        } else if (vid == R.id.btn_enhance_done) {
            onModeClick(IMGMode.ENHANCE);
            mRSTool.setBitmap(mCurrBitmap);
            resetEnhance();
        } else if (vid == R.id.btn_enhance_cancel) {
            resetEnhance();
            onModeClick(IMGMode.ENHANCE);
        } else if (vid == R.id.tv_enhance_reset) {
            resetEnhance();
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mEnhanceMode.updateParamFromProgress(progress);
            updateImage();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private void updateImage() {
        if (mImageUpdateDisposable != null && !mImageUpdateDisposable.isDisposed()) {
            mImageUpdateDisposable.dispose();
        }
        mImageUpdateDisposable = Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
//            Bitmap bitmap = mRSTool.blur(EnhanceMode.BLUR.getParam(), mEnhanceBitmap);
//            Bitmap bitmap = mRSTool.brightness(EnhanceMode.BRIGHTNESS.getParam(), EnhanceMode.CONTRAST.getParam(), mEnhanceBitmap);
//            Bitmap bitmap = mRSTool.contrast(EnhanceMode.BRIGHTNESS.getParam(), EnhanceMode.CONTRAST.getParam(), mEnhanceBitmap);
//            Bitmap bitmap = mRSTool.saturation(EnhanceMode.SATURATION.getParam(), mEnhanceBitmap);
//            Bitmap bitmap = mRSTool.hue(EnhanceMode.HUE.getParam(), mEnhanceBitmap);
//            Bitmap bitmap = mRSTool.emboss(EnhanceMode.EMBOSS.getParam(), mEnhanceBitmap);
            Bitmap bitmap = mRSTool.enhance(
                    EnhanceMode.BRIGHTNESS.getParam(),
                    EnhanceMode.CONTRAST.getParam(),
                    EnhanceMode.SATURATION.getParam(),
                    EnhanceMode.HUE.getParam()
            );
            emitter.onSuccess(bitmap);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    mEnhanceBitmap = mCurrBitmap;
                    mCurrBitmap = bitmap;
                    mImgView.setImageBitmap(bitmap);
                }, Throwable::printStackTrace);
    }

    private void resetCurrentEnhance() {
        mEnhanceMode.resetParam();
        mEnhanceSeekBar.setProgress(mEnhanceMode.getProgressFromParam());
        updateImage();
    }

    private void resetEnhance() {
        EnhanceMode.BLUR.resetParam();
        EnhanceMode.BRIGHTNESS.resetParam();
        EnhanceMode.CONTRAST.resetParam();
        EnhanceMode.SATURATION.resetParam();
        EnhanceMode.HUE.resetParam();
        mEnhanceSeekBar.setProgress(mEnhanceMode.getProgressFromParam());
        updateImage();
    }

    private final RadioGroup.OnCheckedChangeListener mFilterChangeListener = (group, checkedId) -> {
        if (mImgView.getImageBitmap() != mCurrBitmap) {
            mImgView.getImageBitmap().recycle();
        }

        Bitmap bitmap = null;
        if (checkedId == R.id.btn_origin) {
            bitmap = mCurrBitmap;
        } else if (checkedId == R.id.btn_grayscale) {
            bitmap = mRSTool.grayscale();
        } else if (checkedId == R.id.btn_black_gold) {
            bitmap = mRSTool.blackGold();
        } else if (checkedId == R.id.btn_invert) {
            bitmap = mRSTool.invert();
        } else if (checkedId == R.id.btn_nostalgia) {
            bitmap = mRSTool.nostalgia();
        } else if (checkedId == R.id.btn_comic) {
            bitmap = mRSTool.comic();
        } else if (checkedId == R.id.btn_emboss) {
            bitmap = mRSTool.emboss(0.6f);
        }
        mImgView.setImageBitmap(bitmap);
    };

    private final RadioGroup.OnCheckedChangeListener mEnhanceChangeListener = (group, checkedId) -> {
        if (checkedId == R.id.btn_brightness) {
            mEnhanceMode = EnhanceMode.BRIGHTNESS;
        } else if (checkedId == R.id.btn_contrast) {
            mEnhanceMode = EnhanceMode.CONTRAST;
        } else if (checkedId == R.id.btn_saturation) {
            mEnhanceMode = EnhanceMode.SATURATION;
        } else if (checkedId == R.id.btn_hue) {
            mEnhanceMode = EnhanceMode.HUE;
        } else if (checkedId == R.id.btn_blur) {
            mEnhanceMode = EnhanceMode.BLUR;
        }
        mEnhanceSeekBar.setProgress(mEnhanceMode.getProgressFromParam());
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
            mEnhanceMode = EnhanceMode.BRIGHTNESS;
            mEnhanceGroup.check(R.id.btn_brightness);
            mEnhanceSeekBar.setProgress(EnhanceMode.BRIGHTNESS.getProgressFromParam());
            mEnhanceScrollView.scrollTo(0, 0);
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
