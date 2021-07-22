package com.plantation.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.plantation.R;

/**
 * Created by Michael on 15/09/2016.
 */
public class SplashActivity extends AppCompatActivity {
    AlertDialog setup;
    SharedPreferences mSharedPrefs, prefs;
    private ThreeBounce mWaveDrawable;
    private Button btn_manual, btn_realtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializer();
    }

    private void initializer() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //this will be done every 1000 milliseconds ( 1 seconds )
                ImageView imageView = findViewById(R.id.image);
                mWaveDrawable = new ThreeBounce();
                mWaveDrawable.setColor(Color.WHITE);
                imageView.setImageDrawable(mWaveDrawable);
                imageView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                mWaveDrawable.start();
            }

            @Override
            public void onFinish() {
                //  SetupOption();
                finish();
                Intent login = new Intent(getApplicationContext(), CompanyURLConfigActivity.class);
                startActivity(login);
            }

        }.start();

    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onStop() {
        super.onStop();
        mWaveDrawable.stop();

    }


}
