package com.plantation.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class CheckInOPActivity extends AppCompatActivity {

    static SharedPreferences mSharedPrefs, prefs;
    public Intent mIntent;
    public Toolbar toolbar;


    Spinner spTask;
    TextView tvMachine, tvEmployee;

    EditText edtwmtCheckIn;
    TextView textAccountId, textEmployee, textEmployeeNo;
    ListView listEmployees;

    TextView textMachineId, textMachineNo, textMachineOP;
    ListView listMachines;


    String taskid = null;
    String tasks;
    ArrayList<String> taskdata = new ArrayList<>();
    ArrayAdapter<String> taskadapter;


    SQLiteDatabase db;
    DBHelper dbhelper;
    DecimalFormat formatter;

    Button btnAllocate;

    SearchView searchView;
    SimpleCursorAdapter ca;
    AlertDialog dMachine, dEmployee;

    // Declaration of Date Formats.
    SimpleDateFormat dateTimeFormatA;
    SimpleDateFormat dateTimeFormatB;

    String DeliverNoteNumber, sDate, sterminalID, smachineNo, semployeeNo, scheckinTime, smTaskCode, smCompany, smEstate;
    int checkinWeighment;
    int maxOperators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin_operators);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Machine Allocation");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        return;
    }

    public void initializer() {

        prefs = PreferenceManager.getDefaultSharedPreferences(CheckInOPActivity.this);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(CheckInOPActivity.this);

        dbhelper = new DBHelper(CheckInOPActivity.this);
        db = dbhelper.getReadableDatabase();

        formatter = new DecimalFormat("00");

        dateTimeFormatA = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateTimeFormatB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        DeliverNoteNumber = prefs.getString("DeliverNoteNumber", "");

        tvMachine = findViewById(R.id.tvMachine);
        tvEmployee = findViewById(R.id.tvEmployee);
        spTask = findViewById(R.id.spTask);
        edtwmtCheckIn = findViewById(R.id.edtwmtCheckIn);


        tvMachine.setOnClickListener(v -> {
            Machine();
        });


        tvEmployee.setOnClickListener(v -> {
            Employee();
        });

        TaskList();

        btnAllocate = findViewById(R.id.btnAllocate);
        btnAllocate.setOnClickListener(v -> {


            if (tvMachine.getText().equals(getResources().getString(R.string.spinner_prompt_machine))) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Select Machine");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getApplicationContext(), "Please Select Produce", Toast.LENGTH_LONG).show();
                return;
            }
            if (tvEmployee.getText().equals(getResources().getString(R.string.spinner_prompt_employee))) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Select Employee");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getApplicationContext(), "Please Select Produce", Toast.LENGTH_LONG).show();
                return;
            }
            if (taskid == null) {

                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Select Task Code");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getApplicationContext(), "Please Select Produce", Toast.LENGTH_LONG).show();
                return;
            }

            sDate = dateTimeFormatA.format(new Date(getDate()));
            sterminalID = mSharedPrefs.getString("terminalID", "");
            smachineNo = prefs.getString("MachineNo", "");
            semployeeNo = prefs.getString("EmployeeNo", "");
            Calendar cal = Calendar.getInstance();
            scheckinTime = dateTimeFormatB.format(cal.getTime());
            checkinWeighment = getcheckinWeighment(smachineNo, sDate);
            smTaskCode = taskid;
            smCompany = mSharedPrefs.getString("company_prefix", "");
            smEstate = prefs.getString("estateCode", "");

            Cursor checkAlloc = dbhelper.CheckMachineOperator(semployeeNo, sDate);
            if (checkAlloc.getCount() > 0) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Employee already Allocated Today");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getApplicationContext(), "Employee already exists", Toast.LENGTH_SHORT).show();
                return;
            }


            maxOperators = prefs.getInt("Operators", 0);
            Cursor checkOperators = dbhelper.CheckMaxOperators(smachineNo, sDate);
            if (checkOperators.getCount() == maxOperators) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Maximum Number of Operators Allocated for this Machine.");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                return;
            }

            dbhelper.AddMachineOperators(sDate, sterminalID, smachineNo, semployeeNo, scheckinTime, checkinWeighment, smTaskCode, smCompany, smEstate);

            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("Allocated Successfully: " + semployeeNo);
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
        });


    }

    public int getcheckinWeighment(String machineNo, String sDate) {
        int checkinWeighment = 0;
        Cursor bagCount = db.rawQuery("select MAX(LoadCount) from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                + Database.EmployeeNo + "= '" + machineNo + "' and " + Database.CollDate + "='" + sDate + "'", null);
        if (bagCount != null) {
            if (bagCount.getCount() > 0) {

                bagCount.moveToFirst();
                if (bagCount.getString(0) == null) {
                    checkinWeighment = 1;
                } else {
                    checkinWeighment = Integer.parseInt(bagCount.getString(0)) + 1;
                }
            }
        }

        return checkinWeighment;
    }

    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    private void Machine() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CheckInOPActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_machine_list, null);
        dialogBuilder.setView(dialogView);

        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("Machine List");

        searchView = dialogView.findViewById(R.id.searchView);
        searchView.setQueryHint("Search Machine ...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(constraint -> {
                    String MachineCode = constraint.toString();
                    return dbhelper.SearchSpecificMachine(MachineCode);

                });
                // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText);
                ca.setFilterQueryProvider(constraint -> {
                    String MachineCode = constraint.toString();
                    return dbhelper.SearchMachine(MachineCode);

                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        searchView.requestFocus();

        listMachines = dialogView.findViewById(R.id.lvMachines);
        getMachineList();
        listMachines.setOnItemClickListener((parent, selectedView, arg2, arg3) -> {

            textMachineId = selectedView.findViewById(R.id.txtAccountId);
            textMachineNo = selectedView.findViewById(R.id.txtUserName);
            textMachineOP = selectedView.findViewById(R.id.tvOperators);
            String MachineNo = textMachineNo.getText().toString();
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("MachineNo", MachineNo);
            edit.apply();
            edit.putInt("Operators", Integer.parseInt(textMachineOP.getText().toString()));
            edit.apply();
            Log.d("Accounts", "Selected Account Id : " + MachineNo);
            sDate = dateTimeFormatA.format(new Date(getDate()));
            tvMachine.setText(textMachineNo.getText().toString() + " - " + textMachineOP.getText().toString() + " Operators");
            edtwmtCheckIn.setText(String.valueOf(getcheckinWeighment(MachineNo, sDate)));
            dMachine.dismiss();
        });


        dialogBuilder.setPositiveButton("BACK", (dialog, whichButton) -> {
            //do something with edt.getText().toString();

        });

        dMachine = dialogBuilder.create();
        dMachine.show();


    }

    public void getMachineList() {

        try {
            int ROWID = 0;
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.MACHINE_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);

            String[] from = {Database.ROW_ID, Database.MC_ID, Database.MC_NAME};
            int[] to = {R.id.txtAccountId, R.id.txtUserName, R.id.tvOperators};


            ca = new SimpleCursorAdapter(this, R.layout.list_item, accounts, from, to);

            listMachines.setAdapter(ca);
            // dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void TaskList() {
        taskdata.clear();
        int mtype = 2;
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select tkID,tkName from tasks where tkType='" + mtype + "'", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    tasks = c.getString(c.getColumnIndex("tkName"));
                    taskdata.add(tasks);

                } while (c.moveToNext());
            }
        }


        taskadapter = new ArrayAdapter<String>(CheckInOPActivity.this, R.layout.spinner_item, taskdata);
        taskadapter.setDropDownViewResource(R.layout.spinner_item);
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

    private void Employee() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CheckInOPActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_employee_list, null);
        dialogBuilder.setView(dialogView);

        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("Employee List");

        searchView = dialogView.findViewById(R.id.searchView);
        searchView.setQueryHint("Search Employee No ...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(constraint -> {
                    String EmployeeCode = constraint.toString();
                    return dbhelper.SearchSpecificEmployee(EmployeeCode);

                });
                // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText);
                ca.setFilterQueryProvider(constraint -> {
                    String EmployeeCode = constraint.toString();
                    return dbhelper.SearchEmployee(EmployeeCode);

                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        searchView.requestFocus();

        listEmployees = dialogView.findViewById(R.id.lvEmployees);
        getEmployeeList();
        listEmployees.setOnItemClickListener((parent, selectedView, arg2, arg3) -> {

            textAccountId = selectedView.findViewById(R.id.txtAccountId);
            textEmployee = selectedView.findViewById(R.id.tv_name);
            textEmployeeNo = selectedView.findViewById(R.id.tv_number);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("Employee", textEmployee.getText().toString());
            edit.commit();
            edit.putString("EmployeeNo", textEmployeeNo.getText().toString());
            edit.commit();
            Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());

            tvEmployee.setText(textEmployeeNo.getText().toString() + " - " + textEmployee.getText().toString());
            dEmployee.dismiss();

        });


        dialogBuilder.setPositiveButton("BACK", (dialog, whichButton) -> {
            //do something with edt.getText().toString();

        });

        dEmployee = dialogBuilder.create();
        dEmployee.show();

    }

    public void getEmployeeList() {

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


            listEmployees.setAdapter(ca);
            listEmployees.setTextFilterEnabled(true);
            //dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    //This is method to call the date and not accessible outside this class
    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }

}
