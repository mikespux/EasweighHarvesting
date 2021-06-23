package com.plantation.fragments;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.plantation.R;
import com.plantation.activities.CheckInActivity;
import com.plantation.activities.CheckInBothActivity;
import com.plantation.activities.CheckInFingerprintActivity;
import com.plantation.activities.CheckOutActivity;
import com.plantation.activities.CheckOutBothActivity;
import com.plantation.activities.CheckOutFingerprintActivity;
import com.plantation.activities.TaskListActivity;
import com.plantation.activities.UploadTsksandAttActivity;
import com.plantation.data.DBHelper;
import com.plantation.helpers.CustomList;
import com.plantation.services.EasyWeighService;

import java.util.ArrayList;


public class TaskingFragment extends Fragment {

    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String TRANCELL_TI500 = "TI-500";
    public static final String DR_150 = "DR-150";
    public static final String FINGERPRINT = "FingerPrint";
    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    public static final String BOTH = "Both";
    public static String cachedDeviceAddress;
    static SharedPreferences mSharedPrefs, prefs, pref;
    public View mView;
    public Intent mIntent;
    public LinearLayoutManager layoutManager;
    public Context mContext;
    Spinner spEstate, spDivision, spGrade;
    EditText edtPrice;
    DBHelper dbhelper;
    SharedPreferences.Editor edit;
    Button btn_next, btnHome;
    EasyWeighService resetConn;
    SQLiteDatabase db;
    String estateid = null;
    String estates;
    ArrayList<String> estatedata = new ArrayList<String>();
    ArrayAdapter<String> estateadapter;
    String divisionid = null;
    String divisions;
    ArrayList<String> divisiondata = new ArrayList<String>();
    ArrayAdapter<String> divisionadapter;
    String BaseDate, BatchDate, DelDate;
    String[] web = {
            "Check-In", "Allocate Task", "Checkout Task", "Upload Tasks/Attendance", "Capital Projects", "Reports"
    };
    Integer[] imageId = {
            R.drawable.ic_checkin,
            R.drawable.ic_task,
            R.drawable.ic_checkout,
            R.drawable.ic_upload,
            R.drawable.capitalprojects,
            R.drawable.ic_zreport
    };
    ListView list;
    private Snackbar snackbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_tasking, container, false);
        initializer();
        //setupProgressBar();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        return mView;
    }

    public void initializer() {

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        edit = prefs.edit();
        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
        resetConn = new EasyWeighService();
        spEstate = mView.findViewById(R.id.spEstate);
        spDivision = mView.findViewById(R.id.spDivision);

        enableBT();
        EstateList();
        DivisionList();
        CustomList adapter = new
                CustomList(getActivity(), web, imageId);
        list = mView.findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onItemClick(AdapterView parent, View view,
                                    int position, long id) {
                if (position == 1) {
                    //Intent myIntent = new Intent(Ali10Activity.this, Hassan.class);
                    //startActivity(myIntent);
                }

                switch (position) {
                    case 0:
                        if (spEstate.getSelectedItem().equals("Select ...")) {
                            Context context = getActivity().getApplicationContext();
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Please Select Estate");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            //Toast.makeText(getApplicationContext(), "Please Select Estate", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (spDivision.getSelectedItem().equals("Select ...")) {
                            Context context = getActivity().getApplicationContext();
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Please Select Division");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            //Toast.makeText(getApplicationContext(), "Please Select Division", Toast.LENGTH_LONG).show();
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
                            // customtoast.show();
                            //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                            // return;
                        }

                        edit.putString("estateCode", estateid);
                        edit.commit();
                        edit.putString("divisionCode", divisionid);
                        edit.commit();
                        if (mSharedPrefs.getString("vModes", "FingerPrint").equals(FINGERPRINT)) {

                            getActivity().finish();
                            mIntent = new Intent(getActivity(), CheckInFingerprintActivity.class);
                            startActivity(mIntent);

                        } else if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {

                            mIntent = new Intent(getActivity(), CheckInActivity.class);
                            startActivity(mIntent);

                        } else if (mSharedPrefs.getString("vModes", "Manual").equals(MANUAL)) {
                            mIntent = new Intent(getActivity(), CheckInActivity.class);
                            startActivity(mIntent);

                        } else if (mSharedPrefs.getString("vModes", "Both").equals(BOTH)) {

                            mIntent = new Intent(getActivity(), CheckInBothActivity.class);
                            startActivity(mIntent);
                        } else {
                            mIntent = new Intent(getActivity(), CheckInBothActivity.class);
                            startActivity(mIntent);
                        }
                        break;
                    case 1:

                        if (spEstate.getSelectedItem().equals("Select ...")) {
                            Context context = getActivity().getApplicationContext();
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Please Select Estate");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            //Toast.makeText(getApplicationContext(), "Please Select Estate", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (spDivision.getSelectedItem().equals("Select ...")) {
                            Context context = getActivity().getApplicationContext();
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Please Select Division");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            //Toast.makeText(getApplicationContext(), "Please Select Division", Toast.LENGTH_LONG).show();
                            return;
                        }


                        edit.putString("estateCode", estateid);
                        edit.commit();
                        edit.putString("divisionCode", divisionid);
                        edit.commit();
                        mIntent = new Intent(getActivity(), TaskListActivity.class);
                        startActivity(mIntent);
                        break;
                    case 2:
                        if (mSharedPrefs.getString("vModes", "FingerPrint").equals(FINGERPRINT)) {

                            getActivity().finish();
                            mIntent = new Intent(getActivity(), CheckOutFingerprintActivity.class);
                            startActivity(mIntent);

                        } else if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {

                            mIntent = new Intent(getActivity(), CheckOutActivity.class);
                            startActivity(mIntent);

                        } else if (mSharedPrefs.getString("vModes", "Manual").equals(MANUAL)) {
                            mIntent = new Intent(getActivity(), CheckOutActivity.class);
                            startActivity(mIntent);

                        } else if (mSharedPrefs.getString("vModes", "Both").equals(BOTH)) {

                            mIntent = new Intent(getActivity(), CheckOutBothActivity.class);
                            startActivity(mIntent);
                        } else {
                            mIntent = new Intent(getActivity(), CheckOutBothActivity.class);
                            startActivity(mIntent);
                        }
                        break;
                    case 3:
                        mIntent = new Intent(getActivity(), UploadTsksandAttActivity.class);
                        startActivity(mIntent);
                        break;
                    case 4:
                        Toast.makeText(getActivity(), "In Progress ...", Toast.LENGTH_LONG).show();
                        break;
                    case 5:
                        Toast.makeText(getActivity(), "In Progress ...", Toast.LENGTH_LONG).show();
                        break;


                    default:
                        break;
                }


            }
        });


    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

        }
        return getActivity().onKeyDown(keyCode, event);
    }

    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    private void EstateList() {
        estatedata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select esID,esName from estates ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    estates = c.getString(c.getColumnIndex("esName"));
                    estatedata.add(estates);

                } while (c.moveToNext());
            }
        }


        estateadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_min, estatedata);
        estateadapter.setDropDownViewResource(R.layout.spinner_item_min);
        spEstate.setAdapter(estateadapter);
        spEstate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String estateName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select esID from estates where esName= '" + estateName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    estateid = c.getString(c.getColumnIndex("esID"));


                }
                c.close();
                db.close();
                dbhelper.close();

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

    private void DivisionList() {
        divisiondata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select dvID,dvName from divisions ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    divisions = c.getString(c.getColumnIndex("dvName"));
                    divisiondata.add(divisions);

                } while (c.moveToNext());
            }
        }


        divisionadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_min, divisiondata);
        divisionadapter.setDropDownViewResource(R.layout.spinner_item_min);
        spDivision.setAdapter(divisionadapter);
        spDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String divisionName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select dvID from divisions where dvName= '" + divisionName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    divisionid = c.getString(c.getColumnIndex("dvID"));


                }
                c.close();
                db.close();
                dbhelper.close();
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
