package com.plantation.activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.services.EasyWeighBioService;

import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Created by Michael on 16/08/2016.
 */
public class VerificationModeAllocatedTaskActivity extends AppCompatActivity {
    static SharedPreferences mSharedPrefs, prefs;
    public Intent mIntent;
    public Toolbar toolbar;
    Button btn_cardsearch, btn_manualsearch, btn_fingerprintsearch;
    EasyWeighBioService resetConn;
    private LocalDate systemdate, currentdate, batchdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_modes);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Verification Modes");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        return;
    }

    public void initializer() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(VerificationModeAllocatedTaskActivity.this);
        prefs = PreferenceManager.getDefaultSharedPreferences(VerificationModeAllocatedTaskActivity.this);
        resetConn = new EasyWeighBioService();
        systemdate = LocalDate.parse(prefs.getString("basedate", ""));
        batchdate = LocalDate.parse(prefs.getString("BatchON", ""));
        currentdate = new LocalDate();
        if (Days.daysBetween(currentdate, systemdate).getDays() >= 1) {

            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("Please Contact the Administrator to Solve Base Date Error!!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            new LogOut().execute();
            return;
        } else if (Days.daysBetween(batchdate, currentdate).getDays() >= 1) {

            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("Please ensure the Batch Date is the same as Current Date!!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            finish();
            return;
        } else if (Days.daysBetween(systemdate, currentdate).getDays() >= 7) {
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("Please Contact the Administrator to Solve Base Date Error!!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            new LogOut().execute();
            return;
        }

        enableBT();


        btn_manualsearch = findViewById(R.id.btn_manualsearch);
        btn_manualsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mIntent = new Intent(VerificationModeAllocatedTaskActivity.this, AllocatedTaskDetailsActivity.class);
                startActivity(mIntent);
            }


        });

        btn_cardsearch = findViewById(R.id.btn_cardsearch);
        btn_cardsearch.setVisibility(View.GONE);
        btn_cardsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_fingerprintsearch = findViewById(R.id.btn_fingerprintsearch);
        btn_fingerprintsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }


    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }


    public void disableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
    }

    private class LogOut extends AsyncTask<Void, Void, String> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(VerificationModeAllocatedTaskActivity.this,
                    getString(R.string.please_wait),
                    getString(R.string.logging_out),
                    true);
        }

        @Override
        protected String doInBackground(Void... params) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(VerificationModeAllocatedTaskActivity.this);
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("pass");
            edit.commit();

            try {
                Thread.sleep(1000);
                resetConn.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return "";
        }

        @Override
        protected void onPostExecute(String result) {

            dialog.dismiss();
            disableBT();
            finish();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

}
