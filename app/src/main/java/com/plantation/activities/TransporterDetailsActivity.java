package com.plantation.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.synctocloud.MasterApiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Michael on 30/06/2016.
 */
public class TransporterDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddTpt, btn_svTpt;
    ListView listTransporters;
    EditText tptID, tptName;
    String s_tptID, s_tptName;
    String accountId, Transportercode;
    TextView textAccountId;
    Boolean success = true;

    String CRecordIndex, TRecordIndex;
    String restApiResponse;
    int response;
    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());
    SharedPreferences mSharedPrefs, prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_tpt);

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
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dbhelper = new DBHelper(getApplicationContext());
        btAddTpt = findViewById(R.id.btAddUser);
        btAddTpt.setVisibility(View.GONE);
        btAddTpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });
        listTransporters = this.findViewById(R.id.lvUsers);
        listTransporters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                // Intent intent = new Intent(Activity_ListStock.this, UpdateStock.class);
                // intent.putExtra("accountid", textAccountId.getText().toString());
                // startActivity(intent);
                showUpdateUserDialog();
            }
        });


    }

    public void showAddUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_transporter, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Transporters");
        tptID = dialogView.findViewById(R.id.tptID);
        tptName = dialogView.findViewById(R.id.tptName);


        btn_svTpt = dialogView.findViewById(R.id.btn_svTpt);
        btn_svTpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_tptID = tptID.getText().toString();
                    s_tptName = tptName.getText().toString();


                    if (s_tptID.equals("") || s_tptName.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkTransporter = dbhelper.CheckTransporter(s_tptID);
                    //Check for duplicate id number
                    if (checkTransporter.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Transporter already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddTransporter(s_tptID, s_tptName, "");
                    if (success) {


                        Toast.makeText(TransporterDetailsActivity.this, "Transporter Saved successfully!!", Toast.LENGTH_LONG).show();

                        tptID.setText("");
                        tptName.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(TransporterDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
                    }

                }

            }
        });

        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                getdata();

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                getdata();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sync, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {

            case R.id.action_sync:
                if (!isInternetOn()) {
                    createNetErrorDialog();
                    return true;
                }
                LoadTransporters();

                return true;
            case R.id.action_clear:

                SQLiteDatabase db = dbhelper.getWritableDatabase();
                db.delete(Database.TRANSPORTER_TABLE_NAME, null, null);
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.TRANSPORTER_TABLE_NAME + "'");
                getdata();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec = (ConnectivityManager) getBaseContext().getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {


            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {


            return false;
        }
        return false;
    }

    protected void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(TransporterDetailsActivity.this);
        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>You need internet connection to proceed. Please turn on mobile network or Wi-Fi in Settings.</font>"))
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setNegativeButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (mSharedPrefs.getString("internetAccessModes", "WF").equals("WF")) {

                                    Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                    startActivity(i);
                                } else {
                                    Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                    startActivity(i);

                                }


                            }
                        }
                )
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void LoadTransporters() {
        progressDialog = ProgressDialog.show(TransporterDetailsActivity.this,
                "Loading Transporters",
                "Please Wait.. ");

        executor.execute(() -> {
            //Background work here

            CRecordIndex = prefs.getString("CRecordIndex", null);
            //  Log.e(TAG, "Response from url: " + jsonStr);
            restApiResponse = new MasterApiRequest(getApplicationContext()).getTransporters(CRecordIndex);
            response = prefs.getInt("transresponse", 0);
            if (response == 200) {
                try {


                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                    Cursor routes = db.query(true, Database.TRANSPORTER_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (routes.getCount() == 0) {
                        String DefaultRoute = "INSERT INTO " + Database.TRANSPORTER_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.TPT_NAME + ") Values ('0', 'Select ...')";
                        db.execSQL(DefaultRoute);
                    }

                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);
                        TRecordIndex = obj.getString("RecordIndex");
                        s_tptID = obj.getString("TptCode");
                        s_tptName = obj.getString("TptName");

                        Log.i("TRecordIndex", TRecordIndex);
                        Log.i("s_tptID", s_tptID);

                        Cursor checkRoute = dbhelper.CheckTransporterID(TRecordIndex);
                        //Check for duplicate shed
                        if (checkRoute.getCount() > 0) {
                            // Toast.makeText(getApplicationContext(), "Route already exists",Toast.LENGTH_SHORT).show();

                        } else {
                            dbhelper.AddTransporter(s_tptID, s_tptName, TRecordIndex);
                        }


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());
                    Log.e("Server Response", e.toString());
                    e.printStackTrace();

                }

            }
            handler.post(() -> {
                //UI Thread work here
                progressDialog.dismiss();
                getdata();
            });
        });
    }

    public void showUpdateUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_transporter, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Transporters");
        accountId = textAccountId.getText().toString();

        tptID = dialogView.findViewById(R.id.tptID);
        tptID.setEnabled(false);
        tptName = dialogView.findViewById(R.id.tptName);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.TRANSPORTER_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            tptID.setText(account.getString(account
                    .getColumnIndex(Database.TPT_ID)));
            tptName.setText(account.getString(account
                    .getColumnIndex(Database.TPT_NAME)));


        }
        account.close();
        db.close();
        dbhelper.close();


        btn_svTpt = dialogView.findViewById(R.id.btn_svTpt);
        btn_svTpt.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteTransporter();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateTransporter();
                getdata();


            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        getdata();
    }

    public void updateTransporter() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.TPT_ID, tptID.getText().toString());
            values.put(Database.TPT_NAME, tptName.getText().toString());


            long rows = db.update(Database.TRANSPORTER_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            db.close();
            if (rows > 0) {
                Toast.makeText(this, "Updated Transporter Successfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Transporter!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteTransporter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Transporter?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteCurrentAccount();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
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
           /* Cursor c=db.rawQuery("select fzCode from Transporters where _id= '"+accountId+"' ", null);
            if(c!=null)
            {
                c.moveToFirst();
                Transportercode= c.getString(c.getColumnIndex("fzCode"));
            }
            c.close();

            Cursor c1=db.rawQuery("select * from CollectionCenters where MccTransporter= '" + Transportercode + "' ", null);
            if(c1.getCount() > 0){
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Could not delete Transporter! ,Because its related in sheds");
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(this, "Could not delete Transporter! ,Because its related in sheds", Toast.LENGTH_LONG).show();
                c1.close();
            }
            else{*/

            int rows = db.delete(Database.TRANSPORTER_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if (rows == 1) {
                Toast.makeText(this, "Transporter Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            } else {
                Toast.makeText(this, "Could not delete Transporter!", Toast.LENGTH_LONG).show();
            }
            //}

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {
            int ROWID = 0;
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.TRANSPORTER_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);

            String[] from = {Database.ROW_ID, Database.TPT_ID, Database.TPT_NAME};
            int[] to = {R.id.txtAccountId, R.id.txtUserName, R.id.txtUserType};

            @SuppressWarnings("deprecation")
            SimpleCursorAdapter ca = new SimpleCursorAdapter(this, R.layout.userlist, accounts, from, to);

            ListView listusers = this.findViewById(R.id.lvUsers);
            listusers.setAdapter(ca);
            dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


}
