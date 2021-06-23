package com.plantation.activities;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cn.com.aratek.dev.Terminal;
import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintImage;
import cn.com.aratek.fp.FingerprintScanner;
import cn.com.aratek.util.Result;

@SuppressLint({"SdCardPath", "HandlerLeak"})
public class CheckOutFingerprintActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String FINGERPRINT = "FingerPrint";
    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    private static final String TAG = "FingerprintDemo";
    private static final String FP_DB_PATH = "/sdcard/fp.db";
    private static final int MSG_SHOW_ERROR = 0;
    private static final int MSG_SHOW_INFO = 1;
    private static final int MSG_UPDATE_IMAGE = 2;
    private static final int MSG_UPDATE_TEXT = 3;
    private static final int MSG_UPDATE_BUTTON = 4;
    private static final int MSG_UPDATE_SN = 5;
    private static final int MSG_UPDATE_FW_VERSION = 6;
    private static final int MSG_SHOW_PROGRESS_DIALOG = 7;
    private static final int MSG_DISMISS_PROGRESS_DIALOG = 8;
    private static final boolean D = true;
    public static BluetoothAdapter mBluetoothAdapter;
    static TextView tvEmployeeName, tvEmployeeNo, tvField, tvTask, tvUnits;
    static SharedPreferences prefs;
    static SharedPreferences mSharedPrefs;
    static int FingerNo = 0;
    static int CaptureNo = 0;
    static String EmployeeName, Employee_No, AuthMethod, VerMethod, DateTimeIn, DateCheckin, Estate, Division, Rtype, Vtype, TerminalID, UserID, TimeIn, TimeOut, Co_prefix, Current_User, Project;
    private final int totalRecords = 0;
    public Toolbar toolbar;
    public SimpleCursorAdapter ca;
    DBHelper dbhelper;
    SQLiteDatabase db;
    ListView listEmployees;
    String accountId;
    TextView textAccountId, textEmployee, textEmployeeNo;
    Boolean success = true;
    SearchView searchView;
    String fieldid = null;
    String fields;
    ArrayList<String> fielddata = new ArrayList<String>();
    ArrayAdapter<String> fieldadapter;
    String divisionID;
    Spinner spField;
    Spinner spType;
    Button btn_accept;
    EditText etUnits;
    String ColDate, Time, DataDevice, BatchDate, BatchNo, EmNo, EmployeeNo, Clerk, Field, TType;
    String TaskType, TaskCode, TaskOT, TaskMT, TaskUnits, CheckinMethod;
    LinearLayout ltfield;
    SimpleDateFormat dateTimeFormat;
    AlertDialog tasks;
    boolean taskdiag = false;
    String CardNo, Cardsn;
    String s_checkout, s_checkouttime, s_checkoutmethod, s_overtime, s_multiple;
    Button btn_verify;
    TextView txtdesc;
    int cloudid = 0;
    String error, errorNo;
    Cursor attend;
    String Type;
    Boolean Match = false;
    ImageView Thumbnail;
    private TextView mInformation;
    private TextView mDetails;
    private TextView mSN;
    private TextView mFwVersion;
    private Button mBtnEnroll;
    private Button mBtnVerify;
    private Button mBtnIdentify;
    private Button mBtnClear;
    private Button mBtnShow;
    private EditText mCaptureTime;
    private EditText mExtractTime;
    private EditText mGeneralizeTime;
    private EditText mVerifyTime;
    private ImageView mFingerprintImage;
    private ProgressDialog mProgressDialog;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_ERROR: {
                    mInformation.setTextColor(getResources().getColor(R.color.error_text_color));
                    mDetails.setTextColor(getResources().getColor(R.color.error_details_text_color));
                    mInformation.setText(((Bundle) msg.obj).getString("information"));
                    mDetails.setText(((Bundle) msg.obj).getString("details"));
                    break;
                }
                case MSG_SHOW_INFO: {
                    mInformation.setTextColor(getResources().getColor(R.color.information_text_color));
                    mDetails.setTextColor(getResources().getColor(R.color.information_details_text_color));
                    mInformation.setText(((Bundle) msg.obj).getString("information"));
                    mDetails.setText(((Bundle) msg.obj).getString("details"));
                    break;
                }
                case MSG_UPDATE_IMAGE: {
                    mFingerprintImage.setImageBitmap((Bitmap) msg.obj);
                    break;
                }
                case MSG_UPDATE_TEXT: {
                    String[] texts = (String[]) msg.obj;
                    mCaptureTime.setText(texts[0]);
                    mExtractTime.setText(texts[1]);
                    mGeneralizeTime.setText(texts[2]);
                    mVerifyTime.setText(texts[3]);
                    break;
                }
                case MSG_UPDATE_BUTTON: {
                    Boolean enable = (Boolean) msg.obj;
                    mBtnEnroll.setEnabled(enable);
                    mBtnVerify.setEnabled(enable);
                    mBtnIdentify.setEnabled(enable);
                    mBtnClear.setEnabled(enable);
                    mBtnShow.setEnabled(enable);
                    break;
                }
                case MSG_UPDATE_SN: {
                    mSN.setText((String) msg.obj);
                    break;
                }
                case MSG_UPDATE_FW_VERSION: {
                    mFwVersion.setText((String) msg.obj);
                    break;
                }
                case MSG_SHOW_PROGRESS_DIALOG: {
                    String[] info = (String[]) msg.obj;
                    txtdesc.setText(info[1]);
                    mProgressDialog.setTitle(info[0]);
                    mProgressDialog.setMessage(info[1]);
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.show();
                    break;
                }
                case MSG_DISMISS_PROGRESS_DIALOG: {
                    txtdesc.setText("");
                    mProgressDialog.dismiss();
                    break;
                }
            }
        }
    };
    private FingerprintScanner mScanner;
    private FingerprintTask mTask;
    private int mId;
    private String soapResponse, Attendance, returnValue;
    private int progressStatus = 0;
    private int count = 0;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_fingerprint_employee);
        setupToolbar();
        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        prefs = PreferenceManager.getDefaultSharedPreferences(CheckOutFingerprintActivity.this);
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        mScanner = FingerprintScanner.getInstance(this);
        Thumbnail = findViewById(R.id.Thumbnail);

        txtdesc = findViewById(R.id.txtdesc);
        mInformation = findViewById(R.id.tv_info);
        mDetails = findViewById(R.id.tv_details);
        mSN = findViewById(R.id.tv_fps_sn);
        mFwVersion = findViewById(R.id.tv_fps_fw);
        mCaptureTime = findViewById(R.id.captureTime);
        mExtractTime = findViewById(R.id.extractTime);
        mGeneralizeTime = findViewById(R.id.generalizeTime);
        mVerifyTime = findViewById(R.id.verifyTime);
        mFingerprintImage = findViewById(R.id.fingerimage);

        mBtnEnroll = findViewById(R.id.bt_enroll);
        mBtnVerify = findViewById(R.id.bt_verify);
        mBtnIdentify = findViewById(R.id.bt_identify);
        mBtnClear = findViewById(R.id.bt_clear);
        mBtnShow = findViewById(R.id.bt_show);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);

        enableControl(false);

        updateSingerTestText(-1, -1, -1, -1);
        btn_verify = this.findViewById(R.id.btn_verify);
        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                identify();
            }
        });
        TType = prefs.getString("TaskType", "");
        TaskCode = prefs.getString("TaskCode", "");

        spField = findViewById(R.id.spField);
        FieldList();
        ltfield = findViewById(R.id.ltfield);
        if (TType.equals("4")) {
            ltfield.setVisibility(View.GONE);
        }

        listEmployees = this.findViewById(R.id.lvEmployee);
        listEmployees.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                CaptureNo = 0;
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
        //searchView.requestFocus();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Employees");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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


        fieldadapter = new ArrayAdapter<String>(CheckOutFingerprintActivity.this, R.layout.spinner_item_min, fielddata);
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

        ArrayAdapter<String> typeadapter = new ArrayAdapter<String>(CheckOutFingerprintActivity.this, R.layout.spinner_item_min, spinnerArray);
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
                    TaskType = "5";
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
                                SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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
                                tasks.dismiss();


                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                showInformation(getString(R.string.fingerprint_device_open_ready), getString(R.string.fingerprint_device_open_scan));

                            }
                        });
                final AlertDialog alert2 = builder.create();
                alert2.show();


            }
        });


        dialogBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (!mSharedPrefs.getBoolean("fpScan", false) == true) {
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    identify();
                    StartIdentifying();
                }


                showInformation(getString(R.string.fingerprint_device_open_ready), getString(R.string.fingerprint_device_open_scan));
            }
        });
        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });


        tasks = dialogBuilder.create();
        tasks.show();

    }


    @Override
    public void onStart() {
        super.onStart();
        getdata();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void getEmployee() {
        try {

            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, Database.EM_ID + "='" + EmNo + "'", null, null, null, null, null, null);
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


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {
            int ROWID = 0;
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);
            if (accounts.getCount() == 0) {
                Toast.makeText(this, "no task records", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onResume() {
        super.onResume();

        openDevice();
    }

    @Override
    protected void onPause() {
        if (!mSharedPrefs.getBoolean("fpScan", false) == true) {
            closeDevice();
            //Toast.makeText(getBaseContext(), "AutoScan not enabled on settings", Toast.LENGTH_LONG).show();
        } else {
            timer.purge();
            timer.cancel();
            closeDevice();
        }


        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_enroll:
                enroll();
                break;
            case R.id.bt_verify:
                verify();
                break;
            case R.id.bt_identify:
                identify();
                break;
            case R.id.bt_clear:
                ClearFingerprintDB();
                break;
            case R.id.bt_show:
                showFingerprintImage();
                break;
        }
    }

    public void ClearFingerprintDB() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                CheckOutFingerprintActivity.this);
        // Setting Dialog Title
        alertDialog.setTitle("Clear Database?");
        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to clear the Fingerprint Database?");

        // Setting Positive "Yes" Button
        alertDialog.setNegativeButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = dbhelper.getWritableDatabase();

                        db.delete(Database.FINGERPRINT_TABLE_NAME, null, null);
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FINGERPRINT_TABLE_NAME + "'");
                        clearFingerprintDatabase();


                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setPositiveButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        dialog.cancel();
                    }
                });
        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            // startActivity(intent);
            if (!mSharedPrefs.getBoolean("fpScan", false) == true) {
                closeDevice();
            } else {
                timer.purge();
                timer.cancel();
                closeDevice();
            }


            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void updateFingerprintImage(FingerprintImage fi) {
        byte[] fpBmp = null;
        Bitmap bitmap;
//        fi.convert2Wsq();
        if (fi == null || (fpBmp = fi.convert2Bmp()) == null || (bitmap = BitmapFactory.decodeByteArray(fpBmp, 0, fpBmp.length)) == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nofinger);
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_IMAGE, bitmap));
    }

    private void updateSingerTestText(long captureTime, long extractTime, long generalizeTime, long verifyTime) {
        String[] texts = new String[4];
        if (captureTime < 0) {
            texts[0] = getString(R.string.not_done);
        } else if (captureTime < 1) {
            texts[0] = "< 1ms";
        } else {
            texts[0] = captureTime + "ms";
        }

        if (extractTime < 0) {
            texts[1] = getString(R.string.not_done);
        } else if (extractTime < 1) {
            texts[1] = "< 1ms";
        } else {
            texts[1] = extractTime + "ms";
        }

        if (generalizeTime < 0) {
            texts[2] = getString(R.string.not_done);
        } else if (generalizeTime < 1) {
            texts[2] = "< 1ms";
        } else {
            texts[2] = generalizeTime + "ms";
        }

        if (verifyTime < 0) {
            texts[3] = getString(R.string.not_done);
        } else if (verifyTime < 1) {
            texts[3] = "< 1ms";
        } else {
            texts[3] = verifyTime + "ms";
        }

        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_TEXT, texts));
    }

    private void enableControl(boolean enable) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_BUTTON, enable));
    }

    private void openDevice() {
        new Thread() {
            @Override
            public void run() {
                synchronized (CheckOutFingerprintActivity.this) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.preparing_device));
                    int error;
                    if ((error = mScanner.powerOn()) != FingerprintScanner.RESULT_OK) {
                        showError(getString(R.string.fingerprint_device_power_on_failed), getFingerprintErrorString(error));
                    }
                    if ((error = mScanner.open()) != FingerprintScanner.RESULT_OK) {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SN, getString(R.string.fps_sn, "null")));
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FW_VERSION, getString(R.string.fps_fw, "null")));
                        showError(getString(R.string.fingerprint_device_open_failed), getFingerprintErrorString(error));
                    } else {
                        Result res = mScanner.getSN();
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SN, Terminal.getSdkVersion()));
                        res = mScanner.getFirmwareVersion();
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FW_VERSION, getString(R.string.fps_fw, res.data)));
                        showInformation(getString(R.string.fingerprint_device_open_success), null);
                        enableControl(true);


                        if (!mSharedPrefs.getBoolean("fpScan", false) == true) {

                            //Toast.makeText(getBaseContext(), "AutoScan not enabled on settings", Toast.LENGTH_LONG).show();
                        } else {
                            identify();
                            StartIdentifying();
                        }

                    }
                    if ((error = Bione.initialize(CheckOutFingerprintActivity.this, FP_DB_PATH)) != Bione.RESULT_OK) {
                        showError(getString(R.string.algorithm_initialization_failed), getFingerprintErrorString(error));
                    }
                    Log.i(TAG, "Fingerprint algorithm version: " + Bione.getVersion());
                    dismissProgressDialog();
                }
            }
        }.start();
    }

    public void StartIdentifying() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Result has = mScanner.hasFinger();
                        if (has.data == "true") {
                            //showInformation(getString(R.string.fingerprint_device_open_success), String.valueOf(has.arg1)+has.data);

                        } else {
                            identify();
                            //showInformation(getString(R.string.fingerprint_device_open_success), String.valueOf(has.arg1)+has.data);
                            /*if(taskdiag){
                                mProgressDialog.dismiss();
                            }*/
                            dismissProgressDialog();
                        }

                    }
                });
            }
        }, 3000, 1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!mSharedPrefs.getBoolean("fpScan", false) == true) {
            closeDevice();
            //Toast.makeText(getBaseContext(), "AutoScan not enabled on settings", Toast.LENGTH_LONG).show();
        } else {
            timer.purge();
            timer.cancel();
            closeDevice();
        }

        //finish();
        Intent mIntent = new Intent(CheckOutFingerprintActivity.this, TaskListActivity.class);
        //startActivity(mIntent);

    }

    private void closeDevice() {
        new Thread() {
            @Override
            public void run() {
                synchronized (CheckOutFingerprintActivity.this) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.closing_device));
                    enableControl(false);
                    int error;
                    if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
                        mTask.cancel(false);
                        mTask.waitForDone();
                    }
                    if ((error = mScanner.close()) != FingerprintScanner.RESULT_OK) {
                        showError(getString(R.string.fingerprint_device_close_failed), getFingerprintErrorString(error));
                    } else {
                        showInformation(getString(R.string.fingerprint_device_close_success), null);
                        finish();
                        Intent mIntent = new Intent(CheckOutFingerprintActivity.this, MainActivity.class);
                        //    startActivity(mIntent);

                    }
                    if ((error = mScanner.powerOff()) != FingerprintScanner.RESULT_OK) {
                        showError(getString(R.string.fingerprint_device_power_off_failed), getFingerprintErrorString(error));
                    }
                    if ((error = Bione.exit()) != Bione.RESULT_OK) {
                        showError(getString(R.string.algorithm_cleanup_failed), getFingerprintErrorString(error));
                    }
                    dismissProgressDialog();
                }
            }
        }.start();
    }

    private void enroll() {
        mTask = new FingerprintTask();
        mTask.execute("enroll");
    }

    private void verify() {

        mTask = new FingerprintTask();
        mTask.execute("verify");

    }

    private void identify() {
        mTask = new FingerprintTask();
        mTask.execute("identify");
        CaptureNo = 1;
    }

    private void clearFingerprintDatabase() {
        int error = Bione.clear();
        if (error == Bione.RESULT_OK) {
            showInformation(getString(R.string.clear_fingerprint_database_success), null);
        } else {
            showError(getString(R.string.clear_fingerprint_database_failed), getFingerprintErrorString(error));
        }
    }

    private void showFingerprintImage() {
        mTask = new FingerprintTask();
        mTask.execute("show");
    }

    private void showProgressDialog(String title, String message) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_PROGRESS_DIALOG, new String[]{title, message}));
    }

    private void dismissProgressDialog() {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_DISMISS_PROGRESS_DIALOG));
    }

    private void showError(String info, String details) {
        Bundle bundle = new Bundle();
        bundle.putString("information", info);
        bundle.putString("details", details);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_ERROR, bundle));
    }

    private void showInformation(String info, String details) {
        Bundle bundle = new Bundle();
        bundle.putString("information", info);
        bundle.putString("details", details);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_INFO, bundle));
    }

    private String getFingerprintErrorString(int error) {
        int strid;
        switch (error) {
            case FingerprintScanner.RESULT_OK:
                strid = R.string.operation_successful;
                break;
            case FingerprintScanner.RESULT_FAIL:
                strid = R.string.error_operation_failed;
                break;
            case FingerprintScanner.WRONG_CONNECTION:
                strid = R.string.error_wrong_connection;
                break;
            case FingerprintScanner.DEVICE_BUSY:
                strid = R.string.error_device_busy;
                break;
            case FingerprintScanner.DEVICE_NOT_OPEN:
                strid = R.string.error_device_not_open;
                break;
            case FingerprintScanner.TIMEOUT:
                strid = R.string.error_timeout;
                break;
            case FingerprintScanner.NO_PERMISSION:
                strid = R.string.error_no_permission;
                break;
            case FingerprintScanner.WRONG_PARAMETER:
                strid = R.string.error_wrong_parameter;
                break;
            case FingerprintScanner.DECODE_ERROR:
                strid = R.string.error_decode;
                break;
            case FingerprintScanner.INIT_FAIL:
                strid = R.string.error_initialization_failed;
                break;
            case FingerprintScanner.UNKNOWN_ERROR:
                strid = R.string.error_unknown;
                break;
            case FingerprintScanner.NOT_SUPPORT:
                strid = R.string.error_not_support;
                break;
            case FingerprintScanner.NOT_ENOUGH_MEMORY:
                strid = R.string.error_not_enough_memory;
                break;
            case FingerprintScanner.DEVICE_NOT_FOUND:
                strid = R.string.error_device_not_found;
                break;
            case FingerprintScanner.DEVICE_REOPEN:
                strid = R.string.error_device_reopen;
                break;
            case FingerprintScanner.NO_FINGER:
                strid = R.string.error_no_finger;
                break;
            case Bione.INITIALIZE_ERROR:
                strid = R.string.error_algorithm_initialization_failed;
                break;
            case Bione.INVALID_FEATURE_DATA:
                mId = -1;
                strid = R.string.error_invalid_feature_data;
                break;
            case Bione.BAD_IMAGE:
                mId = -1;
                strid = R.string.error_bad_image;
                break;
            case Bione.NOT_MATCH:
                strid = R.string.error_not_match;
                break;
            case Bione.LOW_POINT:
                strid = R.string.error_low_point;
                break;
            case Bione.NO_RESULT:
                strid = R.string.error_no_result;
                break;
            case Bione.OUT_OF_BOUND:
                strid = R.string.error_out_of_bound;
                break;
            case Bione.DATABASE_FULL:
                strid = R.string.error_database_full;
                break;
            case Bione.LIBRARY_MISSING:
                strid = R.string.error_library_missing;
                break;
            case Bione.UNINITIALIZE:
                strid = R.string.error_algorithm_uninitialize;
                break;
            case Bione.REINITIALIZE:
                strid = R.string.error_algorithm_reinitialize;
                break;
            case Bione.REPEATED_ENROLL:
                strid = R.string.error_repeated_enroll;
                break;
            case Bione.NOT_ENROLLED:
                strid = R.string.error_not_enrolled;
                break;
            default:
                strid = R.string.error_other;
                break;
        }
        return getString(strid);
    }

    public void showTask() {
        String EmployeeNo, EmployeeName = "", Task;
        EmployeeNo = EmNo;
//        Task=txtTaskCode.getText().toString();
        Cursor account = db.query(Database.EM_TABLE_NAME, null,
                " emID = ?", new String[]{EmployeeNo}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            EmployeeName = account.getString(account
                    .getColumnIndex(Database.EM_NAME));
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Employee Checkin");
        dialogBuilder.setMessage(Html.fromHtml(
                "<font color='#FA0703'>CheckIn </font>" +
                        "Employee No:<font color='#0036ff'>\n" + EmployeeNo + "</font> Name: <font color='#0036ff'> " + EmployeeName + "</font>"));
        // accountId = textAccountId.getText().toString();


        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                dialog.dismiss();

            }
        });
        dialogBuilder.setNegativeButton("Check Out", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                CheckOut();
                // getdata1();
                dialog.dismiss();


            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);
    }

    public void CheckOut() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command
            Calendar cal = Calendar.getInstance();
            Date date = new Date(getDate());
            SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat format3 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            ColDate = format1.format(date);
            Time = format2.format(cal.getTime());
            s_checkout = "1";
            s_checkouttime = ColDate + " " + Time;
            if (mSharedPrefs.getString("vModes", "FingerPrint").equals(FINGERPRINT)) {
                s_checkoutmethod = "2";
            } else if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {
                s_checkoutmethod = "1";

            } else if (mSharedPrefs.getString("vModes", "Manual").equals(MANUAL)) {
                s_checkoutmethod = "1";

            } else {
                s_checkoutmethod = "1";
            }

            Employee_No = EmNo;
            AuthMethod = s_checkoutmethod;
            DateTimeIn = format3.format(cal.getTime());
            DateCheckin = format1.format(date);
            Estate = prefs.getString("estateCode", "");
            Division = prefs.getString("divisionCode", " ");
            Rtype = "2";
            Vtype = "1";
            TerminalID = mSharedPrefs.getString("terminalID", "");
            UserID = prefs.getString("user", "");


            Cursor account = db.query(Database.EM_TABLE_NAME, null,
                    " emID = ?", new String[]{Employee_No}, null, null, null);
            //startManagingCursor(accounts);
            if (account.moveToFirst()) {
                // update view
                EmployeeName = account.getString(account
                        .getColumnIndex(Database.EM_NAME));
                CardNo = account.getString(account
                        .getColumnIndex(Database.EM_CARDID));
            }
            Cursor checkEmployee = dbhelper.CheckIn(Employee_No, DateCheckin, Rtype);
            //Check for duplicate id number
            if (checkEmployee.getCount() > 0) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Employee " + Employee_No + " - " + EmployeeName + " has already Checked-Out Today!!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                // Toast.makeText(getApplicationContext(), "Employee already Checked-Out Today!!",Toast.LENGTH_SHORT).show();
                return;
            }
            dbhelper.AddCheckin(Employee_No, CardNo, AuthMethod, DateTimeIn, DateCheckin, Estate, Division, TerminalID, Rtype, Vtype, UserID);


            if (!mSharedPrefs.getBoolean("realtimeServices", false) == true) {

                //Toast.makeText(getBaseContext(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();

            } else {

                // Method to Send Weighments to Cloud.
                new PostAttRecord().execute();


            }
            if (success) {
                EmployeeNo = "";

                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText(Html.fromHtml(
                        "<font color='#FA0703'>Checked-Out Successfully!!\n</font>" +
                                "Employee No:<font color='#0036ff'>\n" + Employee_No + "</font> Name: <font color='#0036ff'> " + EmployeeName + "</font>"));
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();

                // Toast.makeText(CheckInActivity.this, "Checked-Out Successfully!!", Toast.LENGTH_LONG).show();


            }


        } catch (Exception ex) {
            if (success) {
                Toast.makeText(CheckOutFingerprintActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
            }
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();

        }
    }

    @SuppressLint("StringFormatMatches")
    public void showEmployee() {
        SQLiteDatabase db = dbhelper.getReadableDatabase();

        if (mId < 0) {
            mId = -1;
            return;
        }
        Cursor accounts = db.rawQuery("select * from " + Database.FINGERPRINT_TABLE_NAME + " where " + Database.FEM_FINGERPRINT + "=" + mId, null);
        if (accounts.getCount() > 0) {
            while (accounts.moveToNext()) {

                EmNo = accounts.getString(accounts.getColumnIndex(Database.FEM_ID));
                showInformation(getString(R.string.fingerprint_match), getString(R.string.matched_id, mId) + " Employee No: " + EmNo);

            }
            if (!mSharedPrefs.getBoolean("fpScan", false) == true) {

                //Toast.makeText(getBaseContext(), "AutoScan not enabled on settings", Toast.LENGTH_LONG).show();
            } else {
                timer.purge();
                timer.cancel();

            }
           /* if(taskdiag){
                mProgressDialog.dismiss();
            }*/
            //dismissProgressDialog();
            // mTask.cancel(true);
            //mTask.waitForDone();

        }
    }

    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }

    private class FingerprintTask extends AsyncTask<String, Integer, Void> {
        private boolean mIsDone = false;

        @Override
        protected void onPreExecute() {
            enableControl(false);
        }

        @Override
        protected Void doInBackground(String... params) {
            long startTime, captureTime = -1, extractTime = -1, generalizeTime = -1, verifyTime = -1;
            FingerprintImage fi = null;
            byte[] fpFeat = null, fpTemp = null;
            Result res;

            do {
                if (params[0].equals("show") || params[0].equals("enroll") || params[0].equals("verify") || params[0].equals("identify")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.press_finger));
                    int capRetry = 0;
                    mScanner.prepare();
                    do {
                        startTime = System.currentTimeMillis();
                        res = mScanner.capture();
                        captureTime = System.currentTimeMillis() - startTime;

                        fi = (FingerprintImage) res.data;
                        int quality;
                        if (fi != null) {
                            quality = Bione.getFingerprintQuality(fi);
                            Log.i(TAG, "Fingerprint image quality is " + quality);
                            if (quality < 50 && capRetry < 3 && !isCancelled()) {
                                capRetry++;
                                continue;
                            }
                        }

                        if (res.error != FingerprintScanner.NO_FINGER || isCancelled()) {
                            break;
                        }
                    } while (true);
                    mScanner.finish();
                    if (isCancelled()) {
                        break;
                    }
                    if (res.error != FingerprintScanner.RESULT_OK) {
                        mId = -1;
                        showError(getString(R.string.capture_image_failed), getFingerprintErrorString(res.error));
                        break;
                    }
                    updateFingerprintImage(fi);
                }

                if (params[0].equals("show")) {
                    showInformation(getString(R.string.capture_image_success), null);
                } else if (params[0].equals("enroll")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.enrolling));
                } else if (params[0].equals("verify")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.verifying));
                } else if (params[0].equals("identify")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.identifying));
                }

                if (params[0].equals("enroll") || params[0].equals("verify") || params[0].equals("identify")) {
                    startTime = System.currentTimeMillis();
                    res = Bione.extractFeature(fi);
                    extractTime = System.currentTimeMillis() - startTime;
                    if (res.error != Bione.RESULT_OK) {
                        showError(getString(R.string.enroll_failed_because_of_extract_feature), getFingerprintErrorString(res.error));
                        break;
                    }
                    fpFeat = (byte[]) res.data;
                }

                if (params[0].equals("enroll")) {
                    startTime = System.currentTimeMillis();
                    res = Bione.makeTemplate(fpFeat, fpFeat, fpFeat);
                    generalizeTime = System.currentTimeMillis() - startTime;
                    if (res.error != Bione.RESULT_OK) {
                        showError(getString(R.string.enroll_failed_because_of_make_template), getFingerprintErrorString(res.error));
                        break;
                    }
                    fpTemp = (byte[]) res.data;

                    int id = Bione.getFreeID();
                    if (id < 0) {
                        showError(getString(R.string.enroll_failed_because_of_get_id), getFingerprintErrorString(id));
                        break;
                    }
                    int ret = Bione.enroll(id, fpTemp);
                    if (ret != Bione.RESULT_OK) {
                        showError(getString(R.string.enroll_failed_because_of_error), getFingerprintErrorString(ret));
                        break;
                    }
                    mId = id;

                    showInformation(getString(R.string.enroll_success), getString(R.string.enrolled_id, id) + mId);
                } else if (params[0].equals("verify")) {
                    startTime = System.currentTimeMillis();
                    res = Bione.verify(mId, fpFeat);

                    verifyTime = System.currentTimeMillis() - startTime;
                    if (res.error != Bione.RESULT_OK) {
                        showError(getString(R.string.verify_failed_because_of_error), getFingerprintErrorString(res.error));
                        break;
                    }
                    if ((Boolean) res.data) {

                        showInformation(getString(R.string.fingerprint_match), getString(R.string.fingerprint_similarity, res.arg1) + mId);

                    } else {
                        showError(getString(R.string.fingerprint_not_match), getString(R.string.fingerprint_similarity, res.arg1));
                    }
                } else if (params[0].equals("identify")) {
                    startTime = System.currentTimeMillis();
                    int id = Bione.identify(fpFeat);
                    verifyTime = System.currentTimeMillis() - startTime;
                    if (id < 0) {
                        mId = id;

                        showError(getString(R.string.identify_failed_because_of_error), getFingerprintErrorString(id));
                        break;
                    }
                    mId = id;
                    showEmployee();
                    // dismissProgressDialog();

                    // showInformation(getString(R.string.identify_match), getString(R.string.matched_id, id));
                }
            } while (false);

            updateSingerTestText(captureTime, extractTime, generalizeTime, verifyTime);
            enableControl(true);
            dismissProgressDialog();
            mIsDone = true;
            return null;

        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Void result) {
            if (CaptureNo == 1) {
                if (mId < 0) {
                    mId = -1;
                    return;
                }
                Cursor em = db.query(Database.EM_TABLE_NAME, null,
                        " emID = ?", new String[]{EmNo}, null, null, null);
                //startManagingCursor(accounts);
                if (em.moveToFirst()) {
                    // update view
                    EmployeeName = em.getString(em
                            .getColumnIndex(Database.EM_NAME));
                    CardNo = em.getString(em
                            .getColumnIndex(Database.EM_CARDID));
                }
                Date date = new Date(getDate());
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                ColDate = format1.format(date);
                Cursor check = db.rawQuery("select * from " + Database.EM_CHECKIN_TABLE_NAME + " WHERE "
                        + Database.Date + " ='" + ColDate + "' and " + Database.Employee_No + " ='" + EmNo + "' and " + Database.Rtype + " ='2'", null);
                if (check.getCount() > 0) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Employee " + EmNo + " - " + EmployeeName + " has already Checked-Out Today!!");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    EmNo = "";
                    getdata();
                    return;
                } else {
                    showTask();
                }

                // listEmployees.performItemClick(listEmployees.getAdapter().getView(0, null, null), 0, listEmployees.getAdapter().getItemId(0));
                if (!mSharedPrefs.getBoolean("fpScan", false) == true) {

                    //Toast.makeText(getBaseContext(), "AutoScan not enabled on settings", Toast.LENGTH_LONG).show();
                } else {
                    timer.purge();
                    timer.cancel();

                }


            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onCancelled() {
        }

        public void waitForDone() {
            while (!mIsDone) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public class PostAttRecord extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");

        }

        @Override
        protected String doInBackground(String... aurl) {
            Log.i(TAG, "doInBackground");
            try {
                attend = db.rawQuery("select * from " + Database.EM_CHECKIN_TABLE_NAME + " WHERE "
                        + Database.Date + " ='" + DateCheckin + "'and " + Database.Rtype + " ='" + Rtype + "' and "
                        + Database.Employee_No + " ='" + Employee_No + "' and " + Database.CloudID + " ='" + cloudid + "'", null);
                count = count + attend.getCount();
                //csvWrite.writeNext(attend.getColumnNames());
                while (attend.moveToNext()) {


                    String TransDateTime = attend.getString(attend.getColumnIndex(Database.DateTime));
                    String TransDate = attend.getString(attend.getColumnIndex(Database.Date)) + " 00:00:00";

                    Employee_No = attend.getString(attend.getColumnIndex(Database.Employee_No));
                    CardNo = attend.getString(attend.getColumnIndex(Database.CardNo));
                    AuthMethod = attend.getString(attend.getColumnIndex(Database.AuthMethod));
                    VerMethod = attend.getString(attend.getColumnIndex(Database.Vtype));
                    SimpleDateFormat frmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = null;
                    try {
                        date = frmt.parse(TransDateTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    DateTimeIn = format2.format(date);

                    Date date1 = null;
                    try {
                        date1 = frmt.parse(TransDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd");
                    DateCheckin = format3.format(date1);


                    Estate = attend.getString(attend.getColumnIndex(Database.Estate));
                    Division = attend.getString(attend.getColumnIndex(Database.Division));
                    Rtype = attend.getString(attend.getColumnIndex(Database.Rtype));
                    TerminalID = attend.getString(attend.getColumnIndex(Database.TerminalID));
                    TimeIn = attend.getString(attend.getColumnIndex(Database.TimeIn));
                    TimeOut = attend.getString(attend.getColumnIndex(Database.TimeOut));
                    UserID = attend.getString(attend.getColumnIndex(Database.UserID));


                    Co_prefix = mSharedPrefs.getString("company_prefix", "");
                    Current_User = prefs.getString("user", "");
                    Project = "";


                    StringBuilder sb = new StringBuilder();
                    sb.append("5" + ",");
                    sb.append(Employee_No + ",");
                    sb.append(CardNo + ",");
                    sb.append(AuthMethod + ",");
                    sb.append(VerMethod + ",");
                    sb.append(DateTimeIn + ",");
                    sb.append(DateCheckin + ",");
                    sb.append(Estate + ",");
                    sb.append(Division + ",");
                    sb.append(Rtype + ",");
                    sb.append(TerminalID + ",");
                    sb.append(UserID + ",");
                    sb.append(Co_prefix);

                    Attendance = sb.toString();


                    try {
                        soapResponse = new SoapRequest(CheckOutFingerprintActivity.this).PostClockingRecord(Attendance);
                        error = soapResponse;
                        if (Integer.valueOf(CheckOutFingerprintActivity.this.soapResponse).intValue() < 0) {
                            ContentValues values = new ContentValues();
                            values.put(Database.CloudID, 0);
                            long rows = db.update(Database.EM_CHECKIN_TABLE_NAME, values,
                                    Database.Employee_No + " = ? AND " + Database.Date + " = ? AND " + Database.Estate + " = ?",
                                    new String[]{Employee_No, DateCheckin, Estate});

                            if (rows > 0) {
                                Log.i("error:", soapResponse);

                            }
                            return null;
                        }
                        if (Integer.valueOf(soapResponse).intValue() > 0) {
                            returnValue = soapResponse;
                            ContentValues values = new ContentValues();
                            values.put(Database.CloudID, soapResponse);
                            long rows = db.update(Database.EM_CHECKIN_TABLE_NAME, values,
                                    Database.Employee_No + " = ? AND " + Database.Date + " = ? AND " + Database.Estate + " = ?",
                                    new String[]{Employee_No, DateCheckin, Estate});

                            if (rows > 0) {
                                Log.i("error:", soapResponse);

                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        returnValue = e.toString();
                    }
                    progressStatus++;
                    publishProgress("" + progressStatus);
                }


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
                    returnValue = soapResponse;

                    Toast.makeText(CheckOutFingerprintActivity.this, "Data Saved Successfully !!!", Toast.LENGTH_LONG).show();


                    return;
                }
            } catch (NumberFormatException e) {
                errorNo = prefs.getString("errorNo", "");

                if (errorNo.equals("-8080")) {
                    Toast.makeText(CheckOutFingerprintActivity.this, "Server Not Available !!", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                } else {

                    Toast.makeText(CheckOutFingerprintActivity.this, error, Toast.LENGTH_LONG).show();
                    // finish();
                }
            }


        }
    }
}
