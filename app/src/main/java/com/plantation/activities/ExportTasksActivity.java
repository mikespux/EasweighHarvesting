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
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Created by Michael on 30/06/2016.
 */
@SuppressWarnings("ALL")
public class ExportTasksActivity extends AppCompatActivity {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;
    public Toolbar toolbar;
    public SimpleCursorAdapter ca;
    DBHelper dbhelper;
    Button btnExport, btnBack;
    Intent mIntent;
    File file;
    String path;
    Handler _handler;
    int count = 0;
    ArcProgress arcProgress;
    EditText etFrom, etTo, etEmployeeNo;
    String fromDate, toDate;
    String condition = " _id > 0 ";
    String condition1 = " _id > 0 ";
    AlertDialog b;
    SQLiteDatabase db;
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat timeFormat;
    SimpleDateFormat dateFormat;
    SimpleDateFormat dateOnlyFormat;
    SimpleDateFormat BatchDateFormat;
    ListView listReciepts;
    String BatchDate;
    TextView textBatchNo, textBatchDate, textDelNo, textStatus, txtAccountId;
    TextView textDeliNo, textBatchCrates, textTrailer;
    String cond;
    SearchView searchView;
    int closed = 1;
    int closed1 = 1;
    int cloudid = 0;
    Cursor curBatchNames, curCSV;
    Cursor tasks;
    String DelNo;
    String error;
    String BatchNo;
    String FileName;
    String RecordType;
    String batchNo, deviceID, stringOpenDate, deliveryNoteNo, Weight, dipatchedTime, userID, userID2, stringOpenTime, weighingSession,
            closedb, stringCloseTime, factory, tractorNo, trailerNo, TransporterCode, DelivaryNo, Co_prefix, Current_User;
    String ColDate, Time, DataDevice, BatchNO, TaskCode, EmployeeNo;
    String WorkerNo, FieldClerk, TaskType;
    String TaskUnits, GradeCode, EstateCode, DivisionCode;
    String GrossTotal, TareWeight, Crates;
    String UnitPrice, RecieptNo, WeighmentNo, NetWeight, FieldCode;
    private int progressStatus = 0;
    private ProgressDialog mProgressDialog;
    private ProgressBar progressBar;
    private TextView textView, txtFNo;
    private Button pickFrom, pickTo;
    private Button btnSearchReceipt;
    private Button btnFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_export);
        setupToolbar();
        initializer();
        _handler = new Handler();
    }

    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Export CSV");

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
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        FileName = format2.format(cal.getTime());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(ExportTasksActivity.this);
        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();

        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        BatchDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }


        });
        btnExport = (Button) findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                curBatchNames = db.rawQuery("SELECT * FROM " + Database.TASKSUPPLIESCONSIGNMENTS_TABLE_NAME + " where " + Database.BatchDate + " = '" + fromDate + "'" + "and SignedOff = 1", null);
                count = curBatchNames.getCount();
                if (count == 0) {
                    finish();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("No Batches Found To Export");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }


                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage(Html.fromHtml("<font color='#4285F4'>Are you sure you want to export all to csv ?</font>"))
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                b.dismiss();
                                new ExportAllAsync().execute();
                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                            }
                        });
                final AlertDialog alert2 = builder.create();
                alert2.show();


            }


        });
        btnFilter = (Button) findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Restart().execute();

            }
        });
        listReciepts = (ListView) this.findViewById(R.id.lvReciepts);
        listReciepts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textBatchNo = (TextView) selectedView.findViewById(R.id.tv_reciept);
                textBatchDate = (TextView) selectedView.findViewById(R.id.tv_date);
                textDelNo = (TextView) selectedView.findViewById(R.id.tv_number);
                txtAccountId = (TextView) selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textBatchNo.getText().toString());
                showRecieptDetails();
            }
        });
        String selectQuery = "SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE SignedOff=1";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() <= 0) {
            Toast.makeText(ExportTasksActivity.this, "No Batch Dispatched to Export !!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        cursor.close();
        showSearchReceipt();
    }

    public void showSearchReceipt() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_search_batches, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Search Batches");
        etFrom = (EditText) dialogView.findViewById(R.id.edtFromDate);
        etTo = (EditText) dialogView.findViewById(R.id.edtToDate);
        etTo.setVisibility(View.GONE);
        etEmployeeNo = (EditText) dialogView.findViewById(R.id.edtFarmerNo);
        TextView To = (TextView) dialogView.findViewById(R.id.to);
        To.setVisibility(View.GONE);
        Date date = new Date(getDate());
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        etFrom.setText(format1.format(date));
        etTo.setText(format1.format(date));


        pickFrom = (Button) dialogView.findViewById(R.id.btnFrom);
        pickFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");

            }
        });

        pickTo = (Button) dialogView.findViewById(R.id.btnTo);
        pickTo.setVisibility(View.GONE);
        pickTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment2();
                newFragment.show(getFragmentManager(), "datePicker");

            }
        });


        btnSearchReceipt = (Button) dialogView.findViewById(R.id.btn_SearchReceipt);
        btnSearchReceipt.setVisibility(View.VISIBLE);
        btnSearchReceipt.setText("SEARCH BATCH");
        btnSearchReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDate = etFrom.getText().toString();
                toDate = etTo.getText().toString();
                EmployeeNo = etEmployeeNo.getText().toString();

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("fromDate", fromDate);
                edit.commit();
                edit.putString("toDate", toDate);
                edit.commit();
                edit.putString("EmployeeNo", EmployeeNo);
                edit.commit();

                if (fromDate.length() > 0)
                    condition += " and  " + Database.BatchDate + " = '" + fromDate + "'";
                if (closed > 0)
                    condition += " and  " + Database.Closed + " = '" + closed + "'";
                condition += " and  " + Database.SignedOff + "='" + closed + "'";

                //getSearch();
                ca.getFilter().filter(condition.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String reciept = constraint.toString();
                        return dbhelper.SearchBatchByDate(reciept);
                    }
                });

                b.dismiss();
            }
        });


        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    return true;
                }
                return false;
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
        final View dialogView = inflater.inflate(R.layout.activity_listtaskbatches, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Tasks Allocated");
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

        searchView = (SearchView) dialogView.findViewById(R.id.searchView);
        //searchView.setQueryHint("Search Farmer No ...");
        searchView.setVisibility(View.GONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String EmployeeNo = constraint.toString();
                        return dbhelper.SearchSpecificOnR(EmployeeNo, cond);

                    }
                });
                // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String EmployeeNo = constraint.toString();
                        return dbhelper.SearchOnR(EmployeeNo);

                    }
                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        Cursor batch = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                + Database.ROW_ID + " ='" + txtAccountId.getText().toString() + "'", null);
        textBatchCrates = (TextView) dialogView.findViewById(R.id.textBatchDetails);
        textDeliNo = (TextView) dialogView.findViewById(R.id.textDeliNo);
        textTrailer = (TextView) dialogView.findViewById(R.id.textTrailer);
        if (batch != null) {

            batch.moveToFirst();

            /*textBatchCrates.setText( batch.getString(batch.getColumnIndex(Database.BatchCrates)));
            textDeliNo.setText(batch.getString(batch.getColumnIndex(Database.DelivaryNO)));
            textTrailer.setText(batch.getString(batch.getColumnIndex(Database.Tractor)));*/

        }
        batch.close();

        Cursor accounts = db.rawQuery("select * from " + Database.EM_TASK_ALLOCATION_TABLE_NAME + " WHERE "
                + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNo + "'", null);
        TextView txtStatus = (TextView) dialogView.findViewById(R.id.textStatus);

        if (accounts.getCount() == 0) {
            txtStatus.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.GONE);
        } else {
            //Toast.makeText(this, "records found", Toast.LENGTH_LONG).show();}


            final DecimalFormat df = new DecimalFormat("#0.0#");
            Cursor c = db.rawQuery("select " +
                    "" + Database.DataCaptureDevice +
                    ",COUNT(" + Database.ROW_ID + ")" +

                    " from TaskAllocation WHERE "
                    + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNo + "'", null);
            if (c != null) {

                c.moveToFirst();
                txtStatus.setVisibility(View.VISIBLE);
                txtStatus.setText("Number of Tasks: " + c.getInt(1));

            }
            c.close();

        }
        while (accounts.moveToNext()) {
            String from[] = {Database.ROW_ID, Database.EmployeeNo, Database.TaskCode};
            int to[] = {R.id.txtAccountId, R.id.tv_number, R.id.tv_phone};


            ca = new SimpleCursorAdapter(dialogView.getContext(), R.layout.t_list, accounts, from, to);

            ListView listBatches = (ListView) dialogView.findViewById(R.id.lvUsers);
            listBatches.setAdapter(ca);
            listBatches.setTextFilterEnabled(true);
            //db.close();
            //dbhelper.close();
        }


        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


            }
        });
        dialogBuilder.setNegativeButton("Export", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        b = dialogBuilder.create();
        b.show();
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.BLUE);
        b.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        b.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);

        b.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                builder.setMessage(Html.fromHtml("<font color='#4285F4'>Are you sure you want to export csv ?</font>"))
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                b.dismiss();
                                new ExportFileAsync().execute();

                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                b.dismiss();

                            }
                        });
                final AlertDialog alert2 = builder.create();
                alert2.show();
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                if (wantToCloseDialog)
                    b.dismiss();
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void openFolder() {
        /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory()+ "/" + "/Easyweigh/Masters/");
        intent.setDataAndType(uri, "text/csv");
        startActivity(Intent.createChooser(intent, "Open folder"));*/
        Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/" + "/Easyweigh/Masters/");
        chooser.addCategory(Intent.CATEGORY_OPENABLE);
        chooser.setDataAndType(uri, "text/csv");
        // startActivity(chooser);
        try {
            // startActivityForResult(chooser, SELECT_FILE);
            startActivity(Intent.createChooser(chooser, "Open folder"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Data Export ...");
                mProgressDialog.setMessage("Exporting data from file ...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
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

            Cursor accounts = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                    + Database.Closed + " ='" + closed + "' and SignedOff=1", null);
            if (accounts.getCount() > 0) {
                String from[] = {Database.ROW_ID, Database.DeliveryNoteNumber, Database.DataDevice, Database.BatchNumber, Database.BatchDate};
                int to[] = {R.id.txtAccountId, R.id.tv_number, R.id.tv_device, R.id.tv_reciept, R.id.tv_date};


                ca = new SimpleCursorAdapter(this, R.layout.batch_list, accounts, from, to);

                ListView listBatches = (ListView) this.findViewById(R.id.lvReciepts);

                listBatches.setAdapter(ca);
                listBatches.setTextFilterEnabled(true);
                db.close();
                //dbhelper.close();
            } else {

                new NoReceipt().execute();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed


        finish();
        /*btnExport.setVisibility(View.VISIBLE);
        mIntent = new Intent(ExportActivity.this,MainActivity.class);
       startActivity(mIntent);*/
        return;
    }

    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
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

    class ExportFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(DIALOG_DOWNLOAD_PROGRESS);
            arcProgress = (ArcProgress) findViewById(R.id.arc_progress);
            arcProgress.setProgress(0);
            arcProgress.setVisibility(View.VISIBLE);
            textView = (TextView) findViewById(R.id.textView1);
            btnExport.setVisibility(View.GONE);
            listReciepts.setVisibility(View.GONE);
            btnFilter.setVisibility(View.GONE);

            File dbFile = getDatabasePath(DBHelper.DB_NAME);
            //DBHelper dbhelper = new DBHelper(getApplicationContext());
            File exportDir = new File(Environment.getExternalStorageDirectory() + "/" + "/Easyweigh/Exports/");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            DelNo = textDelNo.getText().toString();
            String fileName = "TASK-" + DelNo + ".csv";
            file = new File(exportDir, fileName);
        }

        @Override
        protected String doInBackground(String... aurl) {

            try {
                BatchNo = textBatchNo.getText().toString();

                String dbtBatchOn = textBatchDate.getText().toString() + " 00:00:00";
                String dbtBatchOn1 = textBatchDate.getText().toString();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                Date date = null;
                try {
                    date = fmt.parse(dbtBatchOn);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                BatchDate = format1.format(date);

                if (dbtBatchOn1.length() > 0)
                    condition += " and  " + Database.BatchDate + " = '" + dbtBatchOn1 + "'";

                if (BatchNo.length() > 0)
                    condition += " and  " + Database.BatchNumber + " = '" + BatchNo + "'";

                if (closed1 > 0)
                    condition += " and  " + Database.Closed + " = '" + closed1 + "'";
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                curCSV = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where " + condition + " and SignedOff=1", null);
                count = curCSV.getCount();

                //csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {

                    Date openTime = dateTimeFormat.parse(curCSV.getString(curCSV.getColumnIndex(Database.BatchDate)).toString() +
                            " " +
                            curCSV.getString(curCSV.getColumnIndex(Database.OpeningTime)).toString());
                    Date closeTime = dateTimeFormat.parse(curCSV.getString(curCSV.getColumnIndex(Database.BatchDate)).toString() +
                            " " +
                            curCSV.getString(curCSV.getColumnIndex(Database.ClosingTime)).toString());
                    batchNo = curCSV.getString(curCSV.getColumnIndex(Database.BatchNumber));
                    deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
                    stringOpenDate = dateFormat.format(openTime);
                    deliveryNoteNo = curCSV.getString(curCSV.getColumnIndex(Database.DeliveryNoteNumber));
                    userID = curCSV.getString(curCSV.getColumnIndex(Database.Userid));
                    stringOpenTime = timeFormat.format(openTime);
                    if (curCSV.getString(curCSV.getColumnIndex(Database.BatchSession)) == null) {
                        weighingSession = "1";
                    } else {
                        weighingSession = curCSV.getString(curCSV.getColumnIndex(Database.BatchSession));
                    }
                    closedb = curCSV.getString(curCSV.getColumnIndex(Database.Closed));
                    stringCloseTime = timeFormat.format(closeTime);

                    Weight = curCSV.getString(curCSV.getColumnIndex(Database.TotalWeights));
                    dipatchedTime = curCSV.getString(curCSV.getColumnIndex(Database.Dispatched));
                    BatchDate = BatchDateFormat.format(closeTime);

                    factory = curCSV.getString(curCSV.getColumnIndex(Database.Factory));
                    if (curCSV.getString(curCSV.getColumnIndex(Database.Transporter)) == null) {
                        TransporterCode = "";
                    } else {
                        TransporterCode = curCSV.getString(curCSV.getColumnIndex(Database.Transporter));
                    }
                    tractorNo = curCSV.getString(curCSV.getColumnIndex(Database.Tractor));
                    trailerNo = curCSV.getString(curCSV.getColumnIndex(Database.Trailer));

                    if (curCSV.getString(curCSV.getColumnIndex(Database.DelivaryNO)) == null) {
                        DelivaryNo = "";
                    } else {
                        DelivaryNo = curCSV.getString(curCSV.getColumnIndex(Database.DelivaryNO));
                    }
                    Co_prefix = mSharedPrefs.getString("company_prefix", "").toString();
                    Current_User = prefs.getString("user", "");
                    ;

                    //Which column you want to export
                    String Batches[] = {"1",
                            batchNo,
                            deviceID,
                            userID,
                            deliveryNoteNo,
                            stringOpenTime,
                            stringCloseTime,
                            Weight,
                            dipatchedTime,
                            factory,
                            tractorNo,
                            trailerNo,
                            DelivaryNo + "\r\n"};

                    csvWrite.writeNext(Batches);
                    BatchNo = curCSV.getString(6);
                    progressStatus++;
                    publishProgress("" + progressStatus);

                    progressStatus++;
                    publishProgress("" + progressStatus);
                }
                curCSV.close();
                tasks = db.rawQuery("select * from " + Database.EM_TASK_ALLOCATION_TABLE_NAME + " WHERE "
                        + Database.CollDate + " ='" + stringOpenDate + "' and " + Database.BatchNo + " ='" + BatchNo + "'  and " + Database.CloudID + " ='" + cloudid + "'", null);
                count = count + tasks.getCount();
                //csvWrite.writeNext(tasks.getColumnNames());
                while (tasks.moveToNext()) {


                    ColDate = tasks.getString(tasks.getColumnIndex(Database.CollDate));
                    String dbtTransOn = ColDate + " 00:00:00";
                    SimpleDateFormat frmt = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    Date date1 = null;
                    try {
                        date1 = frmt.parse(dbtTransOn);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
                    String TransDate = format2.format(date1);

                    Time = tasks.getString(tasks.getColumnIndex(Database.CaptureTime));
                    BatchNo = tasks.getString(tasks.getColumnIndex(Database.BatchNo));
                    DataDevice = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
                    EmployeeNo = tasks.getString(tasks.getColumnIndex(Database.EmployeeNo));
                    TaskCode = tasks.getString(tasks.getColumnIndex(Database.TaskCode));
                    TaskType = tasks.getString(tasks.getColumnIndex(Database.TaskType));
                    TaskUnits = tasks.getString(tasks.getColumnIndex(Database.TaskUnits));
                    EstateCode = tasks.getString(tasks.getColumnIndex(Database.SourceEstate));
                    DivisionCode = tasks.getString(tasks.getColumnIndex(Database.SourceDivision));
                    FieldCode = tasks.getString(tasks.getColumnIndex(Database.SourceField));

                    //RecieptNo =tasks.getString(tasks.getColumnIndex(Database.DataCaptureDevice))+tasks.getString(tasks.getColumnIndex(Database.ReceiptNo));
                    FieldClerk = tasks.getString(tasks.getColumnIndex(Database.FieldClerk));

                    Co_prefix = mSharedPrefs.getString("company_prefix", "").toString();
                    Current_User = prefs.getString("user", "");

                    String TaskAllocations[] = {"2",
                            TransDate,
                            DataDevice,
                            Time,
                            FieldClerk,
                            EstateCode,
                            DivisionCode,
                            FieldCode,
                            BatchNo,
                            EmployeeNo,
                            TaskCode,
                            TaskType,
                            TaskUnits + "\r\n"};
                    csvWrite.writeNext(TaskAllocations);

                    progressStatus++;
                    publishProgress("" + progressStatus);
                }
                csvWrite.close();

                tasks.close();
            } catch (Exception sqlEx) {
                Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
            }


            return null;

        }

        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
            //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setMax(count);
            arcProgress.setBottomText("EXPORTING ...");
            textView.setText(Integer.parseInt(progress[0]) + "/" + count + " Records");
        }

        @Override
        protected void onPostExecute(String unused) {
            // dismissDialog(DIALOG_DOWNLOAD_PROGRESS);

            //mIntent = new Intent(ExportActivity.this,MainActivity.class);
            //startActivity(mIntent);

            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText(count + " Records Exported successfully");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();


            arcProgress.setVisibility(View.GONE);
            btnExport.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            listReciepts.setVisibility(View.VISIBLE);
            btnFilter.setVisibility(View.VISIBLE);
            new Restart().execute();

            // b.dismiss();
            //Toast.makeText(ExportMasterActivity.this, "Data Exported successfully!!", Toast.LENGTH_LONG).show();
        }
    }

    class ExportAllAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(DIALOG_DOWNLOAD_PROGRESS);
            arcProgress = (ArcProgress) findViewById(R.id.arc_progress);
            arcProgress.setProgress(0);
            arcProgress.setVisibility(View.VISIBLE);
            textView = (TextView) findViewById(R.id.textView1);
            btnExport.setVisibility(View.GONE);
            listReciepts.setVisibility(View.GONE);
            btnFilter.setVisibility(View.GONE);

            File dbFile = getDatabasePath(DBHelper.DB_NAME);
            //DBHelper dbhelper = new DBHelper(getApplicationContext());

        }

        @Override
        protected String doInBackground(String... aurl) {

            try {
                File exportDir = new File(Environment.getExternalStorageDirectory() + "/" + "/Easyweigh/Exports/");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                SQLiteDatabase db = dbhelper.getReadableDatabase();
                curCSV = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where " + Database.BatchDate + " = '" + fromDate + "'" + "and SignedOff = 1", null);
                count = curCSV.getCount();
                //csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {
                    DelNo = curCSV.getString(curCSV.getColumnIndex(Database.DeliveryNoteNumber));
                    String fileName = "TASK-" + DelNo + ".csv";
                    file = new File(exportDir, fileName);
                    file.createNewFile();
                    CSVWriter csvWrite = new CSVWriter(new FileWriter(file), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);


                    Date openTime = dateTimeFormat.parse(curCSV.getString(curCSV.getColumnIndex(Database.BatchDate)).toString() +
                            " " +
                            curCSV.getString(curCSV.getColumnIndex(Database.OpeningTime)).toString());
                    Date closeTime = dateTimeFormat.parse(curCSV.getString(curCSV.getColumnIndex(Database.BatchDate)).toString() +
                            " " +
                            curCSV.getString(curCSV.getColumnIndex(Database.ClosingTime)).toString());
                    batchNo = curCSV.getString(curCSV.getColumnIndex(Database.BatchNumber));
                    deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
                    stringOpenDate = dateFormat.format(openTime);
                    deliveryNoteNo = curCSV.getString(curCSV.getColumnIndex(Database.DeliveryNoteNumber));
                    userID = curCSV.getString(curCSV.getColumnIndex(Database.Userid));
                    stringOpenTime = timeFormat.format(openTime);
                    if (curCSV.getString(curCSV.getColumnIndex(Database.BatchSession)) == null) {
                        weighingSession = "1";
                    } else {
                        weighingSession = curCSV.getString(curCSV.getColumnIndex(Database.BatchSession));
                    }
                    closedb = curCSV.getString(curCSV.getColumnIndex(Database.Closed));
                    stringCloseTime = timeFormat.format(closeTime);

                    Weight = curCSV.getString(curCSV.getColumnIndex(Database.TotalWeights));
                    dipatchedTime = curCSV.getString(curCSV.getColumnIndex(Database.Dispatched));
                    BatchDate = BatchDateFormat.format(closeTime);

                    factory = curCSV.getString(curCSV.getColumnIndex(Database.Factory));
                    if (curCSV.getString(curCSV.getColumnIndex(Database.Transporter)) == null) {
                        TransporterCode = "";
                    } else {
                        TransporterCode = curCSV.getString(curCSV.getColumnIndex(Database.Transporter));
                    }
                    tractorNo = curCSV.getString(curCSV.getColumnIndex(Database.Tractor));
                    trailerNo = curCSV.getString(curCSV.getColumnIndex(Database.Trailer));

                    if (curCSV.getString(curCSV.getColumnIndex(Database.DelivaryNO)) == null) {
                        DelivaryNo = "";
                    } else {
                        DelivaryNo = curCSV.getString(curCSV.getColumnIndex(Database.DelivaryNO));
                    }
                    Co_prefix = mSharedPrefs.getString("company_prefix", "").toString();
                    Current_User = prefs.getString("user", "");
                    ;

                    //Which column you want to export
                    String Batches[] = {"1",
                            batchNo,
                            deviceID,
                            userID,
                            deliveryNoteNo,
                            stringOpenTime,
                            stringCloseTime,
                            Weight,
                            dipatchedTime,
                            factory,
                            tractorNo,
                            trailerNo,
                            DelivaryNo + "\r\n"};

                    csvWrite.writeNext(Batches);
                    BatchNo = curCSV.getString(6);
                    progressStatus++;
                    publishProgress("" + progressStatus);

                    progressStatus++;
                    publishProgress("" + progressStatus);
                    // }

                    tasks = db.rawQuery("select * from " + Database.EM_TASK_ALLOCATION_TABLE_NAME + " WHERE "
                            + Database.CollDate + " ='" + stringOpenDate + "' and " + Database.BatchNo + " ='" + BatchNo + "'  and " + Database.CloudID + " ='" + cloudid + "'", null);
                    count = count + tasks.getCount();
                    //csvWrite.writeNext(tasks.getColumnNames());
                    while (tasks.moveToNext()) {

                        ColDate = tasks.getString(tasks.getColumnIndex(Database.CollDate));
                        String dbtTransOn = ColDate + " 00:00:00";
                        SimpleDateFormat frmt = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                        Date date1 = null;
                        try {
                            date1 = frmt.parse(dbtTransOn);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
                        String TransDate = format2.format(date1);

                        Time = tasks.getString(tasks.getColumnIndex(Database.CaptureTime));
                        BatchNo = tasks.getString(tasks.getColumnIndex(Database.BatchNo));
                        DataDevice = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
                        EmployeeNo = tasks.getString(tasks.getColumnIndex(Database.EmployeeNo));
                        TaskCode = tasks.getString(tasks.getColumnIndex(Database.TaskCode));
                        TaskType = tasks.getString(tasks.getColumnIndex(Database.TaskType));
                        TaskUnits = tasks.getString(tasks.getColumnIndex(Database.TaskUnits));
                        EstateCode = tasks.getString(tasks.getColumnIndex(Database.SourceEstate));
                        DivisionCode = tasks.getString(tasks.getColumnIndex(Database.SourceDivision));
                        FieldCode = tasks.getString(tasks.getColumnIndex(Database.SourceField));

                        //RecieptNo =tasks.getString(tasks.getColumnIndex(Database.DataCaptureDevice))+tasks.getString(tasks.getColumnIndex(Database.ReceiptNo));
                        FieldClerk = tasks.getString(tasks.getColumnIndex(Database.FieldClerk));

                        Co_prefix = mSharedPrefs.getString("company_prefix", "").toString();
                        Current_User = prefs.getString("user", "");

                        String TaskAllocations[] = {"2",
                                TransDate,
                                DataDevice,
                                Time,
                                FieldClerk,
                                EstateCode,
                                DivisionCode,
                                FieldCode,
                                BatchNo,
                                EmployeeNo,
                                TaskCode,
                                TaskType,
                                TaskUnits + "\r\n"};
                        csvWrite.writeNext(TaskAllocations);

                        progressStatus++;
                        publishProgress("" + progressStatus);
                    }
                    csvWrite.close();

                    //tasks.close();

                    // curCSV.close();
                }
                curBatchNames.close();
            } catch (Exception sqlEx) {
                Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
            }


            return null;

        }

        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
            //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setMax(count);
            arcProgress.setBottomText("EXPORTING ...");
            textView.setText(Integer.parseInt(progress[0]) + "/" + count + " Records");
        }

        @Override
        protected void onPostExecute(String unused) {
            // dismissDialog(DIALOG_DOWNLOAD_PROGRESS);

            //mIntent = new Intent(ExportActivity.this,MainActivity.class);
            //startActivity(mIntent);

            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText(count + " Records Exported successfully");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();


            arcProgress.setVisibility(View.GONE);
            btnExport.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            listReciepts.setVisibility(View.VISIBLE);
            btnFilter.setVisibility(View.VISIBLE);
            finish();

            // b.dismiss();
            //Toast.makeText(ExportMasterActivity.this, "Data Exported successfully!!", Toast.LENGTH_LONG).show();
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
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return "";
        }

        @Override
        protected void onPostExecute(String result) {


            mIntent = new Intent(getApplicationContext(), ExportTasksActivity.class);
            startActivity(mIntent);
        }
    }

    public class NoReceipt extends AsyncTask<Void, Void, String> {


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
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText("No Batches Found To Export");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
        }
    }
}