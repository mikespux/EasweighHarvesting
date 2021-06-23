package com.plantation.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.helpers.DirectoryChooserDialog;
import com.plantation.preferences.PreferenceCompanySettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Created by Michael on 30/06/2016.
 */
public class ImportMasterActivity extends AppCompatActivity {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    static SharedPreferences mSharedPrefs, prefs;
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btnImport;
    Intent mIntent;
    String COMPANY_MASTER = "0";
    String ESTATE_MASTER = "1";
    String DIVISION_MASTER = "2";
    String FIELD_MASTER = "3";
    String BLOCK_MASTER = "4";
    String FACTORY_MASTER = "5";
    String COMMODITY_MASTER = "6";
    String VARIETY_MASTER = "7";
    String GRADE_MASTER = "8";
    String TASK_MASTER = "9";
    String EMPLOYEE_MASTER = "10";
    String USER_MASTER = "11";
    String MACHINE_MASTER = "12";
    String TRANSPORTER_MASTER = "13";
    String CAPITALP_MASTER = "14";
    String s_esID, s_esName, s_esCompany;
    String s_dvID, s_dvName, s_dvEstate;
    String s_fdID, s_fdDiv;
    String s_bkID, s_bkField;
    String s_fryprefix, s_fryname;
    String s_CMID, s_CMName;
    String s_vtID, s_vtName, s_vtComm;
    String s_grID, s_grName, s_grComm;
    String s_tkID, s_tkName, s_tkType, s_tkOT, s_tkMT;
    String s_emID, s_emName, s_emTeam, s_emPickerNo, s_emIDNo, s_emCardID;
    String s_MID, s_MName;
    String s_CPID, s_CPName;
    String s_etFullName, s_etNewUserId, s_etPassword, s_spUserLevel;
    String s_tptID, s_tptName;
    String Sco_prefix, Sco_name, Sco_letterbox, Sco_postcode, Sco_postname, Sco_postregion, Sco_telephone, license_key, server_url;
    String path;
    Handler _handler;
    int count = 0;
    ArcProgress arcProgress;
    private int progressStatus = 0;
    private ProgressDialog mProgressDialog;
    private ProgressBar progressBar;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_importmaster);
        setupToolbar();
        initializer();
        _handler = new Handler();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.nav_item_import);

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
        btnImport = findViewById(R.id.btnImport);
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnImport.setEnabled(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ImportMasterActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        btnImport.setEnabled(true);
                    } else {
                        btnImport.setEnabled(true);
                        openfolder();

                    }
                } else {
                    openfolder();
                    btnImport.setEnabled(true);
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // btnImport.setEnabled(true);
    }

    public void openfolder() {


        /////////////////////////////////////////////////////////////////////////////////////////////////
        //Create FileSaveDialog and register a callback
        /////////////////////////////////////////////////////////////////////////////////////////////////
        DirectoryChooserDialog FileSaveDialog = new DirectoryChooserDialog(ImportMasterActivity.this, "FileSave", new DirectoryChooserDialog.SimpleFileDialogListener() {
            @Override
            public void onChosenDir(String chosenDir) {
                path = chosenDir;


                SQLiteDatabase db = dbhelper.getWritableDatabase();
                db.delete(Database.OPERATORSMASTER_TABLE_NAME, null, null);
                db.delete(Database.ESTATES_TABLE_NAME, null, null);
                db.delete(Database.DIVISIONS_TABLE_NAME, null, null);
                db.delete(Database.FIELD_TABLE_NAME, null, null);
                db.delete(Database.BLOCK_TABLE_NAME, null, null);
                db.delete(Database.FACTORY_TABLE_NAME, null, null);
                db.delete(Database.PRODUCE_TABLE_NAME, null, null);
                db.delete(Database.PRODUCEVARIETIES_TABLE_NAME, null, null);
                db.delete(Database.PRODUCEGRADES_TABLE_NAME, null, null);
                db.delete(Database.TASK_TABLE_NAME, null, null);
                db.delete(Database.EM_TABLE_NAME, null, null);
                db.delete(Database.MACHINE_TABLE_NAME, null, null);
                db.delete(Database.TRANSPORTER_TABLE_NAME, null, null);

                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.OPERATORSMASTER_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.ESTATES_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.DIVISIONS_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FIELD_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.BLOCK_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FACTORY_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.PRODUCE_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.PRODUCEVARIETIES_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.PRODUCEGRADES_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.TASK_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.EM_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.MACHINE_TABLE_NAME + "'");
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.TRANSPORTER_TABLE_NAME + "'");

                String DefaultEstates = "INSERT INTO " + Database.ESTATES_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.ES_NAME + ") Values ('0', 'Select ...')";
                String DefaultDivision = "INSERT INTO " + Database.DIVISIONS_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.DV_NAME + ") Values ('0', 'Select ...')";
                String DefaultField = "INSERT INTO " + Database.FIELD_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.FD_ID + ") Values ('0', 'Select ...')";
                String DefaultBlock = "INSERT INTO " + Database.BLOCK_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.BK_ID + ") Values ('0', 'Select ...')";
                String DefaultComm = "INSERT INTO " + Database.PRODUCE_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.MP_DESCRIPTION + ") Values ('0', 'Select ...')";
                String DefaultVariety = "INSERT INTO " + Database.PRODUCEVARIETIES_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.VRT_NAME + ") Values ('0', 'Select ...')";
                String DefaultGrade = "INSERT INTO " + Database.PRODUCEGRADES_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.PG_DNAME + ") Values ('0', 'Select ...')";
                String DefaultTask = "INSERT INTO " + Database.TASK_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.TK_NAME + ") Values ('0', 'Select ...')";
                String DefaultEmployee = "INSERT INTO " + Database.EM_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.EM_NAME + ") Values ('0', 'Select ...')";
                String DefaultMachine = "INSERT INTO " + Database.MACHINE_TABLE_NAME + " ("
                        + Database.ROW_ID + ", "
                        + Database.MC_NAME + ") Values ('0', 'Select ...')";

                db.execSQL(DefaultEstates);
                db.execSQL(DefaultDivision);
                db.execSQL(DefaultBlock);
                db.execSQL(DefaultField);
                db.execSQL(DefaultComm);
                db.execSQL(DefaultVariety);
                db.execSQL(DefaultGrade);
                db.execSQL(DefaultTask);
                db.execSQL(DefaultEmployee);
                db.execSQL(DefaultMachine);

                new ImportFileAsync().execute(path);

            }
        });

        //You can change the default filename using the public variable "Default_File_Name"
        FileSaveDialog.Default_File_Name = "";

        FileSaveDialog.chooseFile_or_Dir();

        /////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Data Import ...");
                mProgressDialog.setMessage("Importing data from file ...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        //Display alert message when back button has been pressed

        if (count > 0) {
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("You cannot close window while importing master !!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor accounts = db.query(true, Database.ESTATES_TABLE_NAME, null, null, null, null, null, null, null, null);
        if (accounts.getCount() <= 1) {
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("Please Import Master !!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        } else {
            finish();
            btnImport.setVisibility(View.VISIBLE);
            mIntent = new Intent(ImportMasterActivity.this, MainActivity.class);
            startActivity(mIntent);
        }

        return;
    }

    class ImportFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(DIALOG_DOWNLOAD_PROGRESS);
            arcProgress = findViewById(R.id.arc_progress);
            arcProgress.setProgress(0);

            textView = findViewById(R.id.textView1);
            btnImport.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... aurl) {


            String[] next = {};
            File file = new File(path);
            try {

                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader csvStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String input;

                while ((input = bufferedReader.readLine()) != null) {
                    count++;
                }

                // System.out.println("Count : "+count);

                CSVReader reader = new CSVReader(csvStreamReader);

                for (; ; ) {
                    next = reader.readNext();


                    if (next != null) {
                        if (next[0].equals(COMPANY_MASTER)) {

                            Sco_prefix = next[1];
                            Sco_name = next[2];
                            Sco_letterbox = next[3];
                            Sco_postcode = next[4];
                            Sco_postname = next[5];
                            Sco_postregion = next[6];
                            Sco_telephone = next[7];
                            license_key = next[8];
                            server_url = next[9];

                            SharedPreferences.Editor edit = mSharedPrefs.edit();

                            edit.putString("company_prefix", Sco_prefix);
                            edit.commit();
                            edit.putString("company_name", Sco_name);
                            edit.commit();
                            edit.putString("company_letterbox", Sco_letterbox);
                            edit.commit();
                            edit.putString("company_postalcode", Sco_postcode);
                            edit.commit();
                            edit.putString("company_postalname", Sco_postname);
                            edit.commit();
                            edit.putString("company_postregion", Sco_postregion);
                            edit.commit();
                            edit.putString("company_posttel", Sco_telephone);
                            edit.commit();
                            edit.putString("licenseKey", license_key);
                            edit.commit();
                            edit.putString("portalURL", server_url);
                            edit.commit();

                        } else if (next[0].equals(ESTATE_MASTER)) {

                            s_esID = next[1];
                            s_esName = next[2];
                            s_esCompany = next[3];

                            dbhelper.AddEstate(s_esID, s_esName, s_esCompany);

                        } else if (next[0].equals(DIVISION_MASTER)) {

                            s_dvID = next[1];
                            s_dvName = next[2];
                            s_dvEstate = next[3];
                            dbhelper.AddDivision(s_dvID, s_dvName, s_dvEstate);
                        } else if (next[0].equals(FIELD_MASTER)) {

                            s_fdID = next[1];
                            s_fdDiv = next[2];
                            dbhelper.AddField(s_fdID, s_fdDiv);
                        } else if (next[0].equals(BLOCK_MASTER)) {

                            s_bkID = next[1];
                            s_bkField = next[2];

                            dbhelper.AddBlock(s_bkID, s_bkField);
                        } else if (next[0].equals(FACTORY_MASTER)) {

                            s_fryprefix = next[1];
                            s_fryname = next[2];
                            dbhelper.AddFactories(s_fryprefix, s_fryname);

                        } else if (next[0].equals(COMMODITY_MASTER)) {

                            s_CMID = next[1];
                            s_CMName = next[2];
                            dbhelper.AddProduce(s_CMID, s_CMName);

                        } else if (next[0].equals(VARIETY_MASTER)) {

                            s_vtID = next[1];
                            s_vtName = next[2];
                            s_vtComm = next[3];
                            dbhelper.AddVariety(s_vtID, s_vtName, s_vtComm);

                        } else if (next[0].equals(GRADE_MASTER)) {

                            s_grID = next[1];
                            s_grName = next[2];
                            s_grComm = next[3];
                            dbhelper.AddGrade(s_grID, s_grName, s_grComm);

                        } else if (next[0].equals(TASK_MASTER)) {


                            s_tkID = next[1];
                            s_tkName = next[2];
                            s_tkType = next[3];
                            s_tkOT = next[4];
                            s_tkMT = next[5];

                            dbhelper.AddTask(s_tkID, s_tkName, s_tkType, s_tkOT, s_tkMT);
                        } else if (next[0].equals(EMPLOYEE_MASTER)) {

                            s_emID = next[1];
                            s_emName = next[2];
                            s_emIDNo = next[3];
                            s_emCardID = next[4];
                            s_emPickerNo = next[5];

                            dbhelper.AddEM(s_emID, s_emName, s_emIDNo, s_emCardID, s_emPickerNo);
                        }

                        if (next[0].equals(USER_MASTER)) {

                            s_etNewUserId = next[1];
                            s_etFullName = next[2];
                            s_etPassword = next[3];
                            s_spUserLevel = next[4];
                            dbhelper.AddUsers(s_etFullName, s_etNewUserId, s_etPassword, s_spUserLevel);

                        } else if (next[0].equals(MACHINE_MASTER)) {

                            s_MID = next[1];
                            s_MName = next[2];

                            dbhelper.AddMachine(s_MID, s_MName);
                        } else if (next[0].equals(TRANSPORTER_MASTER)) {

                            s_tptID = next[1];
                            s_tptName = next[2];
                            dbhelper.AddTransporter(s_tptID, s_tptName);
                        } else if (next[0].equals(CAPITALP_MASTER)) {

                            s_CPID = next[1];
                            s_CPName = next[2];
                            dbhelper.AddCapitalP(s_CPID, s_CPName);
                        }


                    } else {
                        break;
                    }
                    progressStatus++;
                    publishProgress("" + progressStatus);


                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
            //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setMax(count);
            arcProgress.setBottomText("IMPORTING ...");
            textView.setText(Integer.parseInt(progress[0]) + "/" + count + " Records");
        }

        @Override
        protected void onPostExecute(String unused) {
            // dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            finish();
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText(count + " Records Imported successfully");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();


            if (mSharedPrefs.getString("terminalID", "").equals("")) {
                mIntent = new Intent(ImportMasterActivity.this, PreferenceCompanySettings.class);
                startActivity(mIntent);


                View customToastroot1 = inflater.inflate(R.layout.blue_toast, null);
                TextView text1 = customToastroot1.findViewById(R.id.toast);
                text1.setText("Prepare Settings ...");
                Toast customtoast1 = new Toast(context);
                customtoast1.setView(customToastroot1);
                customtoast1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast1.setDuration(Toast.LENGTH_LONG);
                customtoast1.show();
                return;
            }
            mIntent = new Intent(ImportMasterActivity.this, LoginActivity.class);
            startActivity(mIntent);

            //Toast.makeText(ImportMasterActivity.this, "Data Imported successfully!!", Toast.LENGTH_LONG).show();
        }
    }

}