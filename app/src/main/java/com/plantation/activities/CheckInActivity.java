package com.plantation.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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
import com.plantation.soap.SoapRequest;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;


public class CheckInActivity extends AppCompatActivity {
    public static final String FINGERPRINT = "FingerPrint";
    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    private static final boolean D = true;
    public static BluetoothAdapter mBluetoothAdapter;
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;
    static int CaptureNo = 0;
    static String EmployeeName;
    static String Employee_No, AuthMethod, VerMethod, DateTimeIn, DateCheckin, Estate, Division, Rtype, Vtype, TerminalID, UserID, TimeIn, TimeOut, Co_prefix, Current_User, Project;
    // list of NFC technologies detected:
    private final String[][] techList = new String[][]{
            new String[]{
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };
    private final int totalRecords = 0;
    public Toolbar toolbar;
    public SimpleCursorAdapter ca;
    DBHelper dbhelper;
    ListView listEmployees;
    String s_checkout, s_checkouttime, s_checkoutmethod, s_overtime, s_multiple;
    String accountId;
    TextView textAccountId, txtEmployeeNo, txtTaskCode;
    Boolean success = true;
    SQLiteDatabase db;
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat timeFormat;
    SimpleDateFormat dateFormat;
    SimpleDateFormat dateOnlyFormat;
    SimpleDateFormat BatchDateFormat;
    String ColDate, Time;
    SearchView searchView;
    Intent mIntent;
    String CardNo, Cardsn;
    SharedPreferences pref;
    TextView desc;
    String EmployeeNo = "";
    int cloudid = 0;
    String error, errorNo;
    Cursor attend;
    private TextView mTextView; //ECP 2017-01-16
    private String soapResponse, Attendance, returnValue;
    private int progressStatus = 0;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listalloc_creaderweigh);

        mTextView = findViewById(R.id.txtdesc);
        mTextView.setText("");
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check-In Employee");

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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(CheckInActivity.this);
        pref = PreferenceManager.getDefaultSharedPreferences(CheckInActivity.this);
        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();

        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        BatchDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        desc = this.findViewById(R.id.txtdesc);

        dbhelper = new DBHelper(getApplicationContext());

        listEmployees = findViewById(R.id.lvTasks);
        listEmployees.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                txtEmployeeNo = selectedView.findViewById(R.id.tv_number);
                txtTaskCode = selectedView.findViewById(R.id.tv_task);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());

                showTask();

                //CheckIn();


            }
        });


    }


    public void showTask() {
        String EmployeeName = "", Task;//
        // Task=txtTaskCode.getText().toString();
        Cursor account = db.query(Database.EM_TABLE_NAME, null,
                " emID = ?", new String[]{EmployeeNo}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            EmployeeName = account.getString(account
                    .getColumnIndex(Database.EM_NAME));
            CardNo = account.getString(account
                    .getColumnIndex(Database.EM_CARDID));
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Employee Checkin");
        dialogBuilder.setMessage(Html.fromHtml(
                "<font color='#FA0703'>CheckIn </font>" +
                        "Employee No:<font color='#0036ff'>\n" + EmployeeNo + "</font> Name: <font color='#0036ff'> " + EmployeeName + "</font>"));
        accountId = textAccountId.getText().toString();


        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                dialog.dismiss();

            }
        });
        dialogBuilder.setNegativeButton("Check In", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass


            }
        });
        final AlertDialog b = dialogBuilder.create();
        b.show();
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckIn();
                b.dismiss();
                //getdata();

            }
        });
    }

    public void CheckIn() {
        try {
            dbhelper = new DBHelper(getApplicationContext());
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command
            Calendar cal = Calendar.getInstance();
            Date date = new Date(getDate());
            SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat format3 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            ColDate = format1.format(date);
            Time = format2.format(cal.getTime());
            s_checkout = "1";
            s_checkouttime = ColDate + " " + Time;
            if (mSharedPrefs.getString("vModes", "FingerPrint").equals(FINGERPRINT)) {
                s_checkoutmethod = "2";
            } else if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {
                s_checkoutmethod = "1";

            } else if (mSharedPrefs.getString("vModes", "Manual").equals(MANUAL)) {
                s_checkoutmethod = "1";

            } else {
                s_checkoutmethod = "1";
            }

            Employee_No = EmployeeNo;
            AuthMethod = s_checkoutmethod;
            DateTimeIn = format3.format(cal.getTime());
            DateCheckin = format1.format(date);
            Estate = prefs.getString("estateCode", "");
            Division = prefs.getString("divisionCode", "");
            Rtype = "1";
            Vtype = "0";
            TerminalID = mSharedPrefs.getString("terminalID", "");
            UserID = prefs.getString("user", "");

            Cursor checkEmployee = dbhelper.CheckIn(EmployeeNo, DateCheckin, Rtype);
            //Check for duplicate id number
            if (checkEmployee.getCount() > 0) {
                Toast.makeText(getApplicationContext(), "Employee already Checked-In Today!!", Toast.LENGTH_SHORT).show();
                return;
            }
            Cursor account = db.query(Database.EM_TABLE_NAME, null,
                    " emID = ?", new String[]{EmployeeNo}, null, null, null);
            //startManagingCursor(accounts);
            if (account.moveToFirst()) {
                // update view
                EmployeeName = account.getString(account
                        .getColumnIndex(Database.EM_NAME));
                CardNo = account.getString(account
                        .getColumnIndex(Database.EM_CARDID));
            }

            dbhelper.AddCheckin(Employee_No, CardNo, AuthMethod, DateTimeIn, DateCheckin, Estate, Division, TerminalID, Rtype, Vtype, UserID);
            if (!mSharedPrefs.getBoolean("realtimeServices", false) == true) {

                //Toast.makeText(getBaseContext(), "Real time Services not enabled on Settings", Toast.LENGTH_LONG).show();

            } else {

                // Method to Send Weighments to Cloud.
                new PostAttRecord().execute();


            }

            if (success) {
                EmployeeNo = "";

                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText(Html.fromHtml(
                        "<font color='#FA0703'>Checked-In Successfully!!\n</font>" +
                                "Employee No:<font color='#0036ff'>\n" + EmployeeNo + "</font> Name: <font color='#0036ff'> " + EmployeeName + "</font>"));
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();

                // Toast.makeText(CheckInActivity.this, "Checked-In Successfully!!", Toast.LENGTH_LONG).show();


            }


        } catch (Exception ex) {
            if (success) {
                Toast.makeText(CheckInActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
            }
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getdata();
        EmployeeNo = "";

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {
        final Cursor accounts;
        try {
            int ROWID = 0;
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            if (EmployeeNo != "") {
                accounts = db.rawQuery("SELECT * FROM " + Database.EM_TABLE_NAME
                        + " WHERE emID=?", new String[]{EmployeeNo});
                if (accounts.getCount() == 0) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Card Not Registered!!");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                } else {
                    Cursor em = db.query(Database.EM_TABLE_NAME, null,
                            " emID = ?", new String[]{EmployeeNo}, null, null, null);
                    //startManagingCursor(accounts);
                    if (em.moveToFirst()) {
                        // update view
                        EmployeeName = em.getString(em
                                .getColumnIndex(Database.EM_NAME));
                        CardNo = em.getString(em
                                .getColumnIndex(Database.EM_CARDID));
                    }

                    Date date = new Date(getDate());
                    SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                    ColDate = format1.format(date);
                    Cursor check = db.rawQuery("select * from " + Database.EM_CHECKIN_TABLE_NAME + " WHERE "
                            + Database.Date + " ='" + ColDate + "' and " + Database.Employee_No + " ='" + EmployeeNo + "' and " + Database.Rtype + " ='1'", null);
                    if (check.getCount() > 0) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Employee " + EmployeeNo + " - " + EmployeeName + " has already Checked-In Today!!");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        EmployeeNo = "";
                        getdata();
                        return;
                    } else {
                        listEmployees.performItemClick(listEmployees.getAdapter().getView(0, null, null), 0, listEmployees.getAdapter().getItemId(0));
                    }
                }
            } else {
                EmployeeNo = "";
                accounts = db.query(true, Database.EM_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);

            }
            //

            String[] from = {Database.ROW_ID, Database.EM_ID, Database.EM_NAME, Database.EM_TEAM};
            int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_name, R.id.tv_pickerno};


            ca = new SimpleCursorAdapter(this, R.layout.employee_list, accounts, from, to);

            listEmployees = findViewById(R.id.lvTasks);
            listEmployees.setAdapter(ca);
            listEmployees.setTextFilterEnabled(true);

            //dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "1");

        //mTextView.setText("onResume:");
        // creating pending intent:
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // creating intent receiver for NFC events:
        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        // enabling foreground dispatch for getting intent from NFC event:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("onPause", "1");

        // disabling foreground dispatch:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "1");

        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Log.d("onNewIntent", "2");
            mTextView.setText("NFC Tag\n" + ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)));

            //if(getIntent().hasExtra(NfcAdapter.EXTRA_TAG)){

            Parcelable tagN = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tagN != null) {
                Log.d(TAG, "Parcelable OK");
                NdefMessage[] msgs;
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                byte[] payload = dumpTagData(tagN).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};

                //Log.d(TAG, msgs[0].toString());
                dbhelper = new DBHelper(this);
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor accounts = db.rawQuery("select * from " + Database.EM_TABLE_NAME + " where " + Database.EM_CARDID + " COLLATE NOCASE ='" + Cardsn + "'", null);
                if (accounts.getCount() == 0) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Card Not Registered!!");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    EmployeeNo = "";
                    return;
                }
                if (accounts.getCount() > 0) {
                    CaptureNo = 1;
                    while (accounts.moveToNext()) {
                        CardNo = accounts.getString(accounts.getColumnIndex(Database.EM_CARDID));

                        EmployeeNo = accounts.getString(accounts.getColumnIndex(Database.EM_ID));
                        getdata();
                        //listEmployees.performItemClick(listEmployees.getAdapter().getView(0, null, null), 0, listEmployees.getAdapter().getItemId(0));
                        //showWeighDialog();
                        //showWeighDialog();
                        //Toast.makeText(CheckInActivity.this, "EmployeeNO:" +EmployeeNo, Toast.LENGTH_LONG).show();

                    }
                }

            } else {
                Log.d(TAG, "Parcelable NULL");
            }


            Parcelable[] messages1 = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (messages1 != null) {
                Log.d(TAG, "Found " + messages1.length + " NDEF messages");
            } else {
                Log.d(TAG, "Not EXTRA_NDEF_MESSAGES");
            }

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {

                Log.d("onNewIntent:", "NfcAdapter.EXTRA_TAG");

                Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (messages != null) {
                    Log.d(TAG, "Found " + messages.length + " NDEF messages");
                }
            } else {
                Log.d(TAG, "Write to an unformatted tag not implemented");
            }


            //mTextView.setText( "NFC Tag\n" + ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_TAG)));
        }
    }

    private String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        sb.append("ID (reversed): ").append(getReversed(id)).append("\n");
        Cardsn = getHex(id);


        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                String type = "Unknown";
                switch (mifareTag.getType()) {
                    case MifareClassic.TYPE_CLASSIC:
                        type = "Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        type = "Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        type = "Pro";
                        break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }
        Log.d("Datos: ", sb.toString());

        DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
        Date now = new Date();

        mTextView.setText(TIME_FORMAT.format(now) + '\n' + sb.toString());
        return sb.toString();
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        // for (int i = bytes.length - 1; i >= 0; --i) {
        for (int i = 0; i <= bytes.length - 1; i++) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private String ByteArrayToHexString(byte[] inarray) {

        Log.d("ByteArrayToHexString", inarray.toString());

        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
//CE7AEED4
//EE7BEED4
        Log.d("ByteArrayToHexString", String.format("%0" + (inarray.length * 2) + "X", new BigInteger(1, inarray)));


        return out;
    }

    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }

    public class PostAttRecord extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");

        }

        @Override
        protected String doInBackground(String... aurl) {
            Log.i(TAG, "doInBackground");
            try {
                attend = db.rawQuery("select * from " + Database.EM_CHECKIN_TABLE_NAME + " WHERE "
                        + Database.Date + " ='" + DateCheckin + "'and " + Database.Rtype + " ='" + Rtype + "' and "
                        + Database.Employee_No + " ='" + Employee_No + "' and " + Database.CloudID + " ='" + cloudid + "'", null);
                count = count + attend.getCount();
                //csvWrite.writeNext(attend.getColumnNames());
                while (attend.moveToNext()) {


                    String TransDateTime = attend.getString(attend.getColumnIndex(Database.DateTime));
                    String TransDate = attend.getString(attend.getColumnIndex(Database.Date)) + " 00:00:00";

                    Employee_No = attend.getString(attend.getColumnIndex(Database.Employee_No));
                    CardNo = attend.getString(attend.getColumnIndex(Database.CardNo));
                    AuthMethod = attend.getString(attend.getColumnIndex(Database.AuthMethod));
                    VerMethod = attend.getString(attend.getColumnIndex(Database.Vtype));
                    SimpleDateFormat frmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = null;
                    try {
                        date = frmt.parse(TransDateTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    DateTimeIn = format2.format(date);

                    Date date1 = null;
                    try {
                        date1 = frmt.parse(TransDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd");
                    DateCheckin = format3.format(date1);


                    Estate = attend.getString(attend.getColumnIndex(Database.Estate));
                    Division = attend.getString(attend.getColumnIndex(Database.Division));
                    Rtype = attend.getString(attend.getColumnIndex(Database.Rtype));
                    TerminalID = attend.getString(attend.getColumnIndex(Database.TerminalID));
                    TimeIn = attend.getString(attend.getColumnIndex(Database.TimeIn));
                    TimeOut = attend.getString(attend.getColumnIndex(Database.TimeOut));
                    UserID = attend.getString(attend.getColumnIndex(Database.UserID));


                    Co_prefix = mSharedPrefs.getString("company_prefix", "");
                    Current_User = prefs.getString("user", "");
                    Project = "";


                    StringBuilder sb = new StringBuilder();
                    sb.append("5" + ",");
                    sb.append(Employee_No + ",");
                    sb.append(CardNo + ",");
                    sb.append(AuthMethod + ",");
                    sb.append(VerMethod + ",");
                    sb.append(DateTimeIn + ",");
                    sb.append(DateCheckin + ",");
                    sb.append(Estate + ",");
                    sb.append(Division + ",");
                    sb.append(Rtype + ",");
                    sb.append(TerminalID + ",");
                    sb.append(UserID + ",");
                    sb.append(Co_prefix);

                    Attendance = sb.toString();


                    try {
                        soapResponse = new SoapRequest(CheckInActivity.this).PostClockingRecord(Attendance);
                        error = soapResponse;
                        if (Integer.valueOf(CheckInActivity.this.soapResponse).intValue() < 0) {
                            ContentValues values = new ContentValues();
                            values.put(Database.CloudID, 0);
                            long rows = db.update(Database.EM_CHECKIN_TABLE_NAME, values,
                                    Database.Employee_No + " = ? AND " + Database.Date + " = ? AND " + Database.Estate + " = ?",
                                    new String[]{Employee_No, DateCheckin, Estate});

                            if (rows > 0) {
                                Log.i("error:", soapResponse);

                            }
                            return null;
                        }
                        if (Integer.valueOf(soapResponse).intValue() > 0) {
                            returnValue = soapResponse;
                            ContentValues values = new ContentValues();
                            values.put(Database.CloudID, soapResponse);
                            long rows = db.update(Database.EM_CHECKIN_TABLE_NAME, values,
                                    Database.Employee_No + " = ? AND " + Database.Date + " = ? AND " + Database.Estate + " = ?",
                                    new String[]{Employee_No, DateCheckin, Estate});

                            if (rows > 0) {
                                Log.i("error:", soapResponse);

                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        returnValue = e.toString();
                    }
                    progressStatus++;
                    publishProgress("" + progressStatus);
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
        }

        @Override
        protected void onPostExecute(String unused) {
            db = dbhelper.getReadableDatabase();
            if (error.equals("-8080")) {
                errorNo = prefs.getString("errorNo", "");

                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
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

                if (Integer.valueOf(soapResponse).intValue() > 0) {
                    returnValue = soapResponse;

                    Toast.makeText(CheckInActivity.this, "Data Saved Successfully !!!", Toast.LENGTH_LONG).show();


                    return;
                }
            } catch (NumberFormatException e) {
                errorNo = prefs.getString("errorNo", "");

                if (errorNo.equals("-8080")) {
                    Toast.makeText(CheckInActivity.this, "Server Not Available !!", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                } else {

                    Toast.makeText(CheckInActivity.this, error, Toast.LENGTH_LONG).show();
                    // finish();
                }
            }


        }
    }

}
