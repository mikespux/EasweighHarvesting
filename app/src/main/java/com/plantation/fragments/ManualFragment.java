package com.plantation.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.plantation.R;
import com.plantation.activities.BothCardWeighActivity;
import com.plantation.activities.CardWeighActivity;
import com.plantation.activities.MainActivity;
import com.plantation.activities.ScaleEasyWeighActivity;
import com.plantation.data.DBHelper;
import com.plantation.services.EasyWeighService;

import java.util.ArrayList;


public class ManualFragment extends Fragment {

    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";

    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    public static final String BOTH = "Both";

    public static String cachedDeviceAddress;
    static SharedPreferences mSharedPrefs, prefs, pref;
    public View mView;
    public Intent mIntent;
    public Context mContext;

    Spinner spProduce, spVariety, spGrade, spTask, spField;
    DBHelper dbhelper;
    Button btn_next, btnHome;
    EasyWeighService resetConn;

    String grade, gradeid;
    ArrayList<String> gradedata = new ArrayList<String>();
    ArrayAdapter<String> gradeadapter;

    String variety, varietyid;
    ArrayList<String> varietydata = new ArrayList<String>();
    ArrayAdapter<String> varietyadapter;

    String produce, produceid, produceCode;
    ArrayList<String> producedata = new ArrayList<String>();
    ArrayAdapter<String> produceadapter;

    String fieldid = null;
    String fields;
    ArrayList<String> fielddata = new ArrayList<String>();
    ArrayAdapter<String> fieldadapter;

    String taskid = null;
    String tasks;
    ArrayList<String> taskdata = new ArrayList<String>();
    ArrayAdapter<String> taskadapter;

    TextView tv;
    String disabled;
    String BaseDate, BatchDate;
    String divisionID;
    Double MaxCRange = 0.0;
    Double MinCRange = 0.0;
    int MaxBatchCrates = 0;

    SQLiteDatabase db;
    private Snackbar snackbar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {
            mView = inflater.inflate(R.layout.fragment_produce_browser_tea, container, false);
        } else {
            mView = inflater.inflate(R.layout.fragment_produce_browser, container, false);

        }
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initializer();

        return mView;
    }

    public void setupSnackBar() {
        snackbar = Snackbar.make(mView.findViewById(R.id.ParentLayout), getString(R.string.ScaleError), Snackbar.LENGTH_LONG);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#FF5252"));
    }

    public void initializer() {

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
        resetConn = new EasyWeighService();

        spProduce = mView.findViewById(R.id.spProduce);
        spVariety = mView.findViewById(R.id.spVariety);
        spGrade = mView.findViewById(R.id.spGrade);
        spTask = mView.findViewById(R.id.spTask);
        spField = mView.findViewById(R.id.spField);

        btnHome = mView.findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            getActivity().finish();
            mIntent = new Intent(getActivity(), MainActivity.class);
            startActivity(mIntent);
        });

        Produce();
        Variety();
        Grade();
        TaskList();
        FieldList();

        btn_next = mView.findViewById(R.id.btn_next);
        btn_next.setOnClickListener(v -> {

            if (prefs.getString("DeliverNoteNumber", "").equals("")
                    || prefs.getString("DeliverNoteNumber", "").equals("No Batch Opened")) {
                // snackbar.show();
                Context context = getActivity();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Open A Batch To Proceed...");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                return;
            }
            BaseDate = prefs.getString("basedate", "");
            BatchDate = prefs.getString("BatchON", "");
            if (!BaseDate.equals(BatchDate)) {
                // snackbar.show();
                Context context = getActivity();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Close Yesterday's Batch and\nOpen A New To Proceed...");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                return;
            }
            if (mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {
            } else {


                MaxBatchCrates = Integer.parseInt(mSharedPrefs.getString("maxBatchCrates", "0"));
                if (MaxBatchCrates == 0) {
                    Context context = getActivity().getApplicationContext();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Set Maximum Batch Crates in Settings");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
                    return;

                }

                MinCRange = Double.parseDouble(mSharedPrefs.getString("minCRange", "0"));
                if (MinCRange == 0) {
                    Context context = getActivity().getApplicationContext();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Set Minimum Crate Weight in Settings");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
                    return;

                }
                MaxCRange = Double.parseDouble(mSharedPrefs.getString("maxCRange", "0"));
                if (MaxCRange == 0) {
                    Context context = getActivity().getApplicationContext();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Set Maximum Crate Weight in Settings");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
                    return;

                }
            }

            if (mSharedPrefs.getString("scaleVersion", "").equals("")) {
                // snackbar.show();
                Context context = getActivity();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Select Scale Model to Weigh");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                return;
            }

            if (mSharedPrefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15) ||
                    mSharedPrefs.getString("scaleVersion", "").equals(EASYWEIGH_VERSION_11)) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                cachedDeviceAddress = pref.getString("address", "");
                if (cachedDeviceAddress.equals("")) {
                    // snackbar.show();
                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
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
                    Context context = getActivity();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Select Produce");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                    return;
                }
                if (spField.getSelectedItem().equals("Select ...")) {
                    Context context = getActivity();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Select Field");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                    return;
                }
                if (taskid == null) {

                    Context context = getActivity();
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

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("produceCode", produceCode);
                edit.apply();
                edit.putString("varietyCode", varietyid);
                edit.apply();
                edit.putString("taskType", "2");
                edit.apply();
                if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {

                    mIntent = new Intent(getActivity(), CardWeighActivity.class);
                    startActivity(mIntent);

                } else if (mSharedPrefs.getString("vModes", "Both").equals(BOTH)) {

                    mIntent = new Intent(getActivity(), BothCardWeighActivity.class);
                    startActivity(mIntent);

                } else if (mSharedPrefs.getString("vModes", "Manual").equals(MANUAL)) {
                    mIntent = new Intent(getActivity(), ScaleEasyWeighActivity.class);
                    startActivity(mIntent);

                }


            }

        });


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


        produceadapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, producedata);
        produceadapter.setDropDownViewResource(R.layout.spinner_item);
        spProduce.setAdapter(produceadapter);
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
                    //Toast.makeText(getActivity(), "Please sel", Toast.LENGTH_LONG).show();

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


        gradeadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, gradedata);
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


        varietyadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, varietydata);
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
        int etype = 1;
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select tkID,tkName from tasks where tkType='" + etype + "'", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    tasks = c.getString(c.getColumnIndex("tkName"));
                    taskdata.add(tasks);

                } while (c.moveToNext());
            }
        }


        taskadapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, taskdata);
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


        fieldadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, fielddata);
        fieldadapter.setDropDownViewResource(R.layout.spinner_item);
        spField.setAdapter(fieldadapter);
        spField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fieldName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select fdID from fields where fdID= '" + fieldName + "'", null);
                if (c != null) {
                    if (c.getCount() > 1) {
                        c.moveToFirst();
                        fieldid = c.getString(c.getColumnIndex("fdID"));

                    }

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
}
