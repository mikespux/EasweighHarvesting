package com.plantation.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.helpers.StringWithTag;
import com.plantation.synctocloud.MasterApiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncUsersActivity extends AppCompatActivity {
    static SharedPreferences mSharedPrefs, prefs;
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btnImport;
    Button btnImportO;
    ListView lvUsers;
    Intent mIntent;
    String s_recordindex, s_etFullName, s_etNewUserId, s_etPassword, s_spUserLevel;

    int usercount;
    int count = 0;
    int overwrite = 0;
    ArcProgress arcProgress;
    String systembasedate;
    String _URL, _TOKEN, FRecordIndex;
    boolean refresh;
    String estateid = null, ERecordIndex = "";
    String estates;
    ArrayList<StringWithTag> estatedata = new ArrayList<StringWithTag>();
    ArrayAdapter<StringWithTag> estateadapter;
    Spinner spEstate;
    String DRecordIndex, s_dvID, s_dvName, s_dvEstate;

    String restApiResponse;
    int response;
    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    private int progressStatus = 0;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_syncusers);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.sync_users);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void initializer() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (mSharedPrefs.getString("internetAccessModes", null).equals("WF")) {
            _URL = mSharedPrefs.getString("portalURL", null) + ":"
                    + mSharedPrefs.getString("coPort", null) + "/" +
                    mSharedPrefs.getString("coApp", null);

        } else {
            _URL = mSharedPrefs.getString("mdportalURL", null) + ":"
                    + mSharedPrefs.getString("coPort", null) + "/" +
                    mSharedPrefs.getString("coApp", null);
        }

        _TOKEN = prefs.getString("token", null);

        refresh = mSharedPrefs.getBoolean("prefs_refresh", false);

        dbhelper = new DBHelper(getApplicationContext());
        btnImportO = findViewById(R.id.btnImportO);
        if (refresh) {
            btnImportO.setVisibility(View.VISIBLE);
            btnImportO.setOnClickListener(v -> {
                overwrite = 1;
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                db.delete(Database.OPERATORSMASTER_TABLE_NAME, null, null);
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.OPERATORSMASTER_TABLE_NAME + "'");
                showEstates();

            });
        } else {
            btnImportO.setVisibility(View.GONE);
        }
        btnImport = findViewById(R.id.btnImport);
        btnImport.setOnClickListener(v -> {

            //showEstates();
            ERecordIndex = prefs.getString("ERecordIndex", null);
            LoadDivisions();
            LoadUsers();

        });


    }

    private void EstateList() {
        estatedata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select esID,esName from estates ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    estates = c.getString(c.getColumnIndex("esName"));
                    if (c.getString(c.getColumnIndex("esID")) == null) {
                        estateid = "0";
                    } else {
                        estateid = c.getString(c.getColumnIndex("esID"));
                    }
                    estatedata.add(new StringWithTag(estates, estateid));
                } while (c.moveToNext());
            }
        }


        estateadapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, estatedata);
        estateadapter.setDropDownViewResource(R.layout.spinner_item);
        spEstate.setAdapter(estateadapter);
        spEstate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select esID,CloudID from estates where esID= '" + s.tag + "'", null);
                if (c != null) {
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        ERecordIndex = c.getString(c.getColumnIndex("CloudID"));
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("ERecordIndex", ERecordIndex);
                        edit.apply();

                    }
                }
                c.close();
                db.close();
                dbhelper.close();

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

    public void showEstates() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_estate_list, null);
        dialogBuilder.setView(dialogView);

        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("Estates");
        spEstate = dialogView.findViewById(R.id.spinnerEstate);
        EstateList();

        dialogBuilder.setPositiveButton("SYNC", (dialog, whichButton) -> {


        });

        final AlertDialog b = dialogBuilder.create();
        b.show();
        b.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (spEstate.getSelectedItem().toString().equals("Select ...")) {
                Toast.makeText(getApplicationContext(), "Please Select Estate", Toast.LENGTH_LONG).show();
                return;
            }
            b.dismiss();
            ERecordIndex = prefs.getString("ERecordIndex", null);
            LoadDivisions();
            LoadUsers();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showUsersDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_listclerks, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        // dialogBuilder.setTitle("Add Users");
        toolbar = dialogView.findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ADDED USERS");


        try {
            lvUsers = dialogView.findViewById(R.id.lvUsers);
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.OPERATORSMASTER_TABLE_NAME, null, null, null, null, null, null, null, null);

            String[] from = {Database.ROW_ID, Database.CLERKNAME, Database.USERIDENTIFIER};
            int[] to = {R.id.txtAccountId, R.id.txtUserName, R.id.txtUserType};

            @SuppressWarnings("deprecation")
            SimpleCursorAdapter ca = new SimpleCursorAdapter(this, R.layout.userlist, accounts, from, to);


            lvUsers.setAdapter(ca);
            // dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }


        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                if (overwrite == 1) {
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("user");
                    edit.remove("pass");
                    edit.apply();
                    finish();
                    Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(login);
                    return;
                }
                if (refresh == true) {
                    finish();
                    return;
                }
                systembasedate = prefs.getString("basedate", "");
                if (systembasedate.equals("")) {
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("user");
                    edit.remove("pass");
                    edit.apply();
                    SharedPreferences.Editor edit1 = mSharedPrefs.edit();
                    edit1.remove("token");
                    edit1.remove("expires_in");
                    edit1.remove("expires");
                    edit1.apply();
                    finish();
                    Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(login);

                    return;
                }
            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed

        if (count > 0) {
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("You cannot close window while syncing users !!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }
        systembasedate = prefs.getString("basedate", "");
        if (mSharedPrefs.getString("scaleVersion", "").equals("")) {

            finish();
            mIntent = new Intent(getApplicationContext(), CompanyDetailsActivity.class);
            startActivity(mIntent);

            return;
        }

        if (refresh == true) {
            finish();
        }


    }

    public void LoadDivisions() {
        progressDialog = ProgressDialog.show(SyncUsersActivity.this,
                "Loading Divisions",
                "Please Wait.. ");


        executor.execute(() -> {

            //Background work here
            restApiResponse = new MasterApiRequest(getApplicationContext()).getDivisions(ERecordIndex);
            response = prefs.getInt("getdivresponse", 0);
            if (response == 200) {
                try {


                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                    Cursor divisions = db.query(true, Database.DIVISIONS_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (divisions.getCount() == 0) {
                        String DefaultDivisions = "INSERT INTO " + Database.DIVISIONS_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.DV_ID + ", "
                                + Database.DV_NAME + ", "
                                + Database.DV_ESTATE + ", "
                                + Database.CloudID + ") Values ('0','0', 'Select ...','0','0')";
                        db.execSQL(DefaultDivisions);
                    }

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);

                        DRecordIndex = obj.getString("Recordindex");
                        s_dvID = obj.getString("edCode");
                        s_dvName = obj.getString("edName");
                        s_dvEstate = obj.getString("edEstate");

                        Log.i("s_dvID", s_dvID);
                        Cursor checkDivision = dbhelper.CheckDivision(s_dvID);
                        //Check for duplicate
                        if (checkDivision.getCount() > 0) {

                        } else {
                            dbhelper.AddDivision(s_dvID, s_dvName, s_dvEstate, DRecordIndex);
                        }


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }
            }
            handler.post(() -> {
                //UI Thread work here
                progressDialog.dismiss();
            });
        });

    }

    public void LoadUsers() {
        arcProgress = findViewById(R.id.arc_progress);
        arcProgress.setProgress(0);

        textView = findViewById(R.id.textView1);
        btnImport.setVisibility(View.GONE);
        btnImportO.setVisibility(View.GONE);

        executor.execute(() -> {

            //Background work here
            restApiResponse = new MasterApiRequest(getApplicationContext()).getClerks(ERecordIndex, "0");
            response = prefs.getInt("getclerksresponse", 0);
            if (response == 200) {
                s_recordindex = "0";
                try {
                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    SQLiteDatabase db = dbhelper.getReadableDatabase();
                    refresh = mSharedPrefs.getBoolean("prefs_refresh", false);
                    if (refresh) {

                        // Do something with object.
                        for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                            JSONObject obj = arrayKnownAs.getJSONObject(i);
                            s_recordindex = obj.getString("Recordindex");
                            s_etFullName = obj.getString("KitUserName");
                            s_etNewUserId = obj.getString("KitUserNumber");
                            s_spUserLevel = obj.getString("AccessLevel");
                            s_etPassword = "1234";

                            Log.i("s_etNewUserId", s_etNewUserId);

                            Cursor checkusername = dbhelper.fetchUsername(s_etNewUserId);
                            //Check for duplicate id number
                            if (checkusername.getCount() > 0) {
                                // Toast.makeText(getApplicationContext(), "Username already exists",Toast.LENGTH_SHORT).show();

                            } else {


                                dbhelper.AddUsers(s_etFullName, s_etNewUserId, s_etPassword, s_spUserLevel);
                            }
                            count += 1;
                            progressStatus++;
                            runOnUiThread(() -> {
                                arcProgress.setProgress(progressStatus);
                                arcProgress.setMax(count);
                                arcProgress.setBottomText("IMPORTING ...");
                                textView.setText(progressStatus + "/" + count + " Records");
                            });
                        }

                    } else {
                        db.delete(Database.OPERATORSMASTER_TABLE_NAME, null, null);
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.OPERATORSMASTER_TABLE_NAME + "'");

                        // Do something with object.
                        for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                            JSONObject obj = arrayKnownAs.getJSONObject(i);
                            s_recordindex = obj.getString("Recordindex");
                            s_etFullName = obj.getString("KitUserName");
                            s_etNewUserId = obj.getString("KitUserNumber");
                            s_spUserLevel = obj.getString("AccessLevel");
                            s_etPassword = "1234";

                            Log.i("s_etNewUserId", s_etNewUserId);

                            Cursor checkusername = dbhelper.fetchUsername(s_etNewUserId);
                            //Check for duplicate id number
                            if (checkusername.getCount() > 0) {
                                // Toast.makeText(getApplicationContext(), "Username already exists",Toast.LENGTH_SHORT).show();

                            } else {


                                dbhelper.AddUsers(s_etFullName, s_etNewUserId, s_etPassword, s_spUserLevel);
                            }
                            count += 1;
                            progressStatus++;
                            runOnUiThread(() -> {
                                arcProgress.setProgress(progressStatus);
                                arcProgress.setMax(count);
                                arcProgress.setBottomText("IMPORTING ...");
                                textView.setText(progressStatus + "/" + count + " Records");
                            });
                        }


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }

            }

            handler.post(() -> {
                //UI Thread work here
                response = prefs.getInt("getclerksresponse", 0);
                if (response == 200) {
                    if (Integer.parseInt(s_recordindex) > 0) {

                        SQLiteDatabase db = dbhelper.getReadableDatabase();
                        Cursor users = db.query(true, Database.OPERATORSMASTER_TABLE_NAME, null, Database.ACCESSLEVEL + "='1'", null, null, null, null, null, null);
                        usercount = users.getCount();

                        if (usercount == 0) {
                            s_etFullName = "OCTAGON";
                            s_etNewUserId = "ODS";
                            s_etPassword = "4321";
                            s_spUserLevel = "1";

                            dbhelper.AddUsers(s_etFullName, s_etNewUserId, s_etPassword, s_spUserLevel);

                        }

                        showUsersDialog();
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText(count + " users saved successfully");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();

                        return;
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(SyncUsersActivity.this, "Could not Load List", Toast.LENGTH_LONG).show();
                }
            });
        });

    }


}