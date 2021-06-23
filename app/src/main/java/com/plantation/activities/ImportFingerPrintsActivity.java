package com.plantation.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.plantation.fingerprintreader.EmployeeFingerprintsAratek;
import com.plantation.helpers.DirectoryChooserDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import cn.com.aratek.dev.Terminal;
import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintScanner;
import cn.com.aratek.util.Result;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by Michael on 30/06/2016.
 */
public class ImportFingerPrintsActivity extends AppCompatActivity {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private static final String TAG = "FingerprintDemo";
    private static final String FP_DB_PATH = "/sdcard/fp.db";
    private static final int MSG_SHOW_ERROR = 0;
    private static final int MSG_SHOW_INFO = 1;
    private static final int MSG_UPDATE_IMAGE = 2;
    private static final int MSG_UPDATE_TEXT = 3;
    private static final int MSG_UPDATE_BUTTON = 4;
    private static final int MSG_UPDATE_SN = 5;
    private static final int MSG_UPDATE_FW_VERSION = 6;
    private static final int MSG_SHOW_PROGRESS_DIALOG = 7;
    private static final int MSG_DISMISS_PROGRESS_DIALOG = 8;
    static SharedPreferences mSharedPrefs, prefs;
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btnImport, btnExport, btnEnroll, btnClearDB;
    Intent mIntent;
    String USER_MASTER = "1";
    File file;
    String s_femID, s_femPickerNO, s_femFingerNO, s_femFinger, s_femFingerprintb64, s_femFingerprinthex;
    Cursor fprint;
    String path;
    Handler _handler;
    int count = 0;
    int lineCount = 0;
    ArcProgress arcProgress;
    String systembasedate;
    int option = 0;
    private int progressStatus = 0;
    private ProgressDialog mProgressDialog;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_ERROR: {
                    Toast.makeText(getApplicationContext(), getString(R.string.fingerprint_device_power_on_failed), LENGTH_LONG).show();
                    break;
                }
                case MSG_SHOW_INFO: {
                    Toast.makeText(getApplicationContext(), getString(R.string.fingerprint_device_open_success), LENGTH_LONG).show();
                    //  mInformation.setTextColor(getResources().getColor(R.color.information_text_color));
                    //  mDetails.setTextColor(getResources().getColor(R.color.information_details_text_color));
                    //   mInformation.setText(((Bundle) msg.obj).getString("information"));
                    //   mDetails.setText(((Bundle) msg.obj).getString("details"));
                    break;
                }
                case MSG_UPDATE_IMAGE: {
                    // mFingerprintImage.setImageBitmap((Bitmap) msg.obj);
                    break;
                }
                case MSG_UPDATE_TEXT: {
                    // String[] texts = (String[]) msg.obj;
                    // mCaptureTime.setText(texts[0]);
                    // mExtractTime.setText(texts[1]);
                    //mGeneralizeTime.setText(texts[2]);
                    // mVerifyTime.setText(texts[3]);
                    break;
                }
                case MSG_UPDATE_BUTTON: {
                    // Boolean enable = (Boolean) msg.obj;
                    // mBtnEnroll.setEnabled(enable);
                    // mBtnVerify.setEnabled(enable);
                    // mBtnIdentify.setEnabled(enable);
                    // mBtnClear.setEnabled(enable);
                    // mBtnShow.setEnabled(enable);
                    break;
                }
                case MSG_UPDATE_SN: {
                    //  mSN.setText((String) msg.obj);
                    break;
                }
                case MSG_UPDATE_FW_VERSION: {
                    // mFwVersion.setText((String) msg.obj);
                    break;
                }
                case MSG_SHOW_PROGRESS_DIALOG: {
                    String[] info = (String[]) msg.obj;
                    // txtdesc.setText(info[1]);
                    mProgressDialog.setTitle(info[0]);
                    mProgressDialog.setMessage(info[1]);
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.show();
                    break;
                }
                case MSG_DISMISS_PROGRESS_DIALOG: {
                    //txtdesc.setText("");
                    mProgressDialog.dismiss();
                    break;
                }
            }
        }
    };
    private ProgressBar progressBar;
    private TextView textView;
    private TextView mInformation;
    private TextView mDetails;
    private TextView mSN;
    private TextView mFwVersion;
    private Button mBtnEnroll;
    private Button mBtnVerify;
    private Button mBtnIdentify;
    private Button mBtnClear;
    private Button mBtnShow;
    private EditText mCaptureTime;
    private EditText mExtractTime;
    private EditText mGeneralizeTime;
    private EditText mVerifyTime;
    private ImageView mFingerprintImage;
    private FingerprintScanner mScanner;
    private int mId;

    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        s = s.replaceAll(" ", "");
        int len = s.length();
        if (len % 2 == 1) {
            Log.d("Hex Length ", len + " " + s);
            String s2 = s.replaceAll(" ", "");
            Log.d("Hex Length 2 ", s2.length() + " " + s2);
            throw new IllegalArgumentException("Hex string must have even number of characters");

        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String ByteArrayToHexString(byte[] bytes, int size) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[size * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < size; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_import_fingerprints);
        setupToolbar();
        initializer();
        _handler = new Handler();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.import_fingerprints);

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
        mScanner = FingerprintScanner.getInstance(this);

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
                        ActivityCompat.requestPermissions(ImportFingerPrintsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        btnImport.setEnabled(true);
                    } else {
                        btnImport.setEnabled(true);
                        openDevice();
                        openfolder();

                    }
                } else {
                    openDevice();
                    openfolder();
                    btnImport.setEnabled(true);
                }

            }
        });

        btnEnroll = findViewById(R.id.btnEnroll);
        btnEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // closeDevice();
                finish();
                mIntent = new Intent(getApplicationContext(), EmployeeFingerprintsAratek.class);
                startActivity(mIntent);
            }
        });
        btnExport = findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        ImportFingerPrintsActivity.this);
                // Setting Dialog Title
                alertDialog.setTitle("Export");
                // Setting Dialog Message
                alertDialog.setMessage("Are you sure you want to export Fingerprints?");

                // Setting Positive "Yes" Button
                alertDialog.setNegativeButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new ExportFileAsync().execute();
                                // stopService(new Intent(MainActivity.this, WeighingService.class));


                            }
                        });
                // Setting Negative "NO" Button
                alertDialog.setPositiveButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to invoke NO event
                                dialog.cancel();
                            }
                        });
                // Showing Alert Message
                alertDialog.show();
            }
        });
        btnClearDB = findViewById(R.id.btnClearDB);
        btnClearDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openDevice();
                    Thread.sleep(500);
                    ClearFingerprintDB();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    public void ClearFingerprintDB() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                ImportFingerPrintsActivity.this);
        // Setting Dialog Title
        alertDialog.setTitle("Clear Database?");
        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to clear the Fingerprints Database?");

        // Setting Positive "Yes" Button
        alertDialog.setNegativeButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int error = Bione.clear();
                        if (error == Bione.RESULT_OK) {

                            SQLiteDatabase db = dbhelper.getWritableDatabase();
                            db.delete(Database.FINGERPRINT_TABLE_NAME, null, null);
                            db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FINGERPRINT_TABLE_NAME + "'");

                            Context context = getApplicationContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText(getString(R.string.clear_fingerprint_database_success));
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(LENGTH_LONG);
                            customtoast.show();

                            closeDevice();
                            finish();
                            mIntent = new Intent(getApplicationContext(), ImportFingerPrintsActivity.class);
                            startActivity(mIntent);
                        } else {
                            Context context = getApplicationContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText(getString(R.string.clear_fingerprint_database_failed));
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(LENGTH_LONG);
                            customtoast.show();
                            //showError(getString(R.string.clear_fingerprint_database_failed), getFingerprintErrorString(error));
                        }


                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setPositiveButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        dialog.cancel();
                        finish();
                        mIntent = new Intent(getApplicationContext(), ImportFingerPrintsActivity.class);
                        startActivity(mIntent);
                    }
                });
        // Showing Alert Message
        alertDialog.show();
    }

    private void showInformation(String info, String details) {
        Bundle bundle = new Bundle();
        bundle.putString("information", info);
        bundle.putString("details", details);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_INFO, bundle));
    }

    private void showError(String info, String details) {
        Bundle bundle = new Bundle();
        bundle.putString("information", info);
        bundle.putString("details", details);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_ERROR, bundle));
    }

    private void openDevice() {
        new Thread() {
            @Override
            public void run() {
                synchronized (ImportFingerPrintsActivity.this) {

                    //Toast.makeText(getApplicationContext(),getString(R.string.loading)+ getString(R.string.preparing_device),LENGTH_LONG).show();
                    int error;
                    if ((error = mScanner.powerOn()) != FingerprintScanner.RESULT_OK) {
                        //Toast.makeText(getApplicationContext(),getString(R.string.fingerprint_device_power_on_failed),LENGTH_LONG).show();
                        showError(getString(R.string.fingerprint_device_power_on_failed), "");
                    }
                    if ((error = mScanner.open()) != FingerprintScanner.RESULT_OK) {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SN, getString(R.string.fps_sn, "null")));
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FW_VERSION, getString(R.string.fps_fw, "null")));
                        showError(getString(R.string.fingerprint_device_open_failed), "");
                        // Toast.makeText(getApplicationContext(),getString(R.string.fingerprint_device_open_failed), LENGTH_LONG).show();
                    } else {
                        Result res = mScanner.getSN();
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SN, Terminal.getSdkVersion()));
                        res = mScanner.getFirmwareVersion();
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FW_VERSION, getString(R.string.fps_fw, res.data)));
                        showInformation(getString(R.string.fingerprint_device_open_success), "");
                        //  Toast.makeText(getApplicationContext(),getString(R.string.fingerprint_device_open_success), LENGTH_LONG).show();

                    }
                    if ((error = Bione.initialize(ImportFingerPrintsActivity.this, FP_DB_PATH)) != Bione.RESULT_OK) {
                        //Toast.makeText(getApplicationContext(),getString(R.string.algorithm_initialization_failed), LENGTH_LONG).show();
                    }
                    Log.i(TAG, "Fingerprint algorithm version: " + Bione.getVersion());

                }
            }
        }.start();
    }

    private void closeDevice() {
        new Thread() {
            @Override
            public void run() {
                synchronized (ImportFingerPrintsActivity.this) {
                    // showProgressDialog(getString(R.string.loading), getString(R.string.closing_device));
                    //enableControl(false);
                    int error;
                    /*if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
                        mTask.cancel(false);
                        mTask.waitForDone();
                    }*/
                    if ((error = mScanner.close()) != FingerprintScanner.RESULT_OK) {
                        //showError(getString(R.string.fingerprint_device_close_failed), getFingerprintErrorString(error));
                    } else {
                        // showInformation(getString(R.string.fingerprint_device_close_success), null);
                    }
                    if ((error = mScanner.powerOff()) != FingerprintScanner.RESULT_OK) {
                        //showError(getString(R.string.fingerprint_device_power_off_failed), getFingerprintErrorString(error));
                    }
                    if ((error = Bione.exit()) != Bione.RESULT_OK) {
                        // showError(getString(R.string.algorithm_cleanup_failed), getFingerprintErrorString(error));
                    }

                }
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // btnImport.setEnabled(true);
        //openDevice();
    }

    @Override
    protected void onPause() {

        // closeDevice();
        super.onPause();
    }

    public void openfolder() {


        /////////////////////////////////////////////////////////////////////////////////////////////////
        //Create FileSaveDialog and register a callback
        /////////////////////////////////////////////////////////////////////////////////////////////////
        DirectoryChooserDialog FileSaveDialog = new DirectoryChooserDialog(ImportFingerPrintsActivity.this, "FileSave", new DirectoryChooserDialog.SimpleFileDialogListener() {
            @Override
            public void onChosenDir(String chosenDir) {
                path = chosenDir;
                int error = Bione.clear();
                if (error == Bione.RESULT_OK) {

                    SQLiteDatabase db = dbhelper.getWritableDatabase();

                    db.delete(Database.FINGERPRINT_TABLE_NAME, null, null);
                    db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FINGERPRINT_TABLE_NAME + "'");

                    new ImportFileAsync().execute(path);
                } else {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Failed to import FingerPrint, Device not Open");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(LENGTH_LONG);
                    customtoast.show();
                    //showError(getString(R.string.clear_fingerprint_database_failed), getFingerprintErrorString(error));
                }


            }
        });

        //You can change the default filename using the public variable "Default_File_Name"
        FileSaveDialog.Default_File_Name = "";

        FileSaveDialog.choosefpFile_or_Dir();

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
            text.setText("You cannot close window while importing FingerPrints !!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
            customtoast.setDuration(LENGTH_LONG);
            customtoast.show();
            return;
        }

        closeDevice();
        finish();
        Intent mIntent = new Intent(ImportFingerPrintsActivity.this, MainActivity.class);
        startActivity(mIntent);


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
                InputStreamReader csvStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
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


                        // if (next[0].equals(USER_MASTER)) {


                        s_femID = next[0];
                        s_femPickerNO = next[1];
                        s_femFingerNO = next[2];
                        s_femFinger = next[3];
                        //  s_femFingerprintb64 = next[4];
                        s_femFingerprintb64 = "";
                        s_femFingerprinthex = next[4];
                        //byte []fpTemp = Base64.decode(s_femFingerprintb64, Base64.DEFAULT);
                        byte[] fpTemp = HexStringToByteArray(s_femFingerprinthex);

                        int ret = Bione.enroll(Integer.parseInt(s_femFinger), fpTemp);
                        if (ret != Bione.RESULT_OK) {
                            showError(getString(R.string.enroll_failed_because_of_error), "");
                            break;
                        }


                        //  showInformation(getString(R.string.enroll_success), getString(R.string.enrolled_id, Integer.parseInt(s_femFinger)));

                        dbhelper.AddFP(s_femID, s_femPickerNO, s_femFingerNO, s_femFinger, s_femFingerprintb64, s_femFingerprinthex);


                        //}


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
            text.setText(count + " FingerPrints imported successfully");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(LENGTH_LONG);
            customtoast.show();
            closeDevice();
            finish();
            mIntent = new Intent(ImportFingerPrintsActivity.this, MainActivity.class);
            startActivity(mIntent);

            //Toast.makeText(ImportMasterActivity.this, "Data Imported successfully!!", Toast.LENGTH_LONG).show();
        }
    }

    class ExportFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(DIALOG_DOWNLOAD_PROGRESS);
            arcProgress = findViewById(R.id.arc_progress);
            arcProgress.setProgress(0);
            arcProgress.setVisibility(View.VISIBLE);
            textView = findViewById(R.id.textView1);


            File dbFile = getDatabasePath(DBHelper.DB_NAME);
            //DBHelper dbhelper = new DBHelper(getApplicationContext());
            File exportDir = new File(Environment.getExternalStorageDirectory() + "/" + "/Easyweigh/Fingerprints/");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            String fileName = mSharedPrefs.getString("terminalID", "") + "EmployeeFingerPrints.csv";
            file = new File(exportDir, fileName);
        }

        @Override
        protected String doInBackground(String... aurl) {

            try {
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

                fprint = db.rawQuery("select * from " + Database.FINGERPRINT_TABLE_NAME + "", null);
                count = count + fprint.getCount();
                //csvWrite.writeNext(fprint.getColumnNames());
                while (fprint.moveToNext()) {


                    s_femID = fprint.getString(fprint.getColumnIndex(Database.FEM_ID));
                    s_femPickerNO = fprint.getString(fprint.getColumnIndex(Database.FEM_PICKERNO));
                    s_femFingerNO = fprint.getString(fprint.getColumnIndex(Database.FEM_FINGERNO));

                    s_femFinger = fprint.getString(fprint.getColumnIndex(Database.FEM_FINGERPRINT));
                    s_femFingerprintb64 = fprint.getString(fprint.getColumnIndex(Database.FEM_FINGERPRINTB64));
                    s_femFingerprinthex = fprint.getString(fprint.getColumnIndex(Database.FEM_FINGERPRINTHEX));
                    // s_femFingerprint="";


                    String[] fprintance = {s_femID,
                            s_femPickerNO,
                            s_femFingerNO,
                            s_femFinger,
                            s_femFingerprinthex};
                    csvWrite.writeNext(fprintance);

                    progressStatus++;
                    publishProgress("" + progressStatus);
                }
                csvWrite.close();
                fprint.close();
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
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText(count + " Records Exported successfully");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();


            count = 0;

            // b.dismiss();
            //Toast.makeText(ExportMasterActivity.this, "Data Exported successfully!!", Toast.LENGTH_LONG).show();
        }
    }

}