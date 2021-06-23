package com.plantation.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Michael on 30/06/2016.
 */
public class AllocatedTaskDetailsActivity extends AppCompatActivity {
    public static final String FINGERPRINT = "FingerPrint";
    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;
    public Toolbar toolbar;
    public SimpleCursorAdapter ca;
    DBHelper dbhelper;
    ListView listTasks;
    Button btAddTasks, btn_svTask;
    EditText tkcode, tkname, tktype;
    CheckBox overtime, multiple;
    String s_checkout, s_checkouttime, s_checkoutmethod, s_overtime, s_multiple;
    String accountId;
    TextView textAccountId, txtEmployeeNo, txtTaskCode;
    Boolean success = true;
    SQLiteDatabase db;
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat timeFormat;
    SimpleDateFormat dateFormat;
    SimpleDateFormat dateOnlyFormat;
    SimpleDateFormat BatchDateFormat;
    String ColDate, Time;

    SearchView searchView;
    Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listtasks);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Allocated Tasks");

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

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(AllocatedTaskDetailsActivity.this);
        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();

        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        BatchDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        dbhelper = new DBHelper(getApplicationContext());
        btAddTasks = findViewById(R.id.btAddTasks);
        btAddTasks.setVisibility(View.GONE);
        btAddTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //showAddTask();
            }
        });
        listTasks = this.findViewById(R.id.lvTasks);
        listTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                txtEmployeeNo = selectedView.findViewById(R.id.tv_number);
                txtTaskCode = selectedView.findViewById(R.id.tv_task);

                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                ShowTask();

            }
        });

        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search Employee ...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String TaskCode = constraint.toString();
                        return dbhelper.SearchSpecificAllocTask(TaskCode);

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
                        String TaskCode = constraint.toString();
                        return dbhelper.SearchAllocTask(TaskCode);

                    }
                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        searchView.requestFocus();


    }


    public void ShowTask() {
        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        String EmployeeNo, EmployeeName = "", Task;
        EmployeeNo = txtEmployeeNo.getText().toString();
        Task = txtTaskCode.getText().toString();
        Cursor account = db.query(Database.EM_TABLE_NAME, null,
                " emID = ?", new String[]{EmployeeNo}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            EmployeeName = account.getString(account
                    .getColumnIndex(Database.EM_NAME));
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Employee Checkout");
        dialogBuilder.setMessage(Html.fromHtml(
                "<font color='#FA0703'>Checkout this </font>" +
                        "Employee No:<font color='#0036ff'>\n" + EmployeeNo + "</font> Name: <font color='#0036ff'> " + EmployeeName + "</font> From Task: <font color='#0036ff'>" + Task + "</font>"));
        accountId = textAccountId.getText().toString();


        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                dialog.dismiss();

            }
        });
        dialogBuilder.setNegativeButton("CheckOut", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                CheckOut();
                getdata();


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
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
            ColDate = format1.format(date);
            Time = format2.format(cal.getTime());
            s_checkout = "1";
            s_checkouttime = ColDate + " " + Time;
            if (mSharedPrefs.getString("vModes", "FingerPrint").equals(FINGERPRINT)) {
                s_checkoutmethod = "1";
            } else if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {
                s_checkoutmethod = "2";

            } else if (mSharedPrefs.getString("vModes", "Manual").equals(MANUAL)) {
                s_checkoutmethod = "3";

            } else {
                s_checkoutmethod = "3";
            }

            accountId = textAccountId.getText().toString();

            ContentValues values = new ContentValues();
            values.put(Database.Checkout, s_checkout);
            values.put(Database.CheckoutTime, s_checkouttime);
            values.put(Database.CheckoutMethod, s_checkoutmethod);


            long rows = db.update(Database.EM_TASK_ALLOCATION_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            //db.close();
            if (rows > 0) {
                Toast.makeText(this, "CheckOut Successful!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update CheckOut!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        getdata();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {
            String ROWID = "0";
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            //  Cursor accounts = db.query(true, Database.EM_TASK_ALLOCATION_TABLE_NAME, null, Database.Checkout + "='" + ROWID + "'", null, null, null, null, null, null);
            Cursor accounts = db.rawQuery("SELECT TaskAllocation._id,TaskCode,EmployeeNo,employees.emName,CollDate,CaptureTime FROM " + Database.EM_TASK_ALLOCATION_TABLE_NAME + ", " + Database.EM_TABLE_NAME + " WHERE  emID=EmployeeNo AND " + Database.Checkout + "=?", new String[]{ROWID}, null);
            if (accounts.getCount() == 0) {
                new NoReceipt().execute();
                // Toast.makeText(this, "no task records", Toast.LENGTH_LONG).show();
            }
            String[] from = {Database.ROW_ID, Database.TaskCode, Database.EmployeeNo, Database.EM_NAME, Database.CollDate, Database.CaptureTime};
            int[] to = {R.id.txtAccountId, R.id.tv_task, R.id.tv_number, R.id.tv_name, R.id.tv_date, R.id.tv_checkin};


            ca = new SimpleCursorAdapter(this, R.layout.talloc_list, accounts, from, to);

            listTasks = this.findViewById(R.id.lvTasks);
            listTasks.setAdapter(ca);
            listTasks.setTextFilterEnabled(true);
            // dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        // mIntent = new Intent(FarmerDetailsActivity.this,MainActivity.class);
        //startActivity(mIntent);
        return;
    }

    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }

    private class NoReceipt extends AsyncTask<Void, Void, String> {


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {


            try {
                Thread.sleep(50);
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
            text.setText("No Employee to CheckOut");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
        }
    }
}
