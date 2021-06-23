package com.plantation.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.soap.SoapRequest;

import org.xmlpull.v1.XmlPullParser;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Michael on 30/06/2016.
 */
public class BatchRecieptsActivity extends AppCompatActivity {
    public static final String TAG = "Uploading";
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;
    static EditText etFrom;
    static EditText etTo;
    static String errorNo;
    static Activity _activity;
    public Toolbar toolbar;
    public SimpleCursorAdapter ca, ca2;
    DBHelper dbhelper;
    ListView listReciepts;
    String BatchNo, BatchDate;
    String strTractor, strTrailer;
    EditText Vehicle, Tractor;
    TextView textBatchNo, textBatchDate, textDelNo, txtAccountId;
    TextView textDeliNo, textBatchCrates, textTrailer;
    Boolean success = true;
    SearchView searchView;
    Intent mIntent;
    EditText etFarmerNo;
    String fromDate, toDate, farmerNo;
    String condition = " _id > 0 ";
    String cond;
    int closed = 1;
    AlertDialog b;
    SQLiteDatabase db;
    ListView listprod;
    ArcProgress arcProgress;
    TextView textStatus;
    AlertDialog weightsupload;
    ImageView ic_connecting;
    String returnValue;
    int cloudid = 0;
    Cursor produce;
    String error, weighmentInfo, verifyWeighment;
    String BatchID, BatchCrates, Crates, EmployeeCrates, EmployeeTotalKg, ColDate, Time, TerminalID, DataDevice, BatchNumber, EmployeeNo;
    String FieldClerk, TaskCode, TaskType, ProduceCode, VarietyCode, GradeCode;
    String Estate, Division, Field, Block, CheckinMethod;
    String EstateCode, DivisionCode, FieldCode, Co_prefix, Current_User;
    String BatchSerial;
    String NetWeight, TareWeight, UnitCount;
    String UnitPrice, RecieptNo, WeighmentNo;
    private Button btnSearchReceipt, btnFilter, btnVerify;
    private Button pickFrom, pickTo;
    private Fragment mFragment;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private String soapResponse, verifyResponse, serverBatchNo;
    private int progressStatus = 0, count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listbatch);
        setupToolbar();
        initializer();
        _activity = this;
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.nav_item_batches);

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
        dbhelper = new DBHelper(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(BatchRecieptsActivity.this);

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
                textBatchNo = selectedView.findViewById(R.id.tv_reciept);
                textBatchDate = selectedView.findViewById(R.id.tv_date);
                textDelNo = selectedView.findViewById(R.id.tv_number);
                txtAccountId = selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textBatchNo.getText().toString());
                showRecieptDetails();
            }
        });

        searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
        searchView.setQueryHint("Search Batch No ...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String recieptNo = constraint.toString();
                        return dbhelper.SearchSpecificBatch(recieptNo);

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
        final View dialogView = inflater.inflate(R.layout.dialog_search_batches, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Search Batches");
        etFrom = dialogView.findViewById(R.id.edtFromDate);
        etTo = dialogView.findViewById(R.id.edtToDate);
        etFarmerNo = dialogView.findViewById(R.id.edtFarmerNo);

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
        btnSearchReceipt.setText("SEARCH BATCH");
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
                    condition += " and  " + Database.BatchDate + " >= '" + fromDate + "'";

                if (toDate.length() > 0)
                    condition += " and  " + Database.BatchDate + " <= '" + toDate + "'";

                /*if (closed > 0)
                    condition += " and  " + Database.Closed + " = '" + closed + "'";*/

                //getSearch();
                ca.getFilter().filter(condition);
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
        Vehicle = dialogView.findViewById(R.id.etTrailer);
        Tractor = dialogView.findViewById(R.id.etTractor);
        BatchNo = textBatchNo.getText().toString();
        BatchSerial = textDelNo.getText().toString();

        Cursor batchinfo = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                + Database.DeliveryNoteNumber + " ='" + textDelNo.getText().toString() + "'", null);
        while (batchinfo.moveToNext()) {
            Tractor.setText(batchinfo.getString(batchinfo
                    .getColumnIndex(Database.Tractor)));
            Vehicle.setText(batchinfo.getString(batchinfo
                    .getColumnIndex(Database.Trailer)));

        }


        btnVerify = dialogView.findViewById(R.id.btnVerify);

        Cursor cursor = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                + Database.DeliveryNoteNumber + " ='" + BatchSerial + "' AND Closed=0", null);

        if (cursor.getCount() > 0) {
            if (!mSharedPrefs.getBoolean("realtimeServices", false) == true) {

                //Toast.makeText(getBaseContext(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();
                btnVerify.setVisibility(View.GONE);
            } else {

                btnVerify.setVisibility(View.VISIBLE);
                btnVerify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SQLiteDatabase db = dbhelper.getReadableDatabase();
                        Cursor weighments = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                                + Database.DataCaptureDevice + " ='" + BatchSerial + "' and " + Database.CloudID + " <='" + cloudid + "' and " + Database.NetWeight + " >'0'", null);
                        if (weighments.getCount() > 0) {
                            showWeightUpload();
                            return;
                        }
                    }
                });

            }

        } else {
            btnVerify.setVisibility(View.GONE);
        }

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
                Cursor weighments = db.rawQuery("select " +
                        "" + Database.DataCaptureDevice +
                        ",COUNT(" + Database.ROW_ID + ")" +
                        ",SUM(" + Database.Tareweight + ")" +
                        ",SUM(" + Database.NetWeight + ")" +
                        " from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                        + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNo + "' and " + Database.CloudID + " <=0", null);
                if (weighments.getCount() > 0) {
                    if (weighments != null) {
                        weighments.moveToFirst();


                        txtStatus.setVisibility(View.VISIBLE);
                        txtStatus.setText("Total Weighments: " + df1.format(c.getDouble(1)) + "\n" +
                                "Total Net Weight: " + df.format(c.getDouble(3)) + " Kgs\n" +
                                "Not uploaded: " + df1.format(weighments.getDouble(1)) + "\n" +
                                "Un-Uploaded Weight: " + df.format(weighments.getDouble(3)) + " Kgs.");
                    }
                } else {
                    txtStatus.setVisibility(View.VISIBLE);
                    txtStatus.setText("Total Weighments: " + df1.format(c.getDouble(1)) + "\n" +
                            "Total Net Weight: " + df1.format(c.getDouble(3)) + " Kgs\n");
                }
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

        Cursor tasks = db.rawQuery("select * from " + Database.EM_TASK_ALLOCATION_TABLE_NAME + " WHERE "
                + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNo + "'", null);
        TextView txttaskStatus = dialogView.findViewById(R.id.texttskstatus);

        if (tasks.getCount() == 0) {
            txttaskStatus.setVisibility(View.VISIBLE);
            txttaskStatus.setText("No Tasks");
            searchView.setVisibility(View.GONE);
        } else {
            //Toast.makeText(this, "records found", Toast.LENGTH_LONG).show();}
            final DecimalFormat df = new DecimalFormat("#0.0#");
            Cursor tsk = db.rawQuery("select " +
                    "" + Database.DataCaptureDevice +
                    ",COUNT(" + Database.ROW_ID + ")" +
                    " from TaskAllocation WHERE "
                    + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNo + " ='" + BatchNo + "'", null);
            if (tsk != null) {

                tsk.moveToFirst();
                txttaskStatus.setVisibility(View.VISIBLE);
                txttaskStatus.setText("Number of Tasks: " + tsk.getInt(1));

            }
            tsk.close();

        }
        while (tasks.moveToNext()) {
            String[] from = {Database.ROW_ID, Database.EmployeeNo, Database.TaskCode};
            int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_phone};


            ca2 = new SimpleCursorAdapter(dialogView.getContext(), R.layout.t_list, tasks, from, to);

            ListView listBatches = dialogView.findViewById(R.id.lvTasks);
            listBatches.setAdapter(ca2);
            listBatches.setTextFilterEnabled(true);
            //db.close();
            //dbhelper.close();
        }


        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


            }
        });
        /*dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

               // deleteBatch();

                // execute insert command

                ContentValues values = new ContentValues();
                values.put( Database.DeliveryNoteNumber, "CO22019060701");
                values.put(Database.BatchDate, "2019-06-07");

                long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                        "_id = ?", new String[] { txtAccountId.getText().toString() });

                db.close();
                if (rows > 0){
                    Toast.makeText(getApplicationContext(), "Updated Successfully!",
                            Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Sorry! Could not update!",
                            Toast.LENGTH_LONG).show();}

            }
        });*/
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });


        final AlertDialog b = dialogBuilder.create();
        b.show();
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Tractor.length() > 7) {
                    Tractor.setError("Enter a Valid Number Plate");
                    return;
                }
                if (Vehicle.length() > 7) {
                    Vehicle.setError("Enter a Valid Number Plate");
                    return;
                }
                strTractor = Tractor.getText().toString();
                strTractor = strTractor.replace(",", "");
                strTrailer = Vehicle.getText().toString();
                strTrailer = strTrailer.replace(",", "");

                ContentValues values = new ContentValues();
                values.put(Database.Tractor, strTractor);
                values.put(Database.Trailer, strTrailer);
                //values.put(Database.DeliveryNoteNumber,"RT3B2019052501");
                long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                        "DeliveryNoteNumber = ?", new String[]{textDelNo.getText().toString()});
                if (rows > 0) {
                    Toast.makeText(getApplicationContext(), "Vehicle and Tractor Updated Successfully", Toast.LENGTH_LONG).show();
                }
                b.dismiss();

            }
        });

    }

    public void showWeightUpload() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(_activity);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_upload, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("------- UPLOAD -------");
        arcProgress = dialogView.findViewById(R.id.arc_progress);
        ic_connecting = dialogView.findViewById(R.id.ic_connecting);
        textStatus = dialogView.findViewById(R.id.textStatus);
        new WeighmentsToCloud().execute();
        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();


            }
        });

        weightsupload = dialogBuilder.create();
        weightsupload.show();

    }

    public void deleteBatch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Html.fromHtml("<font color='#4285F4'>Are you sure you want to delete this batch?</font>"))
                .setCancelable(false)
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Date date = new Date(getDate());
                        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("BatchON", format1.format(date));
                        edit.commit();

                        edit.remove("DeliverNoteNumber");
                        edit.commit();
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
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);

    }

    public void deleteCurrentAccount() {
        try {
            DBHelper dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            int rows = db.delete(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, "DeliveryNoteNumber=?", new String[]{textDelNo.getText().toString()});

            if (rows == 1) {
                Toast.makeText(this, "Batch Deleted Successfully!", Toast.LENGTH_LONG).show();
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
                int rows1 = db.delete(Database.EM_PRODUCE_COLLECTION_TABLE_NAME,
                        Database.CollDate + "=? AND " + Database.BatchNumber + "=? ", new String[]{BatchDate, textBatchNo.getText().toString()}
                );

                if (rows1 == 1) {
                    Toast.makeText(this, "Transactions Deleted Successfully!", Toast.LENGTH_LONG).show();

                } else {
                    //  Toast.makeText(this, "Could not delete Transactions!", Toast.LENGTH_LONG).show();
                }
                dbhelper.close();
                getdata();
            } else
                Toast.makeText(this, "Could not delete Batch!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
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
            //Cursor accounts = db.query(true, Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, Database.Closed + "='" + closed + "'", null, null, null, null, null, null);
            Cursor accounts = db.query(true, Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, null, null, null, null, null, null);
            if (accounts.getCount() > 0) {
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

    public class WeighmentsToCloud extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");

            arcProgress.setVisibility(View.VISIBLE);
            arcProgress.setProgress(0);
            Glide.with(getApplicationContext()).load(R.drawable.connecting).into(ic_connecting);
            textStatus.setVisibility(View.VISIBLE);
            textStatus.setText("Connecting to Server ...");
            BatchSerial = textDelNo.getText().toString();

            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("vresponse");
            edit.commit();
        }

        @Override
        protected String doInBackground(String... aurl) {
            Log.i(TAG, "doInBackground");
            try {
                db = dbhelper.getReadableDatabase();
                serverBatchNo = prefs.getString("serverBatchNo", "");

                produce = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                        + Database.DataCaptureDevice + " ='" + BatchSerial + "' and " + Database.CloudID + " <='" + cloudid + "'", null);
                count = produce.getCount();
                if (count > 0) {
                    //csvWrite.writeNext(produce.getColumnNames());
                    while (produce.moveToNext()) {

                        ColDate = produce.getString(produce.getColumnIndex(Database.CollDate));
                        String dbtTransOn = ColDate + " 00:00:00";
                        SimpleDateFormat frmt = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                        Date date1 = null;
                        try {
                            date1 = frmt.parse(dbtTransOn);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd");
                        String TransDate = format3.format(date1);

                        Time = produce.getString(produce.getColumnIndex(Database.CaptureTime));
                        BatchNo = produce.getString(produce.getColumnIndex(Database.BatchNo));
                        BatchSerial = produce.getString(produce.getColumnIndex(Database.DataCaptureDevice));
                        TerminalID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
                        TaskCode = produce.getString(produce.getColumnIndex(Database.TaskCode));
                        EmployeeNo = produce.getString(produce.getColumnIndex(Database.EmployeeNo));
                        ProduceCode = produce.getString(produce.getColumnIndex(Database.DeliveredProduce));


                        if (produce.getString(produce.getColumnIndex(Database.ProduceVariety)) == null) {
                            VarietyCode = "";
                        } else {
                            VarietyCode = produce.getString(produce.getColumnIndex(Database.ProduceVariety));
                        }
                        if (produce.getString(produce.getColumnIndex(Database.ProduceGrade)) == null) {
                            GradeCode = "";
                        } else {

                            GradeCode = produce.getString(produce.getColumnIndex(Database.ProduceGrade));
                        }

                        EstateCode = produce.getString(produce.getColumnIndex(Database.SourceEstate));
                        DivisionCode = produce.getString(produce.getColumnIndex(Database.SourceDivision));
                        FieldCode = produce.getString(produce.getColumnIndex(Database.SourceField));
                        if (FieldCode.equals("Select ...")) {
                            FieldCode = "";
                        } else {
                            FieldCode = produce.getString(produce.getColumnIndex(Database.SourceField));
                        }
                        Block = produce.getString(produce.getColumnIndex(Database.SourceBlock));
                        if (Block.equals("Select ...")) {
                            Block = "";
                        } else {
                            Block = produce.getString(produce.getColumnIndex(Database.SourceBlock));
                        }
                        NetWeight = produce.getString(produce.getColumnIndex(Database.NetWeight));
                        TareWeight = produce.getString(produce.getColumnIndex(Database.Tareweight));

                        if (produce.getString(produce.getColumnIndex(Database.BagCount)) == null) {
                            Crates = "1";

                        } else {
                            Crates = produce.getString(produce.getColumnIndex(Database.BagCount));
                        }

                        UnitPrice = produce.getString(produce.getColumnIndex(Database.UnitPrice));
                        WeighmentNo = produce.getString(produce.getColumnIndex(Database.LoadCount));
                        RecieptNo = produce.getString(produce.getColumnIndex(Database.DataCaptureDevice)) + produce.getString(produce.getColumnIndex(Database.ReceiptNo));
                        FieldClerk = produce.getString(produce.getColumnIndex(Database.FieldClerk));
                        CheckinMethod = produce.getString(produce.getColumnIndex(Database.UsedSmartCard));

                        Co_prefix = mSharedPrefs.getString("company_prefix", "");
                        Current_User = prefs.getString("user", "");
                        TaskType = "";


                        StringBuilder sb = new StringBuilder();
                        sb.append("2" + ",");
                        sb.append(TransDate + ",");
                        sb.append(TerminalID + ",");
                        sb.append(Time + ",");
                        sb.append(FieldClerk + ",");
                        sb.append(ProduceCode + ",");
                        sb.append(EstateCode + ",");
                        sb.append(DivisionCode + ",");
                        sb.append(FieldCode + ",");
                        sb.append(Block + ",");
                        sb.append(BatchNo + ",");
                        sb.append(TaskCode + ",");
                        sb.append(EmployeeNo + ",");
                        sb.append(NetWeight + ",");
                        sb.append(TareWeight + ",");
                        sb.append(Crates + ",");
                        sb.append(RecieptNo + ",");
                        sb.append(WeighmentNo + ",");
                        sb.append(VarietyCode + ",");
                        sb.append(GradeCode + ",");
                        sb.append(UnitPrice + ",");
                        sb.append(Co_prefix + ",");
                        sb.append(Current_User + ",");
                        sb.append(CheckinMethod);

                        weighmentInfo = sb.toString();
                        StringBuilder vd = new StringBuilder();
                        vd.append("2" + ",");
                        vd.append(Co_prefix + ",");
                        vd.append(EmployeeNo + ",");
                        vd.append(TaskCode + ",");
                        vd.append(TaskType + ",");
                        vd.append(ColDate + ",");
                        vd.append(TerminalID + ",");
                        vd.append(serverBatchNo + ",");
                        vd.append(WeighmentNo);
                        verifyWeighment = vd.toString();
                        try {
                            //soapResponse = new SoapRequest(_activity).PostWeighingRecord(serverBatchNo, weighmentInfo);
                            verifyResponse = new SoapRequest(_activity).VerifyAttendancePost(verifyWeighment);
                            Log.i("Records", verifyWeighment);
                            error = verifyResponse;
                            if (Integer.valueOf(prefs.getString("vresponse", "")).intValue() > 0) {
                                // if (Integer.valueOf(soapResponse).intValue() < 0) {
                                returnValue = prefs.getString("vresponse", "");
                                //returnValue = "0";
                                ContentValues values = new ContentValues();
                                values.put(Database.CloudID, returnValue);
                                long rows = db.update(Database.EM_PRODUCE_COLLECTION_TABLE_NAME, values,
                                        Database.EmployeeNo + " = ? AND " + Database.DataCaptureDevice + " = ? AND " + Database.LoadCount + " = ?",
                                        new String[]{EmployeeNo, BatchSerial, WeighmentNo});

                                if (rows > 0) {
                                    // Log.i("error:",soapResponse);
                                    Log.i("success:", returnValue);
                                }
                                //


                            } else if (Integer.valueOf(prefs.getString("vresponse", "")).intValue() == 0) {

                                //else if(Integer.valueOf(soapResponse).intValue()> 0){
                                soapResponse = new SoapRequest(_activity).PostWeighingRecord(serverBatchNo, weighmentInfo);
                                if (Integer.valueOf(soapResponse).intValue() > 0) {
                                    returnValue = soapResponse;
                                    ContentValues values = new ContentValues();
                                    values.put(Database.CloudID, returnValue);
                                    long rows = db.update(Database.EM_PRODUCE_COLLECTION_TABLE_NAME, values,
                                            Database.EmployeeNo + " = ? AND " + Database.DataCaptureDevice + " = ? AND " + Database.LoadCount + " = ?",
                                            new String[]{EmployeeNo, BatchSerial, WeighmentNo});

                                    if (rows > 0) {
                                        Log.i("success:", returnValue);

                                    }
                                }
                                error = soapResponse;
                                if (Integer.valueOf(BatchRecieptsActivity.this.soapResponse).intValue() < 0) {
                                    // returnValue = "0";

                                    return null;
                                }
                            } else {
                                return null;
                            }


                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            returnValue = e.toString();
                            Log.i("Catch Exc:", returnValue);
                        }

                        progressStatus++;
                        publishProgress("" + progressStatus);

                    }


                    produce.close();

                } else {

                    //Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

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
            arcProgress.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setMax(count);
            arcProgress.setBottomText("Uploading ...");
            ic_connecting.setVisibility(View.GONE);
            textStatus.setText("Uploading... " + Integer.parseInt(progress[0]) + "/" + count + " Records");


        }

        @Override
        protected void onPostExecute(String unused) {
            db = dbhelper.getReadableDatabase();
            if (error.equals("-8080")) {
                errorNo = prefs.getString("errorNo", "");

                Context context = _activity;
                LayoutInflater inflater = _activity.getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Server Not Available !!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();

                //Toast.makeText(mActivity, "Server Not Available !!", Toast.LENGTH_LONG).show();
                // Log.i(TAG, "Server Not Available !!");
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("error", "Server Not Available !!");
                edit.commit();


                return;
            }
            try {
                weightsupload.dismiss();
                if (Integer.valueOf(soapResponse).intValue() > 0) {


                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("success", "Uploaded Successfully !!!");
                    edit.commit();
                    Toast.makeText(_activity, "Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
                    weightsupload.dismiss();
                    return;
                }
            } catch (NumberFormatException e) {
                returnValue = "0";
                errorNo = prefs.getString("errorNo", "");
                ContentValues values = new ContentValues();
                values.put(Database.CloudID, 0);
                long rows = db.update(Database.EM_PRODUCE_COLLECTION_TABLE_NAME, values,
                        Database.EmployeeNo + " = ? AND " + Database.DataCaptureDevice + " = ? AND " + Database.LoadCount + " = ?",
                        new String[]{EmployeeNo, BatchSerial, WeighmentNo});

                if (rows > 0) {

                }
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("error", error);
                edit.commit();
                weightsupload.dismiss();
                Toast.makeText(_activity, error, Toast.LENGTH_LONG).show();
                return;

            }

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


            mIntent = new Intent(getApplicationContext(), BatchRecieptsActivity.class);
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
            text.setText("No Batches Found");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
        }
    }
}
