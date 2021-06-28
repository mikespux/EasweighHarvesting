package com.plantation.preferences;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.data.DBHelper;


/**
 * Created by Abderrahim on 8/19/2015.
 */
public class PreferenceCompanySettings extends PreferenceActivity {
    static SharedPreferences mSharedPrefs, prefs;
    DBHelper dbhelper;
    Button btnImport;
    Intent mIntent;
    String cachedDeviceAddress;
    Button btn_pairscale, btn_pairprinter;
    AlertDialog b;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_companysettings);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        root.addView(toolbar, 0);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(R.string.General_settings);
        toolbar.setTitleTextColor(Color.WHITE);


        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dbhelper = new DBHelper(getApplicationContext());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        //Display alert message when back button has been pressed

        if (mSharedPrefs.getString("scaleVersion", "").equals("")) {
            finish();
            mIntent = new Intent(PreferenceCompanySettings.this, PreferenceGeneralSettings.class);
            startActivity(mIntent);
            return;
        }
        finish();
        return;

    }
}
