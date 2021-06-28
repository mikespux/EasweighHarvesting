package com.plantation.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.helpers.Delivary;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Michael on 30/06/2016.
 */
public class DeliveryEditActivity extends AppCompatActivity {
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;
    private final ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    public Toolbar toolbar;
    public SimpleCursorAdapter ca;
    DBHelper dbhelper;
    ListView listReciepts;
    Boolean success = true;
    SearchView searchView;
    Intent mIntent;
    static EditText etFrom, etTo, etFarmerNo;
    String fromDate, toDate, farmerNo;
    String condition = " _id > 0 ";
    AlertDialog b;
    Cursor accounts;
    DeliveryArrayAdapter ArrayAdapter;
    String TransporterCode, FactoryCode, Transporter, Factory;

    String accountId;
    EditText etDriver, etTurnMan, Trailer, etDeliveryNo, etTicketNo, etQuantity, etVehicle, etTractor, etRejectwt, etQuality;
    Button btnCloseBatch;
    SQLiteDatabase db;
    String stDelNo, stTicketNo, stQuantity, stVehicle, stTractor, stRejectwt, stQuality;
    String DeliveryNo, DNoteNo, DelDate, Vehicle, Tractor, Driver, TurnMan, ArrivalTime;

    private Button btnSearchReceipt, btnFilter;

    private Button pickFrom, pickTo;

    Spinner spinnerFactory;
    String factorys;
    String factoryid = null;
    ArrayList<String> factorydata = new ArrayList<String>();
    ArrayAdapter<String> factoryadapter;

    String transporters;
    String transporterid = null;
    ArrayList<String> transporterdata = new ArrayList<String>();
    ArrayAdapter<String> transporteradapter;
    Spinner mc_ctransporter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listbatch);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Delivery Edit");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void initializer() {
        btnFilter = findViewById(R.id.btnFilter);
        dbhelper = new DBHelper(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(DeliveryEditActivity.this);

        btnFilter.setOnClickListener(v -> new Restart().execute());


        searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
        searchView.requestFocus();

        showSearchReceipt();
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    public void showSearchReceipt() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_search_batches, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Search Deliveries");
        etFrom = dialogView.findViewById(R.id.edtFromDate);
        etTo = dialogView.findViewById(R.id.edtToDate);
        etFarmerNo = dialogView.findViewById(R.id.edtFarmerNo);

        Date date = new Date(getDate());
        // SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        etFrom.setText(format1.format(date));
        etTo.setText(format1.format(date));

        pickFrom = dialogView.findViewById(R.id.btnFrom);
        pickFrom.setOnClickListener(v -> {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getFragmentManager(), "datePicker");

        });

        pickTo = dialogView.findViewById(R.id.btnTo);
        pickTo.setOnClickListener(v -> {
            DialogFragment newFragment = new DatePickerFragment2();
            newFragment.show(getFragmentManager(), "datePicker");

        });


        btnSearchReceipt = dialogView.findViewById(R.id.btn_SearchReceipt);
        btnSearchReceipt.setVisibility(View.VISIBLE);
        btnSearchReceipt.setOnClickListener(v -> {
            fromDate = etFrom.getText().toString();
            toDate = etTo.getText().toString();
            farmerNo = etFarmerNo.getText().toString();

            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("fromDate", fromDate);
            edit.commit();
            edit.putString("toDate", toDate);
            edit.commit();
            edit.putString("farmerNo", farmerNo);
            edit.commit();

            if (fromDate.length() > 0)
                condition += " and  " + Database.FdDate + " >= '" + fromDate + "'";

            if (toDate.length() > 0)
                condition += " and  " + Database.FdDate + " <= '" + toDate + "'";

            if (farmerNo.length() > 0)
                condition += " and  " + Database.FdDNoteNum + " = '" + farmerNo + "'";

            getdata();
            b.dismiss();
        });


        dialogBuilder.setOnKeyListener((dialog, keyCode, event) -> {
            // Toast.makeText(FarmerRecieptsActivity.this, "Please Click Search to proceed", Toast.LENGTH_LONG).show();
            return keyCode == KeyEvent.KEYCODE_BACK;
        });
        dialogBuilder.setPositiveButton("Back", (dialog, whichButton) -> {
            //pass
            //getdata();
            finish();
        });
        b = dialogBuilder.create();
        b.show();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        getdata();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {

            SQLiteDatabase db = dbhelper.getReadableDatabase();
            ArrayList<Delivary> arraylist = new ArrayList<Delivary>();

            accounts = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " where " + condition + "", null);
            if (accounts.getCount() > 0) {
                while (accounts.moveToNext()) {

                    arraylist.add(new Delivary(accounts.getString(accounts.getColumnIndex(Database.ROW_ID)), accounts.getString(accounts.getColumnIndex(Database.FdDNoteNum)),
                            accounts.getString(accounts.getColumnIndex(Database.FdDate)),
                            accounts.getString(accounts.getColumnIndex(Database.FdFieldWt))));
                }

                ArrayAdapter = new DeliveryArrayAdapter(DeliveryEditActivity.this, R.layout.delivery_list, arraylist);
                listReciepts = this.findViewById(R.id.lvReciepts);

                listReciepts.setAdapter(ArrayAdapter);
                ArrayAdapter.notifyDataSetChanged();
                listReciepts.setTextFilterEnabled(true);

                //db.close();
                //dbhelper.close();


            } else {

                new NoReceipt().execute();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        // mIntent = new Intent(FarmerDetailsActivity.this,MainActivity.class);
        //startActivity(mIntent);
        return;
    }

    public void showUpdateDeliveryDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edit_delivery, null);
        dialogBuilder.setView(dialogView);
        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("Update Deliveries");
        accountId = prefs.getString("_id", "");

        mc_ctransporter = dialogView.findViewById(R.id.mc_ctransporter);
        spinnerFactory = dialogView.findViewById(R.id.spinnerFactory);

        FactoryList();
        TransporterList();
        etDeliveryNo = dialogView.findViewById(R.id.etDeliveryNo);
        etTicketNo = dialogView.findViewById(R.id.etTicketNo);
        etQuantity = dialogView.findViewById(R.id.etQuantity);
        etVehicle = dialogView.findViewById(R.id.etVehicle);
        etTractor = dialogView.findViewById(R.id.etTractor);
        etDriver = dialogView.findViewById(R.id.etDriver);
        etTurnMan = dialogView.findViewById(R.id.etTurnMan);
        etRejectwt = dialogView.findViewById(R.id.etRejectwt);
        etQuality = dialogView.findViewById(R.id.etQuality);


        // Adds the TextWatcher as TextChangedListener to both EditTexts


        dbhelper = new DBHelper(this);
        db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.Fmr_FactoryDeliveries, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            etDeliveryNo.setText(account.getString(account
                    .getColumnIndex(Database.FdDNoteNum)));

            etTicketNo.setText(account.getString(account
                    .getColumnIndex(Database.FdWeighbridgeTicket)));
            etQuantity.setText(account.getString(account
                    .getColumnIndex(Database.FdFieldWt)));
            etVehicle.setText(account.getString(account
                    .getColumnIndex(Database.FdVehicle)));

            etTractor.setText(account.getString(account
                    .getColumnIndex(Database.FdTractor)));

            etDriver.setText(account.getString(account
                    .getColumnIndex(Database.FdDriver)));

            etTurnMan.setText(account.getString(account
                    .getColumnIndex(Database.FdTurnMan)));
            etQuality.setText(account.getString(account
                    .getColumnIndex(Database.FdQualityScore)));


            String FdFactory = "", FdTransporter = "";
            FdFactory = account.getString(account.getColumnIndex(Database.FdFactory));
            FdTransporter = account.getString(account.getColumnIndex(Database.FdTransporter));
            if (FdFactory != null) {

                Cursor factory = db.query(Database.FACTORY_TABLE_NAME, null,
                        " FryPrefix = ?", new String[]{FdFactory}, null, null, null);
                if (factory.getCount() > 0) {
                    if (factory.moveToFirst()) {

                        spinnerFactory.setSelection(factoryadapter.getPosition(factory.getString(factory
                                .getColumnIndex(Database.FRY_TITLE))));
                    }
                }
            }
            if (FdTransporter != null) {
                Cursor transporter = db.query(Database.TRANSPORTER_TABLE_NAME, null,
                        " tptID = ?", new String[]{FdTransporter}, null, null, null);

                if (transporter.getCount() > 0) {
                    if (transporter.moveToFirst()) {

                        mc_ctransporter.setSelection(transporteradapter.getPosition(transporter.getString(transporter
                                .getColumnIndex(Database.TPT_NAME))));
                    }
                }
            }

        }
        account.close();
        //db.close();
        //dbhelper.close();



        btnCloseBatch = dialogView.findViewById(R.id.btnCloseBatch);
        btnCloseBatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transporterid != null) {
                    if (mc_ctransporter.getSelectedItem().toString().equals("Select ...")) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Transporter");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        return;
                    }
                }
                if (factoryid != null) {
                    if (spinnerFactory.getSelectedItem().toString().equals("Select ...")) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Factory");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        return;
                    }
                }

                if (etQuantity.length() <= 0) {
                    etQuantity.setError("Enter GrossWT.");
                    return;
                }
                if (etVehicle.length() <= 0) {
                    etVehicle.setError("Enter Vehicle.");

                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Update Delivery?</font>"))
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String DNoteNum = prefs.getString("_id", "");


                                Cursor count = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " WHERE "
                                        + Database.ROW_ID + " ='" + DNoteNum + "'", null);

                                if (count.getCount() > 0) {
                                    stDelNo = etDeliveryNo.getText().toString();
                                    stTicketNo = etTicketNo.getText().toString();
                                    stQuantity = etQuantity.getText().toString();
                                    stVehicle = etVehicle.getText().toString();
                                    stTractor = etTractor.getText().toString();
                                    Driver = etDriver.getText().toString();
                                    TurnMan = etTurnMan.getText().toString();
                                    stTractor = etTractor.getText().toString();
                                    stRejectwt = etRejectwt.getText().toString();
                                    stQuality = etQuality.getText().toString();


                                    if (!mSharedPrefs.getBoolean("realtimeServices", false) == true) {
                                        ContentValues values = new ContentValues();
                                        values.put(Database.FdDNoteNum, stDelNo);
                                        values.put(Database.FdWeighbridgeTicket, stTicketNo);
                                        values.put(Database.FdGrossWt, stQuantity);
                                        values.put(Database.FdVehicle, stVehicle);
                                        values.put(Database.FdDriver, Driver);
                                        values.put(Database.FdTurnMan, TurnMan);
                                        values.put(Database.FdVehicle, stVehicle);
                                        values.put(Database.FdTractor, stTractor);
                                        values.put(Database.FdFactory, factoryid);
                                        values.put(Database.FdTransporter, transporterid);

                                        long rows = db.update(Database.Fmr_FactoryDeliveries, values,
                                                Database.ROW_ID + " = ?", new String[]{DNoteNum});

                                        ContentValues value = new ContentValues();
                                        value.put(Database.DelivaryNO, etDeliveryNo.getText().toString());
                                        value.put(Database.Tractor, stTractor);
                                        value.put(Database.Trailer, stVehicle);
                                        long rows1 = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, value,
                                                Database.DelivaryNO + " = ?", new String[]{etDeliveryNo.getText().toString()});

                                        if (rows1 > 0 && rows > 0) {
                                            Toast.makeText(getApplicationContext(), "Delivery Updated Successfully !!", Toast.LENGTH_LONG).show();
                                            b.dismiss();
                                            getdata();
                                        }
                                    } else {
                                        count.moveToFirst();
                                        DNoteNo = stDelNo;
                                        DelDate = count.getString(count.getColumnIndex(Database.FdDate));

                                        Vehicle = stVehicle;
                                        Tractor = stTractor;

                                        ArrivalTime = count.getString(count.getColumnIndex(Database.FdArrivalTime));
                                        Transporter = transporterid;
                                        Factory = factoryid;
                                        DeliveryNo = count.getString(count.getColumnIndex(Database.CloudID));


                                        //new UpdateDelivary().execute();
                                    }



                                                   /* getApplicationContext().finish();
                                                    mIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                    startActivity(mIntent);*/


                                } else {
                                    Context context = getApplicationContext();
                                    LayoutInflater inflater = getLayoutInflater();
                                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                    TextView text = customToastroot.findViewById(R.id.toast);
                                    text.setText("Sorry! No Deliveries!");
                                    Toast customtoast = new Toast(context);
                                    customtoast.setView(customToastroot);
                                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                    customtoast.setDuration(Toast.LENGTH_LONG);
                                    customtoast.show();

                                }

                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();


                            }
                        });
                AlertDialog alert2 = builder.create();
                alert2.show();


            }
        });

        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                // deleteUser();

            }
        });
        /*dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateUsers();
                getdata();



            }
        });*/
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


        transporteradapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, transporterdata);
        transporteradapter.setDropDownViewResource(R.layout.spinner_item);
        mc_ctransporter.setAdapter(transporteradapter);
        mc_ctransporter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String transporterName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                if (transporterName.equals("Select ...")) {
                    transporterid = "";

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


        factoryadapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, factorydata);
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


    @SuppressLint("ValidFragment")
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            // edtBaseDate.setText(view.getYear() + "/" + view.getMonth() + "/" + view.getDayOfMonth());

            // Create a Date variable/object with user chosen date
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(year, month, day, 0, 0, 0);
            Date chosenDate = cal.getTime();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
            etFrom.setText(format1.format(chosenDate));
        }
    }

    @SuppressLint("ValidFragment")
    public static class DatePickerFragment2 extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            // edtBaseDate.setText(view.getYear() + "/" + view.getMonth() + "/" + view.getDayOfMonth());
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(year, month, day, 0, 0, 0);
            Date chosenDate = cal.getTime();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
            etTo.setText(format1.format(chosenDate));
        }
    }

    private class Restart extends AsyncTask<Void, Void, String> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            /*dialog = ProgressDialog.show( FarmerRecieptsActivity.this,
                    getString(R.string.please_wait),
                    getString(R.string.logging_out),
                    true);*/
        }

        @Override
        protected String doInBackground(Void... params) {
            finish();

            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return "";
        }

        @Override
        protected void onPostExecute(String result) {

            //dialog.dismiss();

            mIntent = new Intent(getApplicationContext(), DeliveryEditActivity.class);
            startActivity(mIntent);
        }
    }

    private class NoReceipt extends AsyncTask<Void, Void, String> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            /*dialog = ProgressDialog.show( FarmerRecieptsActivity.this,
                    getString(R.string.please_wait),
                    getString(R.string.logging_out),
                    true);*/
        }

        @Override
        protected String doInBackground(Void... params) {


            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return "";
        }

        @Override
        protected void onPostExecute(String result) {

            //dialog.dismiss();
            finish();
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("No Deliveries Found!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
        }
    }

    public class DeliveryArrayAdapter extends ArrayAdapter<Delivary> {

        Context context;
        int layoutResourceId;
        ArrayList<Delivary> students = new ArrayList<Delivary>();

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
                StudentWrapper.id = item.findViewById(R.id.txtAccountId);
                StudentWrapper.number = item.findViewById(R.id.tv_number);
                StudentWrapper.deldate = item.findViewById(R.id.tv_date);
                StudentWrapper.totalkgs = item.findViewById(R.id.txtTotalKgs);
                StudentWrapper.print = item.findViewById(R.id.btnPrint);

                item.setTag(StudentWrapper);
            } else {
                StudentWrapper = (StudentWrapper) item.getTag();
            }

            Delivary student = students.get(position);
            StudentWrapper.id.setText(student.getID());
            StudentWrapper.number.setText(student.getName());
            StudentWrapper.deldate.setText(student.getAge());
            StudentWrapper.totalkgs.setText(student.getAddress());

            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("_id", student.getID());
            edit.commit();
            StudentWrapper.print.setText("View");
            StudentWrapper.print.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dbhelper = new DBHelper(context);
                    db = dbhelper.getReadableDatabase();
                    listReciepts.performItemClick(listReciepts.getAdapter().getView(position, null, null), position, listReciepts.getAdapter().getItemId(0));
                    showUpdateDeliveryDialog();
                }
            });


            return item;

        }

        private class StudentWrapper {
            TextView id;
            TextView number;
            TextView deldate;
            TextView totalkgs;
            Button print;

        }

    }
}
