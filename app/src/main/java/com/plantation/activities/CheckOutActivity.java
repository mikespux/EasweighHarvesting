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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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


public class CheckOutActivity extends AppCompatActivity {

    public static final String TAG = "Tasking";
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
    String smachineNo, semployeeNo, scheckoutTime;
    int checkoutWeighment;
    String accountId;
    EditText edtwmtCheckOut;
    AlertDialog dCheckout;
    TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_operators);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check-Out Operators");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void initializer() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(CheckOutActivity.this);
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
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(selectedView.getContext());
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_checkout, null);
                dialogBuilder.setView(dialogView);

                TextView toolbar = dialogView.findViewById(R.id.app_bar);
                toolbar.setText("Operator Check-Out");

                edtwmtCheckOut = dialogView.findViewById(R.id.edtwmtCheckOut);
                edtwmtCheckOut.setText(String.valueOf(getcheckoutWeighment(smachineNo, MDate)));

                tvMessage = dialogView.findViewById(R.id.tvMessage);
                tvMessage.setText(Html.fromHtml("<font color='#FA0703'>Check Out </font>" +
                        "Employee No:<font color='#0036ff'>\n" + textEmployeeNo.getText().toString() + "</font>"));

                dialogBuilder.setNegativeButton("CHECKOUT", (dialog, whichButton) -> {
                    //do something with edt.getText().toString();
                    dialog.dismiss();

                });
                dialogBuilder.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //pass
                        dialog.dismiss();

                    }
                });
                dialogBuilder.setNeutralButton("DELETE", (dialog, whichButton) -> {
                    //do something with edt.getText().toString();


                });
                dCheckout = dialogBuilder.create();
                dCheckout.show();
                dCheckout.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.WHITE);
                dCheckout.getButton(AlertDialog.BUTTON_NEUTRAL).setBackgroundColor(Color.RED);
                dCheckout.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteEmployee();
                    }
                });
                dCheckout.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
                dCheckout.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.BLUE);
                dCheckout.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkoutEmployee();
                    }
                });
            }
        });


        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search Employee No ...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(constraint -> {
                    String EmployeeCode = constraint.toString();
                    return dbhelper.SearchSpecificEmployeeOperator(EmployeeCode);

                });
                // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText);
                ca.setFilterQueryProvider(constraint -> {
                    String EmployeeCode = constraint.toString();
                    return dbhelper.SearchEmployeeOperator(EmployeeCode);

                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        searchView.requestFocus();


    }

    public int getcheckoutWeighment(String machineNo, String sDate) {
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

    public void CheckOut() {
        try {
            smachineNo = prefs.getString("MachineNo", "");
            semployeeNo = prefs.getString("EmployeeNo", "");
            Calendar cal = Calendar.getInstance();
            scheckoutTime = dateTimeFormat.format(cal.getTime());
            MDate = dateTimeFormatB.format(cal.getTime());
            if (edtwmtCheckOut.getText().length() == 0) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Enter Weighment CheckOut");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                return;
            } else {
                checkoutWeighment = Integer.parseInt(edtwmtCheckOut.getText().toString());
                if (checkoutWeighment == 1) {
                    deleteEmployee();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Enter Weighment CheckOut More than (1) or Delete Check-In");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }

            }


            ContentValues values = new ContentValues();
            values.put(Database.CHECKOUTWEIGHMENT, checkoutWeighment);
            values.put(Database.CHECKOUTTIME, scheckoutTime);
            values.put(Database.MSTATUS, 2);


            long rows = db.update(Database.MACHINEOP_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            if (rows > 0) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText(Html.fromHtml(
                        "<font color='#FA0703'>Checked-Out Successfully!!\n</font>" +
                                "Employee No:<font color='#0036ff'>\n" + semployeeNo + "</font>"));
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                getdata();
                dCheckout.dismiss();
            } else {
                Toast.makeText(this, "Sorry! Could not CheckOut!",
                        Toast.LENGTH_LONG).show();
            }


        } catch (Exception ex) {

            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();

        }
    }

    public void checkoutEmployee() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Html.fromHtml(
                "Are you sure you want to <font color='#FA0703'>CHECK OUT!!\n</font>" +
                        " this Employee?"))
                .setCancelable(false)
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CheckOut();

                    }
                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void deleteEmployee() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Html.fromHtml(
                "Are you sure you want to <font color='#FA0703'>DELETE!!\n</font>" +
                        " this CHECK-IN?"))
                .setCancelable(false)
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteCurrentAccount();

                    }
                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void deleteCurrentAccount() {
        try {
            DBHelper dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            int rows = db.delete(Database.MACHINEOP_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if (rows == 1) {
                Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show();

                getdata();
            } else {
                Toast.makeText(this, "Could not delete!", Toast.LENGTH_LONG).show();
            }
            // }

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

            SQLiteDatabase db = dbhelper.getReadableDatabase();
            //  Cursor accounts = db.query(true, Database.MACHINEOP_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);
            Calendar cal = Calendar.getInstance();
            MDate = dateTimeFormatB.format(cal.getTime());
            smachineNo = prefs.getString("MachineNo", "");
            Cursor accounts = db.rawQuery("select * from " + Database.MACHINEOP_TABLE_NAME + " WHERE "
                    + Database.MDATE + " ='" + MDate + "' and " + Database.MACHINENUMBER + " ='" + smachineNo + "'", null);// and " + Database.MSTATUS + " ='1'
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
