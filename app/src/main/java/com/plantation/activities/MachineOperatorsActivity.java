package com.plantation.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MachineOperatorsActivity extends AppCompatActivity {

    public static final String TAG = "MachineOperators";
    static SharedPreferences prefs;
    static SharedPreferences mSharedPrefs;
    public Toolbar toolbar;
    public SimpleCursorAdapter ca;
    ListView listEmployees;
    TextView textAccountId, textMachineNo, textEmployeeNo;
    SearchView searchView;
    Intent mIntent;
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat dateTimeFormatB;

    DBHelper dbhelper;
    SQLiteDatabase db;

    String MDate;
    String smachineNo;
    String accountId;

    AlertDialog dDate;
    static EditText etDate;
    private Button btnSearchReceipt;
    private Button pickFrom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_operators);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Machine Operators");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void initializer() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(MachineOperatorsActivity.this);
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateTimeFormatB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();

        listEmployees = this.findViewById(R.id.lvEmployees);
        listEmployees.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                textMachineNo = selectedView.findViewById(R.id.tv_machineno);
                textEmployeeNo = selectedView.findViewById(R.id.tv_employeeno);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("MachineNo", textMachineNo.getText().toString());
                edit.commit();
                edit.putString("EmployeeNo", textEmployeeNo.getText().toString());
                edit.commit();
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                accountId = textAccountId.getText().toString();

            }
        });

        smachineNo = prefs.getString("MachineNo", "");
        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search Employee No ...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(constraint -> {
                    String EmployeeCode = constraint.toString();
                    return dbhelper.SearchSpecificMOperator(EmployeeCode, smachineNo, MDate);

                });
                // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText);
                ca.setFilterQueryProvider(constraint -> {
                    String EmployeeCode = constraint.toString();
                    return dbhelper.SearchMOperator(EmployeeCode, smachineNo, MDate);

                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        searchView.requestFocus();


    }

    public void showSearchReceipt() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_search_operators, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Search");
        etDate = dialogView.findViewById(R.id.edtDate);


        Date date = new Date(getDate());
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        etDate.setText(format1.format(date));

        pickFrom = dialogView.findViewById(R.id.btnFrom);
        pickFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");

            }
        });


        btnSearchReceipt = dialogView.findViewById(R.id.btn_SearchReceipt);
        btnSearchReceipt.setVisibility(View.VISIBLE);
        btnSearchReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDate = etDate.getText().toString();
                getdata();
                dDate.dismiss();
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
        dDate = dialogBuilder.create();
        dDate.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        showSearchReceipt();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {

            SQLiteDatabase db = dbhelper.getReadableDatabase();
            //  Cursor accounts = db.query(true, Database.MACHINEOP_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);
            // Calendar cal = Calendar.getInstance();
            // MDate = dateTimeFormatB.format(cal.getTime());
            smachineNo = prefs.getString("MachineNo", "");
            Cursor accounts = db.rawQuery("select * from " + Database.MACHINEOP_TABLE_NAME + " WHERE "
                    + Database.MDATE + " ='" + MDate + "' and " + Database.MACHINENUMBER + " ='" + smachineNo + "'", null);
            if (accounts.getCount() == 0) {
                Toast.makeText(this, "No Records For Machine No: " + smachineNo, Toast.LENGTH_LONG).show();
                finish();
            }
            String[] from = {Database.ROW_ID, Database.MDATE, Database.CHECKINTIME, Database.MACHINENUMBER, Database.EMPLOYEENUMBER};
            int[] to = {R.id.txtAccountId, R.id.tv_date, R.id.tv_time, R.id.tv_machineno, R.id.tv_employeeno};


            ca = new SimpleCursorAdapter(this, R.layout.operators_list, accounts, from, to);

            listEmployees.setAdapter(ca);
            listEmployees.setTextFilterEnabled(true);

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
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
            etDate.setText(format1.format(chosenDate));
        }
    }


    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        return;
    }

    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }
}
