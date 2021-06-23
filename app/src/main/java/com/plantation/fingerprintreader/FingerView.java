package com.plantation.fingerprintreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;


@SuppressLint("AppCompatCustomView")
public class FingerView extends ImageView {
    private static final String TAG = "FingerView";
    private static final int STATE_NORMAL = 0;
    private static final int STATE_SELECTED = 1;
    private static final int STATE_REGISTED = 2;
    private static final int STATE_SELECTED_AND_REGISTED = 3;
    private static final int STATE_READING = 4;
    /*
     * 0:noraml 1:selected 2:registed 3:selected & registed 4:reading
     */
    private final Drawable[] mDrawables = new Drawable[5];
    private boolean mSelected = false;
    private boolean mRegisted = false;
    private boolean mReading = false;
    // private int mHeights[] = new int[5];

    public FingerView(Context context) {
        super(context);
        Log.i(TAG, "FingerView(Context context)");
        for (int i = 0; i < mDrawables.length; i++) {
            mDrawables[i] = getDrawable();
        }
    }

    public FingerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "FingerView(Context context, AttributeSet attrs)");
        for (int i = 0; i < mDrawables.length; i++) {
            mDrawables[i] = getDrawable();
        }
    }

    public FingerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.i(TAG,
                "FingerView(Context context, AttributeSet attrs, int defStyle)");
        for (int i = 0; i < mDrawables.length; i++) {
            mDrawables[i] = getDrawable();
        }
    }

    @Override
    public boolean performClick() {
        setSelected(!mSelected);
        return super.performClick();
    }

    @Override
    public void setSelected(boolean isSelected) {
        super.setSelected(isSelected);
        mSelected = isSelected;
        updateView();
    }

    public boolean getRegisted() {
        return mRegisted;
    }

    public void setRegisted(boolean isRegisted) {
        mRegisted = isRegisted;
        updateView();
    }

    public void setReading(boolean isReading) {
        mReading = isReading;
        updateView();
    }

    private void updateView() {
        Drawable d;
        if (mSelected) {
            if (mRegisted) {
                d = mDrawables[STATE_SELECTED_AND_REGISTED];
            } else if (mReading) {
                d = mDrawables[STATE_READING];
            } else {
                d = mDrawables[STATE_SELECTED];
            }
        } else {
            if (mRegisted) {
                d = mDrawables[STATE_REGISTED];
            } else {
                d = mDrawables[STATE_NORMAL];
            }
        }
        setImageDrawable(d);
    }

    public void setImageDrawables(Drawable noraml, Drawable selected, Drawable reading, Drawable registed, Drawable selectedAndRegisted) {
        mDrawables[STATE_NORMAL] = noraml;
        mDrawables[STATE_SELECTED] = selected;
        mDrawables[STATE_REGISTED] = registed;
        mDrawables[STATE_SELECTED_AND_REGISTED] = selectedAndRegisted;
        mDrawables[STATE_READING] = reading;
    }

    public void setImageResources(int noraml, int selected, int reading, int registed, int selectedAndRegisted) {
        Resources res = getResources();
        setImageDrawables(res.getDrawable(noraml), res.getDrawable(selected),
                res.getDrawable(reading), res.getDrawable(registed),
                res.getDrawable(selectedAndRegisted));
    }

    /*public void setFingerStateHeights(int noraml, int selected, int reading,
            int registed, int selectedAndRegisted) {
        mHeights[STATE_NORMAL] = noraml;
        mHeights[STATE_SELECTED] = selected;
        mHeights[STATE_REGISTED] = registed;
        mHeights[STATE_SELECTED_AND_REGISTED] = selectedAndRegisted;
        mHeights[STATE_READING] = reading;
    }*/
}
