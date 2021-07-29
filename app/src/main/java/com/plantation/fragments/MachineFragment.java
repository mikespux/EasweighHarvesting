package com.plantation.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.plantation.R;
import com.plantation.activities.CheckInOPActivity;
import com.plantation.activities.CheckOutActivity;
import com.plantation.activities.MachineFuelActivity;
import com.plantation.activities.MachineProduceActivity;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.helpers.CustomList;
import com.plantation.services.EasyWeighService;


public class MachineFragment extends Fragment {

    public static String cachedDeviceAddress;
    static SharedPreferences mSharedPrefs, prefs;
    public View mView;
    public Intent mIntent;

    public Context mContext;

    DBHelper dbhelper;

    EasyWeighService resetConn;
    SQLiteDatabase db;


    String[] web = {
            "Check-In Operator", "Check-Out Operator", "Machine Fueling", "Weighing"
    };
    Integer[] imageId = {

            R.drawable.ic_checkin_op,
            R.drawable.ic_checkout_op,
            R.drawable.ic_fueling,
            R.drawable.ic_weighing
    };
    ListView list;
    String BaseDate, BatchDate;

    TextView textMachineId, textMachineNo, textMachineOP;
    ListView listMachines;
    TextView tvMachine;
    SearchView searchView;
    SimpleCursorAdapter ca;
    AlertDialog dMachine;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_machine, container, false);
        initializer();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        return mView;
    }

    public void initializer() {

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
        resetConn = new EasyWeighService();


        CustomList adapter = new
                CustomList(getActivity(), web, imageId);
        list = mView.findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    if (prefs.getString("DeliverNoteNumber", "").equals("") || prefs.getString("DeliverNoteNumber", "").equals("No Batch Opened")) {
                        // snackbar.show();
                        Context context = getActivity().getApplicationContext();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
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
                    mIntent = new Intent(getActivity(), CheckInOPActivity.class);
                    startActivity(mIntent);
                    break;
                case 1:
                    if (prefs.getString("DeliverNoteNumber", "").equals("") || prefs.getString("DeliverNoteNumber", "").equals("No Batch Opened")) {
                        // snackbar.show();
                        Context context = getActivity().getApplicationContext();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
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
                    Machine();

                    break;

                case 2:
                    mIntent = new Intent(getActivity(), MachineFuelActivity.class);
                    startActivity(mIntent);
                    break;

                case 3:

                    if (prefs.getString("DeliverNoteNumber", "").equals("") || prefs.getString("DeliverNoteNumber", "").equals("No Batch Opened")) {
                        // snackbar.show();
                        Context context = getActivity().getApplicationContext();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
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

                    mIntent = new Intent(getActivity(), MachineProduceActivity.class);
                    startActivity(mIntent);
                    break;


                default:
                    break;
            }


        });


    }

    private void Machine() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
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
            textMachineNo = selectedView.findViewById(R.id.txtUserName);
            textMachineOP = selectedView.findViewById(R.id.tvOperators);
            String MachineNo = textMachineNo.getText().toString();
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("MachineNo", MachineNo);
            edit.apply();
            edit.putInt("Operators", Integer.parseInt(textMachineOP.getText().toString()));
            edit.apply();
            Log.d("Accounts", "Selected Account Id : " + MachineNo);
            mIntent = new Intent(getActivity(), CheckOutActivity.class);
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
            int[] to = {R.id.txtAccountId, R.id.txtUserName, R.id.tvOperators};


            ca = new SimpleCursorAdapter(getActivity(), R.layout.list_item, accounts, from, to);

            listMachines.setAdapter(ca);
            // dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


}
