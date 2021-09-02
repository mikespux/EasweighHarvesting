package com.plantation.preferences;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.plantation.R;


public class OtherSettings extends PreferenceFragmentCompat {

    static SharedPreferences mSharedPrefs, prefs;
    EditTextPreference buyingPrice;
    CheckBoxPreference checkPrinting;

    public OtherSettings() {
    }

    public static OtherSettings newInstance() {
        OtherSettings fragment = new OtherSettings();
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.other_settings, rootKey);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        buyingPrice = findPreference("buyingPrice");
        buyingPrice.setSummary(mSharedPrefs.getString("buyingPrice", getResources().getString(R.string.ebuyingPriceSummary)));
        buyingPrice.setOnPreferenceChangeListener((preference, newValue) -> {
            buyingPrice.setSummary(newValue.toString());
            return true;
        });

        // Loads the title for the first time

        checkPrinting = findPreference("enablePrinting");
        checkPrinting.setOnPreferenceClickListener(preference -> false);


    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        return;
    }


}
