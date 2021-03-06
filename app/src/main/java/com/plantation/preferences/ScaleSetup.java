package com.plantation.preferences;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.plantation.R;


public class ScaleSetup extends PreferenceFragmentCompat {

    static SharedPreferences mSharedPrefs, prefs;

    ListPreference HarvestedCrop, vModes, scaleVersion, weighingAlgorithm;

    EditTextPreference bagWeight, stabilityReadingCounter, milliSeconds, moisture, maxBatchCrates, minCRange, maxCRange;

    public ScaleSetup() {
    }

    public static ScaleSetup newInstance() {
        ScaleSetup fragment = new ScaleSetup();
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.scale_setup, rootKey);
        enableBT();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());



        vModes = findPreference("vModes");
        vModes.setSummary(mSharedPrefs.getString("vModes", getResources().getString(R.string.verificationModesSummary)));
        vModes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                vModes.setSummary(newValue.toString());
                return true;
            }
        });

        scaleVersion = findPreference("scaleVersion");
        scaleVersion.setSummary(mSharedPrefs.getString("scaleVersion", getResources().getString(R.string.scaleVersionSummary)));
        scaleVersion.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                scaleVersion.setSummary(newValue.toString());
                return true;
            }
        });

        weighingAlgorithm = findPreference("weighingAlgorithm");
        weighingAlgorithm.setSummary(mSharedPrefs.getString("weighingAlgorithm", getResources().getString(R.string.weighingAlgorithmSummary)));
        weighingAlgorithm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                weighingAlgorithm.setSummary(newValue.toString());
                return true;
            }
        });

        bagWeight = findPreference("bagWeight");
        bagWeight.setSummary(mSharedPrefs.getString("bagWeight", getResources().getString(R.string.bagWeightSummary)));
        bagWeight.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        });
        bagWeight.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                bagWeight.setSummary(newValue.toString());
                return true;
            }
        });


        stabilityReadingCounter = findPreference("stabilityReadingCounter");
        stabilityReadingCounter.setSummary(mSharedPrefs.getString("stabilityReadingCounter", getResources().getString(R.string.stabilitySummary)));
        stabilityReadingCounter.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_NUMBER);
            }
        });
        stabilityReadingCounter.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                stabilityReadingCounter.setSummary(newValue.toString());
                return true;
            }
        });


        milliSeconds = findPreference("milliSeconds");
        milliSeconds.setSummary(mSharedPrefs.getString("milliSeconds", getResources().getString(R.string.stabilitySummaryT)));

        moisture = findPreference("moisture");
        moisture.setSummary(mSharedPrefs.getString("moisture", getResources().getString(R.string.prefMoistureSummary)));
        moisture.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        });

        moisture.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                double min = 0.0;
                double max = 25.9;
                double val = Double.parseDouble(newValue.toString());
                if ((val >= min) && (val <= max)) {
                    moisture.setSummary(newValue.toString());
                    //Log.d(LOGTAG, "Value saved: " + val);
                    return true;
                } else {
                    // invalid you can show invalid message
                    Context context = getContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please enter values between " + min + " and " + max);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getApplicationContext(), "Please enter values between "+minTime +" and "+maxTime, Toast.LENGTH_LONG).show();
                    return false;
                }

            }
        });

        maxBatchCrates = findPreference("maxBatchCrates");
        maxBatchCrates.setSummary(mSharedPrefs.getString("maxBatchCrates", getResources().getString(R.string.prefBatchCrates)));
        maxBatchCrates.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                maxBatchCrates.setSummary(newValue.toString());
                return true;
            }
        });

        minCRange = findPreference("minCRange");
        minCRange.setSummary(mSharedPrefs.getString("minCRange", getResources().getString(R.string.minCRange)));
        minCRange.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                minCRange.setSummary(newValue.toString());
                return true;
            }
        });

        maxCRange = findPreference("maxCRange");
        maxCRange.setSummary(mSharedPrefs.getString("maxCRange", getResources().getString(R.string.maxCRange)));
        maxCRange.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                maxCRange.setSummary(newValue.toString());
                return true;
            }
        });
        HarvestedCrop = findPreference("cMode");
        HarvestedCrop.setSummary(mSharedPrefs.getString("cMode", getResources().getString(R.string.cropModeSummary)));
        HarvestedCrop.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                HarvestedCrop.setSummary(newValue.toString());
                if (newValue.toString().equals("HT")) {

                    maxBatchCrates.setVisible(true);
                    minCRange.setVisible(true);
                    maxCRange.setVisible(true);

                } else {
                    maxBatchCrates.setVisible(false);
                    minCRange.setVisible(false);
                    maxCRange.setVisible(false);
                }


                return true;
            }
        });
        if (mSharedPrefs.getString("cMode", getResources().getString(R.string.cropModeSummary)).equals("HT")) {

            maxBatchCrates.setVisible(true);
            minCRange.setVisible(true);
            maxCRange.setVisible(true);

        } else {
            maxBatchCrates.setVisible(false);
            minCRange.setVisible(false);
            maxCRange.setVisible(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        return;
    }

    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }
}
