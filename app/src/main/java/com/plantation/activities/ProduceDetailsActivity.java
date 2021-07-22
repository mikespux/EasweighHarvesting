package com.plantation.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.synctocloud.MasterApiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Michael on 30/06/2016.
 */
public class ProduceDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    public Intent mIntent = null;
    DBHelper dbhelper;
    Button btAddUser, btn_svProduce, btnVariety, btnGrade, btnPrice, btn_svPrice;
    LinearLayout layoutVR;
    ListView listProduce;
    EditText etDgProduceCode, etDgProduceTitle, pr_price1;
    String s_etDgProduceCode, s_etDgProduceTitle;
    String accountId;
    TextView textAccountId;
    Boolean success = true;
    String ProduceCode;
    Spinner spinnerProduce, spinnerVariety, spinnerGrade;
    EditText pr_price, vr_price, gr_price;

    String grade, gradeid;
    ArrayList<String> gradedata = new ArrayList<String>();
    ArrayAdapter<String> gradeadapter;

    String variety, varietyid;
    ArrayList<String> varietydata = new ArrayList<String>();
    ArrayAdapter<String> varietyadapter;

    String produce, produceid;
    ArrayList<String> producedata = new ArrayList<String>();
    ArrayAdapter<String> produceadapter;
    LinearLayout ltprice;

    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    String restApiResponse;
    int response;

    String CRecordIndex, PRRecordIndex;
    String PGRecordIndex, pgdRef, pgdName, MpCode;
    String PVRecordIndex, vtrRef, vrtName;

    SharedPreferences mSharedPrefs, prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_produce);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void initializer() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        layoutVR = findViewById(R.id.layoutVR);
        layoutVR.setVisibility(View.VISIBLE);
        dbhelper = new DBHelper(getApplicationContext());
        btAddUser = findViewById(R.id.btAddUser);
        btAddUser.setVisibility(View.GONE);
        btnGrade = findViewById(R.id.btnGrades);
        btnVariety = findViewById(R.id.btnVarieties);
        btnPrice = findViewById(R.id.btnPrices);

        btnGrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent(ProduceDetailsActivity.this, GradeDetailsActivity.class);
                startActivity(mIntent);
            }
        });
        btnVariety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent(ProduceDetailsActivity.this, VarietyDetailsActivity.class);
                startActivity(mIntent);
            }
        });
        btnPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPrices();
                Produce();
                Grade();
                Variety();
            }
        });
        btAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });
        listProduce = this.findViewById(R.id.lvUsers);
        listProduce.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                showUpdateUserDialog();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sync, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {

            case R.id.action_sync:
                if (!isInternetOn()) {
                    createNetErrorDialog();
                    return true;
                }
                LoadCrops();

                return true;
            case R.id.action_clear:

                SQLiteDatabase db = dbhelper.getWritableDatabase();
                db.delete(Database.PRODUCE_TABLE_NAME, null, null);
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.PRODUCE_TABLE_NAME + "'");
                db.delete(Database.PRODUCEGRADES_TABLE_NAME, null, null);
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.PRODUCEGRADES_TABLE_NAME + "'");
                db.delete(Database.PRODUCEVARIETIES_TABLE_NAME, null, null);
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.PRODUCEVARIETIES_TABLE_NAME + "'");
                getdata();

                return true;
        }

        return super.onOptionsItemSelected(item);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(ProduceDetailsActivity.this);
        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>You need internet connection to proceed. Please turn on mobile network or Wi-Fi in Settings.</font>"))
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
//            SQLiteDatabase db = dbhelper.getWritableDatabase();
//            String DeleteRoutes = "DELETE FROM " + Database.PRODUCE_TABLE_NAME + " WHERE NOT (MpCloudID LIKE '"+produceid+"')";
//            db.execSQL(DeleteRoutes);
            LoadVarieties();
            getdata();
        });
    }

    public void LoadCrops() {
        progressDialog = ProgressDialog.show(ProduceDetailsActivity.this,
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
                    text.setText("Server Not Found");
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
        progressDialog = ProgressDialog.show(ProduceDetailsActivity.this,
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
                    text.setText("Server Not Found");
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
        progressDialog = ProgressDialog.show(ProduceDetailsActivity.this,
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
                    text.setText("Server Not Found");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                }
            });
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
                Cursor c = db.rawQuery("select MpCode,CloudID from Produce where MpDescription= '" + produceName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    PRRecordIndex = c.getString(c.getColumnIndex("CloudID"));
                    produceid = c.getString(c.getColumnIndex("MpCode"));
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


    private void Grade() {
        gradedata.clear();
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select pgdRef,pgdName from ProduceGrades where pgdProduce= '" + produceid + "' ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    grade = c.getString(c.getColumnIndex("pgdName"));
                    gradedata.add(grade);

                } while (c.moveToNext());
            }
        }


        gradeadapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, gradedata);
        gradeadapter.setDropDownViewResource(R.layout.spinner_item);
        gradeadapter.notifyDataSetChanged();
        spinnerGrade.setAdapter(gradeadapter);
        spinnerGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String GradeName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select pgdRef from ProduceGrades where pgdName= '" + GradeName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    gradeid = c.getString(c.getColumnIndex("pgdRef"));

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
               /* if(disabled.equals("true")) {
                    // Set the disable item text color
                    tv.setBackgroundColor(Color.parseColor("#E3E4ED"));

                }*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void Variety() {
        varietydata.clear();
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select vtrRef,vrtName from ProduceVarieties where vrtProduce= '" + produceid + "' ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    variety = c.getString(c.getColumnIndex("vrtName"));
                    varietydata.add(variety);

                } while (c.moveToNext());
            }
        }


        varietyadapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, varietydata);
        varietyadapter.setDropDownViewResource(R.layout.spinner_item);
        varietyadapter.notifyDataSetChanged();
        spinnerVariety.setAdapter(varietyadapter);
        spinnerVariety.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String varietyName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select vtrRef from ProduceVarieties where vrtName= '" + varietyName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    varietyid = c.getString(c.getColumnIndex("vtrRef"));

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
               /* if(disabled.equals("true")) {
                    // Set the disable item text color
                  tv.setBackgroundColor(Color.parseColor("#E3E4ED"));

                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    public void showAddUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_produce, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Produce");
        etDgProduceCode = dialogView.findViewById(R.id.etDgProduceCode);
        etDgProduceTitle = dialogView.findViewById(R.id.etDgProduceTitle);


        btn_svProduce = dialogView.findViewById(R.id.btn_svProduce);
        btn_svProduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_etDgProduceCode = etDgProduceCode.getText().toString();
                    s_etDgProduceTitle = etDgProduceTitle.getText().toString();


                    if (s_etDgProduceTitle.equals("") || s_etDgProduceCode.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Cursor checkProduce = dbhelper.CheckProduce(s_etDgProduceCode);
                    //Check for duplicate id number
                    if (checkProduce.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Produce already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dbhelper.AddProduce(s_etDgProduceCode, s_etDgProduceTitle, "");
                    if (success) {


                        Toast.makeText(ProduceDetailsActivity.this, "Produce Saved successfully!!", Toast.LENGTH_LONG).show();

                        etDgProduceCode.setText("");
                        etDgProduceTitle.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(ProduceDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
                    }

                }

            }
        });

        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                getdata();

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                getdata();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showAddPrices() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_prices, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Prices");
        pr_price = dialogView.findViewById(R.id.pr_price);
        vr_price = dialogView.findViewById(R.id.vr_price);
        gr_price = dialogView.findViewById(R.id.gr_price);

        spinnerProduce = dialogView.findViewById(R.id.spinnerProduce);
        spinnerVariety = dialogView.findViewById(R.id.spinnerVariety);
        spinnerGrade = dialogView.findViewById(R.id.spinnerGrade);


        btn_svPrice = dialogView.findViewById(R.id.btn_svPrice);
        btn_svPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    if (success) {

                        if (pr_price.getText().toString().length() > 0) {

                            updateProducePrice();


                        }

                        if (vr_price.getText().toString().length() > 0) {

                            updateVarietyPrice();


                        }
                        if (gr_price.getText().toString().length() > 0) {

                            updateGradePrice();


                        }
                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(ProduceDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
                    }

                }

            }
        });

        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                getdata();

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                getdata();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showUpdateUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_produce, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Produce");
        accountId = textAccountId.getText().toString();

        etDgProduceCode = dialogView.findViewById(R.id.etDgProduceCode);
        etDgProduceCode.setEnabled(false);
        etDgProduceTitle = dialogView.findViewById(R.id.etDgProduceTitle);
        pr_price1 = dialogView.findViewById(R.id.pr_price);
        ltprice = dialogView.findViewById(R.id.ltprice);
        ltprice.setVisibility(View.VISIBLE);

        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.PRODUCE_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            etDgProduceCode.setText(account.getString(account
                    .getColumnIndex(Database.MP_CODE)));
            etDgProduceTitle.setText(account.getString(account
                    .getColumnIndex(Database.MP_DESCRIPTION)));
            pr_price1.setText(account.getString(account
                    .getColumnIndex(Database.MP_RETAILPRICE)));


        }
        account.close();
        db.close();
        dbhelper.close();


        btn_svProduce = dialogView.findViewById(R.id.btn_svProduce);
        btn_svProduce.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteProduce();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateProduce();
                getdata();


            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        getdata();
    }

    public void updateProduce() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.MP_CODE, etDgProduceCode.getText().toString());
            values.put(Database.MP_DESCRIPTION, etDgProduceTitle.getText().toString());


            long rows = db.update(Database.PRODUCE_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            db.close();
            if (rows > 0) {
                Toast.makeText(this, "Updated Produce Successfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Produce!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void updateProducePrice() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.MP_RETAILPRICE, pr_price.getText().toString());


            long rows = db.update(Database.PRODUCE_TABLE_NAME, values,
                    "MpCode = ?", new String[]{produceid});

            db.close();
            if (rows > 0) {

                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Updated Produce Price Successfully!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.TOP | Gravity.TOP, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                // Toast.makeText(this, "Updated Produce Price Successfully!",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Produce!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void updateGradePrice() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.PG_RETAILPRICE, gr_price.getText().toString());


            long rows = db.update(Database.PRODUCEGRADES_TABLE_NAME, values,
                    "pgdRef = ?", new String[]{gradeid});

            db.close();
            if (rows > 0) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Updated Grade Price Successfully!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                ///Toast.makeText(this, "Updated Grade Price Successfully!",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Grade Price!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void updateVarietyPrice() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.VRT_RETAILPRICE, vr_price.getText().toString());


            long rows = db.update(Database.PRODUCEVARIETIES_TABLE_NAME, values,
                    "vtrRef = ?", new String[]{varietyid});

            db.close();
            if (rows > 0) {

                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Updated Variety Price Successfully!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(this, "Updated Variety Price Successfully!",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Variety!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteProduce() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this produce?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteCurrentAccount();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void deleteCurrentAccount() {
        try {
            DBHelper dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            Cursor c = db.rawQuery("select MpCode from Produce where _id= '" + accountId + "' ", null);
            if (c != null) {
                c.moveToFirst();
                ProduceCode = c.getString(c.getColumnIndex("MpCode"));
            }
            c.close();
            Cursor c1 = db.rawQuery("select * from ProduceGrades where pgdProduce= '" + ProduceCode + "' ", null);
            Cursor c2 = db.rawQuery("select * from ProduceVarieties where vrtProduce= '" + ProduceCode + "' ", null);
            if (c1.getCount() > 0) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Could not delete Produce! ,Because its related in Grades");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                c1.close();
            } else if (c2.getCount() > 0) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Could not delete Produce! ,Because its related in Varieties");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                c2.close();

            } else {
                int rows = db.delete(Database.PRODUCE_TABLE_NAME, "_id=?", new String[]{accountId});
                dbhelper.close();
                if (rows == 1) {
                    Toast.makeText(this, "Produce Deleted Successfully!", Toast.LENGTH_LONG).show();

                    //this.finish();
                    getdata();
                } else
                    Toast.makeText(this, "Could not delete produce!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {
            int ROWID = 0;
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.PRODUCE_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);

            String[] from = {Database.ROW_ID, Database.MP_CODE, Database.MP_DESCRIPTION};
            int[] to = {R.id.txtAccountId, R.id.txtUserName, R.id.txtUserType};

            @SuppressWarnings("deprecation")
            SimpleCursorAdapter ca = new SimpleCursorAdapter(this, R.layout.userlist, accounts, from, to);

            ListView listusers = this.findViewById(R.id.lvUsers);
            listusers.setAdapter(ca);
            dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


}
