package com.plantation.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.helpers.CustomListReport;


public class HarvestReportsActivity extends AppCompatActivity {

    static SharedPreferences mSharedPrefs;
    public View mView;
    public Intent mIntent;
    public LinearLayoutManager layoutManager;
    public Context mContext;
    public Toolbar toolbar;
    SharedPreferences prefs;
    ListView list;
    String[] web = {
            "Employee Report", "Batch Report"
            , "Machines Report"
    };
    Integer[] imageId = {

            R.drawable.ic_receipt,
            R.drawable.ic_zreport,
            R.drawable.ic_delivary
    };


    TextView textMachineId, textMachineNo, textMachineOP;
    ListView listMachines;
    TextView tvMachine;
    SearchView searchView;
    SimpleCursorAdapter ca;
    AlertDialog dMachine;

    DBHelper dbhelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.harvest_report);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reports");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void initializer() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dbhelper = new DBHelper(getApplicationContext());
        CustomListReport adapter = new
                CustomListReport(this, web, imageId);
        list = findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> {

            switch (position) {
                case 0:
                    mIntent = new Intent(getApplicationContext(), EmployeeDetailedRecieptsActivity.class);
                    startActivity(mIntent);
                    break;
                case 1:
                    mIntent = new Intent(getApplicationContext(), HarvestRecieptsActivity.class);
                    startActivity(mIntent);
                    break;
                case 2:

                    Machine();

                    break;


                default:
                    break;
            }


        });

    }

    private void Machine() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HarvestReportsActivity.this);
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
            textMachineNo = selectedView.findViewById(R.id.tvCode);
            textMachineOP = selectedView.findViewById(R.id.tvOperators);
            String MachineNo = textMachineNo.getText().toString();
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("MachineNo", MachineNo);
            edit.apply();
            edit.putInt("Operators", Integer.parseInt(textMachineOP.getText().toString()));
            edit.apply();
            Log.d("Accounts", "Selected Account Id : " + MachineNo);
            mIntent = new Intent(getApplicationContext(), MachineOperatorsActivity.class);
            startActivity(mIntent);
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
            int[] to = {R.id.txtAccountId, R.id.tvCode, R.id.tvOperators};


            ca = new SimpleCursorAdapter(getApplicationContext(), R.layout.list_item, accounts, from, to);

            listMachines.setAdapter(ca);
            // dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
