package com.plantation.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.synctocloud.HttpHandler;
import com.plantation.synctocloud.MasterApiRequest;
import com.plantation.synctocloud.RestApiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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
public class CompanyDetailsActivity extends AppCompatActivity {
    public static final String TAG = "Activate";
    public Toolbar toolbar;
    Intent mIntent;
    EditText co_prefix, co_name, co_letterbox, co_postcode, co_postname, co_postregion, co_telephone;
    String Sco_prefix, Sco_name, Sco_letterbox, Sco_postcode, Sco_postname, Sco_postregion, Sco_telephone;
    DBHelper db;
    Button btn_svCompany, btn_Users;
    String CRecordIndex, CoPrefix, CoName, CoLetterBox, CoPostCode, CoPostName, coPostRegion, CoTelephone,
            CoFax, CoEmail, CoRegistrationNumber, CoPIN, CoVAT, CoCountry, CoCity, CoWebsite;
    String _URL, _TOKEN;
    SharedPreferences mSharedPrefs, prefs;


    String Id, Title, Message;
    String s_factory, s_terminal, s_phone;
    EditText et_terminal, et_phone;
    Button btnActivate;
    AlertDialog dActivate;
    int count = 0;
    String CompanyID, DeviceID, PhoneNo, UserID, SerialNumber;

    String RecordIndex, InternalSerial, ExternalSerial, AllocEstate;

    String factories;
    String factoryid = null;
    ArrayList<String> factorydata = new ArrayList<String>();
    ArrayAdapter<String> factoryadapter;
    Spinner spinnerFactory;
    String FRecordIndex, FryPrefix, FryTitle, FryCapacity, FryCoName, FryPostalAddress, poCode, poName, FryPostOffice, FryTelephone;


    String ERecordIndex, s_esID, s_esName, s_esCompany;

    String TRecordIndex, s_tptID, s_tptName;


    DBHelper dbHelper;
    boolean refresh = false;

    String restApiResponse;
    int response;
    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    public static String getIMEIDeviceId(Context context) {

        String deviceId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
            }
            assert mTelephony != null;
            if (mTelephony.getDeviceId() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceId = mTelephony.getImei();
                } else {
                    deviceId = mTelephony.getDeviceId();
                }
            } else {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        Log.d("deviceId", deviceId);
        return deviceId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companydetails);
        setupToolbar();
        initializer();

    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Company Details");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void initializer() {
        dbHelper = new DBHelper(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!isInternetOn()) {
            createNetErrorDialog();
            return;
        }
        refresh = mSharedPrefs.getBoolean("prefs_refresh", false);
        if (refresh == true) {
            _TOKEN = prefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("") || _TOKEN == "") {
                _TOKEN = new RestApiRequest(getApplicationContext()).getToken();

            } else {
                long token_hours = new RestApiRequest(getApplicationContext()).token_hours();
                if (token_hours >= 23) {
                    _TOKEN = new RestApiRequest(getApplicationContext()).getToken();

                }
            }

        } else {
            _TOKEN = prefs.getString("token", null);
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
        co_prefix = findViewById(R.id.co_prefix);
        co_name = findViewById(R.id.co_name);
        co_letterbox = findViewById(R.id.co_letterbox);
        co_postcode = findViewById(R.id.co_postcode);
        co_postname = findViewById(R.id.co_postname);
        co_postregion = findViewById(R.id.co_postregion);
        co_telephone = findViewById(R.id.co_telephone);
        if (refresh == true) {
            AllocatedFactoryDetails();
        } else {
            CompanyDetails();
        }
        db = new DBHelper(getApplicationContext());

        btn_svCompany = findViewById(R.id.btn_svCompany);
        btn_Users = findViewById(R.id.btn_Users);

        if (refresh == true) {

            btn_svCompany.setText("SAVE");
            btn_Users.setVisibility(View.VISIBLE);
            btn_Users.setOnClickListener(v -> {
                mIntent = new Intent(getApplicationContext(), SyncUsersActivity.class);
                startActivity(mIntent);
            });
        }


        btn_svCompany.setOnClickListener(v -> {
            if (!isInternetOn()) {
                createNetErrorDialog();
                return;
            }
            Sco_prefix = co_prefix.getText().toString();
            Sco_name = co_name.getText().toString();
            Sco_letterbox = co_letterbox.getText().toString();
            Sco_postcode = co_postcode.getText().toString();
            Sco_postname = co_postname.getText().toString();
            Sco_postregion = co_postregion.getText().toString();
            Sco_telephone = co_telephone.getText().toString();
            if (Sco_prefix.equals("") || Sco_name.equals("")) {
                Toast.makeText(getApplicationContext(), "Please enter Company Prefix and Company Name!!", Toast.LENGTH_LONG).show();
                return;
            }
            SharedPreferences.Editor edit = mSharedPrefs.edit();

            edit.putString("company_prefix", Sco_prefix);
            edit.commit();
            edit.putString("company_name", Sco_name);
            edit.commit();
            edit.putString("company_letterbox", Sco_letterbox);
            edit.commit();
            edit.putString("company_postalcode", Sco_postcode);
            edit.commit();
            edit.putString("company_postalname", Sco_postname);
            edit.commit();
            edit.putString("company_postregion", Sco_postregion);
            edit.commit();
            edit.putString("company_posttel", Sco_telephone);
            edit.commit();
            edit.putString("licenseKey", Sco_prefix);
            edit.commit();
            if (refresh == true) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Details Saved Successfully");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
            } else {
                showActivateDialog();
            }


        });


    }

    public void showActivateDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_activate_device, null);
        dialogBuilder.setView(dialogView);
        // dialogBuilder.setTitle("New Event");

        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("Activate Device");

        spinnerFactory = dialogView.findViewById(R.id.spinnerFactory);
        spinnerFactory.setVisibility(View.GONE);
        LoadFactories();
        LoadTransporters();
        et_terminal = dialogView.findViewById(R.id.et_terminal);
        et_phone = dialogView.findViewById(R.id.et_phone);
        btnActivate = dialogView.findViewById(R.id.btnActivate);


        btnActivate.setOnClickListener(v -> {

            try {

                s_terminal = et_terminal.getText().toString();
                s_phone = et_phone.getText().toString();

                if (s_terminal.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter Terminal ID", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.putString("terminalID", s_terminal);
                edit.commit();

                edit.putString("PhoneNo", s_phone);
                edit.commit();
                ValidateDevice();
            } catch (Exception e) {

                Toast.makeText(getApplicationContext(), "Saving  Failed", Toast.LENGTH_LONG).show();


            }

        });

        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();


            }
        });

        dActivate = dialogBuilder.create();
        dActivate.show();
    }

    public String ActivateDevice(String CompanyID, String DeviceID, String PhoneNo) {

        try {


            SerialNumber = getIMEIDeviceId(getApplicationContext());

            Log.d(TAG, SerialNumber);
            String result = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();

            JSONObject IOTjsonObject = new JSONObject();
            IOTjsonObject.put("CompanyID", CompanyID);
            IOTjsonObject.put("DeviceID", DeviceID);
            IOTjsonObject.put("SerialNumber", SerialNumber);
            IOTjsonObject.put("MISDN", PhoneNo);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, IOTjsonObject.toString());
            Request request = new Request.Builder()
                    .url(_URL + "/api/Farmlabor/Activatedevice")
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "application/json")
                    .build();

            response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("RestApiRequest", response.body().string());
            result = responseBodyCopy.string();

            return result;
        } catch (IOException | JSONException e) {
            String Server = "-8080";
            Log.e("SoapApiRequest", e.toString());
            Log.e("Server Response", e.toString());
            e.printStackTrace();
            return Server;
        }

    }

    private void FactoryList() {
        factorydata.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select FryPrefix,FryTitle from factory ", null);
        if (c != null) {

            if (c.moveToFirst()) {
                do {
                    factories = c.getString(c.getColumnIndex("FryTitle"));
                    factorydata.add(factories);

                } while (c.moveToNext());
            }
        }


        factoryadapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, factorydata);
        factoryadapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerFactory.setAdapter(factoryadapter);
        spinnerFactory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String factoryName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor c = db.rawQuery("select FryCloudID from factory where FryTitle= '" + factoryName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    factoryid = c.getString(c.getColumnIndex("FryCloudID"));


                }
                c.close();
                // db.close();
                // dbhelper.close();
                TextView tv = (TextView) view;
                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


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

        AlertDialog.Builder builder = new AlertDialog.Builder(CompanyDetailsActivity.this);
        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>You need internet connection to upload data. Please turn on mobile network or Wi-Fi in Settings.</font>"))
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setNegativeButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (mSharedPrefs.getString("internetAccessModes", "WF").equals("WF")) {

                                    Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                    startActivity(i);
                                } else {
                                    Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                    startActivity(i);

                                }


                            }
                        }
                )
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        if (refresh == true) {
            finish();
        } else {
            finish();
            mIntent = new Intent(getApplicationContext(), CompanyURLConfigActivity.class);
            startActivity(mIntent);
        }
        return;
    }


    public void CompanyDetails() {

        progressDialog = ProgressDialog.show(CompanyDetailsActivity.this,
                "Connecting",
                "Please Wait.. ");

        executor.execute(() -> {

            //Background work here
            try {

                HttpHandler sh = new HttpHandler();
                // Making a request to url and getting response


                String url = _URL + "/api/MasterData/Companies";
                String jsonStr = sh.makeServiceCall(url, _TOKEN);
                Log.i("url", url);
                Log.i("_TOKEN", _TOKEN);
                Log.i("jsonStr", jsonStr);
                CRecordIndex = "0";
                //  Log.e(TAG, "Response from url: " + jsonStr);
                if (jsonStr != null) {
                    try {


                        JSONArray dataArray = new JSONArray(jsonStr);
                        for (int i = 0, l = dataArray.length(); i < l; i++) {
                            JSONObject jsonObject = dataArray.getJSONObject(i);
                            if (jsonObject.has("RecordIndex") && !jsonObject.isNull("RecordIndex")) {
                                // Do something with object.

                                CRecordIndex = jsonObject.getString("RecordIndex");
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("CRecordIndex", CRecordIndex);
                                edit.commit();
                                edit.putString("CompanyIndex", CRecordIndex);
                                edit.commit();
                                CoPrefix = jsonObject.getString("CoPrefix");
                                CoName = jsonObject.getString("CoName");
                                CoLetterBox = jsonObject.getString("CoLetterBox");
                                CoPostCode = jsonObject.getString("CoPostCode");
                                CoPostName = jsonObject.getString("CoPostName");
                                coPostRegion = jsonObject.getString("coPostRegion");
                                CoTelephone = jsonObject.getString("CoTelephone");
                                CoFax = jsonObject.getString("CoFax");

                            }
                        }


                    } catch (final JSONException e) {
                        Log.e("TAG", "Json parsing error: " + e.getMessage());

                    }

                } else {
                    Log.e("TAG", "Couldn't get json from server.");

                }

                if (refresh) {
                    DeviceID = mSharedPrefs.getString("terminalID", "");


                    String url_device = _URL + "/api/MasterData/Weighingkits?Estate=0&Factory=0&$select=RecordIndex,ExternalSerial,DevIMEI,AllocEstate,EstatePrefix,EstateName&$filter=InternalSerial eq '" + DeviceID + "'";
                    String jsonStr_device = sh.makeServiceCall(url_device, _TOKEN);
                    Log.i("url", url_device);
                    Log.i("jsonStr", jsonStr_device);
                    if (jsonStr_device != null) {
                        try {


                            JSONArray dataArrayDevice = new JSONArray(jsonStr_device);
                            if (dataArrayDevice.length() > 0) {
                                for (int i = 0, l = dataArrayDevice.length(); i < l; i++) {
                                    JSONObject jsonObjectDevice = dataArrayDevice.getJSONObject(i);
                                    if (jsonObjectDevice.has("RecordIndex") && !jsonObjectDevice.isNull("RecordIndex")) {
                                        // Do something with object.

                                        RecordIndex = jsonObjectDevice.getString("RecordIndex");
                                        InternalSerial = jsonObjectDevice.getString("InternalSerial");
                                        ExternalSerial = jsonObjectDevice.getString("ExternalSerial");
                                        AllocEstate = jsonObjectDevice.getString("AllocEstate");
                                        SharedPreferences.Editor edit = mSharedPrefs.edit();
                                        edit.putString("Factory", jsonObjectDevice.getString("FryTitle"));
                                        edit.apply();

                                        Log.i("INFO", "RecordIndex: " + RecordIndex + " InternalSerial: " + InternalSerial + " AllocEstate: " + AllocEstate);

                                    }
                                }
                            } else {

                                CRecordIndex = "-1";
                                Title = "";
                                Message = "Registered Device Not Found.";
                                Log.i("INFO", "Message: " + Message);
                                return;
                            }


                        } catch (final JSONException e) {
                            Log.e("TAG", "Json parsing error: " + e.getMessage());

                        }

                    } else {
                        Log.e("TAG", "Couldn't get json from server.");

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                //UI Thread work here
                if (Integer.parseInt(CRecordIndex) > 0) {
                    co_prefix.setText(CoPrefix);
                    co_name.setText(CoName);
                    co_letterbox.setText(CoLetterBox);
                    co_postcode.setText(CoPostCode);
                    co_postname.setText(CoPostName);
                    co_postregion.setText(coPostRegion);
                    co_telephone.setText(CoTelephone);

                    progressDialog.dismiss();

                } else {
                    if (refresh == true) {
                        if (Integer.parseInt(CRecordIndex) == -1) {
                            progressDialog.dismiss();
                            Context context = getApplicationContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText(Message);
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();

                            co_prefix.setText(CoPrefix);
                            co_name.setText(CoName);
                            co_letterbox.setText(CoLetterBox);
                            co_postcode.setText(CoPostCode);
                            co_postname.setText(CoPostName);
                            co_postregion.setText(coPostRegion);
                            co_telephone.setText(CoTelephone);
                            return;
                        }
                    }
                    progressDialog.dismiss();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Company Details Not Loaded");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    if (refresh == true) {
                        finish();
                    } else {
                        finish();
                        mIntent = new Intent(getApplicationContext(), CompanyURLConfigActivity.class);
                        startActivity(mIntent);
                        Toast.makeText(getApplicationContext(), _URL, Toast.LENGTH_LONG).show();
                    }

                }
            });
        });
    }

    public void ValidateDevice() {
        progressDialog = ProgressDialog.show(CompanyDetailsActivity.this,
                "Validating",
                "Please Wait.. ");
        executor.execute(() -> {

            //Background work here
            try {
                restApiResponse = "";


                HttpHandler sh = new HttpHandler();
                // Making a request to url and getting response

                DeviceID = s_terminal;


                String url = _URL + "/api/MasterData/Weighingkits?Estate=0&Factory=0&$select=RecordIndex,InternalSerial,ExternalSerial,DevIMEI,AllocEstate,EstatePrefix,EstateName&$filter=InternalSerial eq '" + DeviceID + "'";
                String jsonStr = sh.makeServiceCall(url, _TOKEN);
                Log.i("url", url);
                Log.i("_TOKEN", _TOKEN);
                Log.i("jsonStr", jsonStr);
                if (jsonStr != null) {
                    try {


                        JSONArray dataArray = new JSONArray(jsonStr);
                        if (dataArray.length() > 0) {
                            for (int i = 0, l = dataArray.length(); i < l; i++) {
                                JSONObject jsonObject = dataArray.getJSONObject(i);
                                if (jsonObject.has("RecordIndex") && !jsonObject.isNull("RecordIndex")) {
                                    // Do something with object.

                                    RecordIndex = jsonObject.getString("RecordIndex");
                                    InternalSerial = jsonObject.getString("InternalSerial");
                                    ExternalSerial = jsonObject.getString("ExternalSerial");
                                    AllocEstate = jsonObject.getString("AllocEstate");
                                    s_esID = jsonObject.getString("EstatePrefix");
                                    s_esName = jsonObject.getString("EstateName");
                                    Log.i("INFO", "RecordIndex: " + RecordIndex + " InternalSerial: " + InternalSerial + " AllocEstate: " + AllocEstate);

                                }
                            }
                        } else {
                            AllocEstate = "-1";
                            Id = "-1";
                            Title = "";
                            Message = "This Device is Not Found!";
                            Log.i("INFO", "Message: " + Message);
                        }

                        if (AllocEstate == null || AllocEstate == "" || AllocEstate == "null") {

                            Id = "-1";
                            Title = "";
                            Message = "Device is not allocated to Estate";
                        } else if (Integer.parseInt(AllocEstate) > 0) {
                            Id = RecordIndex;
                        }

                    } catch (final JSONException e) {
                        Log.e("TAG", "Json parsing error: " + e.getMessage());

                    }

                } else {
                    Log.e("TAG", "Couldn't get json from server.");

                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                //UI Thread work here
                try {

                    if (Integer.parseInt(Id) > 0) {
                        progressDialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(CompanyDetailsActivity.this);
                        builder.setTitle("Ready to proceed?")
                                .setMessage(Html.fromHtml("<font color='#2b419a'>This Phone is Allocated to </font><b>" +
                                        s_esName + " </b>"))
                                .setCancelable(false)
                                .setNegativeButton("Yes", (dialog, id) -> {
                                    Activate();
                                })
                                .setPositiveButton("No", (dialog, id) -> dialog.cancel());

                        final AlertDialog btnback = builder.create();
                        btnback.show();


                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getBaseContext(), Message, Toast.LENGTH_LONG).show();

                    }


                } catch (NumberFormatException e) {
                    progressDialog.dismiss();
                    if (restApiResponse.equals("-8080")) {
                        Toast.makeText(getBaseContext(), "Failed To Connect to Server", Toast.LENGTH_LONG).show();
                    }
                    return;

                }
            });
        });

    }

    public void Activate() {
        progressDialog = ProgressDialog.show(CompanyDetailsActivity.this,
                "Activating",
                "Please Wait.. ");
        executor.execute(() -> {
            //Background work here
            try {
                restApiResponse = "";

                CompanyID = Sco_prefix;

                DeviceID = s_terminal;

                PhoneNo = s_phone;


                restApiResponse = ActivateDevice(CompanyID, DeviceID, PhoneNo);
                JSONObject jsonObject = new JSONObject(restApiResponse);
                Id = jsonObject.getString("Id");
                Title = jsonObject.getString("Title");
                Message = jsonObject.getString("Message");
                Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);


            } catch (final JSONException e) {
                Log.e("TAG", "Json parsing error: " + e.getMessage());
                Message = "Json parsing error: " + e.getMessage();
                Id = "-1";

            }

            handler.post(() -> {
                //UI Thread work here
                try {

                    if (Integer.parseInt(Id) > 0) {
                        progressDialog.dismiss();

                        SharedPreferences.Editor edit = mSharedPrefs.edit();
                        edit.putString("serverTerminalID", Id);
                        edit.apply();
                        edit.putString("ERecordIndex", AllocEstate);
                        edit.apply();

                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        Cursor estates = db.query(true, Database.ESTATES_TABLE_NAME, null, null, null, null, null, null, null, null);
                        if (estates.getCount() == 0) {
                            String DefaultEstates = "INSERT INTO " + Database.ESTATES_TABLE_NAME + " ("
                                    + Database.ROW_ID + ", "
                                    + Database.ES_ID + ", "
                                    + Database.ES_NAME + ", "
                                    + Database.ES_COMPANY + ", "
                                    + Database.CloudID + ") Values ('0','0', 'Select ...','0','0')";
                            db.execSQL(DefaultEstates);
                        }
                        Cursor checkEstate = dbHelper.CheckEstate(s_esID);
                        //Check for duplicate FryPrefix
                        if (checkEstate.getCount() > 0) {

                        } else {
                            CRecordIndex = prefs.getString("CRecordIndex", null);
                            dbHelper.AddEstate(s_esID, s_esName, CRecordIndex, AllocEstate);
                        }


                        Toast.makeText(getBaseContext(), "Device Activation " + Message, Toast.LENGTH_LONG).show();
                        dActivate.dismiss();
                        finish();
                        mIntent = new Intent(getApplicationContext(), SyncUsersActivity.class);
                        startActivity(mIntent);

                        return;

                    } else {
                        progressDialog.dismiss();

                        Toast.makeText(getBaseContext(), Message, Toast.LENGTH_LONG).show();
                        return;
                    }


                } catch (NumberFormatException e) {
                    progressDialog.dismiss();
                    if (restApiResponse.equals("-8080")) {
                        Toast.makeText(getBaseContext(), "Failed To Connect to Server", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(getBaseContext(), Message, Toast.LENGTH_LONG).show();
                    return;

                }

            });
        });

    }

    public void LoadFactories() {
        progressDialog = ProgressDialog.show(CompanyDetailsActivity.this,
                "Loading Factories",
                "Please Wait.. ");
        executor.execute(() -> {
            //Background work here

            CRecordIndex = prefs.getString("CRecordIndex", null);
            //  Log.e(TAG, "Response from url: " + jsonStr);
            restApiResponse = new MasterApiRequest(getApplicationContext()).getFactories(CRecordIndex);
            response = prefs.getInt("factoriesresponse", 0);
            if (response == 200) {
                try {


                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor factories = db.query(true, Database.FACTORY_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (factories.getCount() == 0) {
                        String DefaultFactory = "INSERT INTO " + Database.FACTORY_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.FRY_PREFIX + ", "
                                + Database.FRY_TITLE + ", "
                                + Database.CloudID + ") Values ('0','0', 'Select ...','0')";
                        db.execSQL(DefaultFactory);
                    }

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);
                        FRecordIndex = obj.getString("RecordIndex");
                        FryPrefix = obj.getString("FryPrefix");
                        FryTitle = obj.getString("FryTitle");
                        FryCapacity = obj.getString("FryCapacity");
                        Log.i("FRecordIndex", FRecordIndex);
                        Log.i("FryPrefix", FryPrefix);
                        Cursor checkFactory = dbHelper.CheckFactory(FryPrefix);
                        //Check for duplicate FryPrefix
                        if (checkFactory.getCount() > 0) {

                        } else {
                            dbHelper.AddFactories(FryPrefix, FryTitle, FRecordIndex);
                        }


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }

            }

            handler.post(() -> {
                //UI Thread work here

                progressDialog.dismiss();
                FactoryList();
            });
        });

    }

    public void AllocatedFactoryDetails() {
        if (refresh) {
            progressDialog = ProgressDialog.show(CompanyDetailsActivity.this,
                    "Please Wait",
                    "Connecting... ");
        }
        executor.execute(() -> {
            //Background work here
            CRecordIndex = prefs.getString("CompanyIndex", null);
            AllocEstate = prefs.getString("AllocEstate", null);
            FRecordIndex = "0";

            restApiResponse = new MasterApiRequest(getApplicationContext()).getFactories(CRecordIndex);
            response = prefs.getInt("factoriesresponse", 0);
            if (response == 200) {

                try {


                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);

                        FRecordIndex = obj.getString("RecordIndex");
                        FryPrefix = obj.getString("FryPrefix");
                        FryTitle = obj.getString("FryTitle");
                        FryCapacity = obj.getString("FryCapacity");
                        FryCoName = obj.getString("FryCoName");
                        FryPostalAddress = obj.getString("FryPostalAddress");
                        poCode = obj.getString("poCode");
                        if (poCode == null || poCode == "null") {
                            poCode = "";
                        }
                        poName = obj.getString("poName");
                        if (poName == null || poName == "null") {
                            poName = "";
                        }
                        FryPostOffice = obj.getString("FryPostOffice");
                        FryTelephone = obj.getString("FryTelephone");


                        SharedPreferences.Editor edit2 = prefs.edit();
                        edit2.putString("FRecordIndex", FRecordIndex);
                        edit2.apply();

                        SharedPreferences.Editor edit = mSharedPrefs.edit();

                        edit.putString("company_name", FryCoName);
                        edit.apply();
                        edit.putString("company_letterbox", FryPostalAddress);
                        edit.apply();
                        edit.putString("company_postalcode", poCode);
                        edit.apply();
                        edit.putString("company_postalname", poName);
                        edit.apply();
                        edit.putString("company_posttel", FryTelephone);
                        edit.apply();


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }

            }

            handler.post(() -> {
                //UI Thread work here
                if (response == 200) {
                    if (refresh) {
                        if (Integer.parseInt(FRecordIndex) > 0) {

                            co_prefix.setText(mSharedPrefs.getString("company_prefix", ""));
                            co_name.setText(mSharedPrefs.getString("company_name", ""));
                            co_letterbox.setText(mSharedPrefs.getString("company_letterbox", ""));
                            co_postcode.setText(mSharedPrefs.getString("company_postalcode", ""));
                            co_postname.setText(mSharedPrefs.getString("company_postalname", ""));
                            co_postregion.setText(mSharedPrefs.getString("company_postregion", ""));
                            co_telephone.setText(mSharedPrefs.getString("company_posttel", ""));

                            progressDialog.dismiss();

                        } else {

                            progressDialog.dismiss();
                            Context context = getApplicationContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Details Not Loaded");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            finish();

                        }

                    }
                } else {

                    progressDialog.dismiss();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Details Not Loaded");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    finish();

                }
            });
        });
    }

    public void LoadTransporters() {
        progressDialog = ProgressDialog.show(CompanyDetailsActivity.this,
                "Loading Transporters",
                "Please Wait.. ");

        executor.execute(() -> {
            //Background work here

            CRecordIndex = prefs.getString("CRecordIndex", null);
            //  Log.e(TAG, "Response from url: " + jsonStr);
            restApiResponse = new MasterApiRequest(getApplicationContext()).getTransporters(CRecordIndex);
            response = prefs.getInt("transresponse", 0);
            if (response == 200) {
                try {


                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor routes = db.query(true, Database.TRANSPORTER_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (routes.getCount() == 0) {
                        String DefaultRoute = "INSERT INTO " + Database.TRANSPORTER_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.TPT_NAME + ") Values ('0', 'Select ...')";
                        db.execSQL(DefaultRoute);
                    }

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);
                        TRecordIndex = obj.getString("RecordIndex");
                        s_tptID = obj.getString("TptCode");
                        s_tptName = obj.getString("TptName");

                        Log.i("TRecordIndex", TRecordIndex);
                        Log.i("s_tptID", s_tptID);

                        Cursor checkRoute = dbHelper.CheckTransporterID(TRecordIndex);
                        //Check for duplicate shed
                        if (checkRoute.getCount() > 0) {
                            // Toast.makeText(getApplicationContext(), "Route already exists",Toast.LENGTH_SHORT).show();

                        } else {
                            dbHelper.AddTransporter(s_tptID, s_tptName, TRecordIndex);
                        }


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());
                    Log.e("Server Response", e.toString());
                    e.printStackTrace();

                }

            }
            handler.post(() -> {
                //UI Thread work here
                progressDialog.dismiss();
            });
        });
    }

}
