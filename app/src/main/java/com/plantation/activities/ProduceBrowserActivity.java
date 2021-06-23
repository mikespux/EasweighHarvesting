package com.plantation.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.services.EasyWeighService;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Michael on 16/08/2016.
 */
public class ProduceBrowserActivity extends AppCompatActivity {
    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";
    public static final String TRANCELL_TI500 = "TI-500";
    public static final String DR_150 = "DR-150";
    public static final String FINGERPRINT = "FingerPrint";
    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    public static final String BOTH = "Both";
    public static String cachedDeviceAddress;
    static SharedPreferences mSharedPrefs, prefs, pref;
    public Intent mIntent;
    public Toolbar toolbar;
    Spinner spProduce, spVariety, spGrade, spTask;
    String grade, gradeid;
    ArrayList<String> gradedata = new ArrayList<String>();
    ArrayAdapter<String> gradeadapter;
    String variety, varietyid;
    ArrayList<String> varietydata = new ArrayList<String>();
    ArrayAdapter<String> varietyadapter;
    String produce, produceid;
    ArrayList<String> producedata = new ArrayList<String>();
    ArrayAdapter<String> produceadapter;
    String taskid = null;
    String tasks;
    ArrayList<String> taskdata = new ArrayList<String>();
    ArrayAdapter<String> taskadapter;
    TextView tv;
    String disabled;
    String BaseDate, BatchDate, DelDate;
    int CLOSED = 1;
    SQLiteDatabase db;
    Button btnBatchOn, btnBatchOff, btnCloseBatch;
    String DeliverNoteNumber, DataDevice, BatchNumber, UserID, OpeningTime;
    String ClosingTime, NoOfWeighments, TotalWeights, Factory, strTractor, strTrailer, SignedOff, SignedOffTime, BatchSession, BatchCount, Dispatched;
    String BatchOn, DNumber;
    DBHelper dbhelper;
    int BatchNo = 1;
    DecimalFormat formatter;
    Spinner Spinnersession;
    String BSession;
    AlertDialog b;
    Button btn_next, btnBack;
    EasyWeighService resetConn;
    String taskType;
    String fieldid = null;
    String fields;
    ArrayList<String> fielddata = new ArrayList<String>();
    ArrayAdapter<String> fieldadapter;
    String divisionID;
    Spinner spField;
    String EstateCode, DivisionCode;
    private TextView dateDisplay, txtCompanyInfo, dtpBatchOn, textClock, txtBatchNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ProduceBrowserActivity.this);
        if (mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {
            setContentView(R.layout.activity_produce_browser_tea);
        } else {
            setContentView(R.layout.activity_produce_browser);

        }
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Produce Browser");

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
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove("tvConn");
        edit.commit();
        finish();
        return;
    }

    public void initializer() {

        prefs = PreferenceManager.getDefaultSharedPreferences(ProduceBrowserActivity.this);
        pref = PreferenceManager.getDefaultSharedPreferences(ProduceBrowserActivity.this);
        dbhelper = new DBHelper(ProduceBrowserActivity.this);
        db = dbhelper.getReadableDatabase();
        resetConn = new EasyWeighService();
        formatter = new DecimalFormat("00");
        spProduce = findViewById(R.id.spProduce);
        spVariety = findViewById(R.id.spVariety);
        spGrade = findViewById(R.id.spGrade);
        spTask = findViewById(R.id.spTask);
        spField = findViewById(R.id.spField);
        dtpBatchOn = findViewById(R.id.dtpBatchOn);
        txtBatchNo = findViewById(R.id.txtBatchNo);
        btnBatchOn = findViewById(R.id.btnBatchOn);
        btnBatchOff = findViewById(R.id.btnBatchOff);


        Produce();
        Variety();
        Grade();
        TaskList();
        FieldList();

        enableBT();

        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("DeliverNoteNumber", txtBatchNo.getText().toString());
        edit.commit();

        //Setting TextView to the current date

        UserID = prefs.getString("user", "");
        //  String selectQuery = "SELECT BatchDate,DeliveryNoteNumber FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE Userid ='" + UserID + "' AND Closed =0";
        String selectQuery = "SELECT BatchDate,DeliveryNoteNumber FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE Closed =0";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                BatchOn = (cursor.getString(0));
                DNumber = (cursor.getString(1));
            } while (cursor.moveToNext());
            // SharedPreferences.Editor edit = prefs.edit();
            edit.putString("BatchON", BatchOn);
            edit.commit();
            dtpBatchOn.setText(BatchOn);
            txtBatchNo.setText(DNumber);
            btnBatchOff.setVisibility(View.VISIBLE);
            btnBatchOn.setVisibility(View.GONE);
        } else {
            dtpBatchOn.setText(prefs.getString("basedate", ""));
            txtBatchNo.setText("No Batch Opened");
            btnBatchOn.setVisibility(View.VISIBLE);
            btnBatchOff.setVisibility(View.GONE);
            edit.putString("DeliverNoteNumber", txtBatchNo.getText().toString());
            edit.commit();
        }
        cursor.close();

        btnBatchOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String CLOSED = "1";
                Cursor count = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                        + Database.Closed + " ='" + CLOSED + "'", null);
                if (count.getCount() > 10) {

                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Sorry! Batch Allocation Exhausted!");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    //customtoast.show();
                    //return;
                }
                String selectQuery2 = "SELECT * FROM " + Database.Fmr_FactoryDeliveries + " WHERE FdStatus=0";
                Cursor cursor1 = db.rawQuery(selectQuery2, null);

                if (cursor1.moveToFirst()) {

                    //Toast.makeText(getApplicationContext(), "Complete Pending Delivery !!", Toast.LENGTH_LONG).show();
                    //return;
                }
                DataDevice = mSharedPrefs.getString("terminalID", "");
                if (DataDevice.equals("")) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Set Terminal ID in Settings To Open a Batch");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
                // Setting Dialog Title
                dialogBuilder.setTitle("Open Batch?");
                // Setting Dialog Message
                dialogBuilder.setMessage("Are you sure you want to open a Batch?");

                // Setting Positive "Yes" Button
                dialogBuilder.setNegativeButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                /*SharedPreferences.Editor edit = prefs.edit();
                                edit.remove("basedate");
                                edit.commit();*/

                                BatchDate = prefs.getString("basedate", "");
                                Date date = new Date(getDate());
                                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                                String dateBatch = format.format(date);

                                Cursor count = db.rawQuery("select * from FarmersSuppliesConsignments WHERE BatchDate ='" + BatchDate + "'", null);
                                if (count.getCount() > 0) {
                                    Cursor c = db.rawQuery("select MAX(BatchNumber) from FarmersSuppliesConsignments WHERE BatchDate ='" + BatchDate + "'", null);
                                    if (c != null) {

                                        c.moveToFirst();

                                        BatchNo = Integer.parseInt(c.getString(0)) + 1;
                                        BatchNumber = formatter.format(BatchNo);

                                    }
                                    c.close();
                                } else {
                                    BatchNumber = formatter.format(BatchNo);

                                }
                                DeliverNoteNumber = DataDevice + dateBatch + BatchNumber;
                                UserID = prefs.getString("user", "");

                                Calendar cal = Calendar.getInstance();
                                SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
                                OpeningTime = format2.format(cal.getTime());

                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("DeliverNoteNumber", DeliverNoteNumber);
                                edit.commit();
                                edit.putString("BatchNumber", BatchNumber);
                                edit.commit();

                                dbhelper.AddBatch(BatchDate, DeliverNoteNumber, DataDevice, BatchNumber, UserID, OpeningTime, EstateCode, DivisionCode);
                                Context context = getApplicationContext();
                                LayoutInflater inflater = getLayoutInflater();
                                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                TextView text = customToastroot.findViewById(R.id.toast);
                                text.setText("Opened Batch: " + DeliverNoteNumber + " Successfully at " + OpeningTime);
                                Toast customtoast = new Toast(context);
                                customtoast.setView(customToastroot);
                                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                customtoast.setDuration(Toast.LENGTH_LONG);
                                customtoast.show();
                                // Toast.makeText(getApplicationContext(), "Opened Batch: " + DeliverNoteNumber + " Successfully at " + OpeningTime, Toast.LENGTH_LONG).show();
                                btnBatchOff.setVisibility(View.VISIBLE);
                                btnBatchOn.setVisibility(View.GONE);
                                finish();
                                mIntent = new Intent(getApplicationContext(), ProduceBrowserActivity.class);
                                startActivity(mIntent);

                            }
                        });
                // Setting Negative "NO" Button
                dialogBuilder.setPositiveButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to invoke NO event

                                dialog.cancel();
                            }
                        });
                // Showing Alert Message

                dialogBuilder.show();

            }
        });


        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (prefs.getString("DeliverNoteNumber", "").equals("")
                        || prefs.getString("DeliverNoteNumber", "").equals("No Batch Opened")) {
                    // snackbar.show();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Open A Batch To Proceed...");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getApplicationContext(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                    return;
                }


                if (mSharedPrefs.getString("scaleVersion", "").equals("")) {
                    // snackbar.show();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Select Scale Model to Weigh");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getApplicationContext(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                    return;
                }

                if (mSharedPrefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15) ||
                        mSharedPrefs.getString("scaleVersion", "").equals(EASYWEIGH_VERSION_11)) {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ProduceBrowserActivity.this);
                    cachedDeviceAddress = pref.getString("address", "");
                    if (cachedDeviceAddress.equals("")) {
                        // snackbar.show();
                        Context context = ProduceBrowserActivity.this;
                        LayoutInflater inflater = ProduceBrowserActivity.this.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please pair scale");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(RouteShedActivity.this, "Please pair scale", Toast.LENGTH_LONG).show();
                        return;
                    }

                }
                if (mSharedPrefs.getString("scaleVersion", "").equals(EASYWEIGH_VERSION_15) ||
                        mSharedPrefs.getString("scaleVersion", "").equals(EASYWEIGH_VERSION_11)) {
                    if (spProduce.getSelectedItem().equals("Select ...")) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Produce");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getApplicationContext(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (spField.getSelectedItem().equals("Select ...")) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Field");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getApplicationContext(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("produceCode", produceid);
                    edit.commit();
                    edit.putString("varietyCode", varietyid);
                    edit.commit();

                    if (mSharedPrefs.getString("vModes", "FingerPrint").equals(FINGERPRINT)) {

                        mIntent = new Intent(getApplicationContext(), CardWeighActivity.class);
                        startActivity(mIntent);

                    } else if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {

                        mIntent = new Intent(getApplicationContext(), CardWeighActivity.class);
                        startActivity(mIntent);

                    } else if (mSharedPrefs.getString("vModes", "Manual").equals(MANUAL)) {
                        mIntent = new Intent(getApplicationContext(), ScaleEasyWeighActivity.class);
                        startActivity(mIntent);

                    } else if (mSharedPrefs.getString("vModes", "Both").equals(BOTH)) {
                        if (pref.getString("fpaddress", "").equals("")) {
                            Context context = getApplicationContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Please Pair C/F Reader");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            //customtoast.show();
                            // return;
                        }
                        mIntent = new Intent(getApplicationContext(), VerificationModeActivity.class);
                        startActivity(mIntent);
                    } else {
                        mIntent = new Intent(getApplicationContext(), ScaleEasyWeighActivity.class);
                        startActivity(mIntent);
                    }

                }

                if (mSharedPrefs.getString("scaleVersion", "").equals(TRANCELL_TI500)) {
                    if (spProduce.getSelectedItem().equals("Select ...")) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Produce");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getApplicationContext(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("produceCode", produceid);
                    edit.commit();
                    edit.putString("varietyCode", varietyid);
                    edit.commit();


                    mIntent = new Intent(getApplicationContext(), ScaleSerialWeighActivity.class);
                    startActivity(mIntent);

                   /* Toast.makeText(getApplicationContext(),  prefs.getString("produceCode", "") + " --- "
                            + prefs.getString("varietyCode", "")+"----"+
                            prefs.getString("gradeCode", "")+"---"
                            +prefs.getString("unitPrice", ""), Toast.LENGTH_LONG).show();*/
                }


            }

        });

        btnBatchOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*SharedPreferences.Editor edit = prefs.edit();
                edit.putString("txtBatchNo", txtBatchNo.getText().toString());
                edit.commit();
                edit.putString("textClock", textClock.getText().toString());
                edit.commit();*/

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
                // Setting Dialog Title
                dialogBuilder.setTitle("Close Batch?");
                // Setting Dialog Message
                dialogBuilder.setMessage("Are you sure you want to close a Batch?");

                // Setting Positive "Yes" Button
                dialogBuilder.setNegativeButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // go back to milkers activity
                                //Toast.makeText(getActivity(), "Shifts not enabled on settings", Toast.LENGTH_LONG).show();
                                String dbtBatchOn = dtpBatchOn.getText().toString() + " 00:00:00";
                                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                                Date date = null;
                                try {
                                    date = fmt.parse(dbtBatchOn);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                                BatchDate = format1.format(date);
                                BatchNumber = prefs.getString("BatchNumber", "");
                                // Toast.makeText(getActivity(), BatchDate, Toast.LENGTH_LONG).show();
                                Cursor count = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                                        + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNumber + "'", null);
                                if (count.getCount() > 0) {
                                    final DecimalFormat df = new DecimalFormat("#0.0#");
                                    final DecimalFormat df1 = new DecimalFormat("##");
                                    Cursor c = db.rawQuery("select " +
                                            "" + Database.DataCaptureDevice +
                                            ",COUNT(" + Database.ROW_ID + ")" +
                                            ",SUM(" + Database.Tareweight + ")" +
                                            ",SUM(" + Database.NetWeight + ")" +
                                            " from EmployeeProduceCollection WHERE "
                                            + Database.CollDate + " ='" + BatchDate + "'and " + Database.BatchNo + " ='" + BatchNumber + "'", null);
                                    if (c != null) {

                                        c.moveToFirst();

                                        NoOfWeighments = df1.format(c.getDouble(1));
                                        TotalWeights = df.format(c.getDouble(3));

                                    }
                                    c.close();
                                    DeliverNoteNumber = txtBatchNo.getText().toString();
                                    Calendar cal = Calendar.getInstance();
                                    SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
                                    ClosingTime = format2.format(cal.getTime());
                                    // ClosingTime = textClock.getText().toString();


                                    ContentValues values = new ContentValues();
                                    values.put(Database.Closed, 1);
                                    values.put(Database.ClosingTime, ClosingTime);
                                    values.put(Database.NoOfWeighments, NoOfWeighments);
                                    values.put(Database.TotalWeights, TotalWeights);


                                    long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                                            "DeliveryNoteNumber = ?", new String[]{DeliverNoteNumber});
                                    if (rows > 0) {
                                        SharedPreferences.Editor edit = prefs.edit();
                                        edit.remove("DeliverNoteNumber");
                                        edit.remove("BatchON");
                                        edit.commit();
                                        Context context = getApplicationContext();
                                        LayoutInflater inflater = getLayoutInflater();
                                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                        TextView text = customToastroot.findViewById(R.id.toast);
                                        text.setText("Closed Batch " + DeliverNoteNumber + "" +
                                                "\nNo Of Weighments " + NoOfWeighments + "" +
                                                "\nTotal Weights " + TotalWeights + " Kgs" +
                                                "\nSuccessfully at " + ClosingTime);
                                        Toast customtoast = new Toast(context);
                                        customtoast.setView(customToastroot);
                                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                        customtoast.setDuration(Toast.LENGTH_LONG);
                                        customtoast.show();
                                        //Toast.makeText(getApplicationContext(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Sorry! Could not Close Batch!", Toast.LENGTH_LONG).show();
                                    }
                                    btnBatchOn.setVisibility(View.VISIBLE);
                                    btnBatchOff.setVisibility(View.GONE);
                                    finish();
                                    mIntent = new Intent(getApplicationContext(), ProduceBrowserActivity.class);
                                    startActivity(mIntent);


                                } else {
                                    Context context = getApplicationContext();
                                    LayoutInflater inflater = getLayoutInflater();
                                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                    TextView text = customToastroot.findViewById(R.id.toast);
                                    text.setText("Sorry! Could Not Close Empty Batch!");
                                    Toast customtoast = new Toast(context);
                                    customtoast.setView(customToastroot);
                                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                    customtoast.setDuration(Toast.LENGTH_LONG);
                                    customtoast.show();


                                    deleteBatch();

                                }


                            }
                        });


                // Setting Negative "NO" Button
                dialogBuilder.setPositiveButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to invoke NO event
                                dialog.cancel();


                            }
                        });
                // Showing Alert Message
                dialogBuilder.show();


            }
        });

    }

    public void deleteBatch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Html.fromHtml("<font color='#4285F4'>Do you want to delete this empty batch?</font>"))
                .setCancelable(false)
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Date date = new Date(getDate());
                        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("BatchON", format1.format(date));
                        edit.commit();

                        edit.remove("DeliverNoteNumber");
                        edit.commit();

                        deleteCurrentAccount();
                        finish();
                        mIntent = new Intent(getApplicationContext(), ProduceBrowserActivity.class);
                        startActivity(mIntent);
                    }
                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);

    }


    public void deleteCurrentAccount() {
        try {
            DBHelper dbhelper = new DBHelper(getApplicationContext());
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            int rows = db.delete(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, "DeliveryNoteNumber=?", new String[]{txtBatchNo.getText().toString()});

            if (rows == 1) {
                Toast.makeText(getApplicationContext(), "Batch Deleted Successfully!", Toast.LENGTH_LONG).show();
                int rows1 = db.delete(Database.EM_PRODUCE_COLLECTION_TABLE_NAME,
                        Database.CollDate + "=? AND " + Database.BatchNo + "=? ", new String[]{BatchDate, BatchNumber}
                );
                dbhelper.close();
                if (rows1 == 1) {
                    Toast.makeText(getApplicationContext(), "Transactions Deleted Successfully!", Toast.LENGTH_LONG).show();

                } else {
                    //Toast.makeText(getApplicationContext(), "No Transactions!", Toast.LENGTH_LONG).show();
                }
            } else
                Toast.makeText(getApplicationContext(), "Could not delete Batch!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
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


        produceadapter = new ArrayAdapter<String>(this, R.layout.spinner_item_min, producedata);
        produceadapter.setDropDownViewResource(R.layout.spinner_item_min);
        spProduce.setAdapter(produceadapter);
        //spProduce.setSelection(1);
        spProduce.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String produceName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select MpCode from Produce where MpDescription= '" + produceName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    produceid = c.getString(c.getColumnIndex("MpCode"));

                }
                c.close();


                tv = (TextView) view;
                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }

                if (position == 0) {
                    spVariety.setEnabled(false);
                    spGrade.setEnabled(false);
                    disabled = "true";
                    Variety();
                    Grade();
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("produceCode");
                    edit.commit();
                    edit.remove("varietyCode");
                    edit.commit();
                    edit.remove("gradeCode");
                    edit.commit();
                    edit.remove("unitPrice");
                    edit.commit();
                    //Toast.makeText(this, "Please sel", Toast.LENGTH_LONG).show();

                } else {

                    Variety();
                    Grade();
                    Cursor c1 = db.rawQuery("select * from ProduceGrades where pgdProduce= '" + produceid + "' ", null);
                    Cursor c2 = db.rawQuery("select * from ProduceVarieties where vrtProduce= '" + produceid + "' ", null);
                    if (c2.getCount() > 0) {
                        spVariety.setEnabled(true);
                        disabled = "false";


                        // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                        c2.close();

                    } else {
                        spVariety.setEnabled(false);
                        varietydata.clear();
                        SharedPreferences.Editor edit = prefs.edit();

                        edit.remove("varietyCode");
                        edit.commit();

                    }
                    if (c1.getCount() > 0) {
                        spGrade.setEnabled(true);
                        disabled = "false";

                        // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                        c1.close();
                    } else {
                        spGrade.setEnabled(false);
                        gradedata.clear();
                        SharedPreferences.Editor edit = prefs.edit();

                        edit.remove("gradeCode");
                        edit.commit();
                    }


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

    private void TaskList() {
        taskdata.clear();
        int type = 1;
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select tkID,tkName from tasks where tkType='" + type + "'", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    tasks = c.getString(c.getColumnIndex("tkName"));
                    taskdata.add(tasks);

                } while (c.moveToNext());
            }
        }


        taskadapter = new ArrayAdapter<String>(ProduceBrowserActivity.this, R.layout.spinner_item_min, taskdata);
        taskadapter.setDropDownViewResource(R.layout.spinner_item_min);
        spTask.setAdapter(taskadapter);
        spTask.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String taskName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select tkID from tasks where tkName= '" + taskName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    taskid = c.getString(c.getColumnIndex("tkID"));


                }
                c.close();
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("taskCode", taskid);
                edit.commit();
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

    private void FieldList() {
        fielddata.clear();
        divisionID = prefs.getString("divisionCode", "");
        fielddata.add("Select ...");
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select fdID,fdDivision from fields where fdDivision='" + divisionID + "' ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                // fielddata.add("Select ...");
                do {
                    fields = c.getString(c.getColumnIndex("fdID"));
                    fielddata.add(fields);

                } while (c.moveToNext());
            }
        }


        fieldadapter = new ArrayAdapter<String>(ProduceBrowserActivity.this, R.layout.spinner_item_min, fielddata);
        fieldadapter.setDropDownViewResource(R.layout.spinner_item_min);
        spField.setAdapter(fieldadapter);
        spField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fieldName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select fdID from fields where fdID= '" + fieldName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    fieldid = c.getString(c.getColumnIndex("fdID"));


                }
                c.close();
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("fieldCode", fieldid);
                edit.commit();
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


        gradeadapter = new ArrayAdapter<String>(this, R.layout.spinner_item_min, gradedata);
        gradeadapter.setDropDownViewResource(R.layout.spinner_item_min);
        gradeadapter.notifyDataSetChanged();
        spGrade.setAdapter(gradeadapter);
        spGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String GradeName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select pgdRef from ProduceGrades where pgdName= '" + GradeName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    gradeid = c.getString(c.getColumnIndex("pgdRef"));


                }
                SharedPreferences.Editor edit = prefs.edit();

                edit.putString("gradeCode", gradeid);
                edit.commit();
                c.close();
                //db.close();
                //dbhelper.close();
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


        varietyadapter = new ArrayAdapter<String>(this, R.layout.spinner_item_min, varietydata);
        varietyadapter.setDropDownViewResource(R.layout.spinner_item_min);
        varietyadapter.notifyDataSetChanged();
        spVariety.setAdapter(varietyadapter);
        spVariety.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String varietyName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select vtrRef from ProduceVarieties where vrtName= '" + varietyName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    varietyid = c.getString(c.getColumnIndex("vtrRef"));

                }
                SharedPreferences.Editor edit = prefs.edit();

                edit.putString("varietyCode", varietyid);
                edit.commit();
                c.close();
                //db.close();
                //dbhelper.close();
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


    //This is method to call the date and not accessible outside this class
    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }

}
