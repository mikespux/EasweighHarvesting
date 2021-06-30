package com.plantation.activities;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

//public class CheckInActivity extends AppCompatActivity {

public class CardWeighActivity extends AppCompatActivity {
    static DBHelper dbhelper;
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;
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
    public Toolbar toolbar;
    public SimpleCursorAdapter ca;
    String CardNo, Cardsn;
    Intent mIntent;
    SQLiteDatabase db;
    ListView listEmployees;
    String FpEmployeeNo;
    String s_mccmanagedfarm, shedid;

    SearchView searchView;
    private TextView mTextView, txtScaleConn, txtPrinterConn, tv_number, textAccountId; //ECP 2017-01-16

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_readerweigh);

        mTextView = findViewById(R.id.txtdesc);
        mTextView.setText("");

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(CardWeighActivity.this);

        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();

        txtScaleConn = findViewById(R.id.txtScaleConn);
        txtScaleConn.setText(prefs.getString("scalec", "Scale Not Connected"));

        txtPrinterConn = findViewById(R.id.txtPrinterConn);
        txtPrinterConn.setVisibility(View.VISIBLE);
        txtPrinterConn.setText(prefs.getString("printerc", "Printer Not Connected"));

        /*searchView= findViewById(R.id.searchView);
        searchView.setQueryHint("Search Employee ...");
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String farmerNo = constraint.toString();
                        s_mccmanagedfarm= prefs.getString("s_mccmanagedfarm", "");
                        shedid= prefs.getString("shedCode", "");
                        return dbhelper.FilterSpecific(farmerNo,s_mccmanagedfarm,shedid);

                    }
                });
                // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String farmerNo = constraint.toString();
                        s_mccmanagedfarm= prefs.getString("s_mccmanagedfarm", "");
                        shedid= prefs.getString("shedCode", "");
                        return dbhelper.FilterFarmer(farmerNo,s_mccmanagedfarm,shedid);

                    }
                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });*/
        listEmployees = this.findViewById(R.id.lvMachines);
        listEmployees.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                tv_number = selectedView.findViewById(R.id.tv_number);

                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                //first get scale version
                CardNo = "";
                FpEmployeeNo = tv_number.getText().toString();

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("CardNo", CardNo);
                edit.commit();
                edit.putString("FpEmployeeNo", FpEmployeeNo);
                edit.commit();
                finish();
                mIntent = new Intent(getApplicationContext(), ScaleEasyWeighActivity.class);
                startActivity(mIntent);


                edit.remove("Gross");
                edit.remove("Net");
                edit.commit();

            }
        });
        setupToolbar();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(prefs.getString("scalec", "Scale Not Connected"));
        getSupportActionBar().setSubtitle(prefs.getString("printerc", "Printer Not Connected"));


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "1");

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

    // CARD READER
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
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
                //finish();
                Log.d(TAG, "Parcelable OK");
                NdefMessage[] msgs;
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                byte[] payload = dumpTagData(tagN).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};

                //Log.d(TAG, msgs[0].toString());
                dbhelper = new DBHelper(getApplicationContext());
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor accounts = db.rawQuery("select * from " + Database.EM_TABLE_NAME + " where " + Database.EM_CARDID + " COLLATE NOCASE ='" + Cardsn + "'", null);
                if (accounts.getCount() == 0) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Employee does not exist!!");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    FpEmployeeNo = "";
                    return;
                }
                if (accounts.getCount() > 0) {

                    while (accounts.moveToNext()) {
                        CardNo = accounts.getString(accounts.getColumnIndex(Database.EM_CARDID));

                        FpEmployeeNo = accounts.getString(accounts.getColumnIndex(Database.EM_ID));

                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("CardNo", CardNo);
                        edit.commit();
                        edit.putString("FpEmployeeNo", FpEmployeeNo);
                        edit.commit();
                        finish();
                        mIntent = new Intent(getApplicationContext(), ScaleEasyWeighActivity.class);
                        startActivity(mIntent);


                        Toast.makeText(CardWeighActivity.this, "EmployeeNO:" + FpEmployeeNo, Toast.LENGTH_LONG).show();

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


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        getdata();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {
            int ROWID = 0;
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);
            if (accounts.getCount() == 0) {
                Toast.makeText(this, "no records", Toast.LENGTH_LONG).show();
            }
            String[] from = {Database.ROW_ID, Database.EM_ID, Database.EM_NAME, Database.EM_PICKERNO};
            int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_name, R.id.tv_pickerno};


            ca = new SimpleCursorAdapter(this, R.layout.employee_list, accounts, from, to);

            listEmployees = this.findViewById(R.id.listEmployees);
            listEmployees.setAdapter(ca);
            listEmployees.setTextFilterEnabled(true);
            //dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


}
