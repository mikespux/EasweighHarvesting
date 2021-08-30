package com.plantation.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.helpers.DirectoryChooserDialog;
import com.plantation.helpers.StringWithTag;
import com.plantation.synctocloud.MasterApiRequest;
import com.plantation.synctocloud.RestApiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import au.com.bytecode.opencsv.CSVReader;

public class SyncMastersActivity extends AppCompatActivity {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    static SharedPreferences mSharedPrefs, prefs;
    public Toolbar toolbar;
    DBHelper dbhelper;
    SQLiteDatabase db;
    Button btnCloud, btnFile;
    ListView lvUsers;
    Intent mIntent;
    LinearLayout ltDivision;
    String systembasedate, vmodes;
    String _URL, _TOKEN;
    int count = 0;
    ArcProgress arcProgress;

    String divisionid = null, CRecordIndex = "", ERecordIndex = "", DRecordIndex = "";
    String divisions;
    ArrayList<StringWithTag> divisiondata = new ArrayList<>();
    ArrayAdapter<StringWithTag> divisionadapter;
    Spinner spDivision;
    String Id, Message;
    String Server = "";

    LinearLayout LtDvClear;
    Button btnDivision, btnSingle, btnCDivision;

    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    String restApiResponse;
    int response;
    Spinner spinnerProduce;
    String PRRecordIndex, s_etDgProduceCode, s_etDgProduceTitle;
    String produce, produceid;
    ArrayList<String> producedata = new ArrayList<String>();
    ArrayAdapter<String> produceadapter;

    String PGRecordIndex, pgdRef, pgdName, MpCode;
    String PVRecordIndex, vtrRef, vrtName;


    String EERecordIndex, s_emID, s_emName, s_emPickerNo, s_emTeam, s_emIDNo, s_emCardID;
    String FDRecordIndex, s_fdID, s_fdDiv;
    String BKRecordIndex, s_bkID, s_bkField;
    String TMRecordIndex, s_tmCode, s_tmName, s_dgNumber, s_dgDivision, s_edEstate;
    String TKRecordIndex, s_tkID, s_tkName, s_tkType, s_tkOT, s_tkMT;
    int ctActivityType = 0;
    boolean ctAllowMultiple, ctAllowOT;
    String MRecordIndex, s_MID, s_MName;
    String s_esID, s_esName, s_esCompany;
    String s_dvID, s_dvName, s_dvEstate;
    String s_etFullName, s_etNewUserId, s_etPassword, s_spUserLevel;
    String s_tptID, s_tptName, s_trecordindex = "";
    String s_fryprefix, s_fryname, s_recordindex = "";
    String s_CPID, s_CPName;
    String Sco_prefix, Sco_name, Sco_letterbox, Sco_postcode, Sco_postname, Sco_postregion, Sco_telephone, server_url, server_port, server_application;
    String COMPANY_MASTER = "0";
    String ESTATE_MASTER = "1";
    String DIVISION_MASTER = "2";
    String FIELD_MASTER = "3";
    String BLOCK_MASTER = "4";
    String FACTORY_MASTER = "5";
    String TASK_MASTER = "9";
    String EMPLOYEE_MASTER = "10";
    String USER_MASTER = "11";
    String MACHINE_MASTER = "12";
    String TRANSPORTER_MASTER = "13";
    String CAPITALP_MASTER = "14";
    String path;
    boolean manual_setup = false;
    private int progressStatus = 0;
    private TextView textView;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_syncmasters);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.sync_masters);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void initializer() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();
        ERecordIndex = prefs.getString("ERecordIndex", null);
        ltDivision = findViewById(R.id.ltDivision);
        spDivision = findViewById(R.id.spDivision);
        DivisionList();

        arcProgress = findViewById(R.id.arc_progress);
        textView = findViewById(R.id.textView1);

        btnCloud = findViewById(R.id.btnCloud);
        btnFile = findViewById(R.id.btnFile);

        manual_setup = prefs.getBoolean("manual_setup", false);
        if (manual_setup) {
            ltDivision.setVisibility(View.GONE);
            btnCloud.setVisibility(View.GONE);
        } else {
            btnFile.setVisibility(View.GONE);
            if (mSharedPrefs.getString("internetAccessModes", null).equals("WF")) {
                _URL = mSharedPrefs.getString("portalURL", null) + ":"
                        + mSharedPrefs.getString("coPort", null) + "/" +
                        mSharedPrefs.getString("coApp", null);

            } else {
                _URL = mSharedPrefs.getString("mdportalURL", null) + ":"
                        + mSharedPrefs.getString("coPort", null) + "/" +
                        mSharedPrefs.getString("coApp", null);
            }

            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = new RestApiRequest(getApplicationContext()).getToken();
            } else {
                long token_hours = new RestApiRequest(getApplicationContext()).token_hours();
                if (token_hours >= 23) {
                    _TOKEN = new RestApiRequest(getApplicationContext()).getToken();

                }
            }
            Cursor division = db.query(true, Database.DIVISIONS_TABLE_NAME, null, null, null, null, null, null, null, null);
            if (division.getCount() <= 1) {
                LoadDivisions();
            }


            Cursor produce = db.query(true, Database.PRODUCE_TABLE_NAME, null, null, null, null, null, null, null, null);
            if (produce.getCount() <= 1) {
                LoadCrops();
            }

        }

        btnCloud.setOnClickListener(v -> {
            if (spDivision.getSelectedItem().toString().equals("Select ...")) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Select Division");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                // Toast.makeText(getApplicationContext(), "Please Select Division", Toast.LENGTH_LONG).show();
                return;
            }
            ERecordIndex = prefs.getString("ERecordIndex", null);
            btnCloud.setVisibility(View.GONE);
            LoadBlocks();
        });
        btnFile.setOnClickListener(v -> {
            btnFile.setEnabled(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SyncMastersActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    btnFile.setEnabled(true);
                } else {
                    btnFile.setEnabled(true);
                    openfolder();

                }
            } else {
                openfolder();
                btnFile.setEnabled(true);
            }
        });


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


        finish();
        mIntent = new Intent(SyncMastersActivity.this, MainActivity.class);
        startActivity(mIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_master, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {

            case R.id.action_add:
                manual_setup = prefs.getBoolean("manual_setup", false);
                if (manual_setup) {
                    ltDivision.setVisibility(View.VISIBLE);
                    btnCloud.setVisibility(View.VISIBLE);
                    btnFile.setVisibility(View.GONE);
                    ERecordIndex = prefs.getString("ERecordIndex", null);
                    Cursor division = db.query(true, Database.DIVISIONS_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (division.getCount() <= 1) {
                        LoadDivisions();
                    }
                } else {
                    mIntent = new Intent(SyncMastersActivity.this, ImportMasterActivity.class);
                    startActivity(mIntent);
                }
                return true;
            case R.id.action_clear:

                showClearDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showClearDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SyncMastersActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_clear, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("CLEAR");
        LtDvClear = dialogView.findViewById(R.id.LtDvClear);
        spDivision = dialogView.findViewById(R.id.spDivision);
        LtDvClear.setVisibility(View.GONE);
        DivisionList();
        btnDivision = dialogView.findViewById(R.id.btnDivision);
        btnCDivision = dialogView.findViewById(R.id.btnCDivision);
        btnCDivision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spDivision.getSelectedItem().toString().equals("Select ...")) {
                    Toast.makeText(dialogView.getContext(), "Please select Division", Toast.LENGTH_LONG).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(SyncMastersActivity.this);
                builder.setMessage(Html.fromHtml("<font color='#D50000'>Are you sure your want to clear data?</font>"))
                        .setTitle("Clear Data")
                        .setCancelable(true)
                        .setNegativeButton("Yes",
                                (dialog, id) -> {
                                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                                    db.delete(Database.EM_TABLE_NAME, null, null);
                                    db.delete(Database.FIELD_TABLE_NAME, Database.FD_DIVISION + "='" + divisionid + "'", null);
                                    db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.EM_TABLE_NAME + "'");
                                    db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FIELD_TABLE_NAME + "'");
                                    finish();
                                    mIntent = new Intent(SyncMastersActivity.this, SyncMastersActivity.class);
                                    startActivity(mIntent);
                                }
                        )
                        .setPositiveButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                }
                        );
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        btnSingle = dialogView.findViewById(R.id.btnSingle);
        btnSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SyncMastersActivity.this);
                builder.setMessage(Html.fromHtml("<font color='#D50000'>Are you sure your want to clear all data?</font>"))
                        .setTitle("Clear Data")
                        .setCancelable(true)
                        .setNegativeButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        SQLiteDatabase db = dbhelper.getWritableDatabase();
                                        db.delete(Database.DIVISIONS_TABLE_NAME, null, null);
                                        db.delete(Database.FIELD_TABLE_NAME, null, null);
                                        db.delete(Database.EM_TABLE_NAME, null, null);
                                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.DIVISIONS_TABLE_NAME + "'");
                                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FIELD_TABLE_NAME + "'");
                                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.EM_TABLE_NAME + "'");
                                        finish();
                                        mIntent = new Intent(SyncMastersActivity.this, SyncMastersActivity.class);
                                        startActivity(mIntent);

                                    }
                                }
                        )
                        .setPositiveButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                }
                        );
                AlertDialog alert = builder.create();
                alert.show();
            }
        });


        btnDivision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LtDvClear.setVisibility(View.VISIBLE);
                btnSingle.setVisibility(View.GONE);
                btnDivision.setVisibility(View.GONE);

            }
        });


        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();


            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void DivisionList() {
        divisiondata.clear();
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select dvID,dvName from divisions", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    divisions = c.getString(c.getColumnIndex("dvName"));
                    if (c.getString(c.getColumnIndex("dvID")) == null) {
                        divisionid = "0";
                    } else {
                        divisionid = c.getString(c.getColumnIndex("dvID"));
                    }
                    divisiondata.add(new StringWithTag(divisions, divisionid));
                } while (c.moveToNext());
            }
        }

        divisionadapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, divisiondata);
        divisionadapter.setDropDownViewResource(R.layout.spinner_item);
        spDivision.setAdapter(divisionadapter);
        spDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select dvID,CloudID from divisions where dvID='" + s.tag + "'", null);
                if (c != null) {
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        divisionid = c.getString(c.getColumnIndex("dvID"));
                        DRecordIndex = c.getString(c.getColumnIndex("CloudID"));
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

    public void LoadDivisions() {
        progressDialog = ProgressDialog.show(SyncMastersActivity.this,
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
                DivisionList();
            });
        });

    }

    public void showEmployeesDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_listclerks, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        toolbar = dialogView.findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ADDED EMPLOYEES");


        try {
            lvUsers = dialogView.findViewById(R.id.lvUsers);
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, null, null, null, null, null, null, null);

            String[] from = {Database.ROW_ID, Database.EM_ID, Database.EM_NAME};
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
                vmodes = mSharedPrefs.getString("vModes", "");
                if (vmodes.equals("")) {
                    Context context = getApplicationContext();
                    View customToastroot1 = inflater.inflate(R.layout.blue_toast, null);
                    TextView text1 = customToastroot1.findViewById(R.id.toast);
                    text1.setText("Prepare Settings ...");
                    Toast customtoast1 = new Toast(context);
                    customtoast1.setView(customToastroot1);
                    customtoast1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast1.setDuration(Toast.LENGTH_LONG);
                    customtoast1.show();
                    finish();
                    Intent login = new Intent(getApplicationContext(), SetupActivity.class);
                    startActivity(login);
                    return;
                }
                dialog.dismiss();
            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showProduce() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_produce_list, null);
        dialogBuilder.setView(dialogView);

        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("Produce");
        spinnerProduce = dialogView.findViewById(R.id.spinnerProduce);
        Produce();

        dialogBuilder.setPositiveButton("SAVE", (dialog, whichButton) -> {


        });

        final AlertDialog b = dialogBuilder.create();
        b.show();
        b.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (spinnerProduce.getSelectedItem().toString().equals("Select ...")) {
                Toast.makeText(getApplicationContext(), "Please Select Produce", Toast.LENGTH_LONG).show();
                return;
            }
            b.dismiss();
            LoadVarieties();
        });
    }

    private void Produce() {
        producedata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select MpCode,MpDescription from Produce", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    produce = c.getString(c.getColumnIndex("MpDescription"));
                    producedata.add(produce);

                } while (c.moveToNext());
            }
        }


        produceadapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, producedata);
        produceadapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerProduce.setAdapter(produceadapter);
        spinnerProduce.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String produceName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select CloudID from Produce where MpDescription= '" + produceName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    PRRecordIndex = c.getString(c.getColumnIndex("CloudID"));

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("produceid", produceid);
                    edit.commit();
                }
                //   c.close();


                TextView tv = (TextView) view;
                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }


                // db.close();
                //dbhelper.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //  tv.setHint("Select Country");
            }
        });


    }

    public void LoadCrops() {
        progressDialog = ProgressDialog.show(SyncMastersActivity.this,
                "Loading Crops",
                "Please Wait.. ");

        executor.execute(() -> {

            //Background work here
            CRecordIndex = prefs.getString("CRecordIndex", null);

            restApiResponse = new MasterApiRequest(getApplicationContext()).getCrops(CRecordIndex);
            response = prefs.getInt("getcropsresponse", 0);
            if (response == 200) {
                //  Log.e(TAG, "Response from url: " + jsonStr);
                try {
                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                    Cursor routes = db.query(true, Database.PRODUCE_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (routes.getCount() == 0) {
                        String DefaultProduce = "INSERT INTO " + Database.PRODUCE_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.MP_DESCRIPTION + ") Values ('0', 'Select ...')";
                        db.execSQL(DefaultProduce);
                    }

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);
                        PRRecordIndex = obj.getString("RecordIndex");
                        s_etDgProduceCode = obj.getString("MpCode");
                        s_etDgProduceTitle = obj.getString("MpDescription");
                        Log.i("PRRecordIndex", PRRecordIndex);
                        Cursor checkProduce = dbhelper.CheckProduce(s_etDgProduceCode);
                        //Check for duplicate shed
                        if (checkProduce.getCount() > 0) {
                            // Toast.makeText(getApplicationContext(), "Route already exists",Toast.LENGTH_SHORT).show();
                        } else {
                            dbhelper.AddProduce(s_etDgProduceCode, s_etDgProduceTitle, PRRecordIndex);
                        }
                    }
                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());
                    Server = "-8080";
                    Log.e("Server Response", e.toString());
                    e.printStackTrace();
                }

            }

            handler.post(() -> {
                //UI Thread work here
                response = prefs.getInt("getcropsresponse", 0);
                if (response == 200) {
                    progressDialog.dismiss();
                    showProduce();
                } else {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Server Not Found\nFailed to Connect to " + _URL);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                }
            });
        });
    }

    public void LoadVarieties() {
        progressDialog = ProgressDialog.show(SyncMastersActivity.this,
                "Loading Varieties",
                "Please Wait.. ");

        executor.execute(() -> {

            //Background work here

            restApiResponse = new MasterApiRequest(getApplicationContext()).getVarieties(PRRecordIndex);
            response = prefs.getInt("varietyresponse", 0);
            if (response == 200) {
                //  Log.e(TAG, "Response from url: " + jsonStr);
                try {


                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                    Cursor routes = db.query(true, Database.PRODUCEVARIETIES_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (routes.getCount() == 0) {
                        String DefaultVariety = "INSERT INTO " + Database.PRODUCEVARIETIES_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.VRT_NAME + ") Values ('0', 'Select ...')";
                        db.execSQL(DefaultVariety);
                    }

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);
                        PVRecordIndex = obj.getString("RecordIndex");
                        vtrRef = obj.getString("vtrRef");
                        vrtName = obj.getString("vrtName");
                        MpCode = obj.getString("MpCode");
                        Log.i("PVRecordIndex", PVRecordIndex);

                        Cursor checkVariety = dbhelper.CheckVariety(vtrRef);
                        //Check for duplicate shed
                        if (checkVariety.getCount() > 0) {
                            // Toast.makeText(getApplicationContext(), "Route already exists",Toast.LENGTH_SHORT).show();

                        } else {
                            dbhelper.AddVariety(vtrRef, vrtName, MpCode, PVRecordIndex);
                        }


                    }

                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());
                    Server = "-8080";
                    Log.e("Server Response", e.toString());
                    e.printStackTrace();
                }

            }

            handler.post(() -> {
                //UI Thread work here
                response = prefs.getInt("varietyresponse", 0);
                if (response == 200) {
                    progressDialog.dismiss();
                    LoadGrades();
                } else {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Server Not Found\nFailed to Connect to " + _URL);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                }
            });
        });
    }

    public void LoadGrades() {
        progressDialog = ProgressDialog.show(SyncMastersActivity.this,
                "Loading Grades",
                "Please Wait.. ");

        executor.execute(() -> {

            //Background work here

            restApiResponse = new MasterApiRequest(getApplicationContext()).getGrades(PRRecordIndex);
            response = prefs.getInt("gradesresponse", 0);
            if (response == 200) {
                //  Log.e(TAG, "Response from url: " + jsonStr);
                try {
                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                    Cursor routes = db.query(true, Database.PRODUCEGRADES_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (routes.getCount() == 0) {
                        String DefaultGrade = "INSERT INTO " + Database.PRODUCEGRADES_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.PG_DNAME + ") Values ('0', 'Select ...')";
                        db.execSQL(DefaultGrade);
                    }

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);
                        PGRecordIndex = obj.getString("RecordIndex");
                        pgdRef = obj.getString("pgdRef");
                        pgdName = obj.getString("pgdName");
                        MpCode = obj.getString("MpCode");

                        Log.i("PGRecordIndex", PGRecordIndex);

                        Cursor checkGrade = dbhelper.CheckGrade(pgdRef);
                        //Check for duplicate shed
                        if (checkGrade.getCount() > 0) {
                            // Toast.makeText(getApplicationContext(), "already exists",Toast.LENGTH_SHORT).show();
                        } else {
                            dbhelper.AddGrade(pgdRef, pgdName, MpCode, PGRecordIndex);
                        }

                    }
                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());
                    Server = "-8080";
                    Log.e("Server Response", e.toString());
                    e.printStackTrace();
                }

            }

            handler.post(() -> {
                //UI Thread work here
                response = prefs.getInt("gradesresponse", 0);
                if (response == 200) {
                    progressDialog.dismiss();
                } else {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Server Not Found\nFailed to Connect to " + _URL);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                }
            });
        });
    }


    public void LoadBlocks() {

        arcProgress.setProgress(0);

        executor.execute(() -> {

            //Background work here


            restApiResponse = new MasterApiRequest(getApplicationContext()).getBlocks(ERecordIndex, DRecordIndex);
            response = prefs.getInt("blocksresponse", 0);
            if (response == 200) {
                //  Log.e(TAG, "Response from url: " + jsonStr);
                try {


                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                    Cursor blocks = db.query(true, Database.BLOCK_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (blocks.getCount() == 0) {
                        String DefaultBlocks = "INSERT INTO " + Database.BLOCK_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.BK_ID + ", "
                                + Database.BK_FIELD + ", "
                                + Database.CloudID + ") Values ('0','0', 'Select ...','0')";
                        db.execSQL(DefaultBlocks);
                    }

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    count = arrayKnownAs.length();

                    // Do something with object.
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);

                        BKRecordIndex = obj.getString("Recordindex");
                        s_bkID = obj.getString("BlockNumber");
                        s_bkField = obj.getString("FieldNumber");

                        progressStatus++;
                        runOnUiThread(() -> {
                            arcProgress.setProgress(progressStatus);
                            arcProgress.setMax(count);
                            arcProgress.setBottomText("Saving Blocks ...");
                            textView.setText(progressStatus + "/" + count + " Records");
                        });

                        Log.i("s_bkID", s_bkID);
                        Cursor checkBlock = dbhelper.CheckBlock(s_bkID);
                        //Check for duplicate
                        if (checkBlock.getCount() > 0) {

                        } else {
                            dbhelper.AddBlock(s_bkID, s_bkField, BKRecordIndex);
                        }


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }
            }

            handler.post(() -> {
                //UI Thread work here
                response = prefs.getInt("blocksresponse", 0);
                if (response == 200) {
                    //progressDialog.dismiss();
                    LoadFields();
                } else {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Server Not Found\nFailed to Connect to " + _URL);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    btnCloud.setVisibility(View.VISIBLE);
                }
            });
        });

    }

    public void LoadFields() {

        arcProgress.setProgress(0);
        count = 0;
        progressStatus = 0;
        executor.execute(() -> {

            //Background work here

            restApiResponse = new MasterApiRequest(getApplicationContext()).getFields(ERecordIndex, DRecordIndex);
            response = prefs.getInt("fieldsresponse", 0);
            if (response == 200) {
                //  Log.e(TAG, "Response from url: " + jsonStr);
                try {


                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                    Cursor fields = db.query(true, Database.FIELD_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (fields.getCount() == 0) {
                        String DefaultFields = "INSERT INTO " + Database.FIELD_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.FD_ID + ", "
                                + Database.FD_DIVISION + ", "
                                + Database.CloudID + ") Values ('0','Select ...', '0','0')";
                        db.execSQL(DefaultFields);
                    }

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    count = arrayKnownAs.length();
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);

                        FDRecordIndex = obj.getString("Recordindex");
                        s_fdID = obj.getString("FieldNumber");
                        s_fdDiv = obj.getString("edCode");
                        progressStatus++;
                        runOnUiThread(() -> {
                            arcProgress.setProgress(progressStatus);
                            arcProgress.setMax(count);
                            arcProgress.setBottomText("Saving Fields ...");
                            textView.setText(progressStatus + "/" + count + " Records");
                        });
                        Log.i("s_dvID", s_fdID);
                        Cursor checkField = dbhelper.CheckField(s_fdID);
                        //Check for duplicate
                        if (checkField.getCount() > 0) {

                        } else {
                            dbhelper.AddField(s_fdID, s_fdDiv, FDRecordIndex);
                        }


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }
            }

            handler.post(() -> {
                //UI Thread work here
                response = prefs.getInt("fieldsresponse", 0);
                if (response == 200) {
                    //progressDialog.dismiss();
                    LoadTeams();
                } else {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Server Not Found\nFailed to Connect to " + _URL);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    btnCloud.setVisibility(View.VISIBLE);
                }
            });
        });

    }

    public void LoadTeams() {

        arcProgress.setProgress(0);
        count = 0;
        progressStatus = 0;

        executor.execute(() -> {

            //Background work here

            restApiResponse = new MasterApiRequest(getApplicationContext()).getTeams(DRecordIndex);
            response = prefs.getInt("teamsresponse", 0);
            if (response == 200) {
                //  Log.e(TAG, "Response from url: " + jsonStr);
                try {


                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                    Cursor teams = db.query(true, Database.TEAMS_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (teams.getCount() == 0) {
                        String DefaultTeams = "INSERT INTO " + Database.TEAMS_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.TM_CODE + ", "
                                + Database.TM_NAME + ", "
                                + Database.DG_NUMBER + ", "
                                + Database.DG_DIVISION + ", "
                                + Database.ED_ESTATE + ", "
                                + Database.CloudID + ") Values ('0','0', '0','Select ...','0','0','0')";
                        db.execSQL(DefaultTeams);
                    }

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    count = arrayKnownAs.length();
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);

                        TMRecordIndex = obj.getString("Recordindex");
                        s_tmCode = obj.getString("edCode");
                        s_tmName = obj.getString("edName");
                        s_dgNumber = obj.getString("DGnumber");
                        s_dgDivision = obj.getString("DGdivision");
                        s_edEstate = obj.getString("edEstate");
                        progressStatus++;
                        runOnUiThread(() -> {
                            arcProgress.setProgress(progressStatus);
                            arcProgress.setMax(count);
                            arcProgress.setBottomText("Saving Teams ...");
                            textView.setText(progressStatus + "/" + count + " Records");
                        });
                        Log.i("s_tmCode", s_tmCode);
                        Cursor checkTeam = dbhelper.CheckTeam(s_dgNumber);
                        //Check for duplicate
                        if (checkTeam.getCount() > 0) {

                        } else {
                            dbhelper.AddTeam(s_tmCode, s_tmName, s_dgNumber, s_dgDivision, s_edEstate, TMRecordIndex);
                        }


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }
            }

            handler.post(() -> {
                //UI Thread work here
                response = prefs.getInt("teamsresponse", 0);
                if (response == 200) {
                    //progressDialog.dismiss();
                    LoadPluckingCodes();
                } else {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Server Not Found\nFailed to Connect to " + _URL);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    btnCloud.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    public void LoadPluckingCodes() {
        arcProgress.setProgress(0);
        count = 0;
        progressStatus = 0;
        executor.execute(() -> {

            //Background work here

            restApiResponse = new MasterApiRequest(getApplicationContext()).getPluckingCodes();
            response = prefs.getInt("jobcoderesponse", 0);
            if (response == 200) {

                //  Log.e(TAG, "Response from url: " + jsonStr);
                try {

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    count = arrayKnownAs.length();
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);

                        TKRecordIndex = obj.getString("Recordindex");
                        s_tkID = obj.getString("ctcode");
                        s_tkName = obj.getString("ctTitle");

                        ctActivityType = obj.getInt("ctActivityType");
                        switch (ctActivityType) {
                            case 1:
                                s_tkType = "4";
                                break;
                            case 2:
                            case 4:
                            case 6:
                            case 7:
                                s_tkType = "1";
                                break;
                            case 3:
                                s_tkType = "2";
                                break;
                            case 5:
                                s_tkType = "3";
                                break;
                        }
                        s_tkOT = obj.getString("ctAllowOT");
                        ctAllowMultiple = obj.getBoolean("ctAllowMultiple");
                        if (ctAllowMultiple) {
                            s_tkMT = "1";
                        } else {
                            s_tkMT = "0";
                        }
                        ctAllowOT = obj.getBoolean("ctAllowOT");
                        if (ctAllowOT) {
                            s_tkOT = "1";
                        } else {
                            s_tkOT = "0";
                        }
                        progressStatus++;
                        runOnUiThread(() -> {
                            arcProgress.setProgress(progressStatus);
                            arcProgress.setMax(count);
                            arcProgress.setBottomText("Saving Job Codes ...");
                            textView.setText(progressStatus + "/" + count + " Records");
                        });
                        Log.i("s_tkID", s_tkID);
                        Cursor checktask = dbhelper.CheckTask(s_tkID);
                        //Check for duplicate
                        if (checktask.getCount() > 0) {
                        } else {
                            dbhelper.AddTask(s_tkID, s_tkName, s_tkType, s_tkOT, s_tkMT, TKRecordIndex);
                        }

                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }
            }

            handler.post(() -> {
                //UI Thread work here
                response = prefs.getInt("jobcoderesponse", 0);
                if (response == 200) {
                    //progressDialog.dismiss();
                    LoadMachines();
                } else {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Server Not Found\nFailed to Connect to " + _URL);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    btnCloud.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    public void LoadMachines() {
        arcProgress.setProgress(0);
        count = 0;
        progressStatus = 0;

        executor.execute(() -> {

            //Background work here
            restApiResponse = new MasterApiRequest(getApplicationContext()).getMachines(ERecordIndex, "0");
            response = prefs.getInt("getmachineresponse", 0);
            if (response == 200) {
                try {


                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                    Cursor machines = db.query(true, Database.MACHINE_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (machines.getCount() == 0) {
                        String DefaultMachines = "INSERT INTO " + Database.MACHINE_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.MC_ID + ", "
                                + Database.MC_NAME + ", "
                                + Database.CloudID + ") Values ('0','0', 'Select ...','0')";
                        db.execSQL(DefaultMachines);
                    }

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    count = arrayKnownAs.length();
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);

                        MRecordIndex = obj.getString("RecordIndex");
                        s_MID = obj.getString("MCode");
                        s_MName = obj.getString("NoofOperators");

                        progressStatus++;
                        runOnUiThread(() -> {
                            arcProgress.setProgress(progressStatus);
                            arcProgress.setMax(count);
                            arcProgress.setBottomText("Saving Machines ...");
                            textView.setText(progressStatus + "/" + count + " Records");
                        });

                        Log.i("s_MID", s_MID);
                        Cursor checkMachines = dbhelper.CheckMachine(s_MID);
                        //Check for duplicate
                        if (checkMachines.getCount() > 0) {

                        } else {
                            dbhelper.AddMachine(s_MID, s_MName, MRecordIndex);
                        }


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }
            }
            handler.post(() -> {
                //UI Thread work here
                response = prefs.getInt("getmachineresponse", 0);
                if (response == 200) {
                    //progressDialog.dismiss();
                    LoadEmployees();
                } else {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Server Not Found\nFailed to Connect to " + _URL);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    btnCloud.setVisibility(View.VISIBLE);
                }

            });
        });
    }

    public void LoadEmployees() {
        arcProgress.setProgress(0);
        count = 0;
        progressStatus = 0;
        executor.execute(() -> {

            //Background work here

            restApiResponse = new MasterApiRequest(getApplicationContext()).getEmployees(ERecordIndex, DRecordIndex);
            response = prefs.getInt("employeeresponse", 0);
            EERecordIndex = "0";
            if (response == 200) {
                try {


                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);

                    if (arrayKnownAs.length() > 0) {
                        // Do something with object.
                        count = arrayKnownAs.length();
                        for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                            JSONObject obj = arrayKnownAs.getJSONObject(i);


                            EERecordIndex = obj.getString("Recordindex");
                            s_emID = obj.getString("EPFNo");
                            s_emName = obj.getString("EStaffName");
                            s_emIDNo = obj.getString("ERegistrationNumber");
                            s_emCardID = obj.getString("ECardNumber");
                            s_emPickerNo = "";
                            s_emTeam = obj.getString("dpGang");

                            Cursor checkEmployee = dbhelper.CheckEM(s_emID);
                            //Check for duplicate Employee Number
                            if (checkEmployee.getCount() > 0) {
                            } else {
                                dbhelper.AddEM(s_emID, s_emName, s_emIDNo, s_emCardID, s_emPickerNo, s_emTeam, "");
                            }
                            progressStatus++;
                            runOnUiThread(() -> {
                                arcProgress.setProgress(progressStatus);
                                arcProgress.setMax(count);
                                arcProgress.setBottomText("Saving Employees ...");
                                textView.setText(progressStatus + "/" + count + " Records");
                            });
                        }

                    } else {
                        EERecordIndex = "-1";
                        Message = "No Employees Found";
                    }

                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());
                    Log.e("Server Response", e.toString());
                    e.printStackTrace();
                    Server = "-8080";
                }
            } else {
                Server = "-8080";
            }

            handler.post(() -> {
                //UI Thread work here


                response = prefs.getInt("employeeresponse", 0);
                if (response == 200) {
                    if (Integer.parseInt(EERecordIndex) > 0) {


                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText(count + " employees saved successfully");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        systembasedate = prefs.getString("basedate", "");
                        if (systembasedate.equals("")) {
                            showEmployeesDialog();
                            return;
                        } else {
                            arcProgress.setProgress(0);
                            arcProgress.setBottomText("Masters");
                            btnCloud.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.GONE);
                            count = 0;
                        }
                    } else if (Integer.parseInt(EERecordIndex) < -1) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText(Message + "\nSelect another Division");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                    }
                } else {
                    if (Server.equals("-8080")) {

                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Server Not Found\nFailed to Connect to " + _URL);
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        btnCloud.setVisibility(View.VISIBLE);
                        return;
                    }
                }
            });
        });

    }

    public void openfolder() {


        /////////////////////////////////////////////////////////////////////////////////////////////////
        //Create FileSaveDialog and register a callback
        /////////////////////////////////////////////////////////////////////////////////////////////////
        DirectoryChooserDialog FileSaveDialog = new DirectoryChooserDialog(SyncMastersActivity.this, "FileSave", new DirectoryChooserDialog.SimpleFileDialogListener() {
            @Override
            public void onChosenDir(String chosenDir) {
                path = chosenDir;


                SQLiteDatabase db = dbhelper.getWritableDatabase();
                db.delete(Database.OPERATORSMASTER_TABLE_NAME, null, null);
                db.delete(Database.ESTATES_TABLE_NAME, null, null);
                db.delete(Database.DIVISIONS_TABLE_NAME, null, null);
                db.delete(Database.FIELD_TABLE_NAME, null, null);
                db.delete(Database.BLOCK_TABLE_NAME, null, null);
                db.delete(Database.FACTORY_TABLE_NAME, null, null);
                db.delete(Database.TASK_TABLE_NAME, null, null);
                db.delete(Database.EM_TABLE_NAME, null, null);
                db.delete(Database.MACHINE_TABLE_NAME, null, null);
                db.delete(Database.TRANSPORTER_TABLE_NAME, null, null);

                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.OPERATORSMASTER_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.ESTATES_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.DIVISIONS_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FIELD_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.BLOCK_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FACTORY_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.TASK_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.EM_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.MACHINE_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.TRANSPORTER_TABLE_NAME + "'");

                String DefaultEstates = "INSERT INTO " + Database.ESTATES_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.ES_NAME + ") Values ('0', 'Select ...')";
                String DefaultDivision = "INSERT INTO " + Database.DIVISIONS_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.DV_NAME + ") Values ('0', 'Select ...')";
                String DefaultField = "INSERT INTO " + Database.FIELD_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.FD_ID + ") Values ('0', 'Select ...')";
                String DefaultBlock = "INSERT INTO " + Database.BLOCK_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.BK_ID + ") Values ('0', 'Select ...')";
                String DefaultTask = "INSERT INTO " + Database.TASK_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.TK_NAME + ") Values ('0', 'Select ...')";
                String DefaultEmployee = "INSERT INTO " + Database.EM_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.EM_NAME + ") Values ('0', 'Select ...')";
                String DefaultMachine = "INSERT INTO " + Database.MACHINE_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.MC_NAME + ") Values ('0', 'Select ...')";

                db.execSQL(DefaultEstates);
                db.execSQL(DefaultDivision);
                db.execSQL(DefaultBlock);
                db.execSQL(DefaultField);
                db.execSQL(DefaultTask);
                db.execSQL(DefaultEmployee);
                db.execSQL(DefaultMachine);

                ImportFile();

            }
        });

        //You can change the default filename using the public variable "Default_File_Name"
        FileSaveDialog.Default_File_Name = "";

        FileSaveDialog.chooseFile_or_Dir();

        /////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Data Import ...");
                mProgressDialog.setMessage("Importing data from file ...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    public void ImportFile() {
        arcProgress.setProgress(0);
        btnCloud.setVisibility(View.GONE);
        btnFile.setVisibility(View.GONE);
        ltDivision.setVisibility(View.GONE);
        executor.execute(() -> {

            //Background work here
            String[] next = {};
            File file = new File(path);
            try {

                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader csvStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String input;

                while ((input = bufferedReader.readLine()) != null) {
                    count++;
                }

                // System.out.println("Count : "+count);

                CSVReader reader = new CSVReader(csvStreamReader);

                for (; ; ) {
                    next = reader.readNext();


                    if (next != null) {
                        if (next[0].equals(COMPANY_MASTER)) {

                            Sco_prefix = next[1];
                            Sco_name = next[2];
                            Sco_letterbox = next[3];
                            Sco_postcode = next[4];
                            Sco_postname = next[5];
                            Sco_postregion = next[6];
                            Sco_telephone = next[7];
                            server_url = next[8];
                            server_port = next[9];
                            //server_application = next[10];

                            SharedPreferences.Editor edit = mSharedPrefs.edit();

                            edit.putString("company_prefix", Sco_prefix);
                            edit.apply();
                            edit.putString("company_name", Sco_name);
                            edit.apply();
                            edit.putString("company_letterbox", Sco_letterbox);
                            edit.apply();
                            edit.putString("company_postalcode", Sco_postcode);
                            edit.apply();
                            edit.putString("company_postalname", Sco_postname);
                            edit.apply();
                            edit.putString("company_postregion", Sco_postregion);
                            edit.apply();
                            edit.putString("company_posttel", Sco_telephone);
                            edit.apply();
                            edit.putString("portalURL", server_url);
                            edit.apply();
                            edit.putString("coPort", server_port);
                            edit.apply();
                            edit.putString("coApp", server_application);
                            edit.apply();
                            edit.putString("internetAccessModes", "WF");
                            edit.apply();
                            edit.putBoolean("cloudServices", true);
                            edit.apply();

                        } else if (next[0].equals(ESTATE_MASTER)) {

                            s_esID = next[1];
                            s_esName = next[2];
                            s_esCompany = next[3];

                            dbhelper.AddEstate(s_esID, s_esName, s_esCompany, "");

                        } else if (next[0].equals(DIVISION_MASTER)) {

                            s_dvID = next[1];
                            s_dvName = next[2];
                            s_dvEstate = next[3];
                            dbhelper.AddDivision(s_dvID, s_dvName, s_dvEstate, "");
                        } else if (next[0].equals(FIELD_MASTER)) {

                            s_fdID = next[1];
                            s_fdDiv = next[2];
                            dbhelper.AddField(s_fdID, s_fdDiv, "");
                        } else if (next[0].equals(BLOCK_MASTER)) {

                            s_bkID = next[1];
                            s_bkField = next[2];

                            dbhelper.AddBlock(s_bkID, s_bkField, "");
                        } else if (next[0].equals(FACTORY_MASTER)) {

                            s_fryprefix = next[1];
                            s_fryname = next[2];
                            dbhelper.AddFactories(s_fryprefix, s_fryname, s_recordindex);

                        } else if (next[0].equals(TASK_MASTER)) {


                            s_tkID = next[1];
                            s_tkName = next[2];
                            s_tkType = next[3];
                            s_tkOT = next[4];
                            s_tkMT = next[5];

                            dbhelper.AddTask(s_tkID, s_tkName, s_tkType, s_tkOT, s_tkMT, "");
                        } else if (next[0].equals(EMPLOYEE_MASTER)) {

                            s_emID = next[1];
                            s_emName = next[2];
                            s_emIDNo = next[3];
                            s_emCardID = next[4];
                            s_emPickerNo = next[5];

                            dbhelper.AddEM(s_emID, s_emName, s_emIDNo, s_emCardID, s_emPickerNo, "", "");
                        }

                        if (next[0].equals(USER_MASTER)) {

                            s_etNewUserId = next[1];
                            s_etFullName = next[2];
                            s_etPassword = next[3];
                            s_spUserLevel = next[4];
                            dbhelper.AddUsers(s_etFullName, s_etNewUserId, s_etPassword, s_spUserLevel);

                        } else if (next[0].equals(MACHINE_MASTER)) {

                            s_MID = next[1];
                            s_MName = next[2];

                            dbhelper.AddMachine(s_MID, s_MName, "");
                        } else if (next[0].equals(TRANSPORTER_MASTER)) {

                            s_tptID = next[1];
                            s_tptName = next[2];
                            dbhelper.AddTransporter(s_tptID, s_tptName, "");
                        } else if (next[0].equals(CAPITALP_MASTER)) {

                            s_CPID = next[1];
                            s_CPName = next[2];
                            dbhelper.AddCapitalP(s_CPID, s_CPName, "");
                        }


                    } else {
                        break;
                    }
                    progressStatus++;
                    runOnUiThread(() -> {
                        arcProgress.setProgress(progressStatus);
                        arcProgress.setMax(count);
                        arcProgress.setBottomText("Importing ...");
                        textView.setText(progressStatus + "/" + count + " Records");

                    });

                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                //UI Thread work here
                finish();
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText(count + " Records Imported successfully");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();

                mIntent = new Intent(SyncMastersActivity.this, LoginActivity.class);
                startActivity(mIntent);
            });
        });
    }


}