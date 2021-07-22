package com.plantation.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.plantation.helpers.Fuel;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class MachineFuelActivity extends AppCompatActivity {

    static SharedPreferences mSharedPrefs, prefs;
    public Intent mIntent;
    public Toolbar toolbar;


    EditText edtLitres;

    TextView textMachineId, textMachineNo, textMachineOP;
    ListView listMachines;
    Cursor machines;
    String MDate;


    SQLiteDatabase db;
    DBHelper dbhelper;
    DecimalFormat formatter;

    Button btnSave;

    SearchView searchView;
    SimpleCursorAdapter ca;
    AlertDialog dFuel;
    EditText edtFuel;
    TextView tvMessage;
    FuelArrayAdapter ArrayAdapter;

    // Declaration of Date Formats.
    SimpleDateFormat dateTimeFormatA;
    SimpleDateFormat dateTimeFormatB;

    String DeliverNoteNumber, sDate, sterminalID, smachineNo, mfTime, mfLitres, smTaskCode, smCompany, smEstate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_fuel);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Machine Fueling");

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

        prefs = PreferenceManager.getDefaultSharedPreferences(MachineFuelActivity.this);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(MachineFuelActivity.this);

        dbhelper = new DBHelper(MachineFuelActivity.this);
        db = dbhelper.getReadableDatabase();

        formatter = new DecimalFormat("00");

        dateTimeFormatA = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateTimeFormatB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        DeliverNoteNumber = prefs.getString("DeliverNoteNumber", "");

        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search Machine ...");
        searchView.setVisibility(View.GONE);
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

        listMachines = findViewById(R.id.lvMachines);
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

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(selectedView.getContext());
            LayoutInflater inflater = getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_machine_fuel, null);
            dialogBuilder.setView(dialogView);

            TextView toolbar = dialogView.findViewById(R.id.app_bar);
            toolbar.setText("Fuel");

            edtFuel = dialogView.findViewById(R.id.edtFuel);
            edtFuel.setText(getFuel(MachineNo, sDate));

            tvMessage = dialogView.findViewById(R.id.tvMessage);
            tvMessage.setText(Html.fromHtml("Machine No:<font color='#0036ff'>\n" + textMachineNo.getText().toString() + "</font>"));

            dialogBuilder.setPositiveButton("CANCEL", (dialog, whichButton) -> {
                //do something with edt.getText().toString();
                dialog.dismiss();

            });
            dialogBuilder.setNegativeButton("SAVE", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass


                }
            });
            dFuel = dialogBuilder.create();
            dFuel.show();
            dFuel.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddFuel();
                }
            });
        });


    }

    public String getFuel(String machineNo, String sDate) {
        String fuel = "0";
        Cursor mFuel = db.rawQuery("select mfLitres from " + Database.MACHINEFUEL_TABLE_NAME + " WHERE "
                + Database.MFMACHINENUMBER + "= '" + machineNo + "' and " + Database.MFDATE + "='" + sDate + "'", null);
        if (mFuel != null) {
            if (mFuel.getCount() > 0) {

                mFuel.moveToFirst();
                if (mFuel.getString(0) == null) {
                    fuel = "0";
                } else {
                    fuel = mFuel.getString(0);
                }
            }
        }

        return fuel;
    }

    public void AddFuel() {

        if (edtFuel.getText().length() == 0 || edtFuel.getText().toString().equals("0")) {
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("Please Enter Fuel");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }
        sDate = dateTimeFormatA.format(new Date(getDate()));
        sterminalID = mSharedPrefs.getString("terminalID", "");
        smachineNo = prefs.getString("MachineNo", "");

        Calendar cal = Calendar.getInstance();
        mfTime = dateTimeFormatB.format(cal.getTime());
        mfLitres = edtFuel.getText().toString();


        smTaskCode = "";
        smCompany = mSharedPrefs.getString("company_prefix", "");
        smEstate = prefs.getString("estateCode", "");

        Log.i("Machine", smachineNo + " " + sDate);

        Cursor checkFuel = dbhelper.CheckFuel(smachineNo, sDate);
        if (checkFuel.getCount() > 0) {

            ContentValues values = new ContentValues();
            values.put(Database.MFLitres, mfLitres);


            long rows = db.update(Database.MACHINEFUEL_TABLE_NAME, values,
                    "mfmachineNo = ? and date=? ", new String[]{smachineNo, sDate});

            if (rows > 0) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Updated Successfully");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                dFuel.dismiss();
                getMachineList();

            }
        } else {
            dbhelper.AddMachineFuel(sDate, sterminalID, smachineNo, mfTime, mfLitres, smTaskCode, smCompany, smEstate);

            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("Saved Successfully");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            dFuel.dismiss();
            getMachineList();
        }

    }

    public void getMachineList() {

        try {
            Calendar cal = Calendar.getInstance();
            MDate = dateTimeFormatA.format(cal.getTime());
            SQLiteDatabase db = dbhelper.getReadableDatabase();
//            Cursor machines = db.query(true, Database.MACHINEOP_TABLE_NAME, null,Database.MDATE+"='" + MDate + "'", null, Database.MACHINENUMBER, null, null, null, null);
            machines = db.rawQuery("select * from " + Database.MACHINE_TABLE_NAME + "," + Database.MACHINEOP_TABLE_NAME + "" +
                    " where " + Database.MC_ID + "=" + Database.MACHINENUMBER + " and " + Database.MDATE + "='" + MDate + "' group by machineNo", null);

            ArrayList<Fuel> arraylist = new ArrayList<Fuel>();

            if (machines.getCount() > 0) {
                while (machines.moveToNext()) {

                    arraylist.add(new Fuel(machines.getString(machines.getColumnIndex(Database.ROW_ID)),
                            machines.getString(machines.getColumnIndex(Database.MC_ID)),
                            machines.getString(machines.getColumnIndex(Database.MC_NAME))));
                }
                ArrayAdapter = new FuelArrayAdapter(getApplicationContext(), R.layout.list_fuel, arraylist);

//                String[] from = {Database.ROW_ID, Database.MC_ID, Database.MC_NAME};
//                int[] to = {R.id.txtAccountId, R.id.tvCode, R.id.tvOperators};
//                ca = new SimpleCursorAdapter(this, R.layout.list_item, machines, from, to);

                listMachines.setAdapter(ArrayAdapter);
            }
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

    public class FuelArrayAdapter extends ArrayAdapter<Fuel> {

        Context context;
        int layoutResourceId;
        ArrayList<Fuel> mFuel;

        public FuelArrayAdapter(Context context, int layoutResourceId,
                                ArrayList<Fuel> mFuel) {
            super(context, layoutResourceId, mFuel);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.mFuel = mFuel;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View item = convertView;
            FuelWrapper FuelWrapper = null;

            if (item == null) {
                LayoutInflater inflater = getLayoutInflater();
                item = inflater.inflate(layoutResourceId, parent, false);
                FuelWrapper = new FuelWrapper();
                FuelWrapper.txtAccountId = item.findViewById(R.id.txtAccountId);
                FuelWrapper.tvCode = item.findViewById(R.id.txtUserName);
                FuelWrapper.tvOperators = item.findViewById(R.id.tvOperators);

                item.setTag(FuelWrapper);
            } else {
                FuelWrapper = (FuelWrapper) item.getTag();
            }

            Fuel fl = mFuel.get(position);
            FuelWrapper.txtAccountId.setText(fl.getID());
            FuelWrapper.tvCode.setText(fl.getMachineNo());
            sDate = dateTimeFormatA.format(new Date(getDate()));
            FuelWrapper.tvOperators.setText(getFuel(fl.getMachineNo(), sDate));


            return item;

        }

        private class FuelWrapper {
            TextView txtAccountId;
            TextView tvCode;
            TextView tvOperators;


        }
    }

}
