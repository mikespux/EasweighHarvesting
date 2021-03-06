package com.plantation.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.plantation.R;
import com.plantation.activities.MainActivity;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.synctocloud.RestApiRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HomePageFragment extends Fragment {
    static SharedPreferences mSharedPrefs, prefs;
    private final String TAG = "BatchToCloud";
    public View mView;
    public Intent mIntent;
    public Context mContext;
    private final Boolean dialogShownOnce = false;
    String BatchDate, DeliverNoteNumber, DataDevice, BatchNumber, UserID, OpeningTime;
    Button btnBatchOn, btnBatchOff;
    String batchInfo;
    String ClosingTime, NoOfWeighments, NoOfTasks, TotalWeights, Factory;
    String deviceID, deliveryNoteNo, BatchCloudID, stringCloseTime, Current_User, Co_prefix;
    String weighmentInfo;
    String SessionNo, ColDate, Time, TaskCode, EmployeeNo;
    String WorkerNo, FieldClerk, ProduceCode, TaskUnits, TaskType;
    String VarietyCode, GradeCode, Project;
    String GrossTotal, TareWeight, Crates;
    String UnitPrice, RecieptNo, WeighmentNo, NetWeight, FieldCode, Block;


    String estateid = null;
    String estates;
    ArrayList<String> estatedata = new ArrayList<String>();
    ArrayAdapter<String> estateadapter;
    String divisionid = null;
    String divisions;
    ArrayList<String> divisiondata = new ArrayList<String>();
    ArrayAdapter<String> divisionadapter;
    String EstateCode, DivisionCode;
    Spinner spEstate, spDivision;

    String returnValue;
    String totalWeight;
    String BatchOn, DNumber;
    DBHelper dbhelper;
    SQLiteDatabase db;
    int BatchNo = 1;
    DecimalFormat formatter;
    AlertDialog b;
    int currentPage = 0;
    Timer timer;


    String factorys;
    String factoryid = null;
    ArrayList<String> factorydata = new ArrayList<>();
    ArrayAdapter<String> factoryadapter;
    Spinner spinnerFactory;


    String transporters;
    String transporterid = null;
    ArrayList<String> transporterdata = new ArrayList<String>();
    ArrayAdapter<String> transporteradapter;
    Spinner mc_ctransporter;

    String TransporterCode;
    String strTractor;
    String strTrailer;
    String DelivaryNo;
    String Driver, TurnMan;
    EditText etDeliveryNo;
    EditText etTractor, etVehicle, etDriver, etTurnMan;
    String DDate, Transporter, Vehicle, ArrivalTime, DepartureTime;
    String DeliNo;
    int dcount = 1;

    String success = "", error = "", errorNo = "";
    ImageView batch_refresh, batch_success, batch_error;
    String status = "0", _URL = null;
    int cloudid = 0;
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat timeFormat;
    SimpleDateFormat dateOnlyFormat;
    SimpleDateFormat BatchDateFormat;
    Fragment frg = null;
    String restApiResponse;
    int response;
    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());
    String Id, Title, Message;
    String sId, sTitle, sMessage;
    private TextView textTerminal, dateDisplay, txtCompanyInfo, dtpBatchOn, textClock, txtBatchNo, txtBatchNo2;
    private int progressStatus = 0;
    private int count = 0;
    String CheckinMethod;
    private Activity mActivity;
    int online = 0;
    WeighmentsToCloud asyncTask = new WeighmentsToCloud();
    private String serverBatchNo, BatchSerial;

    String OperatorInfo, RowID, sDate, terminalID, machineNo, employeeNo, checkinTime, checkoutTime,
            checkinWeighment, checkoutWeighment, mTaskCode, operator_share, mCompany, mEstate;
    String FuelInfo, mfDate, mfterminalID, mfmachineNo, mfTime, mfLitres, FuelType, mFCompany, mFEstate;
    String DelCloudId = "0";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_homepage, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        initializer();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setShowHideAnimationEnabled(true);

        return mView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initializer() {
        final ViewPager mViewPager = mView.findViewById(R.id.viewPageAndroid);
        AndroidImageAdapter adapterView = new AndroidImageAdapter(getActivity());
        mViewPager.setAdapter(adapterView);
        /*After setting the adapter use the timer */
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == 4 - 1) {
                    currentPage = 0;
                }
                mViewPager.setCurrentItem(currentPage++, true);
            }
        };
        timer = new Timer(); // This will create a new Thread
        timer.schedule(new TimerTask() { // task to be scheduled

            @Override
            public void run() {
                handler.post(Update);
            }
        }, 500, 3000);

        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
        formatter = new DecimalFormat("00");


        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        BatchDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        mActivity = getActivity();

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        dateDisplay = mView.findViewById(R.id.date);
        dateDisplay.setText(this.getDate());
        textTerminal = mView.findViewById(R.id.textTerminal);
        textTerminal.setText(mSharedPrefs.getString("terminalID", ""));


        textClock = mView.findViewById(R.id.textClock);
        dtpBatchOn = mView.findViewById(R.id.dtpBatchOn);
        txtBatchNo = mView.findViewById(R.id.txtBatchNo);
        txtBatchNo2 = mView.findViewById(R.id.txtBatchNo2);
        txtCompanyInfo = mView.findViewById(R.id.txtCompanyInfo);
        btnBatchOn = mView.findViewById(R.id.btnBatchOn);
        btnBatchOff = mView.findViewById(R.id.btnBatchOff);

        batch_error = mView.findViewById(R.id.batch_error);
        batch_success = mView.findViewById(R.id.batch_success);
        batch_refresh = mView.findViewById(R.id.batch_refresh);

        batch_success.setOnClickListener(v -> ServerSuccessDialog());
        batch_refresh.setOnClickListener(v -> ServerSuccessDialog());
        batch_error.setOnClickListener(v -> ServerErrorDialog());

        SharedPreferences.Editor edit = prefs.edit();

        UserID = prefs.getString("user", "");
        //  String selectQuery = "SELECT BatchDate,DeliveryNoteNumber FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE Userid ='" + UserID + "' AND Closed =0";
        String selectQuery = "SELECT BatchDate,DeliveryNoteNumber,BatCloudID  FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE Closed =0";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                BatchOn = (cursor.getString(0));
                DNumber = (cursor.getString(1));


                if ((cursor.getString(2)) == null || cursor.getString(2).equals("")) {
                    status = "-1";
                } else {
                    status = (cursor.getString(2));
                }
            } while (cursor.moveToNext());
            // SharedPreferences.Editor edit = prefs.edit();
            edit.putString("BatchON", BatchOn);
            edit.apply();
            dtpBatchOn.setText(BatchOn);
            txtBatchNo.setText(DNumber);
            btnBatchOff.setVisibility(View.VISIBLE);
            btnBatchOn.setVisibility(View.GONE);
            if (!mSharedPrefs.getBoolean("realtimeServices", false)) {
                batch_success.setVisibility(View.GONE);
                batch_error.setVisibility(View.GONE);
                batch_refresh.setVisibility(View.GONE);
                //Toast.makeText(getBaseContext(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();

            } else {
                if (!checkList()) {
                    return;
                }
                if (!isInternetOn()) {
                    createNetErrorDialog();
                    // return;
                }
                Glide.with(getActivity()).load(R.drawable.ic_refresh).into(batch_refresh);
                if (status.equals("")) {
                    status = "-1";
                }
                if (Integer.parseInt(status) > 0) {

                    batch_refresh.setVisibility(View.GONE);
                    batch_success.setVisibility(View.VISIBLE);
                    batch_error.setVisibility(View.GONE);
                    txtBatchNo2.setVisibility(View.VISIBLE);
                    txtBatchNo2.setText("ServerID: " + status);
                    errorNo = prefs.getString("batcherrorNo", "0");
                    if (Integer.parseInt(errorNo) < 0) {
                        //  batch_success.setVisibility(View.GONE);
                        // batch_error.setVisibility(View.VISIBLE);
                    }
                    // Toast.makeText(getActivity(), status, Toast.LENGTH_LONG).show();
                } else if (Integer.parseInt(status) == 0) {
                    batch_refresh.setVisibility(View.VISIBLE);
                    batch_success.setVisibility(View.GONE);
                    batch_error.setVisibility(View.GONE);
                    //new BatchToCloud().execute();
                } else {
                    batch_refresh.setVisibility(View.GONE);
                    batch_success.setVisibility(View.GONE);
                    batch_error.setVisibility(View.VISIBLE);
                    //Toast.makeText(getActivity(), status, Toast.LENGTH_LONG).show();
                }
            }
            edit.putString("DeliverNoteNumber", DNumber);
            edit.apply();
        } else {
            dtpBatchOn.setText(prefs.getString("basedate", ""));
            txtBatchNo.setText("No Batch Opened");
            btnBatchOn.setVisibility(View.VISIBLE);
            btnBatchOff.setVisibility(View.GONE);
            edit.putString("DeliverNoteNumber", txtBatchNo.getText().toString());
            edit.apply();
            batch_success.setVisibility(View.GONE);
            batch_error.setVisibility(View.GONE);
            Glide.with(getActivity()).load(R.drawable.ic_refresh).into(batch_refresh);

        }
        cursor.close();
        // db.close();

        Calendar cal = Calendar.getInstance();
        String year = String.format("%d", cal.get(Calendar.YEAR));
        txtCompanyInfo.setText(mSharedPrefs.getString("company_name", "") + " ??" + year);


        Cursor moperators = db.rawQuery("SELECT * FROM " + Database.MACHINEOP_TABLE_NAME + " where " + Database.MDATE + "='" + BatchOn + "'", null);
        Cursor machines = db.rawQuery("select * from " + Database.MACHINE_TABLE_NAME + "," + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + "" +
                " where EmployeeProduceCollection.EmployeeNo=mcID and " + Database.CollDate + "='" + BatchOn + "' group by mcID", null);

        // Toast.makeText(getActivity(), String.valueOf(moperators.getCount()), Toast.LENGTH_LONG).show();


        btnBatchOn.setOnClickListener(v -> {

            db = dbhelper.getReadableDatabase();


            //db.delete(Database.EM_CHECKIN_TABLE_NAME,null,null);
            // db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.EM_CHECKIN_TABLE_NAME + "'");

            String CLOSED = "1";
            Cursor count = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                    + Database.Closed + " ='" + CLOSED + "'", null);
            if (count.getCount() > 10) {

                Context context = getActivity();
                LayoutInflater inflater = getActivity().getLayoutInflater();
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

                //Toast.makeText(getActivity(), "Complete Pending Delivery !!", Toast.LENGTH_LONG).show();
                //return;
            }
            DataDevice = mSharedPrefs.getString("terminalID", "");
            if (DataDevice.equals("")) {
                Context context = getActivity();
                LayoutInflater inflater = getActivity().getLayoutInflater();
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

            EstateDivision();


        });
        btnBatchOff.setOnClickListener(v -> {
            /*SharedPreferences.Editor edit = prefs.edit();
            edit.putString("txtBatchNo", txtBatchNo.getText().toString());
            edit.commit();
            edit.putString("textClock", textClock.getText().toString());
            edit.commit();*/
            BatchDate = dtpBatchOn.getText().toString();
            BatchNumber = prefs.getString("BatchNumber", "");

            if (!mSharedPrefs.getBoolean("realtimeServices", false)) {
                //Toast.makeText(getActivity(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();

            } else {

                if (new RestApiRequest(getActivity()).isOnline()) {
                    //Toast.makeText(getActivity(),"You are online!!!!",8000).show();
                    BatchSerial = prefs.getString("DeliverNoteNumber", "");
                    db = dbhelper.getReadableDatabase();
                    Cursor weighments = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                            + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNumber + "' and " + Database.CloudID + " <=0", null);
                    Log.i("Weights", String.valueOf(weighments.getCount()));
                    if (weighments.getCount() > 0) {

                        online = 1;
                        count = 0;
                        syncTasks();
                        return;
                    }
                } else {
                    Log.v("Home", "############################You are not online!!!!");
                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("You are not online!\nPlease Check Your Internet Connection");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    closeBatchOffline();
                    // Toast.makeText(getActivity(),"You are not online!\nPlease Check Your Connection",Toast.LENGTH_LONG).show();
                    return;

                }
            }


            // Toast.makeText(getActivity(), BatchDate, Toast.LENGTH_LONG).show();

            Cursor produce = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                    + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNumber + "'", null);
            if (produce.getCount() == 0) {
                DispatchBatch();
                return;
            } else {
                FinishDispatch();
            }


        });


        int lastTimeStarted = prefs.getInt("last_time_started", -1);
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_YEAR);
        System.out.println("today:    " + today);
        System.out.println("lastTimeStarted:    " + lastTimeStarted);
        if (today != lastTimeStarted) {
            //startSomethingOnce();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("last_time_started", today);
            editor.apply();
            editor.remove("dcount");
            editor.apply();
        }
    }


    @SuppressLint("Range")
    private void EstateList() {
        estatedata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select esID,esName from estates ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    estates = c.getString(c.getColumnIndex("esName"));
                    estatedata.add(estates);

                } while (c.moveToNext());
            }
        }


        estateadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, estatedata);
        estateadapter.setDropDownViewResource(R.layout.spinner_item);
        spEstate.setAdapter(estateadapter);
        spEstate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String estateName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select esID from estates where esName= '" + estateName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    estateid = c.getString(c.getColumnIndex("esID"));
                    EstateCode = c.getString(c.getColumnIndex("esID"));


                }
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    @SuppressLint("Range")
    private void DivisionList() {
        divisiondata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select dvID,dvName from divisions ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    divisions = c.getString(c.getColumnIndex("dvName"));
                    divisiondata.add(divisions);

                } while (c.moveToNext());
            }
        }


        divisionadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, divisiondata);
        divisionadapter.setDropDownViewResource(R.layout.spinner_item);
        spDivision.setAdapter(divisionadapter);
        spDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String divisionName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select dvID,CloudID from divisions where dvName= '" + divisionName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    divisionid = c.getString(c.getColumnIndex("dvID"));
                    DivisionCode = c.getString(c.getColumnIndex("dvID"));


                }
                c.close();
                //db.close();
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

    public void EstateDivision() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_estate_division, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Open Batch");
        spEstate = dialogView.findViewById(R.id.spEstate);
        spDivision = dialogView.findViewById(R.id.spDivision);
        spinnerFactory = dialogView.findViewById(R.id.spinnerFactory);
        EstateList();
        DivisionList();
        dialogBuilder.setNegativeButton("OPEN", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();

            }
        });
        dialogBuilder.setPositiveButton("BACK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                dialog.cancel();

            }
        });
        b = dialogBuilder.create();
        b.show();
        b.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spEstate.getSelectedItem().equals("Select ...")) {
                    Context context = getActivity().getApplicationContext();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Select Estate");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getApplicationContext(), "Please Select Estate", Toast.LENGTH_LONG).show();
                    return;
                }

                if (spDivision.getSelectedItem().equals("Select ...")) {
                    Context context = getActivity().getApplicationContext();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Select Division");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getApplicationContext(), "Please Select Division", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("estateCode", estateid);
                edit.putString("divisionid", divisionid);
                edit.putString("divisionCode", DivisionCode);
                edit.apply();
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
                OpeningTime = dateTimeFormat.format(cal.getTime());

                edit.putString("DeliverNoteNumber", DeliverNoteNumber);
                edit.putString("BatchNumber", BatchNumber);
                edit.apply();


                if (!mSharedPrefs.getBoolean("realtimeServices", false)) {
                    dbhelper.AddBatch(BatchDate, DeliverNoteNumber, DataDevice, BatchNumber, UserID, OpeningTime, EstateCode, DivisionCode, "0");
                    //Toast.makeText(getBaseContext(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();
                    btnBatchOff.setVisibility(View.VISIBLE);
                    btnBatchOn.setVisibility(View.GONE);

                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Opened Batch: " + DeliverNoteNumber + " Successfully at " + OpeningTime);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    requireActivity().finish();
                    mIntent = new Intent(getActivity(), MainActivity.class);
                    startActivity(mIntent);
                } else {
                    if (!checkList()) {
                        return;
                    }
                    if (!isInternetOn()) {
                        createNetErrorDialog();
                        return;
                    }


                    new BatchToCloud().execute();

                    // Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                }



                // Toast.makeText(getActivity(), "Opened Batch: " + DeliverNoteNumber + " Successfully at " + OpeningTime, Toast.LENGTH_LONG).show();
                b.dismiss();

            }
        });

    }

    public void ServerSuccessDialog() {
        success = prefs.getString("success", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Html.fromHtml("<font color='#006400'>" + success + "</font><br>"))
                .setTitle("Success Details")
                .setCancelable(false)
                .setNegativeButton("OK",
                        (dialog, id) -> {

                            batch_refresh.setVisibility(View.GONE);
                            dialog.dismiss();

                        }
                )
                .setPositiveButton("Cancel",
                        (dialog, id) -> {
                            batch_refresh.setVisibility(View.GONE);
                            dialog.dismiss();
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void ServerErrorDialog() {
        error = prefs.getString("error", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Html.fromHtml("<font color='#8B0000'>" + error + "</font><br>Correct the error first before clicking refresh!!"))
                .setTitle("Error Details")
                .setCancelable(false)
                .setNegativeButton("REFRESH",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // new BatchToCloud().execute();

                            }
                        }
                )
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                batch_refresh.setVisibility(View.GONE);
                                dialog.dismiss();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean checkList() {

        try {
            if (mSharedPrefs.getBoolean("cloudServices", false)) {
                try {
                    if (mSharedPrefs.getString("internetAccessModes", null).equals(null)) {
                        Toast.makeText(getActivity(), "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
                        return false;

                    }

                    try {
                        if (!mSharedPrefs.getString("portalURL", null).equals(null) && !mSharedPrefs.getString("portalURL", null).equals(XmlPullParser.NO_NAMESPACE)) {
                            return true;
                        }

                        Toast.makeText(getActivity(), "Portal URL not configured!", Toast.LENGTH_LONG).show();
                        return false;
                    } catch (Exception e) {

                        Toast.makeText(getActivity(), "Portal URL not configured!", Toast.LENGTH_LONG).show();
                        return false;
                    }


                } catch (Exception e3) {
                    e3.printStackTrace();

                    Toast.makeText(getActivity(), "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            Toast.makeText(getActivity(), "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
            return false;


        } catch (Exception e4) {
            e4.printStackTrace();

            Toast.makeText(getActivity(), "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    public boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);

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

    public void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                                batch_refresh.setVisibility(View.GONE);
                                dialog.dismiss();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void FinishDispatch() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        // Setting Dialog Title
        dialogBuilder.setTitle("Close Batch?");
        // Setting Dialog Message
        dialogBuilder.setMessage("Are you sure you want to close a Batch?");

        // Setting Positive "Yes" Button
        dialogBuilder.setNegativeButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        BatchDate = dtpBatchOn.getText().toString();
                        BatchNumber = prefs.getString("BatchNumber", "");
                        // Toast.makeText(getActivity(), BatchDate, Toast.LENGTH_LONG).show();

                        Cursor produce = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                                + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNumber + "'", null);


                        if (produce.getCount() > 0) {
                            final DecimalFormat df = new DecimalFormat("#0.0#");
                            final DecimalFormat df1 = new DecimalFormat("##");

                            Cursor prod = db.rawQuery("select " +
                                    "" + Database.DataCaptureDevice +
                                    ",COUNT(" + Database.ROW_ID + ")" +
                                    ",SUM(" + Database.Tareweight + ")" +
                                    ",SUM(" + Database.NetWeight + ")" +
                                    " from EmployeeProduceCollection WHERE "
                                    + Database.CollDate + " ='" + BatchDate + "'and " + Database.BatchNo + " ='" + BatchNumber + "'", null);


                            if (prod != null) {

                                prod.moveToFirst();

                                NoOfWeighments = df1.format(prod.getDouble(1));
                                TotalWeights = df.format(prod.getDouble(3));

                            }
                            prod.close();


                            DeliverNoteNumber = txtBatchNo.getText().toString();
                            Calendar cal = Calendar.getInstance();
                            ClosingTime = dateTimeFormat.format(cal.getTime());


                            ContentValues values = new ContentValues();
                            if (produce.getCount() == 0) {
                                values.put(Database.SignedOff, 1);
                                values.put(Database.Factory, factoryid);
                            }
                            if (!mSharedPrefs.getBoolean("realtimeServices", false)) {

                                //Toast.makeText(getBaseContext(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();
                                values.put(Database.Closed, 1);
                            } else {
                                values.put(Database.Closed, 0);
                            }
                            values.put(Database.ClosingTime, ClosingTime);
                            values.put(Database.NoOfWeighments, NoOfWeighments);
                            values.put(Database.NoOfTasks, NoOfTasks);
                            values.put(Database.TotalWeights, TotalWeights);


                            long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                                    "DeliveryNoteNumber = ?", new String[]{DeliverNoteNumber});
                            if (rows > 0) {
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("DeliverNoteNumber", DeliverNoteNumber);
                                edit.apply();
                                Context context = getActivity();
                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                TextView text = customToastroot.findViewById(R.id.toast);
                                if (produce.getCount() == 0) {
                                    text.setText("Closed Batch " + DeliverNoteNumber + "" +
                                            "\nSuccessfully at " + ClosingTime +
                                            "\nThere are no Weighments for this Batch!!");
                                } else {
                                    text.setText("Closed Batch " + DeliverNoteNumber + "" +
                                            "\nNo Of Weighments " + NoOfWeighments + "" +
                                            "\nTotal Weights " + TotalWeights + " Kgs" +
                                            "\nSuccessfully at " + ClosingTime);
                                }
                                Toast customtoast = new Toast(context);
                                customtoast.setView(customToastroot);
                                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                customtoast.setDuration(Toast.LENGTH_LONG);
                                customtoast.show();

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (!mSharedPrefs.getBoolean("realtimeServices", false)) {
                                    //Toast.makeText(getBaseContext(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();
                                    btnBatchOn.setVisibility(View.VISIBLE);
                                    btnBatchOff.setVisibility(View.GONE);
                                    requireActivity().finish();
                                    mIntent = new Intent(getActivity(), MainActivity.class);
                                    startActivity(mIntent);
                                    //Toast.makeText(getBaseContext(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();

                                } else {
                                    if (!checkList()) {
                                        return;
                                    }
                                    if (!isInternetOn()) {
                                        createNetErrorDialog();
                                        return;
                                    }

                                    new CloseBatch().execute();


                                }
                                //Toast.makeText(getActivity(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), "Sorry! Could not Close Batch!", Toast.LENGTH_LONG).show();
                            }



                        } else {
                            Context context = getActivity();
                            LayoutInflater inflater = getActivity().getLayoutInflater();
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

    public void deleteBatch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Html.fromHtml("<font color='#4285F4'>Do you want to delete this empty batch?</font>"))
                .setCancelable(false)
                .setNegativeButton("Yes", (dialog, id) -> {
                    Date date = new Date(getDate());
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("BatchON", dateOnlyFormat.format(date));
                    edit.remove("DeliverNoteNumber");
                    edit.apply();


                    if (!mSharedPrefs.getBoolean("realtimeServices", false)) {

                        //Toast.makeText(getBaseContext(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();
                        deleteCurrentAccount();
                        requireActivity().finish();
                        mIntent = new Intent(getActivity(), MainActivity.class);
                        startActivity(mIntent);
                    } else {
                        if (!checkList()) {
                            return;
                        }
                        if (!isInternetOn()) {
                            createNetErrorDialog();
                            return;
                        }

                        new DeleteBatch().execute();
                    }


                })
                .setPositiveButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);

    }

    public void deleteCurrentAccount() {
        try {
            DBHelper dbhelper = new DBHelper(getActivity());
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            int rows = db.delete(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, "DeliveryNoteNumber=?", new String[]{txtBatchNo.getText().toString()});

            if (rows == 1) {
                Toast.makeText(getActivity(), "Batch Deleted Successfully!", Toast.LENGTH_LONG).show();
                getActivity().finish();
                mIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(mIntent);
            } else
                Toast.makeText(getActivity(), "Could not delete Batch!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void DispatchBatch() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_dispatch_factory, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Batch Details");
        spinnerFactory = dialogView.findViewById(R.id.spinnerFactory);
        FactoryList();
        Factory = factoryid;
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();

            }
        });
        b = dialogBuilder.create();
        b.show();
        b.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (spinnerFactory.getSelectedItem().toString().equals("Select ...")) {
                Context context = mActivity;
                LayoutInflater inflater1 = mActivity.getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Select Factory");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                return;
            }
            FinishDispatch();
            b.dismiss();
        });

    }

    @SuppressLint("Range")
    public void FactoryList() {
        factorydata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select FryPrefix,FryTitle from factory ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    factorys = c.getString(c.getColumnIndex("FryTitle"));
                    factorydata.add(factorys);

                } while (c.moveToNext());
            }
        }


        factoryadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, factorydata);
        factoryadapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerFactory.setAdapter(factoryadapter);
        spinnerFactory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String factoryName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select FryPrefix from factory where FryTitle= '" + factoryName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    factoryid = c.getString(c.getColumnIndex("FryPrefix"));


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

    @SuppressLint("Range")
    private void TransporterList() {
        transporterdata.clear();
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select tptID,tptName from transporter ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    transporters = c.getString(c.getColumnIndex("tptName"));
                    transporterdata.add(transporters);

                } while (c.moveToNext());
            }
        }


        transporteradapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, transporterdata);
        transporteradapter.setDropDownViewResource(R.layout.spinner_item);
        mc_ctransporter.setAdapter(transporteradapter);
        mc_ctransporter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String transporterName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                if (transporterName.equals("Select ...")) {
                    transporterid = " ";

                }
                Cursor c = db.rawQuery("select tptID from transporter where tptName= '" + transporterName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    transporterid = c.getString(c.getColumnIndex("tptID"));

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

    public void closeBatchOffline() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        // Setting Dialog Title
        dialogBuilder.setTitle("Close Batch?");
        // Setting Dialog Message
        dialogBuilder.setMessage("Some data could not be uploaded. Do you want to close and dispatch anyway?");

        // Setting Positive "Yes" Button
        dialogBuilder.setNegativeButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DeliverBatch();
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

    public void DeliverBatch() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_dispatch_batch, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        //   dialogBuilder.setTitle("Batch Details");
        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("Batch Details");
        spinnerFactory = dialogView.findViewById(R.id.spinnerFactory);
        mc_ctransporter = dialogView.findViewById(R.id.mc_ctransporter);
        FactoryList();
        TransporterList();

        etVehicle = dialogView.findViewById(R.id.etVehicle);
        etVehicle.setFilters(new InputFilter[]{
                (cs, start, end, spanned, dStart, dEnd) -> {
                    // TODO Auto-generated method stub
                    if (cs.equals("")) { // for backspace
                        return cs;
                    }
                    if (cs.toString().matches("[a-zA-Z0-9]+")) { // here no space character
                        return cs;
                    }
                    return "";
                }
        });
        etTractor = dialogView.findViewById(R.id.etTractor);
        etTractor.setFilters(new InputFilter[]{
                (cs, start, end, spanned, dStart, dEnd) -> {
                    // TODO Auto-generated method stub
                    if (cs.equals("")) { // for backspace
                        return cs;
                    }
                    if (cs.toString().matches("[a-zA-Z0-9]+")) { // here no space character
                        return cs;
                    }
                    return "";
                }
        });
        etDriver = dialogView.findViewById(R.id.etDriver);
        etTurnMan = dialogView.findViewById(R.id.etTurnMan);
        etDeliveryNo = dialogView.findViewById(R.id.etDeliveryNo);


        if (!mSharedPrefs.getBoolean("enableAutomaticDel", false)) {
            // go back to milkers activity
            //Toast.makeText(getActivity(), "Auto generated delivery not enabled", Toast.LENGTH_LONG).show();
            //return;
        } else {
            etDeliveryNo.setEnabled(false);
            formatter = new DecimalFormat("0000");

            DeliNo = prefs.getString("dcount", "0001");
            if (DeliNo != null) {

                dcount = Integer.parseInt(prefs.getString("dcount", "0001")) + 1;
                DeliNo = formatter.format(dcount);

            } else {

                DeliNo = formatter.format(dcount);
            }
            Date date = new Date(getDate());
            SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
            String dateDel = format.format(date);
            deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
            etDeliveryNo.setText(dateDel + deviceID + "D" + DeliNo);
            //Toast.makeText(getActivity(),DelivaryNo,Toast.LENGTH_LONG).show();
        }

        Button btnCloseBatch = dialogView.findViewById(R.id.btnCloseBatch);
        btnCloseBatch.setOnClickListener(v -> {
            if (spinnerFactory.getSelectedItem().toString().equals("Select ...") || spinnerFactory.getSelectedItem().toString().equals("")) {
                Context context = getActivity();
                LayoutInflater inflater1 = getActivity().getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Select Factory");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                return;
            }
            if (etVehicle.length() < 4 || etVehicle.length() > 10) {
                etVehicle.setError("Enter a Valid Number Plate");
                return;
            }
            if (etDriver.length() < 3 || etDriver.length() > 20) {
                etDriver.setError("Enter a Driver Name");
                return;
            }
            if (etDeliveryNo.length() <= 0) {
                etDeliveryNo.setError("Enter Delivery No");
                return;
            }

            Cursor checkDelNo = dbhelper.CheckDelivary(etDeliveryNo.getText().toString());
            //Check for duplicate id number
            if (checkDelNo.getCount() > 0) {
                Context context = getActivity();
                LayoutInflater inflater1 = getActivity().getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Delivery Number Exists, type a new one");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
            builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Deliver Batches?</font>"))
                    .setCancelable(false)
                    .setNegativeButton("Yes", (dialog, id) -> {

                        String CLOSED = "0";
                        String SIGNEDOFF = "0";
                        Cursor count = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                                + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

                        if (count.getCount() > 0) {

                            BatchDate = dtpBatchOn.getText().toString();
                            BatchNumber = prefs.getString("BatchNumber", "");
                            // Toast.makeText(getActivity(), BatchDate, Toast.LENGTH_LONG).show();

                            Cursor produce = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                                    + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNumber + "'", null);


                            if (produce.getCount() > 0) {
                                final DecimalFormat df = new DecimalFormat("#0.0#");
                                final DecimalFormat df1 = new DecimalFormat("##");

                                Cursor prod = db.rawQuery("select " +
                                        "" + Database.DataCaptureDevice +
                                        ",COUNT(" + Database.ROW_ID + ")" +
                                        ",SUM(" + Database.Tareweight + ")" +
                                        ",SUM(" + Database.NetWeight + ")" +
                                        " from EmployeeProduceCollection WHERE "
                                        + Database.CollDate + " ='" + BatchDate + "'and " + Database.BatchNo + " ='" + BatchNumber + "'", null);


                                if (prod != null) {

                                    prod.moveToFirst();

                                    NoOfWeighments = df1.format(prod.getDouble(1));
                                    TotalWeights = df.format(prod.getDouble(3));

                                }
                                prod.close();

                            }


                            Factory = factoryid;
                            TransporterCode = transporterid;
                            strTrailer = etVehicle.getText().toString();
                            strTrailer = strTrailer.replace(",", "");
                            strTractor = etTractor.getText().toString();
                            strTractor = strTractor.replace(",", "");

                            DelivaryNo = etDeliveryNo.getText().toString();

                            Vehicle = etVehicle.getText().toString();
                            Vehicle = Vehicle.replace(",", "");
                            strTractor = etTractor.getText().toString();
                            strTractor = strTractor.replace(",", "");
                            Driver = etDriver.getText().toString();
                            TurnMan = etTurnMan.getText().toString();
                            EstateCode = prefs.getString("estateCode", "");

                            Calendar cal = Calendar.getInstance();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
                            DeliverNoteNumber = txtBatchNo.getText().toString();
                            ClosingTime = dateTimeFormat.format(cal.getTime());
                            DDate = format.format(cal.getTime());
                            ArrivalTime = DDate + " " + format2.format(cal.getTime());


                            ContentValues values = new ContentValues();
                            values.put(Database.Closed, 1);
                            values.put(Database.SignedOff, 1);
                            values.put(Database.ClosingTime, ClosingTime);
                            values.put(Database.NoOfWeighments, NoOfWeighments);
                            values.put(Database.TotalWeights, TotalWeights);
                            values.put(Database.DelivaryNO, DelivaryNo);
                            values.put(Database.Factory, Factory);
                            values.put(Database.Transporter, TransporterCode);
                            values.put(Database.Trailer, strTrailer);
                            values.put(Database.Tractor, strTractor);


                            long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                                    Database.Closed + " = ? and " + Database.SignedOff + " = ?", new String[]{CLOSED, SIGNEDOFF});


                            if (rows > 0) {

                                dbhelper.AddDelivery(EstateCode, DelivaryNo, DDate, Factory, TransporterCode, strTrailer, strTractor, Driver, TurnMan, ArrivalTime, TotalWeights);

                                new CountDownTimer(1000, 100) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        //this will be done every 1000 milliseconds ( 1 seconds )
                                    }

                                    @Override
                                    public void onFinish() {
                                        //the progressBar will be invisible after 60 000 miliseconds ( 1 minute)

                                        DepartureTime = DDate + " " + format2.format(cal.getTime());
                                        ContentValues values = new ContentValues();
                                        values.put(Database.FdWeighbridgeTicket, "");
                                        values.put(Database.FdGrossWt, TotalWeights);
                                        values.put(Database.FdTareWt, "0.0");
                                        values.put(Database.FdDepartureTime, DepartureTime);
                                        values.put(Database.FdStatus, 1);


                                        long rows = db.update(Database.Fmr_FactoryDeliveries, values,
                                                Database.FdDNoteNum + " = ?", new String[]{DelivaryNo});

                                        if (rows > 0) {

                                            Toast.makeText(getActivity(), "Delivered Successfully !!", Toast.LENGTH_LONG).show();
                                            b.dismiss();

                                            getActivity().finish();
                                            mIntent = new Intent(getActivity(), MainActivity.class);
                                            startActivity(mIntent);
                                        }
                                    }

                                }.start();


                                //Toast.makeText(getActivity(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), "Sorry! Could not Close Batch!", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Context context = getActivity();
                            LayoutInflater inflater1 = getActivity().getLayoutInflater();
                            View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Sorry! No Batches To Deliver!");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();

                        }

                    })
                    .setPositiveButton("No", (dialog, id) -> dialog.cancel());
            final AlertDialog alert2 = builder.create();
            alert2.show();


        });

        dialogBuilder.setPositiveButton("Cancel", (dialog, whichButton) -> {
            //do something with edt.getText().toString();

        });

        dialogBuilder.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);

        b = dialogBuilder.create();
        b.show();
        b.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    //This is method to call the date and not accessible outside this class
    private String getDate() {
        //Return the current date
        return DateFormat.getDateInstance().format(new Date());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSharedPrefs.getBoolean("realtimeServices", false)) {

            if (new RestApiRequest(getActivity()).isOnline()) {
                //  Toast.makeText(getActivity(),"You are online!!!!",8000).show();
                BatchSerial = prefs.getString("DeliverNoteNumber", "");
                db = dbhelper.getReadableDatabase();
                Cursor weighments = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                        + Database.DataCaptureDevice + " ='" + BatchSerial + "' and " + Database.CloudID + " <=0", null);
                Log.i("Weights", String.valueOf(weighments.getCount()));

                if (weighments.getCount() < 10) {
                    online = 0;
                    count = 0;
                    syncTasks();
                }
            } else {

                //  Toast.makeText(getActivity(),"You are not online!!!!",8000).show();
                Log.v("Home", "############################You are not online!!!!");
            }
        }
    }

    private void syncTasks() {
        try {
            if (asyncTask.getStatus() != AsyncTask.Status.RUNNING) {   // check if asyncTasks is running
                asyncTask.cancel(true); // asyncTasks not running => cancel it
                asyncTask = new WeighmentsToCloud(); // reset task
                asyncTask.execute(); // execute new task (the same task)
                //Toast.makeText(mActivity, "Some data not uploaded.\nUploading all data ...", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainActivity_TSK", "Error: " + e);
        }
    }


    public class BatchToCloud extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            mActivity = getActivity();
            batch_refresh.setVisibility(View.VISIBLE);
            batch_success.setVisibility(View.GONE);
            batch_error.setVisibility(View.GONE);

        }

        @Override
        protected String doInBackground(String... aurl) {
            Log.i(TAG, "doInBackground");
            try {
                Co_prefix = mSharedPrefs.getString("company_prefix", "");

                //Which column you want to upload
                StringBuilder batch = new StringBuilder();
                batch.append(BatchNumber + ",");
                batch.append(DataDevice + ",");
                batch.append(UserID + ",");
                batch.append(DeliverNoteNumber + ",");
                batch.append(OpeningTime + ",");
                batch.append(Co_prefix + ",");
                batch.append(EstateCode + ",");
                batch.append(DivisionCode);
                batchInfo = batch.toString();

                Log.i("batchInfo", batchInfo);
                restApiResponse = new RestApiRequest(getActivity()).CreateBatch(batchInfo);
                error = restApiResponse;
                Log.i("restApiResponse", restApiResponse);
                try {

                    JSONObject jsonObject = new JSONObject(restApiResponse);
                    Message = jsonObject.getString("Message");
                    if (Message.equals("Authorization has been denied for this request.")) {
                        Id = "-1";
                        SharedPreferences.Editor edit = mSharedPrefs.edit();
                        edit.remove("token");
                        edit.remove("expires_in");
                        edit.remove("expires");
                        edit.apply();
                        return null;
                    }
                    if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                        Id = jsonObject.getString("Id");
                        Title = jsonObject.getString("Title");
                        Message = jsonObject.getString("Message");

                        Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
                        try {
                            if (Integer.parseInt(Id) > 0) {
                                serverBatchNo = Id;
                                dbhelper.AddBatch(BatchDate, DeliverNoteNumber, DataDevice, BatchNumber, UserID, OpeningTime, EstateCode, DivisionCode, serverBatchNo);
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("serverBatchNo", serverBatchNo);
                                edit.apply();

                            }
                            if (Integer.parseInt(Id) < 0) {
                                error = Id;
                                return null;
                            }
                            //System.out.println(value);}
                        } catch (NumberFormatException e) {
                            //value = 0; // your default value
                            return null;

                        }
                    } else {
                        Id = "-1";
                        Title = "";
                        Message = restApiResponse;
                        return null;

                    }
                } catch (JSONException e) {

                    Id = "-1";
                    Title = "";
                    Message = _URL + "\n" + e;
                    e.printStackTrace();
                    return null;
                }


                progressStatus++;
                publishProgress("" + progressStatus);


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
            batch_refresh.setVisibility(View.VISIBLE);
            batch_success.setVisibility(View.GONE);
            batch_error.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String unused) {

            if (error.equals("-8080")) {

                batch_refresh.setVisibility(View.GONE);
                batch_success.setVisibility(View.GONE);
                batch_error.setVisibility(View.VISIBLE);

                Context context = mActivity;
                LayoutInflater inflater = mActivity.getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Server Not Available !!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(mActivity, "Server Not Available !!", Toast.LENGTH_LONG).show();
                // Log.i(TAG, "Server Not Available !!");
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("error", "Server Not Available !!");
                edit.apply();
                return;
            }
            try {
                if (Integer.parseInt(Id) > 0) {
                    batch_refresh.setVisibility(View.GONE);
                    batch_success.setVisibility(View.VISIBLE);
                    batch_error.setVisibility(View.GONE);
                    if (mActivity != null && isAdded()) {
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("success", "Batch Uploaded Successfully !!!");
                        edit.apply();
                        Log.i("Success", Id + " {" + Title + "} " + Message);
                        Toast.makeText(mActivity, "Batch Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Opened Batch: " + DeliverNoteNumber + " Successfully at " + OpeningTime);
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();

                        btnBatchOff.setVisibility(View.VISIBLE);
                        btnBatchOn.setVisibility(View.GONE);

                        getActivity().finish();
                        mIntent = new Intent(getActivity(), MainActivity.class);
                        startActivity(mIntent);
                    }
                    return;
                }
                if (Integer.parseInt(Id) < 0) {
                    batch_refresh.setVisibility(View.GONE);
                    batch_success.setVisibility(View.GONE);
                    batch_error.setVisibility(View.VISIBLE);
                    if (mActivity != null && isAdded()) {

                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("error", Id + "\n {" + Title + "} \n" + Message);
                        edit.apply();
                        Log.e("Error", Id + " {" + Title + "} " + Message);

                        Toast.makeText(mActivity, Message, Toast.LENGTH_LONG).show();
                        getActivity().finish();
                        mIntent = new Intent(getActivity(), MainActivity.class);
                        startActivity(mIntent);
                    }
                }

            } catch (NumberFormatException e) {
                batch_refresh.setVisibility(View.GONE);
                batch_success.setVisibility(View.GONE);
                batch_error.setVisibility(View.VISIBLE);
                if (mActivity != null && isAdded()) {
                    getActivity().finish();
                    mIntent = new Intent(getActivity(), MainActivity.class);
                    startActivity(mIntent);
                }
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("error", error);
                edit.apply();
                Log.i("RestApiRequest", Id + " {" + Title + "} " + Message);

            }

        }
    }

    private class CloseBatch extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            batch_refresh.setVisibility(View.VISIBLE);
            batch_success.setVisibility(View.GONE);
            batch_error.setVisibility(View.GONE);
            mActivity = getActivity();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                //SQLiteDatabase db= dbhelper.getReadableDatabase();


                Cursor mfuel = db.rawQuery("SELECT * FROM " + Database.MACHINEFUEL_TABLE_NAME + " where " + Database.MFSTATUS + "<'4' and CloudID<=0 and " + Database.MFDATE + "='" + BatchDate + "'", null);
                count = count + mfuel.getCount();
                if (mfuel.getCount() > 0) {
                    mfuel.moveToFirst();
                    while (!mfuel.isAfterLast()) {


                        RowID = mfuel.getString(mfuel.getColumnIndex(Database.ROW_ID));
                        mfDate = mfuel.getString(mfuel.getColumnIndex(Database.MFDATE));
                        mfterminalID = mfuel.getString(mfuel.getColumnIndex(Database.MFTERMINALID));
                        mfmachineNo = mfuel.getString(mfuel.getColumnIndex(Database.MFMACHINENUMBER));
                        mfTime = mfuel.getString(mfuel.getColumnIndex(Database.MFTIME));
                        mfLitres = mfuel.getString(mfuel.getColumnIndex(Database.MFLitres));
                        FuelType = "";
                        mFCompany = mfuel.getString(mfuel.getColumnIndex(Database.MFCOMPANY));
                        mFEstate = mfuel.getString(mfuel.getColumnIndex(Database.MFESTATE));

                        StringBuilder mf = new StringBuilder();
                        mf.append("0" + ",");
                        mf.append(mfDate + ",");
                        mf.append(mfterminalID + ",");
                        mf.append(mfmachineNo + ",");
                        mf.append(mfTime + ",");
                        mf.append(mfLitres + ",");
                        mf.append(FuelType + ",");
                        mf.append(mFCompany + ",");
                        mf.append(mFEstate);


                        FuelInfo = mf.toString();

                        mfuel.moveToNext();

                        restApiResponse = new RestApiRequest(getActivity()).MachineFueling(FuelInfo);
                        error = restApiResponse;
                        try {

                            JSONObject jsonObject = new JSONObject(restApiResponse);

                            Message = jsonObject.getString("Message");
                            if (Message.equals("Authorization has been denied for this request.")) {
                                Id = "-1";
                                SharedPreferences.Editor edit = mSharedPrefs.edit();
                                edit.remove("token");
                                edit.remove("expires_in");
                                edit.remove("expires");
                                edit.apply();
                                return null;
                            }

                            Id = jsonObject.getString("Id");
                            Title = jsonObject.getString("Title");


                            Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
                            try {
                                if (Integer.parseInt(Id) > 0) {
                                    ContentValues values = new ContentValues();
                                    values.put(Database.MFSTATUS, 4);
                                    values.put(Database.CloudID, Id);


                                    long rows = db.update(Database.MACHINEFUEL_TABLE_NAME, values,
                                            "_id = ? ", new String[]{RowID});

                                    if (rows > 0) {

                                    }

                                }
                                if (Integer.parseInt(Id) < 0) {

                                    error = Message;
                                }


                                //System.out.println(value);}
                            } catch (NumberFormatException e) {
                                //value = 0; // your default value


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressStatus++;
                        publishProgress("" + progressStatus);
                    }
                    mfuel.close();
                }

                Cursor batch = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where Closed =0", null);

                count = batch.getCount();
                if (batch.getCount() > 0) {
                    while (batch.moveToNext()) {
                        totalWeight = batch.getString(batch.getColumnIndex(Database.TotalWeights));
                        BatchCloudID = batch.getString(batch.getColumnIndex(Database.BatCloudID));
                        if (BatchCloudID.equals("0")) {
                            BatchCloudID = prefs.getString("serverBatchNo", "");
                        } else {

                            BatchCloudID = batch.getString(batch.getColumnIndex(Database.BatCloudID));
                        }
                        deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);

                        deliveryNoteNo = batch.getString(batch.getColumnIndex(Database.DeliveryNoteNumber));

                        stringCloseTime = batch.getString(batch.getColumnIndex(Database.ClosingTime));


                      /*  Cursor moperators = db.rawQuery("SELECT * FROM " + Database.MACHINEOP_TABLE_NAME + ","+ Database.EM_PRODUCE_COLLECTION_TABLE_NAME +" where " + Database.MSTATUS + "<'4' and machineNo=EmployeeProduceCollection.EmployeeNo and "
                                + Database.MDATE + "='" + BatchDate + "' and "+ Database.DataCaptureDevice + " ='" + deliveryNoteNo + "' group by machineoperators.employeeNo", null);*/
                        Cursor moperators = db.rawQuery("SELECT * FROM " + Database.MACHINEOP_TABLE_NAME + " where " + Database.MDATE + "='" + BatchDate + "'", null);
                        count = count + moperators.getCount();
                        if (moperators.getCount() > 0) {
                            moperators.moveToFirst();
                            while (!moperators.isAfterLast()) {


                                RowID = moperators.getString(moperators.getColumnIndex(Database.ROW_ID));
                                sDate = moperators.getString(moperators.getColumnIndex(Database.MDATE));
                                terminalID = moperators.getString(moperators.getColumnIndex(Database.TERMINALID));
                                machineNo = moperators.getString(moperators.getColumnIndex(Database.MACHINENUMBER));
                                employeeNo = moperators.getString(moperators.getColumnIndex(Database.EMPLOYEENUMBER));
                                checkinTime = moperators.getString(moperators.getColumnIndex(Database.CHECKINTIME));
                                checkinWeighment = moperators.getString(moperators.getColumnIndex(Database.CHECKINWEIGHMENT));


                                if (moperators.getString(moperators.getColumnIndex(Database.CHECKOUTTIME)) == null) {

                                    checkoutTime = "";
                                } else {
                                    checkoutTime = moperators.getString(moperators.getColumnIndex(Database.CHECKOUTTIME));

                                }

                                checkinWeighment = moperators.getString(moperators.getColumnIndex(Database.CHECKINWEIGHMENT));

                                if (moperators.getString(moperators.getColumnIndex(Database.CHECKOUTWEIGHMENT)) == null) {

                                    checkoutWeighment = "0";
                                } else {
                                    checkoutWeighment = moperators.getString(moperators.getColumnIndex(Database.CHECKOUTWEIGHMENT));

                                }

                                if (moperators.getString(moperators.getColumnIndex(Database.MTASKCODE)) == null) {

                                    mTaskCode = "";
                                } else {
                                    mTaskCode = moperators.getString(moperators.getColumnIndex(Database.MTASKCODE));
                                }

                                operator_share = "0";
                                mCompany = moperators.getString(moperators.getColumnIndex(Database.MCOMPANY));
                                mEstate = moperators.getString(moperators.getColumnIndex(Database.MESTATE));

                                StringBuilder mop = new StringBuilder();
                                mop.append("0" + ",");
                                mop.append(sDate + ",");
                                mop.append(terminalID + ",");
                                mop.append(machineNo + ",");
                                mop.append(employeeNo + ",");
                                mop.append(checkinTime + ",");
                                mop.append(checkinWeighment + ",");
                                mop.append(checkoutWeighment + ",");
                                mop.append(checkoutTime + ",");
                                mop.append(mTaskCode + ",");
                                mop.append(operator_share + ",");
                                mop.append(mCompany + ",");
                                mop.append(mEstate);

                                OperatorInfo = mop.toString();

                                moperators.moveToNext();

                                restApiResponse = new RestApiRequest(getActivity()).MachineOperator(OperatorInfo);
                                error = restApiResponse;
                                if (error.equals("-8080")) {
                                    return null;
                                }
                                try {

                                    JSONObject jsonObject = new JSONObject(restApiResponse);

                                    Message = jsonObject.getString("Message");
                                    if (Message.equals("Authorization has been denied for this request.")) {
                                        Id = "-1";
                                        SharedPreferences.Editor edit = mSharedPrefs.edit();
                                        edit.remove("token");
                                        edit.remove("expires_in");
                                        edit.remove("expires");
                                        edit.apply();
                                        return null;
                                    }

                                    Id = jsonObject.getString("Id");
                                    Title = jsonObject.getString("Title");


                                    Log.i("Operators", "ID: " + Id + " Title" + Title + " Message" + Message);
                                    try {
                                        if (Integer.parseInt(Id) > 0) {
                                            ContentValues values = new ContentValues();
                                            values.put(Database.MSTATUS, 4);
                                            values.put(Database.CloudID, Id);

                                            long rows = db.update(Database.MACHINEOP_TABLE_NAME, values,
                                                    "_id = ? ", new String[]{RowID});

                                            if (rows > 0) {

                                            }

                                        }
                                        if (Integer.parseInt(Id) < 0) {

                                            if (Id.equals("-8080")) {
                                                return null;
                                            }

                                            error = Message;
                                        }


                                        //System.out.println(value);}
                                    } catch (NumberFormatException e) {
                                        //value = 0; // your default value


                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                progressStatus++;
                                publishProgress("" + progressStatus);
                            }
                            moperators.close();
                        }




                        restApiResponse = new RestApiRequest(getActivity()).CloseOutgrowersPurchasesBatch(Integer.parseInt(BatchCloudID), stringCloseTime, totalWeight);
                        error = restApiResponse;

                        JSONObject jsonObject = new JSONObject(restApiResponse);
                        Message = jsonObject.getString("Message");
                        if (Message.equals("Authorization has been denied for this request.")) {
                            Id = "-1";
                            SharedPreferences.Editor edit = mSharedPrefs.edit();
                            edit.remove("token");
                            edit.remove("expires_in");
                            edit.remove("expires");
                            edit.apply();
                            return null;
                        }
                        if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                            Id = jsonObject.getString("Id");
                            Title = jsonObject.getString("Title");


                            Log.i("INFO", "ID: " + Id + " Title " + Title + " Message" + Message);
                            try {

                                if (Integer.parseInt(Id) < 0) {
                                    if (Integer.parseInt(Id) == -3411) {
                                        errorNo = "-3411";

                                    } else if (Integer.parseInt(Id) == -3) {

                                    } else {
                                        error = Id;

                                        return null;

                                    }
                                }
                                //System.out.println(value);}
                            } catch (NumberFormatException e) {
                                //value = 0; // your default value
                                return null;

                            }
                        } else {
                            Id = "-1";
                            Title = "";
                            Message = restApiResponse;
                            return null;

                        }

                        progressStatus++;
                        publishProgress("" + progressStatus);


                    }
                    batch.close();


                    // Cursor machines = db.rawQuery("select * from " + Database.MACHINE_TABLE_NAME + "," + Database.MACHINEOP_TABLE_NAME + "" +
                    //   " where " + Database.MC_ID + "=" + Database.MACHINENUMBER + " and " + Database.MDATE + "='" + BatchDate + "' and " + Database.MSTATUS + "='4' group by machineNo", null);
                    Cursor machines = db.rawQuery("select * from " + Database.MACHINE_TABLE_NAME + "," + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + "" +
                            " where EmployeeProduceCollection.EmployeeNo=mcID and " + Database.CollDate + "='" + BatchDate + "' and " + Database.DataCaptureDevice + " ='" + deliveryNoteNo + "' group by mcID", null);
                    count = count + machines.getCount();
                    if (machines.getCount() > 0) {
                        machines.moveToFirst();
                        while (!machines.isAfterLast()) {


                            sDate = machines.getString(machines.getColumnIndex(Database.CollDate));
                            machineNo = machines.getString(machines.getColumnIndex(Database.MC_ID));
                            // mCompany = machines.getString(machines.getColumnIndex(Database.MCOMPANY));
                            mEstate = machines.getString(machines.getColumnIndex(Database.SourceEstate));

                            machines.moveToNext();

                            restApiResponse = new RestApiRequest(getActivity()).Allocatekilos(sDate, mEstate, machineNo);
                            error = restApiResponse;
                            try {

                                JSONObject jsonObject = new JSONObject(restApiResponse);

                                Message = jsonObject.getString("Message");
                                if (Message.equals("Authorization has been denied for this request.")) {
                                    Id = "-1";
                                    SharedPreferences.Editor edit = mSharedPrefs.edit();
                                    edit.remove("token");
                                    edit.remove("expires_in");
                                    edit.remove("expires");
                                    edit.apply();
                                }

                                Id = jsonObject.getString("Id");
                                Title = jsonObject.getString("Title");


                                Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
                                try {
                                    if (Integer.parseInt(Id) > 0) {


                                    }
                                    if (Integer.parseInt(Id) < 0) {

                                        error = Message;
                                    }


                                    //System.out.println(value);}
                                } catch (NumberFormatException e) {
                                    //value = 0; // your default value


                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            progressStatus++;
                            publishProgress("" + progressStatus);
                        }
                        machines.close();
                    }
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


            batch_refresh.setVisibility(View.VISIBLE);
            batch_success.setVisibility(View.GONE);
            batch_error.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String result) {
            if (error.equals("-8080")) {

                batch_refresh.setVisibility(View.GONE);
                batch_success.setVisibility(View.GONE);
                batch_error.setVisibility(View.VISIBLE);

                Context context = getActivity();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Server Not Available !!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(mActivity, "Server Not Available !!", Toast.LENGTH_LONG).show();
                // Log.i(TAG, "Server Not Available !!");
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("error", "Server Not Available !!");
                edit.apply();
                return;
            }
            try {

                if (Integer.parseInt(Id) > 0) {
                    returnValue = restApiResponse;
                    ContentValues values = new ContentValues();
                    //values.put(Database.BatCloudID, serverBatchNo);
                    values.put(Database.Closed, 1);
                    long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                            Database.DeliveryNoteNumber + " = ?", new String[]{deliveryNoteNo});

                    if (rows > 0) {
                        batch_refresh.setVisibility(View.GONE);
                        batch_success.setVisibility(View.VISIBLE);
                        batch_error.setVisibility(View.GONE);
                        if (mActivity != null && isAdded()) {
                            // mFragmentTransaction = getFragmentManager().beginTransaction();
                            //mFragmentTransaction.detach(HomePageFragment.this).attach(HomePageFragment.this).commit();
                            btnBatchOn.setVisibility(View.VISIBLE);
                            btnBatchOff.setVisibility(View.GONE);
                            getActivity().finish();
                            mIntent = new Intent(getActivity(), MainActivity.class);
                            startActivity(mIntent);
                        }
                    }
                    //ShowCloseBatch();
                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
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
                    Toast.makeText(mActivity, "Batch Closed Successfully!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (Integer.parseInt(Id) < 0) {
                    final DecimalFormat df = new DecimalFormat("#0.0#");
                    final DecimalFormat df1 = new DecimalFormat("##");
                    Cursor weighments = db.rawQuery("select " +
                            "" + Database.DataCaptureDevice +
                            ",COUNT(" + Database.ROW_ID + ")" +
                            ",SUM(" + Database.Tareweight + ")" +
                            ",SUM(" + Database.NetWeight + ")" +
                            " from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                            + Database.DataCaptureDevice + " ='" + deliveryNoteNo + "' and " + Database.CloudID + " <=0", null);

                    if (weighments.getCount() > 0) {
                        weighments.moveToFirst();

                        batch_refresh.setVisibility(View.GONE);
                        batch_success.setVisibility(View.GONE);
                        batch_error.setVisibility(View.VISIBLE);

                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("error", Message);
                        edit.apply();

                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText(Message + "\nNot uploaded: " + df1.format(weighments.getDouble(1)) + "\n" +
                                "Un-Uploaded Weight: " + df.format(weighments.getDouble(3)) + " Kgs.");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        // Toast.makeText(mActivity, Message, Toast.LENGTH_LONG).show();
                            online = 1;
                            count = 0;
                            syncTasks();

                        //

//                        if(mActivity != null && isAdded()){
//
//                            mFragmentTransaction = getFragmentManager().beginTransaction();
//                            mFragmentTransaction.detach(HomePageFragment.this).attach(HomePageFragment.this).commit();
//                        }
                        return;
                    } else {

                        batch_refresh.setVisibility(View.GONE);
                        batch_success.setVisibility(View.GONE);
                        batch_error.setVisibility(View.VISIBLE);

                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("error", Message);
                        edit.apply();
                        Toast.makeText(mActivity, Message, Toast.LENGTH_LONG).show();
                        if (mActivity != null && isAdded()) {

                            getActivity().finish();
                            mIntent = new Intent(getActivity(), MainActivity.class);
                            startActivity(mIntent);
                        }
                    }

                    return;
                }

            } catch (NumberFormatException e) {

                batch_refresh.setVisibility(View.GONE);
                batch_success.setVisibility(View.GONE);
                batch_error.setVisibility(View.VISIBLE);

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("error", Message);
                edit.apply();
                Toast.makeText(mActivity, Message, Toast.LENGTH_LONG).show();

                if (mActivity != null && isAdded()) {

                    getActivity().finish();
                    mIntent = new Intent(getActivity(), MainActivity.class);
                    startActivity(mIntent);
                }
                return;

            }
        }
    }

    private class DeleteBatch extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            batch_refresh.setVisibility(View.VISIBLE);
            batch_success.setVisibility(View.GONE);
            batch_error.setVisibility(View.GONE);
            mActivity = getActivity();
        }

        @Override
        protected String doInBackground(String... params) {


            restApiResponse = new RestApiRequest(getActivity()).DeletePurchasesBatch(Integer.parseInt(status));
            error = restApiResponse;
            try {

                JSONObject jsonObject = new JSONObject(restApiResponse);
                Message = jsonObject.getString("Message");
                if (Message.equals("Authorization has been denied for this request.")) {
                    Id = "-1";
                    SharedPreferences.Editor edit = mSharedPrefs.edit();
                    edit.remove("token");
                    edit.remove("expires_in");
                    edit.remove("expires");
                    edit.apply();
                    return null;
                }
                if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                    Id = jsonObject.getString("Id");
                    Title = jsonObject.getString("Title");


                    Log.i("INFO", Id + "" + Title + "" + Message);
                    try {
                        if (Integer.parseInt(Id) < 0) {
                            error = Id;
                            return null;
                        }
                        //System.out.println(value);}


                    } catch (NumberFormatException e) {
                        //value = 0; // your default value
                        return null;

                    }
                } else {
                    Id = "-1";
                    Title = "";
                    Message = restApiResponse;
                    return null;

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            Log.i(TAG, "onProgressUpdate");


            batch_refresh.setVisibility(View.VISIBLE);
            batch_success.setVisibility(View.GONE);
            batch_error.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String result) {
            if (error.equals("-8080")) {


                batch_refresh.setVisibility(View.GONE);
                batch_success.setVisibility(View.GONE);
                batch_error.setVisibility(View.VISIBLE);

                Context context = getActivity();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Server Not Available !!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                batch_error.setVisibility(View.VISIBLE);
                //Toast.makeText(mActivity, "Server Not Available !!", Toast.LENGTH_LONG).show();
                // Log.i(TAG, "Server Not Available !!");
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("error", "Server Not Available !!");
                edit.apply();
                if (mActivity != null && isAdded()) {

                    getActivity().finish();
                    mIntent = new Intent(getActivity(), MainActivity.class);
                    startActivity(mIntent);
                }
                return;
            }
            try {

                if (Integer.parseInt(Id) > 0) {

                    batch_refresh.setVisibility(View.GONE);
                    batch_success.setVisibility(View.VISIBLE);
                    batch_error.setVisibility(View.GONE);
                    deleteCurrentAccount();
                    return;
                }

                if (Integer.parseInt(Id) < 0) {
                    batch_refresh.setVisibility(View.GONE);
                    batch_success.setVisibility(View.GONE);
                    batch_error.setVisibility(View.VISIBLE);

                    Toast.makeText(mActivity, Message, Toast.LENGTH_LONG).show();
                    if (mActivity != null && isAdded()) {

                        getActivity().finish();
                        mIntent = new Intent(getActivity(), MainActivity.class);
                        startActivity(mIntent);
                    }
                    return;
                }
            } catch (NumberFormatException e) {


                batch_refresh.setVisibility(View.GONE);
                batch_success.setVisibility(View.GONE);
                batch_error.setVisibility(View.VISIBLE);
                if (mActivity != null && isAdded()) {

                    getActivity().finish();
                    mIntent = new Intent(getActivity(), MainActivity.class);
                    startActivity(mIntent);
                }


                // Toast.makeText(mActivity, Message, Toast.LENGTH_LONG).show();


                return;

            }
        }
    }

    public class WeighmentsToCloud extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            mActivity = getActivity();

            if (online == 1) {
                progressDialog = ProgressDialog.show(mActivity,
                        "Uploading Data",
                        "Please Wait.. ");
                progressDialog.setCancelable(false);
                Toast.makeText(mActivity, "Some data not uploaded.\nUploading all data ...", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(getActivity(),"Uploading Data\nPlease Wait.. ",Toast.LENGTH_LONG).show();
            }
        }

        @SuppressLint("Range")
        @Override
        protected String doInBackground(String... aurl) {
            Log.i(TAG, "doInBackground");
            try {
                db = dbhelper.getReadableDatabase();
                serverBatchNo = prefs.getString("serverBatchNo", "");
                BatchSerial = prefs.getString("DeliverNoteNumber", "");
                Log.i("INFO", "BatchSerial: " + BatchSerial + " serverBatchNo " + serverBatchNo);
                Cursor produce = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                        + Database.DataCaptureDevice + " ='" + BatchSerial + "' and " + Database.CloudID + " <='" + cloudid + "'", null);
                while (produce.moveToNext()) {
                    ColDate = produce.getString(produce.getColumnIndex(Database.CollDate));
                    Time = produce.getString(produce.getColumnIndex(Database.CaptureTime));
                    BatchNumber = produce.getString(produce.getColumnIndex(Database.BatchNo));
                    DataDevice = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
                    TaskCode = produce.getString(produce.getColumnIndex(Database.TaskCode));
                    EmployeeNo = produce.getString(produce.getColumnIndex(Database.EmployeeNo));
                    ProduceCode = produce.getString(produce.getColumnIndex(Database.DeliveredProduce));
                    if (produce.getString(produce.getColumnIndex(Database.ProduceVariety)) == null) {
                        VarietyCode = "";
                    } else {
                        VarietyCode = produce.getString(produce.getColumnIndex(Database.ProduceVariety));
                    }
                    if (produce.getString(produce.getColumnIndex(Database.ProduceGrade)) == null) {
                        GradeCode = "";
                    } else {

                        GradeCode = produce.getString(produce.getColumnIndex(Database.ProduceGrade));
                    }
                    EstateCode = produce.getString(produce.getColumnIndex(Database.SourceEstate));
                    DivisionCode = produce.getString(produce.getColumnIndex(Database.SourceDivision));
                    if (produce.getString(produce.getColumnIndex(Database.SourceField)) == null ||
                            produce.getString(produce.getColumnIndex(Database.SourceField)).equals("Select ...") ||
                            produce.getString(produce.getColumnIndex(Database.SourceField)).equals("")) {

                        FieldCode = "";

                    } else {
                        FieldCode = produce.getString(produce.getColumnIndex(Database.SourceField));
                    }

                    if (produce.getString(produce.getColumnIndex(Database.SourceBlock)) == null ||
                            produce.getString(produce.getColumnIndex(Database.SourceBlock)).equals("Select ...")) {
                        Block = "";
                    } else {
                        Block = produce.getString(produce.getColumnIndex(Database.SourceBlock));

                    }
                    NetWeight = produce.getString(produce.getColumnIndex(Database.NetWeight));
                    TareWeight = produce.getString(produce.getColumnIndex(Database.Tareweight));

                    if (produce.getString(produce.getColumnIndex(Database.BagCount)) == null) {
                        Crates = "1";

                    } else {
                        Crates = produce.getString(produce.getColumnIndex(Database.BagCount));
                    }

                    UnitPrice = produce.getString(produce.getColumnIndex(Database.UnitPrice));
                    WeighmentNo = produce.getString(produce.getColumnIndex(Database.LoadCount));
                    RecieptNo = produce.getString(produce.getColumnIndex(Database.DataCaptureDevice)) + produce.getString(produce.getColumnIndex(Database.ReceiptNo));
                    SessionNo = produce.getString(produce.getColumnIndex(Database.ReceiptNo));
                    FieldClerk = produce.getString(produce.getColumnIndex(Database.FieldClerk));
                    CheckinMethod = produce.getString(produce.getColumnIndex(Database.UsedSmartCard));

                    Co_prefix = mSharedPrefs.getString("company_prefix", "");
                    Current_User = prefs.getString("user", "");
                    TaskType = produce.getString(produce.getColumnIndex(Database.TaskType));


                    StringBuilder wm = new StringBuilder();
                    wm.append(TaskType + ",");
                    wm.append(ColDate + ",");
                    wm.append(DataDevice + ",");
                    wm.append(Time + ",");
                    wm.append(FieldClerk + ",");
                    wm.append(ProduceCode + ",");
                    wm.append(EstateCode + ",");
                    wm.append(DivisionCode + ",");
                    wm.append(FieldCode + ",");
                    wm.append(Block + ",");
                    wm.append(TaskCode + ",");
                    wm.append(EmployeeNo + ",");
                    wm.append(NetWeight + ",");
                    wm.append(TareWeight + ",");
                    wm.append(Crates + ",");
                    wm.append(RecieptNo + ",");
                    wm.append(BatchNumber + ",");
                    wm.append(WeighmentNo + ",");
                    wm.append(VarietyCode + ",");
                    wm.append(GradeCode + ",");
                    wm.append(Co_prefix + ",");
                    wm.append(Current_User + ",");
                    wm.append(CheckinMethod + ",");
                    wm.append("3");

                    weighmentInfo = wm.toString();

                    try {

                        restApiResponse = new RestApiRequest(getActivity()).VerifyRecord(serverBatchNo, weighmentInfo);

                        if (restApiResponse.equals("-8080")) {
                            Id = "-8080";
                            Title = "";
                            error = restApiResponse;
                            Message = restApiResponse;
                            return null;
                        }

                        JSONObject jsonObject = new JSONObject(restApiResponse);

                        Id = jsonObject.getString("Id");
                        Title = jsonObject.getString("Title");
                        Message = jsonObject.getString("Message");

                        Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);

                        if (Integer.parseInt(Id) > 0) {
                            Cursor checkcloudid = dbhelper.CheckWeighmentCloudID(Id);
                            //Check for duplicate checkcloudid number
                            if (checkcloudid.getCount() > 0) {
                                // Toast.makeText(getApplicationContext(), "checkcloudid already exists",Toast.LENGTH_SHORT).show();

                            } else {
                                ContentValues values = new ContentValues();
                                values.put(Database.CloudID, Id);
                                long rows = db.update(Database.EM_PRODUCE_COLLECTION_TABLE_NAME, values,
                                        Database.EmployeeNo + " = ? AND " + Database.LoadCount + " = ? AND " + Database.DataCaptureDevice + " = ? AND "
                                                + Database.ReceiptNo + " = ?", new String[]{EmployeeNo, WeighmentNo, BatchSerial, SessionNo});

                                if (rows > 0) {
                                    Log.i("success:", Id);

                                }
                            }

                        }
                        if (Integer.parseInt(Id) < 0) {

                            if (Integer.parseInt(Id) != -17) {
                                return null;
                            }
                        }


                    } catch (NumberFormatException | JSONException e) {
                        Id = "-8080";
                        Title = "";
                        error = restApiResponse;
                        Message = restApiResponse;
                        e.printStackTrace();
                        returnValue = e.toString();
                        Log.i("Catch Exc:", returnValue);
                    }

                    progressStatus++;
                    publishProgress("" + progressStatus);

                }
                //Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();


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
            if (online == 1) {
                progressDialog.setProgress(Integer.parseInt(progress[0]));
                progressDialog.setMax(count);
                progressDialog.setMessage("Uploading... " + Integer.parseInt(progress[0]) + "/" + count + " Records\n" + Message);
            }
        }

        @Override
        protected void onPostExecute(String unused) {
            db = dbhelper.getReadableDatabase();
            if (error.equals("-8080")) {

                if (online == 1) {
                    Context context = mActivity;
                    LayoutInflater inflater = mActivity.getLayoutInflater();
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
                    edit.apply();


                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    closeBatchOffline();
                }
                return;
            }
            try {

                //  Toast.makeText(getBaseContext(), "SMS not enabled on Settings", Toast.LENGTH_LONG).show();
                if (Integer.parseInt(Id) > 0) {

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("success", "Uploaded Successfully !!!");
                    edit.apply();

                    if (online == 1) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        FinishDispatch();
                        Toast.makeText(getActivity(), "Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
                    }
                    return;
                } else if (Integer.parseInt(Id) < 0) {
                    if (online == 1) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        closeBatchOffline();
                        Toast.makeText(mActivity, Id + " " + Message, Toast.LENGTH_LONG).show();

                    }
                    return;
                }


            } catch (NumberFormatException e) {

                if (online == 1) {
                    progressDialog.dismiss();
                    //Toast.makeText(mActivity, e.toString(), Toast.LENGTH_LONG).show();
                }
                return;

            }
        }
    }
}
