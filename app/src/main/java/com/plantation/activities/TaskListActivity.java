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
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Michael on 30/06/2016.
 */
public class TaskListActivity extends AppCompatActivity {
    public static final String FINGERPRINT = "FingerPrint";
    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    public static final String BOTH = "Both";
    static SharedPreferences mSharedPrefs, prefs;
    static EditText etTaskDate;
    public Toolbar toolbar;
    public SimpleCursorAdapter ca;
    ListView listTasks;
    String accountId;
    TextView textAccountId, tv_type, tv_code, tv_overtime, tv_multiple;
    Boolean success = true;
    SearchView searchView;
    Intent mIntent;
    SQLiteDatabase db;
    DBHelper dbhelper;
    Button btnBatchOn, btnBatchOff;
    String BaseDate, BatchDate, DelDate;
    int CLOSED = 1;
    String DataDevice, BatchNumber, UserID;
    String Factory;
    int BatchNo = 1;
    DecimalFormat formatter;
    Spinner Spinnersession;
    String BSession;
    AlertDialog b;
    Button pickTaskDate, btnNext;
    String TaskDate;
    private TextView dateDisplay, dtpBatchOn, textClock, txtBatchNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasklist);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tasks");

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
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(TaskListActivity.this);
        prefs = PreferenceManager.getDefaultSharedPreferences(TaskListActivity.this);
        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();
        formatter = new DecimalFormat("00");

        listTasks = this.findViewById(R.id.lvTasks);
        listTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                tv_type = selectedView.findViewById(R.id.tv_type);
                tv_code = selectedView.findViewById(R.id.tv_number);
                tv_overtime = selectedView.findViewById(R.id.tv_ot);
                tv_multiple = selectedView.findViewById(R.id.tv_multiple);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());


                if (tv_type.getText().toString().equals("4") || (tv_type.getText().toString().equals("5"))) {

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("TaskType", tv_type.getText().toString());
                    edit.commit();
                    edit.putString("TaskCode", tv_code.getText().toString());
                    edit.commit();
                    edit.putString("TaskOT", tv_overtime.getText().toString());
                    edit.commit();
                    edit.putString("TaskMT", tv_multiple.getText().toString());
                    edit.commit();
                    if (mSharedPrefs.getString("vModes", "FingerPrint").equals(FINGERPRINT)) {
                        finish();
                        mIntent = new Intent(getApplicationContext(), TaskAllocationIdentifyFingerprints.class);
                        startActivity(mIntent);

                    } else if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {
                        mIntent = new Intent(getApplicationContext(), EmployeeCardBrowserActivity.class);
                        startActivity(mIntent);

                    } else if (mSharedPrefs.getString("vModes", "Manual").equals(MANUAL)) {
                        mIntent = new Intent(getApplicationContext(), EmployeeBrowserActivity.class);
                        startActivity(mIntent);

                    } else if (mSharedPrefs.getString("vModes", "Both").equals(BOTH)) {

                        mIntent = new Intent(getApplicationContext(), EmployeeBrowserActivity.class);
                        startActivity(mIntent);
                    } else {
                        mIntent = new Intent(getApplicationContext(), EmployeeBrowserActivity.class);
                        startActivity(mIntent);
                    }
                    /*else  if (mSharedPrefs.getString("vModes", "Both").toString().equals(BOTH)){
                        mIntent = new Intent(getApplicationContext(), VerificationModeActivity.class);
                        startActivity(mIntent);
                    }*/

                } else {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Choose General or Cultivation Activity e.g Task Type 4 or 5");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();


                }

            }
        });


        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search Task Code ...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String TaskCode = constraint.toString();
                        return dbhelper.SearchSpecificTask(TaskCode);

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
                        return dbhelper.SearchTask(TaskCode);

                    }
                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        searchView.requestFocus();
        showSearchReceipt();

    }

    public void showSearchReceipt() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_taskdate, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Task Date");
        etTaskDate = dialogView.findViewById(R.id.edtFromDate);

        Date date = new Date(getDate());
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        etTaskDate.setText(format1.format(date));


        pickTaskDate = dialogView.findViewById(R.id.btnFrom);
        pickTaskDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");

            }
        });


        btnNext = dialogView.findViewById(R.id.btnNext);
        btnNext.setVisibility(View.VISIBLE);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskDate = etTaskDate.getText().toString();

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("TaskDate", TaskDate);
                edit.commit();

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

    @Override
    protected void onStart() {
        super.onStart();
        getdata();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {
            int ROWID = 0;
            String General = "4";
            String Cultivation = "5";
            if (General.length() != 0) {

                General = "%" + General + "%";
            }
            if (Cultivation.length() != 0) {
                Cultivation = "%" + Cultivation + "%";
            }
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            //  Cursor accounts = db.query(true, Database.TASK_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);
            Cursor accounts = db.rawQuery(" select * from " + Database.TASK_TABLE_NAME + " where " + Database.ROW_ID + ">'" + ROWID + "' and" +
                    " tkType like '" + General + "' or tkType like '" + Cultivation + "'", null);
            if (accounts.getCount() == 0) {
                Toast.makeText(this, "no task records", Toast.LENGTH_LONG).show();
            }
            String[] from = {Database.ROW_ID, Database.TK_ID, Database.TK_NAME, Database.TK_TYPE, Database.TK_OT, Database.TK_MT};
            int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_name, R.id.tv_type, R.id.tv_ot, R.id.tv_multiple};


            ca = new SimpleCursorAdapter(this, R.layout.task_list, accounts, from, to);

            listTasks = this.findViewById(R.id.lvTasks);
            listTasks.setAdapter(ca);
            listTasks.setTextFilterEnabled(true);
            //dbhelper.close();
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

    //This is method to call the date and not accessible outside this class
    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
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
            SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
            etTaskDate.setText(format1.format(chosenDate));
        }
    }

}
