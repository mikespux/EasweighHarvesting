package com.plantation.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Michael on 30/06/2016.
 */
public class CompanyURLConfigActivity extends AppCompatActivity {
    public Toolbar toolbar;
    EditText wf_ipaddress, md_ipaddress, co_port, co_app, co_uname, co_pass;
    String _URL;
    String Sco_ipaddress, Sco_port, Sco_app, Sco_uname, Sco_pass;
    Button btnNext;
    SharedPreferences mSharedPrefs, prefs;
    String _TOKEN, restApiResponse = "";
    String access_token, token_type, expires_in, userName, issued, expires;
    String error, desc;
    RadioButton radioWF, radioMD, radioFile;
    LinearLayout ltWF, ltMD, ltCloud;
    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipconfig);
        setupToolbar();
        initializer();

    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cloud Setup");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void initializer() {

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!isInternetOn()) {
            createNetErrorDialog();
            return;
        }

        ltWF = findViewById(R.id.ltWF);
        ltMD = findViewById(R.id.ltMD);
        ltCloud = findViewById(R.id.ltCloud);

        radioWF = findViewById(R.id.radioWF);
        radioWF.setOnClickListener(view -> {
            ltCloud.setVisibility(View.VISIBLE);
            ltWF.setVisibility(View.VISIBLE);
            ltMD.setVisibility(View.GONE);
        });

        radioMD = findViewById(R.id.radioMD);
        radioMD.setOnClickListener(view -> {
            ltCloud.setVisibility(View.VISIBLE);
            ltWF.setVisibility(View.GONE);
            ltMD.setVisibility(View.VISIBLE);
        });
        radioFile = findViewById(R.id.radioFile);
        radioFile.setOnClickListener(view -> {
            ltCloud.setVisibility(View.GONE);
        });

        wf_ipaddress = findViewById(R.id.wf_ipaddress);
        wf_ipaddress.setText(prefs.getString("wf_ipaddress", ""));

        md_ipaddress = findViewById(R.id.md_ipaddress);
        md_ipaddress.setText(prefs.getString("md_ipaddress", ""));


        co_port = findViewById(R.id.co_port);
        co_port.setText(prefs.getString("coport", ""));

        co_app = findViewById(R.id.co_app);
        co_app.setText(prefs.getString("coapp", ""));

        co_uname = findViewById(R.id.co_uname);
        co_uname.setText(prefs.getString("couser", ""));

        co_uname = findViewById(R.id.co_uname);
        co_uname.setText(prefs.getString("couser", ""));

        co_pass = findViewById(R.id.co_pass);
        co_pass.setText(prefs.getString("copass", ""));


        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            if (radioFile.isChecked()) {
                finish();
                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.putBoolean("manual_setup", true);
                edit.apply();
                Intent mIntent = new Intent(getApplicationContext(), SyncMastersActivity.class);
                startActivity(mIntent);
                return;
            } else {
                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.putBoolean("manual_setup", false);
                edit.apply();
            }
            if (!isInternetOn()) {
                createNetErrorDialog();
                return;
            }

            if (radioWF.isChecked()) {
                Sco_ipaddress = wf_ipaddress.getText().toString();
                Sco_port = co_port.getText().toString();
                Sco_app = co_app.getText().toString();
                if (!mSharedPrefs.getBoolean("cloudServices", false)) {
                    SharedPreferences.Editor edit = mSharedPrefs.edit();
                    edit.putBoolean("cloudServices", true);
                    edit.apply();
                }

                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.putString("internetAccessModes", "WF");
                edit.apply();

                edit.putString("portalURL", "http://" + Sco_ipaddress);
                edit.apply();

                edit.putString("coPort", Sco_port);
                edit.apply();

                edit.putString("coApp", Sco_app);
                edit.apply();


            }
            if (radioMD.isChecked()) {
                Sco_ipaddress = md_ipaddress.getText().toString();
                Sco_port = co_port.getText().toString();
                Sco_app = co_app.getText().toString();

                if (!mSharedPrefs.getBoolean("cloudServices", false)) {
                    SharedPreferences.Editor edit = mSharedPrefs.edit();
                    edit.putBoolean("cloudServices", true);
                    edit.apply();
                }
                if (!mSharedPrefs.getBoolean("realtimeServices", false)) {
                    SharedPreferences.Editor edit = mSharedPrefs.edit();
                    edit.putBoolean("realtimeServices", true);
                    edit.apply();
                }
                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.putString("internetAccessModes", "MD");
                edit.apply();

                edit.putString("mdportalURL", "http://" + Sco_ipaddress);
                edit.apply();

                edit.putString("coPort", Sco_port);
                edit.apply();

                edit.putString("coApp", Sco_app);
                edit.apply();


            }

            Sco_uname = co_uname.getText().toString();
            Sco_pass = co_pass.getText().toString();

            if (Sco_ipaddress.equals("")) {
                Toast.makeText(getApplicationContext(), "Please enter Ip Address", Toast.LENGTH_LONG).show();
                return;
            }
            if (Sco_app.equals("")) {
                Toast.makeText(getApplicationContext(), "Please enter Application", Toast.LENGTH_LONG).show();
                return;
            }
            if (Sco_uname.equals("")) {
                Toast.makeText(getApplicationContext(), "Please enter UserName", Toast.LENGTH_LONG).show();
                return;
            }
            if (Sco_pass.equals("")) {
                Toast.makeText(getApplicationContext(), "Please enter Password", Toast.LENGTH_LONG).show();
                return;
            }
            if (mSharedPrefs.getString("internetAccessModes", null).equals("WF")) {
                if (mSharedPrefs.getString("coPort", "").equals("")) {
                    _URL = mSharedPrefs.getString("portalURL", null) + "/" +
                            mSharedPrefs.getString("coApp", null);
                } else {
                    _URL = mSharedPrefs.getString("portalURL", "") + ":"
                            + mSharedPrefs.getString("coPort", "") + "/" +
                            mSharedPrefs.getString("coApp", "");

                }


            } else {
                if (mSharedPrefs.getString("coPort", "").equals("")) {
                    _URL = mSharedPrefs.getString("mdportalURL", null) + "/" +
                            mSharedPrefs.getString("coApp", null);
                } else {
                    _URL = mSharedPrefs.getString("mdportalURL", "") + ":"
                            + mSharedPrefs.getString("coPort", "") + "/" +
                            mSharedPrefs.getString("coApp", "");

                }
            }

            SharedPreferences.Editor edit = prefs.edit();

            edit.putString("wf_ipaddress", wf_ipaddress.getText().toString());
            edit.apply();

            edit.putString("md_ipaddress", md_ipaddress.getText().toString());
            edit.apply();

            edit.putString("coport", Sco_port);
            edit.apply();

            edit.putString("coapp", Sco_app);
            edit.apply();

            edit.putString("couser", Sco_uname);
            edit.apply();

            edit.putString("copass", Sco_pass);
            edit.apply();

            Login();
            //new Login().execute();

        });


    }

    public String getTokenMain(String URL, String Username, String Password) {

        try {
            String result = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "Grant_type=password&username=" + Username + "&password=" + Password + "");
            Request request = new Request.Builder()
                    .url(URL + "/token")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("RestApiRequest", response.body().string());
            result = responseBodyCopy.string();

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            String Server = "-8080";
            Log.e("SoapApiRequest", e.toString());
            Log.e("Server Response", e.toString());
            e.printStackTrace();
            return Server;
        }
    }

    public boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec = (ConnectivityManager) getBaseContext().getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {


            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {


            return false;
        }
        return false;
    }

    protected void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(CompanyURLConfigActivity.this);
        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>You need internet connection to proceed. Please turn on mobile network or Wi-Fi in Settings.</font>"))
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setNegativeButton("Settings",
                        (dialog, id) -> {
                            if (mSharedPrefs.getString("internetAccessModes", "WF").equals("WF")) {

                                Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                startActivity(i);
                            } else {
                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(i);

                            }


                        }
                )
                .setPositiveButton("Cancel",
                        (dialog, id) -> dialog.dismiss()
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void Login() {
        progressDialog = ProgressDialog.show(CompanyURLConfigActivity.this,
                "Connecting",
                "Please Wait.. ");


        executor.execute(() -> {

            //Background work here

            try {

                restApiResponse = getTokenMain(_URL, Sco_uname, Sco_pass);
                Log.i("_URL", _URL);
                JSONObject jsonObject = new JSONObject(restApiResponse);

                if (jsonObject.has("access_token") && !jsonObject.isNull("access_token")) {
                    access_token = jsonObject.getString("access_token");
                    token_type = jsonObject.getString("token_type");
                    expires_in = jsonObject.getString("expires_in");
                    userName = jsonObject.getString("userName");
                    issued = jsonObject.getString(".issued");
                    expires = jsonObject.getString(".expires");
                    Log.i("INFO", access_token + "" + token_type + "" + expires_in);

                    SharedPreferences.Editor edit = mSharedPrefs.edit();
                    edit.putString("token", access_token);
                    edit.apply();
                    edit.putString("expires_in", expires_in);
                    edit.apply();
                    edit.putString("expires", expires);
                    edit.apply();

                } else {
                    error = jsonObject.getString("error");
                    desc = jsonObject.getString("error_description");

                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                //UI Thread work here
                _TOKEN = access_token;

                if (_TOKEN == null || _TOKEN.equals("")) {
                    progressDialog.dismiss();
                    if (restApiResponse.equals("-8080")) {
                        desc = "Failed to Connect to " + Sco_ipaddress;
                    }
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText(desc);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                } else {
                    progressDialog.dismiss();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Successfully Connected ");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    finish();
                    Intent login = new Intent(getApplicationContext(), CompanyDetailsActivity.class);
                    startActivity(login);
                }

            });
        });

    }

}
