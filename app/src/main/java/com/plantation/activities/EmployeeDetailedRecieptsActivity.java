package com.plantation.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Michael on 30/06/2016.
 */
public class EmployeeDetailedRecieptsActivity extends AppCompatActivity {
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;
    public Toolbar toolbar;
    public SimpleCursorAdapter ca, cb;
    DBHelper dbhelper;
    ListView listReciepts;
    String accountId;
    TextView textAccountId;
    Boolean success = true;
    TextView textCompanyName, textPoBox, textReciept, textTransDate, textTransTime, textTerminal, textemployeeNo, textName,
            textLcCrates, textLcTotal, textExCrates, textExTotal, textTrip, textCrates, textGrossWt, textTareWt, textNetWt, textTotalKgs, textClerk;
    SearchView searchView;
    Intent mIntent;
    EditText etFrom, etTo, etemployeeNo;
    String fromDate, toDate, employeeNo;
    String condition = " _id > 0 ";
    String cond = " _id > 0 ";
    AlertDialog b;
    AlertDialog alert;
    DecimalFormat formatter;
    String SessionNo, DataDevice;
    String ReceiptNo, produce;
    String Kgs, Time;
    ListView listbags;
    TextView NoReceiptFound;
    SimpleDateFormat dateTimeFormat;
    private Button mConnectBtn;
    private Button btnSearchReceipt, btnFilter;
    private Button pickFrom, pickTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listreciepts);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Employee Receipts");

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
        dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        NoReceiptFound = findViewById(R.id.tvNoreceipt);
        mConnectBtn = findViewById(R.id.btnConnect);
        mConnectBtn.setVisibility(View.GONE);
        btnFilter = findViewById(R.id.btnFilter);
        formatter = new DecimalFormat("0000");
        dbhelper = new DBHelper(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(EmployeeDetailedRecieptsActivity.this);
        String mDevice = prefs.getString("mDevice", "");
        // showToast(mDevice);
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Restart().execute();
            }
        });
        listReciepts = this.findViewById(R.id.lvReciepts);
        listReciepts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());


                PrintDetailedReceipt();


            }
        });

        searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
        searchView.setQueryHint("Search Receipt No ...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String recieptNo = constraint.toString();
                        return dbhelper.SearchSpecificReciept(recieptNo);

                    }
                });

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String recieptNo = constraint.toString();
                        return dbhelper.SearchReciept(recieptNo);

                    }
                });

                return false;
            }
        });
        searchView.requestFocus();
        showSearchReceipt();

    }


    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void showSearchReceipt() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_search_receipt, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Search Receipt");
        etFrom = dialogView.findViewById(R.id.edtFromDate);
        etTo = dialogView.findViewById(R.id.edtToDate);
        etemployeeNo = dialogView.findViewById(R.id.edtFarmerNo);

        Date date = new Date(getDate());
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        etFrom.setText(format1.format(date));
        etTo.setText(format1.format(date));

        pickFrom = dialogView.findViewById(R.id.btnFrom);
        pickFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");

            }
        });

        pickTo = dialogView.findViewById(R.id.btnTo);
        pickTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment2();
                newFragment.show(getFragmentManager(), "datePicker");

            }
        });


        btnSearchReceipt = dialogView.findViewById(R.id.btn_SearchReceipt);
        btnSearchReceipt.setVisibility(View.VISIBLE);
        btnSearchReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDate = etFrom.getText().toString();
                toDate = etTo.getText().toString();
                employeeNo = etemployeeNo.getText().toString();

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("fromDate", fromDate);
                edit.commit();
                edit.putString("toDate", toDate);
                edit.commit();
                edit.putString("employeeNo", employeeNo);
                edit.commit();
                if (employeeNo.equals("")) {

                    Context context = EmployeeDetailedRecieptsActivity.this;
                    LayoutInflater inflater = EmployeeDetailedRecieptsActivity.this.getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Enter employee No ...");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(employeeRecieptsActivity.this, "Please Enter employee No", Toast.LENGTH_LONG).show();
                    return;
                }
                if (fromDate.length() > 0)
                    condition += " and  " + Database.CollDate + " >= '" + fromDate + "'";

                if (toDate.length() > 0)
                    condition += " and  " + Database.CollDate + " <= '" + toDate + "'";

                if (employeeNo.length() > 0)
                    condition += " and  " + Database.EmployeeNo + " = '" + employeeNo + "' Group By " + Database.EmployeeNo + "";

                if (fromDate.length() > 0)
                    cond += " and  " + Database.CollDate + " >= '" + fromDate + "'";

                if (toDate.length() > 0)
                    cond += " and  " + Database.CollDate + " <= '" + toDate + "'";

                if (employeeNo.length() > 0)
                    cond += " and  " + Database.EmployeeNo + " = '" + employeeNo + "'";

                getSearch();

                /* ca.getFilter().filter(condition.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String reciept = constraint.toString();
                        return dbhelper.SearchRecieptByDate(reciept);
                    }
                });*/

                b.dismiss();
            }
        });


        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // Toast.makeText(employeeRecieptsActivity.this, "Please Click Search to proceed", Toast.LENGTH_LONG).show();
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        dialogBuilder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                //getdata();
                finish();
            }
        });
        b = dialogBuilder.create();
        b.show();

    }

    public void PrintDetailedReceipt() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_transaction_details_more, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Receipt");
        accountId = textAccountId.getText().toString();
        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor p = db.rawQuery("select MpCode,MpDescription from Produce", null);
        if (p != null) {
            if (p.moveToFirst()) {
                do {
                    produce = p.getString(p.getColumnIndex("MpDescription"));

                } while (p.moveToNext());
            }
        }

        textCompanyName = dialogView.findViewById(R.id.textCompanyName);
        textCompanyName.setText(mSharedPrefs.getString("company_name", ""));
        textPoBox = dialogView.findViewById(R.id.textPoBox);
        textPoBox.setText(mSharedPrefs.getString("company_letterbox", "") + "-" + mSharedPrefs.getString("company_postalcode", "") + ", " +
                mSharedPrefs.getString("company_postalname", ""));
        textReciept = dialogView.findViewById(R.id.textReciept);
        textTransDate = dialogView.findViewById(R.id.textTransDate);
        textTransTime = dialogView.findViewById(R.id.textTransTime);
        textTerminal = dialogView.findViewById(R.id.textTerminal);
        textemployeeNo = dialogView.findViewById(R.id.textFarmerNo);
        textName = dialogView.findViewById(R.id.textName);

        textLcCrates = dialogView.findViewById(R.id.textLcCrates);
        textLcTotal = dialogView.findViewById(R.id.textLcTotal);
        textExCrates = dialogView.findViewById(R.id.textExCrates);
        textExTotal = dialogView.findViewById(R.id.textExTotal);

        textCrates = dialogView.findViewById(R.id.textCrates);
        textGrossWt = dialogView.findViewById(R.id.textGrossWt);
        textTareWt = dialogView.findViewById(R.id.textTareWt);
        textNetWt = dialogView.findViewById(R.id.textNetWt);

        textClerk = dialogView.findViewById(R.id.textClerk);
        textClerk.setText(prefs.getString("user", ""));


        Double grossWeight = 0.0;
        final DecimalFormat df = new DecimalFormat("#0.0#");

        // SQLiteDatabase db = dbhelper.getReadableDatabase();
        //Cursor session = db.query(Database.EM_PRODUCE_COLLECTION_TABLE_NAME, null," _id = ?", new String[]{accountId}, null, null, null);
        Cursor session = db.rawQuery("SELECT *, SUM(NetWeight) AS NetWeight, SUM(BagCount) AS BagCount FROM " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME +
                " WHERE " + condition + "", null);
        if (session.moveToFirst()) {

            String[] allColumns = new String[]{Database.EM_NAME};
            Cursor c = db.query(Database.EM_TABLE_NAME, allColumns, Database.EM_ID + "='" + session.getString(session
                    .getColumnIndex(Database.EmployeeNo)) + "'", null, null, null, null, null);
            if (c != null) {
                c.moveToFirst();

                textName.setText(c.getString(c.getColumnIndex(Database.EM_NAME)));
            }

            textReciept.setText(session.getString(session
                    .getColumnIndex(Database.DataCaptureDevice)) + session.getString(session
                    .getColumnIndex(Database.ReceiptNo)));
            DataDevice = (session.getString(session
                    .getColumnIndex(Database.DataCaptureDevice)));

            textTransDate.setText(session.getString(session.getColumnIndex(Database.CollDate)));

            textTransTime.setText(session.getString(session.getColumnIndex(Database.CaptureTime)));
            textTerminal.setText(prefs.getString("terminalID", ""));
            textemployeeNo.setText(session.getString(session
                    .getColumnIndex(Database.EmployeeNo)));
            textCrates.setText(session.getString(session
                    .getColumnIndex(Database.BagCount)));
            Cursor lc = db.rawQuery("SELECT *, SUM(NetWeight) AS NetWeight, SUM(BagCount) AS BagCount FROM " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME +
                    " WHERE " + cond +
                    " and " + Database.ProduceGrade + " ='LC'", null);
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
                    " WHERE " + cond +
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


            grossWeight = Double.parseDouble(session.getString(session.getColumnIndex(Database.Tareweight)))
                    + Double.parseDouble(session.getString(session.getColumnIndex(Database.NetWeight)));
            textGrossWt.setText(df.format(grossWeight));

            textTareWt.setText(session.getString(session
                    .getColumnIndex(Database.Tareweight)));
            textNetWt.setText(session.getString(session
                    .getColumnIndex(Database.NetWeight)));

            ReceiptNo = session.getString(session.getColumnIndex(Database.ReceiptNo));


        }
        session.close();
        // db.close();
        //dbhelper.close();


        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();


            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        // showSearchReceipt();
        if (employeeNo == null) {
            getdata();

        } else {
            getSearch();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {

            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.EM_PRODUCE_COLLECTION_TABLE_NAME, null, null, null, null, null, null, null, null);
            if (accounts.getCount() > 0) {
                String[] from = {Database.ROW_ID, Database.EmployeeNo, Database.DataCaptureDevice, Database.ReceiptNo, Database.BagCount, Database.NetWeight, Database.CollDate};
                int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_DataDevice, R.id.tv_device, R.id.tv_reciept, R.id.tv_totalkgs, R.id.tv_date};


                ca = new SimpleCursorAdapter(this, R.layout.receipt_list, accounts, from, to);

                ListView listemployees = this.findViewById(R.id.lvReciepts);
                //ca.notifyDataSetChanged();
                listemployees.setAdapter(ca);
                listemployees.setTextFilterEnabled(true);
                db.close();
                dbhelper.close();
            } else {

                new NoReceipt().execute();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getSearch() {

        try {

            SQLiteDatabase db = dbhelper.getReadableDatabase();

            //Cursor accounts = db.query(true, Database.EM_PRODUCE_COLLECTION_TABLE_NAME, null, "" + condition + "", null, null, null, null, null, null);
            Cursor accounts = db.rawQuery("SELECT *, SUM(NetWeight) AS NetWeight, SUM(BagCount) AS BagCount FROM " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME +
                    " WHERE " + condition + "", null);
            if (accounts.getCount() > 0) {
                String[] from = {Database.ROW_ID, Database.EmployeeNo, Database.DataCaptureDevice, Database.ReceiptNo, Database.BagCount, Database.NetWeight, Database.CollDate};
                int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_DataDevice, R.id.tv_device, R.id.tv_reciept, R.id.tv_totalkgs, R.id.tv_date};


                ca = new SimpleCursorAdapter(this, R.layout.receipt_list, accounts, from, to);
                listReciepts = this.findViewById(R.id.lvReciepts);
                listReciepts.setAdapter(ca);
                db.close();
                dbhelper.close();
            } else {
                listReciepts.setVisibility(View.GONE);
                NoReceiptFound.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
        return currentDateTimeString;
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        return;
    }

    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment
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
    public class DatePickerFragment2 extends DialogFragment
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


        @Override
        protected void onPreExecute() {

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

            mIntent = new Intent(getApplicationContext(), EmployeeDetailedRecieptsActivity.class);
            startActivity(mIntent);
        }
    }

    private class NoReceipt extends AsyncTask<Void, Void, String> {


        @Override
        protected void onPreExecute() {

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

            finish();
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("No Receipts To Print");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
        }
    }
}
