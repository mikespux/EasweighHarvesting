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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.plantation.services.EasyWeighService;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Michael on 16/08/2016.
 */
public class MachineProduceActivity extends AppCompatActivity {
    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";

    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    public static final String BOTH = "Both";

    public static String cachedDeviceAddress;
    static SharedPreferences mSharedPrefs, prefs;
    public Intent mIntent;
    public Toolbar toolbar;
    Spinner spProduce, spVariety, spGrade, spTask;
    String grade, gradeid;
    ArrayList<String> gradedata = new ArrayList<String>();
    ArrayAdapter<String> gradeadapter;
    String variety, varietyid;
    ArrayList<String> varietydata = new ArrayList<String>();
    ArrayAdapter<String> varietyadapter;
    String produce, produceid, produceCode;
    ArrayList<String> producedata = new ArrayList<String>();
    ArrayAdapter<String> produceadapter;
    String taskid = null;
    String tasks;
    ArrayList<String> taskdata = new ArrayList<String>();
    ArrayAdapter<String> taskadapter;
    TextView tv;
    String disabled;
    SQLiteDatabase db;

    DBHelper dbhelper;
    DecimalFormat formatter;

    Button btn_next, btnBack;
    EasyWeighService resetConn;
    String taskType;
    String fieldid = null;
    String fields;
    ArrayList<String> fielddata = new ArrayList<String>();
    ArrayAdapter<String> fieldadapter;
    String divisionID;
    Spinner spField;

    TextView textMachineId, textMachineNo, textMachineOP;
    ListView listMachines;
    TextView tvMachine;
    SearchView searchView;
    SimpleCursorAdapter ca;
    AlertDialog dMachine;

    SimpleDateFormat dateTimeFormatB;
    String MDate;
    Cursor machines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produce_browser_tea);

        // this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Produce Browser");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove("tvConn");
        edit.commit();
        finish();
        return;
    }

    public void initializer() {

        prefs = PreferenceManager.getDefaultSharedPreferences(MachineProduceActivity.this);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(MachineProduceActivity.this);
        dbhelper = new DBHelper(MachineProduceActivity.this);
        db = dbhelper.getReadableDatabase();
        resetConn = new EasyWeighService();
        formatter = new DecimalFormat("00");
        dateTimeFormatB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


        spProduce = findViewById(R.id.spProduce);
        spVariety = findViewById(R.id.spVariety);
        spGrade = findViewById(R.id.spGrade);
        spTask = findViewById(R.id.spTask);
        spField = findViewById(R.id.spField);


        Produce();
        Variety();
        Grade();
        TaskList();
        FieldList();

        tvMachine = findViewById(R.id.tvMachine);
        tvMachine.setOnClickListener(v -> {
            Machine();
        });


        enableBT();


        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(v -> {

            if (prefs.getString("DeliverNoteNumber", "").equals("")
                    || prefs.getString("DeliverNoteNumber", "").equals("No Batch Opened")) {
                // snackbar.show();
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Open A Batch To Proceed...");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getApplicationContext(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                return;
            }


            if (mSharedPrefs.getString("scaleVersion", "").equals("")) {
                // snackbar.show();
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Select Scale Model to Weigh");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getApplicationContext(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                return;
            }

            if (mSharedPrefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15) ||
                    mSharedPrefs.getString("scaleVersion", "").equals(EASYWEIGH_VERSION_11)) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MachineProduceActivity.this);
                cachedDeviceAddress = pref.getString("address", "");
                if (cachedDeviceAddress.equals("")) {
                    // snackbar.show();
                    Context context = MachineProduceActivity.this;
                    LayoutInflater inflater = MachineProduceActivity.this.getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please pair scale");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(RouteShedActivity.this, "Please pair scale", Toast.LENGTH_LONG).show();
                    return;
                }

            }
            if (mSharedPrefs.getString("scaleVersion", "").equals(EASYWEIGH_VERSION_15) ||
                    mSharedPrefs.getString("scaleVersion", "").equals(EASYWEIGH_VERSION_11)) {
                if (spProduce.getSelectedItem().equals("Select ...")) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Select Produce");
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
                if (spField.getSelectedItem().equals("Select ...")) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Select Field");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getApplicationContext(), "Please Select Produce", Toast.LENGTH_LONG).show();
                    return;
                }
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
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("produceCode", produceCode);
                edit.commit();
                edit.putString("varietyCode", varietyid);
                edit.commit();
                edit.putString("taskType", "5");
                edit.commit();

                mIntent = new Intent(getApplicationContext(), ScaleEasyWeighActivity.class);
                startActivity(mIntent);


            }
        });


    }


    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    private void Produce() {
        producedata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select MpCode,MpDescription from Produce", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    produce = c.getString(c.getColumnIndex("MpDescription"));
                    producedata.add(produce);

                } while (c.moveToNext());
            }
        }


        produceadapter = new ArrayAdapter<String>(this, R.layout.spinner_item, producedata);
        produceadapter.setDropDownViewResource(R.layout.spinner_item);
        spProduce.setAdapter(produceadapter);
        //spProduce.setSelection(1);
        spProduce.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String produceName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select MpCode,CloudID from Produce where MpDescription= '" + produceName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    produceid = c.getString(c.getColumnIndex("CloudID"));
                    produceCode = c.getString(c.getColumnIndex("MpCode"));

                }
                c.close();


                tv = (TextView) view;
                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }

                if (position == 0) {
                    spVariety.setEnabled(false);
                    spGrade.setEnabled(false);
                    disabled = "true";
                    Variety();
                    Grade();
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("produceCode");
                    edit.commit();
                    edit.remove("varietyCode");
                    edit.commit();
                    edit.remove("gradeCode");
                    edit.commit();
                    edit.remove("unitPrice");
                    edit.commit();
                    //Toast.makeText(this, "Please sel", Toast.LENGTH_LONG).show();

                } else {

                    Variety();
                    Grade();
                    Cursor c1 = db.rawQuery("select * from ProduceGrades where pgdProduce= '" + produceCode + "' ", null);
                    Cursor c2 = db.rawQuery("select * from ProduceVarieties where vrtProduce= '" + produceCode + "' ", null);
                    if (c2.getCount() > 0) {
                        spVariety.setEnabled(true);
                        disabled = "false";


                        // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                        c2.close();

                    } else {
                        spVariety.setEnabled(false);
                        varietydata.clear();
                        SharedPreferences.Editor edit = prefs.edit();

                        edit.remove("varietyCode");
                        edit.commit();

                    }
                    if (c1.getCount() > 0) {
                        spGrade.setEnabled(true);
                        disabled = "false";

                        // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                        c1.close();
                    } else {
                        spGrade.setEnabled(false);
                        gradedata.clear();
                        SharedPreferences.Editor edit = prefs.edit();

                        edit.remove("gradeCode");
                        edit.commit();
                    }


                }
                // db.close();
                //dbhelper.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //  tv.setHint("Select Country");
            }
        });


    }

    private void Grade() {
        gradedata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select pgdRef,pgdName from ProduceGrades where pgdProduce= '" + produceCode + "' ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    grade = c.getString(c.getColumnIndex("pgdName"));
                    gradedata.add(grade);

                } while (c.moveToNext());
            }
        }


        gradeadapter = new ArrayAdapter<String>(this, R.layout.spinner_item, gradedata);
        gradeadapter.setDropDownViewResource(R.layout.spinner_item);
        gradeadapter.notifyDataSetChanged();
        spGrade.setAdapter(gradeadapter);
        spGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String GradeName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select pgdRef from ProduceGrades where pgdName= '" + GradeName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    gradeid = c.getString(c.getColumnIndex("pgdRef"));


                }
                SharedPreferences.Editor edit = prefs.edit();

                edit.putString("gradeCode", gradeid);
                edit.commit();
                c.close();
                //db.close();
                //dbhelper.close();
                TextView tv = (TextView) view;


                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
               /* if(disabled.equals("true")) {
                    // Set the disable item text color
                    tv.setBackgroundColor(Color.parseColor("#E3E4ED"));

                }*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void Variety() {
        varietydata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select vtrRef,vrtName from ProduceVarieties where vrtProduce= '" + produceCode + "' ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    variety = c.getString(c.getColumnIndex("vrtName"));
                    varietydata.add(variety);

                } while (c.moveToNext());
            }
        }


        varietyadapter = new ArrayAdapter<String>(this, R.layout.spinner_item, varietydata);
        varietyadapter.setDropDownViewResource(R.layout.spinner_item);
        varietyadapter.notifyDataSetChanged();
        spVariety.setAdapter(varietyadapter);
        spVariety.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String varietyName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select vtrRef from ProduceVarieties where vrtName= '" + varietyName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    varietyid = c.getString(c.getColumnIndex("vtrRef"));

                }
                SharedPreferences.Editor edit = prefs.edit();

                edit.putString("varietyCode", varietyid);
                edit.commit();
                c.close();
                //db.close();
                //dbhelper.close();
                TextView tv = (TextView) view;
                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
               /* if(disabled.equals("true")) {
                    // Set the disable item text color
                  tv.setBackgroundColor(Color.parseColor("#E3E4ED"));

                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


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


        taskadapter = new ArrayAdapter<String>(MachineProduceActivity.this, R.layout.spinner_item, taskdata);
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

    private void FieldList() {
        fielddata.clear();
        divisionID = prefs.getString("divisionid", "");
        fielddata.add("Select ...");
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select fdID,fdDivision from fields where fdDivision='" + divisionID + "' ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                // fielddata.add("Select ...");
                do {
                    fields = c.getString(c.getColumnIndex("fdID"));
                    fielddata.add(fields);

                } while (c.moveToNext());
            }
        }


        fieldadapter = new ArrayAdapter<String>(MachineProduceActivity.this, R.layout.spinner_item, fielddata);
        fieldadapter.setDropDownViewResource(R.layout.spinner_item);
        spField.setAdapter(fieldadapter);
        spField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fieldName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select fdID from fields where fdID= '" + fieldName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    fieldid = c.getString(c.getColumnIndex("fdID"));


                }
                c.close();
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("fieldCode", fieldid);
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


    private void Machine() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MachineProduceActivity.this);
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
                    return dbhelper.SearchSpecificWMachine(MachineCode);

                });
                // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText);
                ca.setFilterQueryProvider(constraint -> {
                    String MachineCode = constraint.toString();
                    return dbhelper.SearchWMachine(MachineCode);

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

            tvMachine.setText(textMachineNo.getText().toString() + " - " + textMachineOP.getText().toString() + " Operators");

            dMachine.dismiss();
        });


        dialogBuilder.setPositiveButton("BACK", (dialog, whichButton) -> {
            //do something with edt.getText().toString();

        });

        dMachine = dialogBuilder.create();
        if (machines.getCount() == 0) {
            Context context = getApplicationContext();
            LayoutInflater inflater1 = getLayoutInflater();
            View customToastroot = inflater1.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("No Allocated Machines Today");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }
        dMachine.show();


    }

    public void getMachineList() {

        try {
            Calendar cal = Calendar.getInstance();
            MDate = dateTimeFormatB.format(cal.getTime());
            SQLiteDatabase db = dbhelper.getReadableDatabase();
//            Cursor machines = db.query(true, Database.MACHINEOP_TABLE_NAME, null,Database.MDATE+"='" + MDate + "'", null, Database.MACHINENUMBER, null, null, null, null);
            machines = db.rawQuery("select * from " + Database.MACHINE_TABLE_NAME + "," + Database.MACHINEOP_TABLE_NAME + "" +
                    " where " + Database.MC_ID + "=" + Database.MACHINENUMBER + " and " + Database.MDATE + "='" + MDate + "' and " + Database.MSTATUS + "='1' group by machineNo", null);


            String[] from = {Database.ROW_ID, Database.MC_ID, Database.MC_NAME};
            int[] to = {R.id.txtAccountId, R.id.txtUserName, R.id.tvOperators};


            ca = new SimpleCursorAdapter(this, R.layout.list_item, machines, from, to);

            listMachines.setAdapter(ca);
            // dbhelper.close();
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
