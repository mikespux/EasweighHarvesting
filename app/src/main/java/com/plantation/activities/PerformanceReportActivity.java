package com.plantation.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Michael on 30/06/2016.
 */
public class PerformanceReportActivity extends AppCompatActivity {
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;
    static EditText etFrom, etTo, etFarmerNo;
    public Toolbar toolbar;
    public SimpleCursorAdapter ca, ca2;
    DBHelper dbhelper;
    ListView listReciepts;
    String BatchNo, BatchDate;
    TextView textBatchNo, textBatchDate, textDelNo, txtAccountId;
    TextView textDeliNo, textBatchCrates, textTrailer;
    Boolean success = true;
    SearchView searchView;
    Intent mIntent;
    String fromDate, toDate, farmerNo;
    String condition = " _id > 0 ";
    String cond;
    int closed = 1;
    AlertDialog b;
    SQLiteDatabase db;
    ListView listprod;
    TextView txtStatus;
    TextView txtUnits;
    TextView txtTotal;
    private Button btnSearchReceipt, btnFilter;
    private Button pickFrom, pickTo;
    private Fragment mFragment;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance_list);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Performance List");

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
        btnFilter = findViewById(R.id.btnFilter);
        txtStatus = findViewById(R.id.textStatus);
        txtUnits = findViewById(R.id.tv_crates);
        txtTotal = findViewById(R.id.tv_total);

        dbhelper = new DBHelper(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(PerformanceReportActivity.this);

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Restart().execute();
            }
        });
        listReciepts = this.findViewById(R.id.lvReciepts);
        listReciepts.setOnItemClickListener((parent, selectedView, arg2, arg3) -> {
            textBatchNo = selectedView.findViewById(R.id.tv_reciept);
            textBatchDate = selectedView.findViewById(R.id.tv_date);
            textDelNo = selectedView.findViewById(R.id.tv_number);
            txtAccountId = selectedView.findViewById(R.id.txtAccountId);

            //  showRecieptDetails();
        });

        searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
        searchView.setQueryHint("Search Batch No ...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(constraint -> {
                    String recieptNo = constraint.toString();
                    return dbhelper.SearchSpecificBatch(recieptNo);

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
                        return dbhelper.SearchBatch(recieptNo);

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

    public void showSearchReceipt() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_search_performance, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Select Date");
        etFrom = dialogView.findViewById(R.id.edtFromDate);
        etTo = dialogView.findViewById(R.id.edtToDate);
        etFarmerNo = dialogView.findViewById(R.id.edtFarmerNo);

        Date date = new Date(getDate());
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        etFrom.setText(format1.format(date));
        // etTo.setText(format1.format(date));

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
        btnSearchReceipt.setText("SEARCH");
        btnSearchReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    condition += " and  " + Database.CollDate + " = '" + fromDate + "'";

                showPerformance();

                final DecimalFormat df = new DecimalFormat("#0.0#");
                final DecimalFormat df1 = new DecimalFormat("##");
                Cursor c = db.rawQuery("select " +
                        "" + Database.DataCaptureDevice +
                        ",COUNT(" + Database.ROW_ID + ")" +
                        ",SUM(" + Database.BagCount + ")" +
                        ",SUM(" + Database.NetWeight + ")" +
                        " from EmployeeProduceCollection WHERE " + condition, null);
                if (c != null) {

                    c.moveToFirst();

                    txtStatus.setText("Weighments: " + df1.format(c.getDouble(1)) + "   " +
                            "Net Weight: " + df.format(c.getDouble(3)) + " Kgs.");
                    txtUnits.setText(df1.format(c.getDouble(2)));
                    txtTotal.setText(df.format(c.getDouble(3)));


                }
                c.close();

                b.dismiss();
            }
        });


        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
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

    public void showRecieptDetails() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_listclosedbatches, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("All Weighment Receipts");
        dbhelper = new DBHelper(this);
        db = dbhelper.getReadableDatabase();

        BatchNo = textBatchNo.getText().toString();
        String dbtBatchOn = textBatchDate.getText().toString() + " 00:00:00";
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = fmt.parse(dbtBatchOn);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        BatchDate = format1.format(date);
        if (BatchDate.length() > 0)
            cond += " and  " + Database.CollDate + " = '" + BatchDate + "'";

        if (BatchNo.length() > 0)
            cond += " and  " + Database.BatchNumber + " = '" + BatchNo + "'";

        searchView = dialogView.findViewById(R.id.searchView);
        searchView.setQueryHint("Search Farmer No ...");
        searchView.setVisibility(View.GONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String FarmerNo = constraint.toString();
                        return dbhelper.SearchSpecificOnR(FarmerNo, cond);

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
                        String FarmerNo = constraint.toString();
                        return dbhelper.SearchOnR(FarmerNo);

                    }
                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        Cursor batch = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                + Database.ROW_ID + " ='" + txtAccountId.getText().toString() + "'", null);
        textBatchCrates = dialogView.findViewById(R.id.textBatchDetails);
        textDeliNo = dialogView.findViewById(R.id.textDeliNo);
        textTrailer = dialogView.findViewById(R.id.textTrailer);
        if (batch != null) {

            batch.moveToFirst();

            textBatchCrates.setText(batch.getString(batch.getColumnIndex(Database.BatchCrates)));
            textDeliNo.setText(batch.getString(batch.getColumnIndex(Database.DelivaryNO)));
            textTrailer.setText(batch.getString(batch.getColumnIndex(Database.Tractor)));

        }
        batch.close();


        Cursor accounts = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNo + "'", null);
        TextView txtStatus = dialogView.findViewById(R.id.textStatus);

        if (accounts.getCount() == 0) {
            LinearLayout lvProd = dialogView.findViewById(R.id.lvProd);
            lvProd.setVisibility(View.GONE);
            txtStatus.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.GONE);
        } else {
            //Toast.makeText(this, "records found", Toast.LENGTH_LONG).show();}


            final DecimalFormat df = new DecimalFormat("#0.0#");
            final DecimalFormat df1 = new DecimalFormat("##");
            Cursor c = db.rawQuery("select " +
                    "" + Database.DataCaptureDevice +
                    ",COUNT(" + Database.ROW_ID + ")" +
                    ",SUM(" + Database.Tareweight + ")" +
                    ",SUM(" + Database.NetWeight + ")" +
                    " from EmployeeProduceCollection WHERE "
                    + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNo + "'", null);
            if (c != null) {

                c.moveToFirst();
                txtStatus.setVisibility(View.VISIBLE);
                txtStatus.setText("Weighments: " + df1.format(c.getDouble(1)) + "\n" +
                        "Net Weight: " + df.format(c.getDouble(3)) + " Kgs.");

            }
            c.close();

        }
        while (accounts.moveToNext()) {
            String[] from = {Database.ROW_ID, Database.EmployeeNo, Database.NetWeight};
            int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_phone};


            ca = new SimpleCursorAdapter(dialogView.getContext(), R.layout.z_list, accounts, from, to);

            listprod = dialogView.findViewById(R.id.lvUsers);
            listprod.setAdapter(ca);
            listprod.setTextFilterEnabled(true);
            // db.close();
            // dbhelper.close();
        }




        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


            }
        });
       /*dialogBuilder.setNegativeButton("Delete Batch", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                deleteBatch();

            }
        });*/
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showPerformance() {

        db = dbhelper.getReadableDatabase();
        // Cursor accounts = db.query(true, Database.EM_PRODUCE_COLLECTION_TABLE_NAME, null, null, null, null, null, null, null, null);
        Cursor accounts = db.rawQuery("select " +
                "" + Database.ROW_ID +
                "," + Database.EmployeeNo +
                ",SUM(" + Database.BagCount + ") as BagCount" +
                ",SUM(" + Database.NetWeight + ") as NetWeight" +
                " from EmployeeProduceCollection WHERE " + condition + "" +
                " group by EmployeeNo order by NetWeight desc ", null);

        if (accounts.getCount() == 0) {

            new NoReceipt().execute();
            searchView.setVisibility(View.GONE);
        } else {
            //Toast.makeText(this, "records found", Toast.LENGTH_LONG).show();}


            final DecimalFormat df = new DecimalFormat("#0.0#");
            final DecimalFormat df1 = new DecimalFormat("##");
            Cursor c = db.rawQuery("select " +
                    "" + Database.DataCaptureDevice +
                    ",COUNT(" + Database.ROW_ID + ")" +
                    ",SUM(" + Database.BagCount + ")" +
                    ",SUM(" + Database.NetWeight + ")" +
                    " from EmployeeProduceCollection WHERE " + condition + "", null);
            if (c != null) {

                c.moveToFirst();

                txtStatus.setText("Weighments: " + df1.format(c.getDouble(1)) + "   " +
                        "Net Weight: " + df.format(c.getDouble(3)) + " Kgs.");
                txtUnits.setText(df1.format(c.getDouble(2)));
                txtTotal.setText(df.format(c.getDouble(3)));


            }
            c.close();

        }
        while (accounts.moveToNext()) {
            String[] from = {Database.ROW_ID, Database.EmployeeNo, Database.BagCount, Database.NetWeight};
            int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_units, R.id.tv_weight};


            ca = new SimpleCursorAdapter(getApplicationContext(), R.layout.p_list, accounts, from, to);

            ListView listp = this.findViewById(R.id.lvReciepts);

            listp.setAdapter(ca);
            listp.setTextFilterEnabled(true);
            // db.close();
            // dbhelper.close();
        }


    }

    public Cursor getSearch() {


        db = dbhelper.getReadableDatabase();

            /*Cursor accounts = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                    + condition, null);*/
        Cursor accounts = db.rawQuery("select " +
                "" + Database.ROW_ID +
                "," + Database.EmployeeNo +
                ",SUM(" + Database.BagCount + ") as BagCount" +
                ",SUM(" + Database.NetWeight + ") as NetWeight" +
                " from EmployeeProduceCollection WHERE " + condition + "" +
                " group by EmployeeNo order by NetWeight desc ", null);
        return accounts;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        // getdata();
        // showPerformance();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {


            SQLiteDatabase db = dbhelper.getReadableDatabase();
            //Cursor accounts = db.query(true, Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, Database.Closed + "='" + closed + "'", null, null, null, null, null, null);
            Cursor accounts = db.query(true, Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, null, null, null, null, null, null);
            if (accounts.getCount() > 0) {
                Cursor prod = db.query(true, Database.EM_PRODUCE_COLLECTION_TABLE_NAME, null, null, null, null, null, null, null, null);

                if (prod.getCount() == 0) {
                    new NoReceipt().execute();
                }
                String[] from = {Database.ROW_ID, Database.DeliveryNoteNumber, Database.DataDevice, Database.BatchNumber, Database.BatchDate};
                int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_device, R.id.tv_reciept, R.id.tv_date};


                ca = new SimpleCursorAdapter(this, R.layout.batch_list, accounts, from, to);

                ListView listfarmers = this.findViewById(R.id.lvReciepts);

                listfarmers.setAdapter(ca);
                listfarmers.setTextFilterEnabled(true);
                db.close();
                dbhelper.close();
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

        return;
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


            mIntent = new Intent(getApplicationContext(), PerformanceReportActivity.class);
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
            text.setText("No Report Found");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
        }
    }
}
