package com.plantation.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.plantation.R;
import com.plantation.activities.BatchRecieptsActivity;
import com.plantation.activities.DeliveryEditActivity;
import com.plantation.activities.ExportActivity;
import com.plantation.activities.PerformanceReportActivity;
import com.plantation.activities.UploadActivity;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.helpers.CustomList;
import com.plantation.helpers.Delivary;
import com.plantation.services.EasyWeighService;
import com.plantation.synctocloud.RestApiRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DelivaryFragment extends Fragment {
    static SharedPreferences mSharedPrefs, prefs;
    private final String TAG = "Vik";
    public View mView;
    public Intent mIntent;
    public Context mContext;
    Button btnDispatch, btnPrint, btnComplete;
    DBHelper dbhelper;
    SQLiteDatabase db;
    TextView txtUndelivered;
    Button btnCloseBatch;
    String Factory;
    String TransporterCode;
    String strTractor;
    String strTrailer;
    String DelivaryNo;
    Spinner spinnerFactory;
    String Driver, TurnMan;
    EditText etDeliveryNo;
    EditText etTractor, etVehicle, etDriver, etTurnMan;
    DecimalFormat formatter;
    String BatchNumber, BatchDate;
    String factorys;
    String factoryid = null;
    ArrayList<String> factorydata = new ArrayList<String>();
    ArrayAdapter<String> factoryadapter;
    String transporters;
    String transporterid = null;
    ArrayList<String> transporterdata = new ArrayList<String>();
    ArrayAdapter<String> transporteradapter;
    Spinner mc_ctransporter;
    AlertDialog b;
    EasyWeighService resetConn;
    ListView listReciepts;
    int CLOSED = 1;
    int SIGNEDOFF = 0;
    ListView list;
    TextView textDeliNo, textBatchCrates, textWeightments, textNetWeight, textTractor, textTrailer;
    String[] web = {
            "Batch Receipts", "Performance Report", "Upload", "Export Data", "View Deliveries"
    };
    Integer[] imageId = {

            R.drawable.ic_batch,
            R.drawable.ic_preport,
            R.drawable.ic_upload,
            R.drawable.ic_export,
            R.drawable.ic_deliveries
    };
    String DDate, Transporter, Vehicle, ArrivalTime, FieldWt, DepartureTime;
    Cursor accounts;
    DeliveryArrayAdapter ArrayAdapter;
    AlertDialog IncompleteDel;
    int accesslevel = 0;
    String user_level;
    TextView textLcCrates, textLcTotal, textExCrates, textExTotal;
    int dispatchid = 0;
    String deliveryNoteNo, DelDate, EstateCode, CoPrefix, InternalSerial, UserIdentifier;
    String DeliveryNo, deviceID;
    String DeliNo;
    int dcount = 1;
    String DeliveryInfo;
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat timeFormat;
    SimpleDateFormat dateFormat;
    SimpleDateFormat dateOnlyFormat;
    SimpleDateFormat BatchDateFormat;
    String returnValue;
    String error;
    Button btnDeliver;

    private CircleProgress circle_progress;
    private DonutProgress donutProgress;
    private int progress = 0;

    private int progressStatus = 0;
    private int count = 0;
    String Id, Title, Message;
    private Activity mActivity;
    private String restApiResponse;

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_deliver, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        initializer();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setShowHideAnimationEnabled(true);

        return mView;
    }

    public void initializer() {
        mActivity = getActivity();
        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
        resetConn = new EasyWeighService();
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        BatchDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        btnDispatch = mView.findViewById(R.id.btnDispatch);
        btnPrint = mView.findViewById(R.id.btnPrint);
        btnComplete = mView.findViewById(R.id.btnComplete);
        circle_progress = mView.findViewById(R.id.circle_progress);
        donutProgress = mView.findViewById(R.id.donut_progress);
        txtUndelivered = mView.findViewById(R.id.txtUndelivered);
        txtUndelivered.setText("All Batches and Deliveries Completed");

        CustomList adapter = new
                CustomList(getActivity(), web, imageId);
        list = mView.findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent, view, position, id) -> {


            switch (position) {
                case 0:
                    mIntent = new Intent(getActivity(), BatchRecieptsActivity.class);
                    startActivity(mIntent);

                    break;
                case 1:
                    mIntent = new Intent(getActivity(), PerformanceReportActivity.class);
                    startActivity(mIntent);

                    break;
                case 2:
                    if (checkList()) {
                        return;
                    }
                    if (isInternetOn()) {
                        createNetErrorDialog();
                        return;
                    }

                    mIntent = new Intent(getActivity(), UploadActivity.class);
                    startActivity(mIntent);
                    break;
                case 3:
                    mIntent = new Intent(getActivity(), ExportActivity.class);
                    startActivity(mIntent);

                    break;

                case 4:
                    String username = prefs.getString("user", "");
                    Cursor d = dbhelper.getAccessLevel(username);
                    user_level = d.getString(accesslevel);
                    if (user_level.equals("2")) {
                        Toast.makeText(getActivity(), "Please Contact Administrator To View Deliveries!", Toast.LENGTH_LONG).show();
                    } else {
                        mIntent = new Intent(getActivity(), DeliveryEditActivity.class);
                        startActivity(mIntent);
                        break;
                    }


                default:
                    break;
            }


        });

        btnDispatch.setOnClickListener(v -> {

            String CLOSED = "1";
            String SIGNEDOFF = "0";
            Cursor count = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                    + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

            if (count.getCount() == 0) {

                Context context = getActivity();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Sorry! No Batches To Deliver!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                return;
            }

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_list_incomplete_delivery, null);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setCancelable(true);
            dialogBuilder.setTitle("Batch List");

            listReciepts = dialogView.findViewById(R.id.lvReciepts);
            btnDeliver = dialogView.findViewById(R.id.btnDeliver);
            btnDeliver.setOnClickListener(view -> {
                dispatchid = 2;
                DispatchBatch();
            });
            getdata();
            dialogBuilder.setPositiveButton("Cancel", (dialog, whichButton) -> {
                //do something with edt.getText().toString();

            });

            dialogBuilder.setOnKeyListener((dialog, keyCode, event) -> {
                //Toast.makeText(getActivity(), "Please Close Batch", Toast.LENGTH_LONG).show();
                return keyCode == KeyEvent.KEYCODE_BACK;
            });

            IncompleteDel = dialogBuilder.create();
            IncompleteDel.show();


        });

        Cursor batches = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

        if (batches.getCount() > 0) {

            btnDispatch.setVisibility(View.VISIBLE);
            btnPrint.setVisibility(View.GONE);
            btnComplete.setVisibility(View.GONE);
            return;

        }
        String selectQuery = "SELECT * FROM " + Database.Fmr_FactoryDeliveries + " WHERE FdStatus=0";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {

            //btnDispatch.setVisibility(View.GONE);
            // btnPrint.setVisibility(View.VISIBLE);
            //btnComplete.setVisibility(View.VISIBLE);
            btnPrint.setVisibility(View.GONE);
            btnComplete.setVisibility(View.GONE);
        }

    }


    public void DispatchBatch() {

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

        btnCloseBatch = dialogView.findViewById(R.id.btnCloseBatch);
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

                        String CLOSED = "1";
                        String SIGNEDOFF = "0";
                        Cursor count = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                                + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

                        if (count.getCount() > 0) {

                            try {
                                count.moveToFirst();
                                Date openTime = dateTimeFormat.parse(count.getString(count.getColumnIndex(Database.BatchDate)) + " 00:00:00");
                                DelDate = dateFormat.format(openTime);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            count.close();

                            Factory = factoryid;
                            TransporterCode = transporterid;
                            strTrailer = etVehicle.getText().toString();
                            strTrailer = strTrailer.replace(",", "");
                            strTractor = etTractor.getText().toString();
                            strTractor = strTractor.replace(",", "");


                            DelivaryNo = etDeliveryNo.getText().toString();
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putString("DelivaryNo", DelivaryNo);
                            edit.putString("dcount", DeliNo);
                            edit.apply();

                            Transporter = transporterid;
                            Vehicle = etVehicle.getText().toString();
                            Vehicle = Vehicle.replace(",", "");
                            strTractor = etTractor.getText().toString();
                            strTractor = strTractor.replace(",", "");
                            Driver = etDriver.getText().toString();
                            TurnMan = etTurnMan.getText().toString();
                            EstateCode = prefs.getString("estateCode", "");

                            Date date = new Date(getDate());
                            Calendar cal = Calendar.getInstance();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
                            DDate = format.format(date);
                            ArrivalTime = DDate + " " + format2.format(cal.getTime());


                            final DecimalFormat df = new DecimalFormat("#0.0#");
                            Cursor c = db.rawQuery("select " +
                                    "" + Database.DataDevice +
                                    ",COUNT(" + Database.ROW_ID + ")" +
                                    ",SUM(" + Database.TotalWeights + ")" +
                                    " from FarmersSuppliesConsignments WHERE "
                                    + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);
                            if (c != null) {

                                c.moveToFirst();
                                FieldWt = df.format(c.getDouble(2));

                            }
                            c.close();

                            ContentValues values = new ContentValues();
                            if (!mSharedPrefs.getBoolean("realtimeServices", false)) {
                                values.put(Database.SignedOff, 1);
                            }

                            values.put(Database.DelivaryNO, DelivaryNo);
                            values.put(Database.Factory, Factory);
                            values.put(Database.Transporter, TransporterCode);
                            values.put(Database.Trailer, strTrailer);
                            values.put(Database.Tractor, strTractor);


                            long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                                    Database.Closed + " = ? and " + Database.SignedOff + " = ?", new String[]{CLOSED, SIGNEDOFF});


                            if (rows > 0) {
                                if (!mSharedPrefs.getBoolean("realtimeServices", false)) {
                                    dbhelper.AddDelivery(EstateCode, DelivaryNo, DDate, Factory, TransporterCode, strTrailer, strTractor, Driver, TurnMan, ArrivalTime, FieldWt);
                                    //Toast.makeText(getBaseContext(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();
                                    IncompleteDel.dismiss();
                                    b.dismiss();
                                    donutProgress.setVisibility(View.VISIBLE);
                                    circle_progress.setVisibility(View.GONE);
                                    new CountDownTimer(1000, 100) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            //this will be done every 1000 milliseconds ( 1 seconds )
                                            progress = (int) ((1000 - millisUntilFinished) / 5);
                                            donutProgress.setProgress(progress);
                                            txtUndelivered.setText("Delivering Batches ...");

                                        }

                                        @Override
                                        public void onFinish() {
                                            //the progressBar will be invisible after 60 000 miliseconds ( 1 minute)
                                            // pd.dismiss();
                                            donutProgress.setVisibility(View.GONE);
                                            circle_progress.setVisibility(View.VISIBLE);

                                            circle_progress.setProgress(0);
                                            circle_progress.setPrefixText("N");
                                            circle_progress.setSuffixText(" Batch");
                                            txtUndelivered.setText("All batches delivered");

                                            Date date = new Date(getDate());
                                            Calendar cal = Calendar.getInstance();
                                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                            SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                                            DDate = format.format(date);
                                            DepartureTime = DDate + " " + format2.format(cal.getTime());
                                            ContentValues values = new ContentValues();
                                            values.put(Database.FdWeighbridgeTicket, "");
                                            values.put(Database.FdGrossWt, FieldWt);
                                            values.put(Database.FdTareWt, "0.0");
                                            values.put(Database.FdDepartureTime, DepartureTime);
                                            values.put(Database.FdStatus, 1);


                                            long rows = db.update(Database.Fmr_FactoryDeliveries, values,
                                                    Database.FdDNoteNum + " = ?", new String[]{DelivaryNo});

                                            if (rows > 0) {
                                                btnDispatch.setVisibility(View.VISIBLE);
                                                showRecieptDetails();
                                                getbatches();
                                                Toast.makeText(getActivity(), "Delivered Successfully !!", Toast.LENGTH_LONG).show();

                                            }
                                        }

                                    }.start();
                                } else {
                                    if (checkList()) {
                                        return;
                                    }
                                    if (isInternetOn()) {
                                        createNetErrorDialog();
                                        return;
                                    }
                                    new StartDispatch().execute();
                                }

                                //Toast.makeText(getActivity(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), "Sorry! Could not Close Batch!", Toast.LENGTH_LONG).show();
                            }
                                           /* getActivity().finish();
                                            mIntent = new Intent(getActivity(), MainActivity.class);
                                            startActivity(mIntent);*/


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

    public boolean checkList() {

        try {
            if (mSharedPrefs.getBoolean("cloudServices", false)) {
                try {
                    if (mSharedPrefs.getString("internetAccessModes", null) == null) {
                        Toast.makeText(getActivity(), "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
                        return true;

                    }


                    try {
                        if (mSharedPrefs.getString("portalURL", null) != null && !mSharedPrefs.getString("portalURL", null).equals(XmlPullParser.NO_NAMESPACE)) {
                            return false;
                        }
                        //this.checkListReturnValue = "Portal URL not configured!";
                        Toast.makeText(getActivity(), "Portal URL not configured!", Toast.LENGTH_LONG).show();
                        return false;
                    } catch (Exception e) {
                        //this.checkListReturnValue = "Portal URL not configured!";
                        Toast.makeText(getActivity(), "Portal URL not configured!", Toast.LENGTH_LONG).show();
                        return true;
                    }


                } catch (Exception e3) {
                    e3.printStackTrace();
                    //this.checkListReturnValue = "Cloud Services not enabled!";
                    Toast.makeText(getActivity(), "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
                    return true;
                }
            }
            Toast.makeText(getActivity(), "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
            return true;


            //this.checkListReturnValue = "Cloud Services not enabled!";

        } catch (Exception e4) {
            e4.printStackTrace();
            //this.checkListReturnValue = "Cloud Services not enabled!";
            Toast.makeText(getActivity(), "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
            return true;
        }

    }

    public boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {


            return false;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {


            return true;
        }
        return true;
    }

    protected void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>You need internet connection to upload data. Please turn on mobile network or Wi-Fi in Settings.</font>"))
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        getbatches();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void getbatches() {
        try {
            int CLOSED = 1;
            int SIGNEDOFF = 0;

            SQLiteDatabase db = dbhelper.getReadableDatabase();
            final Cursor accounts = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                    + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

            circle_progress.setProgress(accounts.getCount());
            circle_progress.setMax(10);
            if (accounts.getCount() == 0) {

                txtUndelivered.setText("All batches delivered");
                circle_progress.setPrefixText("N");
                circle_progress.setSuffixText(" Batch");

            } else if (accounts.getCount() == 1) {

                txtUndelivered.setText(accounts.getCount() + " batch not delivered");
                circle_progress.setSuffixText(" batch");

            } else {
                txtUndelivered.setText(accounts.getCount() + " batches not delivered");
                circle_progress.setSuffixText(" batches");

            }
            // dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void showRecieptDetails() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView;
        if (mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {
            dialogView = inflater.inflate(R.layout.activity_listdispatchedbatches_tea, null);
        } else {
            dialogView = inflater.inflate(R.layout.activity_listdispatchedbatches, null);
        }

        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Delivery Receipts");
        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
        String DNoteNum = prefs.getString("DelivaryNo", "");
        Cursor batch = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                + Database.DelivaryNO + " ='" + DNoteNum + "'", null);
        Cursor delivery = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " WHERE "
                + Database.FdDNoteNum + " ='" + DelivaryNo + "'", null);
        textBatchCrates = dialogView.findViewById(R.id.textBatchDetails);
        textLcCrates = dialogView.findViewById(R.id.textLcCrates);
        textLcTotal = dialogView.findViewById(R.id.textLcTotal);
        textExCrates = dialogView.findViewById(R.id.textExCrates);
        textExTotal = dialogView.findViewById(R.id.textExTotal);
        textDeliNo = dialogView.findViewById(R.id.textDeliNo);
        textWeightments = dialogView.findViewById(R.id.textWeightments);
        textNetWeight = dialogView.findViewById(R.id.textNetWeight);
        textTractor = dialogView.findViewById(R.id.textTractor);
        textTrailer = dialogView.findViewById(R.id.textTrailer);
        if (mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {
            if (delivery != null) {

                delivery.moveToFirst();

                textNetWeight.setText(delivery.getString(delivery.getColumnIndex(Database.FdFieldWt)));
                textDeliNo.setText(delivery.getString(delivery.getColumnIndex(Database.FdDNoteNum)));
                textTractor.setText(delivery.getString(delivery.getColumnIndex(Database.FdTractor)));
                textTrailer.setText(delivery.getString(delivery.getColumnIndex(Database.FdVehicle)));

            }

        } else {
            if (batch != null) {

                batch.moveToFirst();
                textWeightments.setText(batch.getString(batch.getColumnIndex(Database.NoOfWeighments)));
                textNetWeight.setText(batch.getString(batch.getColumnIndex(Database.TotalWeights)));
                textBatchCrates.setText(batch.getString(batch.getColumnIndex(Database.BatchCrates)));
                textDeliNo.setText(batch.getString(batch.getColumnIndex(Database.DelivaryNO)));
                textTractor.setText(batch.getString(batch.getColumnIndex(Database.Tractor)));
                textTrailer.setText(batch.getString(batch.getColumnIndex(Database.Trailer)));

                BatchNumber = batch.getString(batch.getColumnIndex(Database.BatchNumber));
                BatchDate = batch.getString(batch.getColumnIndex(Database.BatchDate));


                Cursor lc = db.rawQuery("SELECT *, SUM(NetWeight) AS NetWeight, SUM(BagCount) AS BagCount FROM " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME +
                        " WHERE " + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNumber + "'" +
                        " and " + Database.ProduceGrade + " NOT IN ('EX')", null);
                if (lc != null) {
                    if (lc.getCount() > 0) {
                        lc.moveToFirst();
                        textLcCrates.setText(lc.getString(lc.getColumnIndex(Database.BagCount)));
                        textLcTotal.setText(lc.getString(lc.getColumnIndex(Database.NetWeight)));
                    } else {
                        textLcCrates.setText("0");
                        textLcTotal.setText("0.0");
                    }
                }

                Cursor ex = db.rawQuery("SELECT *, SUM(NetWeight) AS NetWeight, SUM(BagCount) AS BagCount FROM " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME +
                        " WHERE " + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNumber + "'" +
                        " and " + Database.ProduceGrade + " ='EX'", null);
                if (ex != null) {
                    if (ex.getCount() > 0) {
                        ex.moveToFirst();
                        textExCrates.setText(ex.getString(ex.getColumnIndex(Database.BagCount)));
                        textExTotal.setText(ex.getString(ex.getColumnIndex(Database.NetWeight)));
                    } else {

                        textExCrates.setText("0");
                        textExTotal.setText("0.0");
                    }
                }

            }
            batch.close();
        }
        dialogBuilder.setPositiveButton("Close", (dialog, whichButton) -> {


        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

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

    private void FactoryList() {
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

    //This is method to call the date and not accessible outside this class
    private String getDate() {
        //Return the current date
        return DateFormat.getDateInstance().format(new Date());
    }

    public void getdata() {

        try {

            SQLiteDatabase db = dbhelper.getReadableDatabase();
            ArrayList<Delivary> arraylist = new ArrayList<Delivary>();
            int CLOSED = 1;
            int SIGNEDOFF = 0;
            accounts = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                    + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);
            if (accounts.getCount() > 0) {
                while (accounts.moveToNext()) {

                    arraylist.add(new Delivary(accounts.getString(accounts.getColumnIndex(Database.ROW_ID)), accounts.getString(accounts.getColumnIndex(Database.DeliveryNoteNumber)),
                            accounts.getString(accounts.getColumnIndex(Database.BatchCrates)),
                            accounts.getString(accounts.getColumnIndex(Database.TotalWeights))));
                }

                ArrayAdapter = new DeliveryArrayAdapter(getActivity(), R.layout.complete_delivery_list, arraylist);


                listReciepts.setAdapter(ArrayAdapter);
                ArrayAdapter.notifyDataSetChanged();
                listReciepts.setTextFilterEnabled(true);

                //db.close();
                //dbhelper.close();


            } else {


                IncompleteDel.dismiss();

                donutProgress.setVisibility(View.VISIBLE);
                circle_progress.setVisibility(View.GONE);
                new CountDownTimer(1000, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //this will be done every 1000 milliseconds ( 1 seconds )
                        progress = (int) ((1000 - millisUntilFinished) / 5);
                        donutProgress.setProgress(progress);
                        txtUndelivered.setText("Processing Deliveries ...");
                        btnDispatch.setVisibility(View.VISIBLE);
                        btnPrint.setVisibility(View.GONE);
                        btnComplete.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFinish() {
                        //the progressBar will be invisible after 60 000 miliseconds ( 1 minute)
                        // pd.dismiss();
                        donutProgress.setVisibility(View.GONE);
                        circle_progress.setVisibility(View.VISIBLE);

                        circle_progress.setProgress(0);
                        circle_progress.setPrefixText("N");
                        circle_progress.setSuffixText(" Batch");
                        txtUndelivered.setText("All Batches and Deliveries Completed");


                    }

                }.start();
                //Toast.makeText(getActivity(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();


            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class StartDispatch extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            mActivity = getActivity();
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            try {
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                DelivaryNo = prefs.getString("DelivaryNo", "");
                formatter = new DecimalFormat("0000");
                deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);

                if (Transporter == null) {
                    Transporter = "";
                } else {
                    Transporter = transporterid;
                }

                CoPrefix = mSharedPrefs.getString("company_prefix", "");
                InternalSerial = mSharedPrefs.getString("terminalID", "");
                UserIdentifier = prefs.getString("user", "");


                StringBuilder del = new StringBuilder();

                del.append(DelivaryNo + ",");
                del.append(DelDate + ",");
                del.append(Factory + ",");
                del.append(Transporter + ",");
                del.append(Vehicle + ",");
                del.append(strTractor + ",");
                del.append(DepartureTime + ",");
                del.append(CoPrefix + ",");
                del.append(EstateCode + ",");
                del.append(UserIdentifier + ",");
                del.append("1" + ",");
                del.append(Driver + ",");
                del.append(TurnMan);
                DeliveryInfo = del.toString();

                restApiResponse = new RestApiRequest(getActivity()).StartDispatch(DeliveryInfo);

                try {

                    JSONObject jsonObject = new JSONObject(restApiResponse);

                    Message = jsonObject.getString("Message");
                    if (Message.equals("Authorization has been denied for this request.")) {

                        return null;
                    }

                    Id = jsonObject.getString("Id");
                    Title = jsonObject.getString("Title");


                    Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
                    try {
                        if (Integer.parseInt(Id) > 0) {

                            DeliveryNo = Id;
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putString("DeliveryNo", DeliveryNo);
                            edit.apply();
                            Log.i("Delivery:", DeliveryNo);

                        }
                        if (Integer.parseInt(Id) < 0) {

                            DeliveryNo = Id;
                            error = Message;
                            return null;
                        }


                        //System.out.println(value);}
                    } catch (NumberFormatException e) {
                        //value = 0; // your default value
                        return null;

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SharedPreferences.Editor edit = prefs.edit();
                Cursor batch = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where " + Database.DelivaryNO + " = '" + DelivaryNo + "'", null);
                if (batch.getCount() > 0) {
                    while (batch.moveToNext()) {
                        deliveryNoteNo = batch.getString(batch.getColumnIndex(Database.DeliveryNoteNumber));
                        restApiResponse = new RestApiRequest(getActivity()).DeliverBatch(Integer.parseInt(DeliveryNo), deliveryNoteNo);
                        error = restApiResponse;
                        Log.i("DBatch Response 0 ", error);

                        try {

                            JSONObject jsonObject = new JSONObject(restApiResponse);

                            Message = jsonObject.getString("Message");
                            if (Message.equals("Authorization has been denied for this request.")) {
                                Id = "-1";
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


                                    Log.i("Delivery:", DeliveryNo);
                                    Log.i("DBatch Response 0 ", Id);
                                }
                                if (Integer.parseInt(Id) < 0) {

                                    error = Message;
                                    return null;
                                }

                                count = count + 1;
                                progressStatus++;
                                publishProgress("" + progressStatus);
                                //System.out.println(value);}
                            } catch (NumberFormatException e) {
                                //value = 0; // your default value
                                DeliveryNo = restApiResponse;
                                //return null;

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                batch.close();


                DeliveryNo = prefs.getString("DeliveryNo", "");
                restApiResponse = new RestApiRequest(getActivity()).CompleteDispatch(DeliveryNo);

                try {

                    JSONObject jsonObject = new JSONObject(restApiResponse);
                    Message = jsonObject.getString("Message");
                    if (Message.equals("Authorization has been denied for this request.")) {
                        Id = "-1";
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


                            Log.i("CompleteDispatch ID", Id);
                            Log.i("CompleteDispatch M", Message);

                        }
                        if (Integer.parseInt(Id) < 0) {

                            error = Message;
                            return null;
                        }
                        count = count + 1;
                        progressStatus++;
                        publishProgress("" + progressStatus);

                        //System.out.println(value);}
                    } catch (NumberFormatException e) {

                        return null;

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
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
        protected void onPostExecute(String result) {
            Log.i(TAG, "onPostExecute");
            try {

                if (Integer.parseInt(Id) > 0) {
                    ContentValues values = new ContentValues();
                    values.put(Database.SignedOff, 1);
                    long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                            Database.DelivaryNO + " = ?", new String[]{DelivaryNo});
                    if (rows > 0) {
                        dbhelper.AddDelivery(EstateCode, DelivaryNo, DDate, Factory, TransporterCode, strTrailer, strTractor, Driver, TurnMan, ArrivalTime, FieldWt);
                        ContentValues values1 = new ContentValues();
                        values1.put(Database.FdStatus, DeliveryNo);
                        values1.put(Database.CloudID, DeliveryNo);
                        long rows1 = db.update(Database.Fmr_FactoryDeliveries, values1,
                                Database.FdDNoteNum + " = ?", new String[]{DelivaryNo});

                        if (rows1 > 0) {
                            b.dismiss();
                            IncompleteDel.dismiss();
                            donutProgress.setVisibility(View.VISIBLE);
                            circle_progress.setVisibility(View.GONE);
                            new CountDownTimer(1000, 100) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    //this will be done every 1000 milliseconds ( 1 seconds )
                                    progress = (int) ((1000 - millisUntilFinished) / 5);
                                    donutProgress.setProgress(progress);
                                    txtUndelivered.setText("Delivering Batches ...");
                                    btnDispatch.setVisibility(View.VISIBLE);

                                }

                                @Override
                                public void onFinish() {
                                    //the progressBar will be invisible after 60 000 miliseconds ( 1 minute)
                                    // pd.dismiss();
                                    donutProgress.setVisibility(View.GONE);
                                    circle_progress.setVisibility(View.VISIBLE);

                                    circle_progress.setProgress(0);
                                    circle_progress.setPrefixText("N");
                                    circle_progress.setSuffixText(" Batch");
                                    txtUndelivered.setText("All batches delivered");
                                    showRecieptDetails();
                                    getbatches();

                                }

                            }.start();
                            Toast.makeText(mActivity, "Data Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
                        }

                    }
                } else if (Integer.parseInt(Id) < 0) {
                    ContentValues values = new ContentValues();
                    values.put(Database.SignedOff, "0");
                    long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                            Database.DelivaryNO + " = ?", new String[]{DelivaryNo});

                    if (rows > 0) {
                        Context context = mActivity;
                        LayoutInflater inflater = mActivity.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText(error);
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        Toast.makeText(mActivity, Message, Toast.LENGTH_LONG).show();

                    }
                }
            } catch (NumberFormatException e) {


                if (Message.equals("-8080")) {
                    Toast.makeText(mActivity, "Server Not Available !!", Toast.LENGTH_LONG).show();

                } else {
                    ContentValues values = new ContentValues();
                    values.put(Database.SignedOff, "0");
                    long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                            Database.DelivaryNO + " = ?", new String[]{DelivaryNo});

                    if (rows > 0) {
                        Context context = mActivity;
                        LayoutInflater inflater = mActivity.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText(error);
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(mActivity, error, Toast.LENGTH_LONG).show();

                    }


                }
            }
        }
    }

    public class DeliveryArrayAdapter extends ArrayAdapter<Delivary> {

        Context context;
        int layoutResourceId;
        ArrayList<Delivary> students;

        public DeliveryArrayAdapter(Context context, int layoutResourceId,
                                    ArrayList<Delivary> studs) {
            super(context, layoutResourceId, studs);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.students = studs;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View item = convertView;
            StudentWrapper StudentWrapper = null;

            if (item == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                item = inflater.inflate(layoutResourceId, parent, false);
                StudentWrapper = new StudentWrapper();
                StudentWrapper.number = item.findViewById(R.id.tv_number);
                StudentWrapper.deldate = item.findViewById(R.id.tv_date);
                StudentWrapper.totalkgs = item.findViewById(R.id.txtTotalKgs);
                StudentWrapper.print = item.findViewById(R.id.btnPrint);

                item.setTag(StudentWrapper);
            } else {
                StudentWrapper = (StudentWrapper) item.getTag();
            }

            final Delivary student = students.get(position);
            StudentWrapper.number.setText(student.getName());
            StudentWrapper.deldate.setText(student.getAge());
            StudentWrapper.totalkgs.setText(student.getAddress());


            StudentWrapper.print.setOnClickListener(v -> {
                dbhelper = new DBHelper(context);
                db = dbhelper.getReadableDatabase();
                listReciepts.performItemClick(listReciepts.getAdapter().getView(position, null, null), position, listReciepts.getAdapter().getItemId(0));
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("DelivaryNo", student.getName());
                edit.apply();
                dispatchid = 1;
                DispatchBatch();
            });


            return item;

        }

        private class StudentWrapper {
            TextView number;
            TextView deldate;
            TextView totalkgs;
            Button print;

        }

    }

}
