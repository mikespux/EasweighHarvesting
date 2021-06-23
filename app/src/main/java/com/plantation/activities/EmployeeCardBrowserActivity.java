package com.plantation.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.soap.SoapRequest;

import org.xmlpull.v1.XmlPullParser;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Michael on 30/06/2016.
 */
public class EmployeeCardBrowserActivity extends AppCompatActivity {
    public static final String TAG = "tasking";
    public static final String FINGERPRINT = "FingerPrint";
    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    public static BluetoothAdapter mBluetoothAdapter;
    static TextView tvEmployeeName, tvEmployeeNo, tvField, tvTask, tvUnits;
    static SharedPreferences prefs;
    static SharedPreferences mSharedPrefs;
    static int CaptureNo = 0;
    private final String[][] techList = new String[][]{
            new String[]{
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };
    public Toolbar toolbar;
    public SimpleCursorAdapter ca;
    DBHelper dbhelper;
    ListView listEmployees;
    String accountId;
    TextView textAccountId, textEmployee, textEmployeeNo;
    Boolean success = true;
    SearchView searchView;
    Intent mIntent;
    String fieldid = null;
    String fields;
    ArrayList<String> fielddata = new ArrayList<String>();
    ArrayAdapter<String> fieldadapter;
    String divisionID;
    Spinner spField;
    Spinner spType;
    Button btn_accept;
    EditText etUnits;
    String FieldClerk, ProduceCode;
    String VarietyCode, GradeCode, EstateCode, DivisionCode, Project;
    String GrossTotal, TareWeight, Crates;
    String ColDate, Time, DataDevice, BatchDate, BatchNo, EmployeeNo, Clerk, Estate, Division, Field, TType;
    String TaskType, TaskCode, TaskOT, TaskMT, TaskUnits, CheckinMethod;
    String FieldCode, CheckoutMethod, CheckoutTime, Co_prefix, Current_User;
    String taskInfo;
    String error = "-8080", errorNo;
    LinearLayout ltfield;
    SimpleDateFormat dateTimeFormat;
    AlertDialog dtasks;
    SQLiteDatabase db;
    Cursor tasks;
    int count;
    int cloudid = 0;
    String returnValue;
    String Type;
    ImageView c_refresh, c_success, c_error;
    String CardNo, Cardsn;
    SharedPreferences pref;
    TextView desc;
    private TextView mTextView; //ECP 2017-01-16
    private String soapResponse;
    private int progressStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_fptask_employee);
        initializer();
        setupToolbar();

    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Employee Browser");
        getSupportActionBar().setSubtitle("FingerPrint Reader not connected");

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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (mSharedPrefs.getString("vModes", "FingerPrint").equals(FINGERPRINT)) {
            setContentView(R.layout.activity_fptask_employee);

        } else if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {
            setContentView(R.layout.activity_cptask_employee);

        } else {
            setContentView(R.layout.activity_fptask_employee);
        }

        mTextView = findViewById(R.id.txtdesc);
        mTextView.setText("");
        pref = PreferenceManager.getDefaultSharedPreferences(EmployeeCardBrowserActivity.this);
        prefs = PreferenceManager.getDefaultSharedPreferences(EmployeeCardBrowserActivity.this);
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();
        listEmployees = this.findViewById(R.id.lvEmployee);
        listEmployees.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                textEmployee = selectedView.findViewById(R.id.tv_name);
                textEmployeeNo = selectedView.findViewById(R.id.tv_number);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("Employee", textEmployee.getText().toString());
                edit.commit();
                edit.putString("EmployeeNo", textEmployeeNo.getText().toString());
                edit.commit();
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());

                showTaskAllocation();

            }
        });
        desc = this.findViewById(R.id.txtdesc);


        TType = prefs.getString("TaskType", "");
        TaskCode = prefs.getString("TaskCode", "");

        spField = findViewById(R.id.spField);
        FieldList();
        ltfield = findViewById(R.id.ltfield);
        if (TType.equals("4")) {
            ltfield.setVisibility(View.GONE);
        }

        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search Employee No ...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String EmployeeCode = constraint.toString();
                        return dbhelper.SearchSpecificEmployee(EmployeeCode);

                    }
                });
                // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String EmployeeCode = constraint.toString();
                        return dbhelper.SearchEmployee(EmployeeCode);

                    }
                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        searchView.requestFocus();


    }

    private void FieldList() {
        fielddata.clear();
        divisionID = prefs.getString("divisionCode", "");
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select fdID,fdDivision from fields where fdDivision='" + divisionID + "' ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    fields = c.getString(c.getColumnIndex("fdID"));
                    fielddata.add(fields);

                } while (c.moveToNext());
            }
        }


        fieldadapter = new ArrayAdapter<String>(EmployeeCardBrowserActivity.this, R.layout.spinner_item_min, fielddata);
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

    public void showTaskAllocation() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_task_allocation, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        tvEmployeeNo = dialogView.findViewById(R.id.tvEmployeeNo);
        tvEmployeeNo.setText(prefs.getString("EmployeeNo", ""));

        tvEmployeeName = dialogView.findViewById(R.id.tvEmployeeName);
        tvEmployeeName.setText(prefs.getString("Employee", ""));

        tvTask = dialogView.findViewById(R.id.tvTask);
        tvTask.setText(TaskCode);
        tvField = dialogView.findViewById(R.id.tvField);

        if (TType.equals("4")) {
            tvField.setText("");
        } else {

            tvField.setText(spField.getSelectedItem().toString());
        }

        tvUnits = dialogView.findViewById(R.id.tvUnits);
        etUnits = dialogView.findViewById(R.id.etUnits);

        spType = dialogView.findViewById(R.id.spType);
        // you need to have a list of data that you want the spinner to display
        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("Full Day");
        spinnerArray.add("Half Day");
        //spinnerArray.add("Overtime");
        spinnerArray.add("Piecework");
        spinnerArray.add("Hourly Task");
        //spinnerArray.add("Holiday Duty");

        ArrayAdapter<String> typeadapter = new ArrayAdapter<String>(EmployeeCardBrowserActivity.this, R.layout.spinner_item_min, spinnerArray);
        typeadapter.setDropDownViewResource(R.layout.spinner_item_min);
        spType.setAdapter(typeadapter);

        if (mSharedPrefs.getString("Ttype", "FullDay").equals("FullDay")) {
            spType.setSelection(0);
        } else if (mSharedPrefs.getString("Ttype", "HalfDay").equals("HalfDay")) {
            spType.setSelection(1);
        } else if (mSharedPrefs.getString("Ttype", "Piecework").equals("Piecework")) {
            spType.setSelection(2);
        } else if (mSharedPrefs.getString("Ttype", "HourlyTask").equals("HourlyTask")) {
            spType.setSelection(3);
        }

        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TaskOT = prefs.getString("TaskOT", "");
                TaskMT = prefs.getString("TaskMT", "");
                Type = parent.getItemAtPosition(position).toString();
                if (Type.equals("Full Day")) {
                    tvUnits.setText("Units");
                    tvUnits.setVisibility(View.VISIBLE);
                    etUnits.setVisibility(View.VISIBLE);
                    etUnits.setEnabled(false);
                    etUnits.setText("1.0");
                    TaskType = "1";

                } else if (Type.equals("Half Day")) {
                    tvUnits.setText("Units");
                    tvUnits.setVisibility(View.VISIBLE);
                    etUnits.setVisibility(View.VISIBLE);
                    etUnits.setText("0.5");
                    etUnits.setEnabled(false);
                    TaskType = "2";


                } else if (Type.equals("Piecework")) {
                    tvUnits.setVisibility(View.VISIBLE);
                    etUnits.setVisibility(View.VISIBLE);
                    tvUnits.setText("Output");
                    etUnits.setEnabled(true);
                    etUnits.setText("");
                    TaskType = "4";

                } else if (Type.equals("Hourly Task")) {
                    tvUnits.setVisibility(View.VISIBLE);
                    etUnits.setVisibility(View.VISIBLE);
                    tvUnits.setText("Hours");
                    etUnits.setEnabled(true);
                    etUnits.setText("");
                    TaskType = "3";
                } else {

                }


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
        btn_accept = dialogView.findViewById(R.id.btn_accept);
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Type.equals("Piecework")) {
                    if (etUnits.getText().toString().equals("")) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please enter Piecework");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();

                        return;
                    }


                } else if (Type.equals("Hourly Task")) {
                    if (etUnits.getText().toString().equals("")) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please enter Hourly Task");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();

                        return;
                    }
                } else {
                }
                TaskOT = prefs.getString("TaskOT", "");
                                              /*if(TaskOT.equals("1")&&!spType.getSelectedItem().equals("Overtime")){

                                              }*/
                TaskMT = prefs.getString("TaskMT", "");
                EmployeeNo = prefs.getString("EmployeeNo", "");
                TaskCode = prefs.getString("TaskCode", "");
                Date date = new Date(getDate());
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                ColDate = format1.format(date);
                Cursor empalloc = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                        + Database.EmployeeNo + " ='" + EmployeeNo + "'" +
                        "and " + Database.CollDate + " ='" + ColDate + "'", null);
                if (empalloc.getCount() > 0) {

                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("You cannot allocated the same Employee Tasking and Weighing on the same day");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();

                    return;
                }

                Cursor taskb = db.rawQuery("select * from " + Database.EM_TASK_ALLOCATION_TABLE_NAME + " WHERE "
                        + Database.EmployeeNo + " ='" + EmployeeNo + "'" +
                        "and " + Database.CollDate + " ='" + ColDate + "'", null);
                if (taskb.getCount() > 0) {

                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("You cannot allocated more than one tasks to the same employee on the same day");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();

                    return;
                }

                Cursor taska = db.rawQuery("select * from " + Database.EM_TASK_ALLOCATION_TABLE_NAME + " WHERE "
                        + Database.EmployeeNo + " ='" + EmployeeNo + "' and " + Database.TaskCode + " ='" + TaskCode + "' " +
                        "and " + Database.CollDate + " ='" + ColDate + "'", null);

                if (taska.getCount() > 0) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("This Task has already been allocated to these employee");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();

                    return;

                }
                                              /*else{
                                                  Cursor taskb = db.rawQuery("select * from " + Database.EM_TASK_ALLOCATION_TABLE_NAME + " WHERE "
                                                          + Database.EmployeeNo + " ='" + EmployeeNo + "'" +
                                                          "and " + Database.CollDate + " ='" + ColDate + "'", null);
                                                  if (taskb.getCount() > 0){

                                                      Context context=getApplicationContext();
                                                      LayoutInflater inflater=getLayoutInflater();
                                                      View customToastroot =inflater.inflate(R.layout.red_toast, null);
                                                      TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                                      text.setText("You cannot allocated more than one tasks to the same employee on the same day");
                                                      Toast customtoast=new Toast(context);
                                                      customtoast.setView(customToastroot);
                                                      customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                                      customtoast.setDuration(Toast.LENGTH_LONG);
                                                      customtoast.show();

                                                      return;
                                                  }else
                                                  {
                                                      if(TaskMT.equals("0")){

                                                          Context context=getApplicationContext();
                                                          LayoutInflater inflater=getLayoutInflater();
                                                          View customToastroot =inflater.inflate(R.layout.red_toast, null);
                                                          TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                                          text.setText("Task does not allow multiple allocations\nplease choose a task which requires multiple allocation");
                                                          Toast customtoast=new Toast(context);
                                                          customtoast.setView(customToastroot);
                                                          customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                                          customtoast.setDuration(Toast.LENGTH_LONG);
                                                          customtoast.show();

                                                          return;
                                                      }

                                                  }


                                              //}*/

                AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Save Task Allocation?</font>"))
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Date BatchD = null;
                                try {
                                    BatchD = dateTimeFormat.parse(prefs.getString("BatchON", "") + " 00:00:00");
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                SimpleDateFormat format0 = new SimpleDateFormat("yyyyMMdd");
                                BatchDate = format0.format(BatchD);
                                BatchNo = prefs.getString("BatchNumber", "");
                                DataDevice = mSharedPrefs.getString("terminalID", "") + BatchDate + BatchNo;

                                Calendar cal = Calendar.getInstance();
                                Date date = new Date(getDate());
                                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                                SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                                ColDate = format1.format(date);
                                Time = format2.format(cal.getTime());
                                Clerk = prefs.getString("user", "");
                                EmployeeNo = prefs.getString("EmployeeNo", "");
                                TaskCode = prefs.getString("TaskCode", "");

                                TaskUnits = etUnits.getText().toString();
                                Estate = prefs.getString("estateCode", "");
                                Division = prefs.getString("divisionCode", " ");
                                Field = tvField.getText().toString();

                                if (mSharedPrefs.getString("vModes", "FingerPrint").equals(FINGERPRINT)) {
                                    CheckinMethod = "1";

                                } else if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {
                                    CheckinMethod = "2";

                                } else if (mSharedPrefs.getString("vModes", "Manual").equals(MANUAL)) {
                                    CheckinMethod = "3";

                                } else {
                                    CheckinMethod = "1";
                                }


                                dbhelper.AddTaskTrans(ColDate, Time, DataDevice, BatchNo, EmployeeNo,
                                        Clerk, TaskCode, TaskType, TaskUnits, Estate, Division, Field, CheckinMethod);
                                if (!mSharedPrefs.getBoolean("realtimeServices", false) == true) {

                                    //Toast.makeText(getBaseContext(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();

                                } else {

                                    // Method to Send Weighments to Cloud.
                                    new PostTaskRecord().execute();


                                }
                                Context context = getApplicationContext();
                                LayoutInflater inflater = getLayoutInflater();
                                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                TextView text = customToastroot.findViewById(R.id.toast);
                                text.setText("Saved Successfully: " + TaskCode + "\n" + "EmployeeNo: " + EmployeeNo + "");
                                Toast customtoast = new Toast(context);
                                customtoast.setView(customToastroot);
                                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                customtoast.setDuration(Toast.LENGTH_LONG);
                                customtoast.show();
                                dtasks.dismiss();


                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();


                            }
                        });
                final AlertDialog alert2 = builder.create();
                alert2.show();


            }
        });


        dialogBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });


        dtasks = dialogBuilder.create();
        dtasks.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "1");

        //mTextView.setText("onResume:");
        // creating pending intent:
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // creating intent receiver for NFC events:
        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        // enabling foreground dispatch for getting intent from NFC event:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("onPause", "1");

        // disabling foreground dispatch:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getdata();
        EmployeeNo = "";

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {
            int ROWID = 0;
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);
            if (accounts.getCount() == 0) {
                Toast.makeText(this, "no records", Toast.LENGTH_LONG).show();
            }
            String[] from = {Database.ROW_ID, Database.EM_ID, Database.EM_NAME, Database.EM_TEAM};
            int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_name, R.id.tv_pickerno};


            ca = new SimpleCursorAdapter(this, R.layout.employee_list, accounts, from, to);

            listEmployees = this.findViewById(R.id.lvEmployee);
            listEmployees.setAdapter(ca);
            listEmployees.setTextFilterEnabled(true);
            //dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove("tvConn");
        edit.commit();
        //Display alert message when back button has been pressed
        finish();
        // mIntent = new Intent(FarmerDetailsActivity.this,MainActivity.class);
        //startActivity(mIntent);
        return;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind from WeighingActivity service and Unregister receiver

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void getEmployee() {
        try {

            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, Database.EM_ID + "='" + EmployeeNo + "'", null, null, null, null, null, null);
            if (accounts.getCount() == 0) {
                Toast.makeText(this, "no task records", Toast.LENGTH_LONG).show();
            }
            String[] from = {Database.ROW_ID, Database.EM_ID, Database.EM_NAME, Database.EM_PICKERNO};
            int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_name, R.id.tv_pickerno};


            ca = new SimpleCursorAdapter(this, R.layout.employee_list, accounts, from, to);

            listEmployees = this.findViewById(R.id.lvEmployee);
            listEmployees.setAdapter(ca);
            listEmployees.setTextFilterEnabled(true);
            //dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "1");

        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Log.d("onNewIntent", "2");
            mTextView.setText("NFC Tag\n" + ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)));

            //if(getIntent().hasExtra(NfcAdapter.EXTRA_TAG)){

            Parcelable tagN = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tagN != null) {
                Log.d(TAG, "Parcelable OK");
                NdefMessage[] msgs;
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                byte[] payload = dumpTagData(tagN).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};

                //Log.d(TAG, msgs[0].toString());
                dbhelper = new DBHelper(this);
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor accounts = db.rawQuery("select * from " + Database.EM_TABLE_NAME + " where " + Database.EM_CARDID + " COLLATE NOCASE ='" + Cardsn + "'", null);
                if (accounts.getCount() == 0) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Employee does not exist!!");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    EmployeeNo = "";
                    return;
                }

                if (accounts.getCount() > 0) {
                    CaptureNo = 1;
                    while (accounts.moveToNext()) {
                        CardNo = accounts.getString(accounts.getColumnIndex(Database.EM_CARDID));

                        EmployeeNo = accounts.getString(accounts.getColumnIndex(Database.EM_ID));
                        getEmployee();
                        listEmployees.performItemClick(listEmployees.getAdapter().getView(0, null, null), 0, listEmployees.getAdapter().getItemId(0));
                        //showWeighDialog();
                        //Toast.makeText(ScaleEasyWeighBioActivity.this, "EmployeeNO:" +EmployeeNo, Toast.LENGTH_LONG).show();

                    }
                }

            } else {
                Log.d(TAG, "Parcelable NULL");
            }


            Parcelable[] messages1 = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (messages1 != null) {
                Log.d(TAG, "Found " + messages1.length + " NDEF messages");
            } else {
                Log.d(TAG, "Not EXTRA_NDEF_MESSAGES");
            }

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {

                Log.d("onNewIntent:", "NfcAdapter.EXTRA_TAG");

                Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (messages != null) {
                    Log.d(TAG, "Found " + messages.length + " NDEF messages");
                }
            } else {
                Log.d(TAG, "Write to an unformatted tag not implemented");
            }


            //mTextView.setText( "NFC Tag\n" + ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_TAG)));
        }
    }

    private String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        sb.append("ID (reversed): ").append(getReversed(id)).append("\n");
        Cardsn = getHex(id);


        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                String type = "Unknown";
                switch (mifareTag.getType()) {
                    case MifareClassic.TYPE_CLASSIC:
                        type = "Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        type = "Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        type = "Pro";
                        break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }
        Log.d("Datos: ", sb.toString());

        DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
        Date now = new Date();

        mTextView.setText(TIME_FORMAT.format(now) + '\n' + sb.toString());
        return sb.toString();
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        // for (int i = bytes.length - 1; i >= 0; --i) {
        for (int i = 0; i <= bytes.length - 1; i++) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private String ByteArrayToHexString(byte[] inarray) {

        Log.d("ByteArrayToHexString", inarray.toString());

        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
//CE7AEED4
//EE7BEED4
        Log.d("ByteArrayToHexString", String.format("%0" + (inarray.length * 2) + "X", new BigInteger(1, inarray)));


        return out;
    }

    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }

    public class PostTaskRecord extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            c_refresh.setVisibility(View.VISIBLE);
            c_success.setVisibility(View.GONE);
            c_error.setVisibility(View.GONE);

        }

        @Override
        protected String doInBackground(String... aurl) {
            Log.i(TAG, "doInBackground");
            try {
                db = dbhelper.getReadableDatabase();

                tasks = db.rawQuery("select * from " + Database.EM_TASK_ALLOCATION_TABLE_NAME + " WHERE "
                        + Database.CollDate + " ='" + ColDate + "' and " + Database.CloudID + " ='" + cloudid + "'", null);
                count = count + tasks.getCount();
                //csvWrite.writeNext(tasks.getColumnNames());
                while (tasks.moveToNext()) {

                    ColDate = tasks.getString(tasks.getColumnIndex(Database.CollDate));
                    String dbtTransOn = ColDate + " 00:00:00";
                    SimpleDateFormat frmt = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    Date date1 = null;
                    try {
                        date1 = frmt.parse(dbtTransOn);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    SimpleDateFormat format4 = new SimpleDateFormat("yyyy-MM-dd");
                    String TransDate = format4.format(date1);

                    Time = tasks.getString(tasks.getColumnIndex(Database.CaptureTime));
                    DataDevice = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
                    EmployeeNo = tasks.getString(tasks.getColumnIndex(Database.EmployeeNo));
                    TaskCode = tasks.getString(tasks.getColumnIndex(Database.TaskCode));
                    TaskType = tasks.getString(tasks.getColumnIndex(Database.TaskType));
                    TaskUnits = tasks.getString(tasks.getColumnIndex(Database.TaskUnits));
                    EstateCode = tasks.getString(tasks.getColumnIndex(Database.SourceEstate));
                    DivisionCode = tasks.getString(tasks.getColumnIndex(Database.SourceDivision));
                    if (tasks.getString(tasks.getColumnIndex(Database.SourceField)) == null) {
                        FieldCode = "";
                    } else {
                        FieldCode = tasks.getString(tasks.getColumnIndex(Database.SourceField));

                    }
                    //Block=tasks.getString(tasks.getColumnIndex(Database.SourceBlock));

                    //RecieptNo =tasks.getString(tasks.getColumnIndex(Database.DataCaptureDevice))+tasks.getString(tasks.getColumnIndex(Database.ReceiptNo));
                    FieldClerk = tasks.getString(tasks.getColumnIndex(Database.FieldClerk));
                    if (tasks.getString(tasks.getColumnIndex(Database.CheckinMethod)) == null) {
                        CheckinMethod = "3";
                    } else {
                        CheckinMethod = tasks.getString(tasks.getColumnIndex(Database.CheckinMethod));

                    }
                    if (tasks.getString(tasks.getColumnIndex(Database.CheckoutMethod)) == null) {
                        CheckoutMethod = "3";
                    } else {
                        CheckoutMethod = tasks.getString(tasks.getColumnIndex(Database.CheckoutMethod));

                    }

                    if (tasks.getString(tasks.getColumnIndex(Database.CheckoutTime)) == null) {
                        CheckoutTime = TransDate + " 00:00:00";
                    } else {
                        CheckoutTime = tasks.getString(tasks.getColumnIndex(Database.CheckoutTime));

                    }

                    Co_prefix = mSharedPrefs.getString("company_prefix", "");
                    Current_User = prefs.getString("user", "");
                    Project = "";

                    StringBuilder sb = new StringBuilder();
                    sb.append("4" + ",");
                    sb.append(TransDate + ",");
                    sb.append(DataDevice + ",");
                    sb.append(Time + ",");
                    sb.append(FieldClerk + ",");
                    sb.append(EstateCode + ",");
                    sb.append(DivisionCode + ",");
                    sb.append(FieldCode + ",");
                    sb.append(EmployeeNo + ",");
                    sb.append(TaskCode + ",");
                    sb.append(TaskType + ",");
                    sb.append(TaskUnits + ",");
                    sb.append(Project + ",");
                    sb.append(Co_prefix + ",");
                    sb.append(Current_User + ",");
                    sb.append(CheckinMethod + ",");
                    sb.append(CheckoutTime + ",");
                    sb.append(CheckoutMethod);

                    taskInfo = sb.toString();

                    try {
                        soapResponse = new SoapRequest(EmployeeCardBrowserActivity.this).PostTaskRecord(taskInfo);
                        error = soapResponse;
                        if (Integer.valueOf(EmployeeCardBrowserActivity.this.soapResponse).intValue() < 0) {
                            ContentValues values = new ContentValues();
                            values.put(Database.CloudID, soapResponse);
                            long rows = db.update(Database.EM_TASK_ALLOCATION_TABLE_NAME, values,
                                    Database.EmployeeNo + " = ? AND " + Database.CollDate + " = ? AND " + Database.CaptureTime + " = ?",
                                    new String[]{EmployeeNo, ColDate, Time});

                            if (rows > 0) {
                                Log.i("error:", soapResponse);

                            }
                            return null;
                        }
                        if (Integer.valueOf(soapResponse).intValue() > 0) {
                            returnValue = soapResponse;
                            ContentValues values = new ContentValues();
                            values.put(Database.CloudID, returnValue);
                            long rows = db.update(Database.EM_TASK_ALLOCATION_TABLE_NAME, values,
                                    Database.EmployeeNo + " = ? AND " + Database.CollDate + " = ? AND " + Database.CaptureTime + " = ?",
                                    new String[]{EmployeeNo, ColDate, Time});

                            if (rows > 0) {
                                Log.i("success:", returnValue);

                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        returnValue = e.toString();
                    }

                    progressStatus++;
                    publishProgress("" + progressStatus);
                }

                tasks.close();


            } catch (Exception e) {
                e.printStackTrace();
                returnValue = e.toString();
                Log.e(getClass().getSimpleName(), "Write file error: " + e.getMessage());

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            Log.i(TAG, "onProgressUpdate");
            c_refresh.setVisibility(View.VISIBLE);
            c_success.setVisibility(View.GONE);
            c_error.setVisibility(View.GONE);


        }

        @Override
        protected void onPostExecute(String unused) {
            db = dbhelper.getReadableDatabase();
            if (error.equals("-8080")) {
                errorNo = prefs.getString("errorNo", "");

                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Server Not Available !!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();

                //Toast.makeText(mActivity, "Server Not Available !!", Toast.LENGTH_LONG).show();
                // Log.i(TAG, "Server Not Available !!");
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("error", "Server Not Available !!");
                edit.commit();


                return;
            }
            try {

                if (Integer.valueOf(soapResponse).intValue() > 0) {

                    c_refresh.setVisibility(View.GONE);
                    c_success.setVisibility(View.VISIBLE);
                    c_error.setVisibility(View.GONE);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("success", "Uploaded Successfully !!!");
                    edit.commit();
                    return;
                }
            } catch (NumberFormatException e) {
                returnValue = "0";
                errorNo = prefs.getString("errorNo", "");
                ContentValues values = new ContentValues();
                values.put(Database.CloudID, 0);
                long rows = db.update(Database.EM_TASK_ALLOCATION_TABLE_NAME, values,
                        Database.EmployeeNo + " = ? AND " + Database.CollDate + " = ? AND " + Database.CaptureTime + " = ?",
                        new String[]{EmployeeNo, ColDate, Time});

                if (rows > 0) {
                    c_refresh.setVisibility(View.VISIBLE);
                    c_success.setVisibility(View.GONE);
                    c_error.setVisibility(View.GONE);
                }
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("error", error);
                edit.commit();
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                return;

            }

        }
    }


}
