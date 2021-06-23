package com.plantation.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.ybq.android.spinkit.style.Pulse;
import com.plantation.R;

/**
 * Created by Michael on 15/09/2016.
 */
public class SplashActivity extends AppCompatActivity {
    private Pulse mWaveDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializer();
    }

    private void initializer() {
        new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //this will be done every 1000 milliseconds ( 1 seconds )
                ImageView imageView = findViewById(R.id.image);
                mWaveDrawable = new Pulse();
                mWaveDrawable.setColor(Color.WHITE);
                imageView.setImageDrawable(mWaveDrawable);
                imageView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                mWaveDrawable.start();
            }

            @Override
            public void onFinish() {
                finish();
                Intent login = new Intent(getApplicationContext(), MainActivity.class);
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
