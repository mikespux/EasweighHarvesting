package com.plantation.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.plantation.R;

/**
 * Created by Michael on 15/03/2017.
 */
public class AndroidImageAdapter extends PagerAdapter {
    private final int[] sliderImagesId = new int[]{
            R.drawable.cashew_1, R.drawable.cashew_2, R.drawable.cashew_3,};
    private final int[] sliderImagesId1 = new int[]{
            R.drawable.tea_1, R.drawable.tea_2, R.drawable.tea_3,

    };
    Context mContext;
    SharedPreferences mSharedPrefs;


    AndroidImageAdapter(Context context) {
        this.mContext = context;

    }

    @Override
    public int getCount() {
        return sliderImagesId.length;
    }

    @Override
    public boolean isViewFromObject(View v, Object obj) {
        return v == obj;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int i) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(container.getContext());
        ImageView mImageView = new ImageView(mContext);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {
            mImageView.setImageResource(sliderImagesId1[i]);

        } else {
            mImageView.setImageResource(sliderImagesId[i]);
        }

        container.addView(mImageView, 0);
        return mImageView;
    }


    @Override
    public void destroyItem(ViewGroup container, int i, Object obj) {
        container.removeView((ImageView) obj);
    }
}
