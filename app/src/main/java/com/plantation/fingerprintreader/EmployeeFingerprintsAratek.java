package com.plantation.fingerprintreader;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.activities.ImportFingerPrintsActivity;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;

import java.util.Timer;

import cn.com.aratek.dev.Terminal;
import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintImage;
import cn.com.aratek.fp.FingerprintScanner;
import cn.com.aratek.util.Result;

@SuppressLint({"SdCardPath", "HandlerLeak"})
public class EmployeeFingerprintsAratek extends AppCompatActivity implements View.OnClickListener {

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
    static TextView txtRightThumb, txtLeftThumb, txtFingerPrint, txtRightIndex, txtLeftIndex;
    static int CaptureNo = 0;
    static int FingerNo = 0;
    static SharedPreferences prefs;
    static SharedPreferences mSharedPrefs;
    public Toolbar toolbar;
    public SimpleCursorAdapter ca;
    String Enrolled, Captured, CardNo, Cardsn;
    DBHelper dbhelper;
    Button btAddAgt, btn_svAgt;
    Button btnRightThumb, btnLeftThumb, btnRightIndex, btnLeftIndex, btnVerify, btn_enroll, btn_finenroll;
    ListView listEmployees;
    EditText emID, emName, emIDNo, emPickerNo, emCardID;
    String s_emID, s_emName, s_emPickerNo, s_emIDNo, s_emCardID;
    String s_femID, s_femPickerNo, s_emFingerNo, s_emFingerprintb64, s_emFingerprinthex;
    String accountId;
    TextView textAccountId, textEmployee, textEmployeeNo, txtUsername, txtdesc;
    Boolean success = true;
    String EmployeeNo;
    Boolean Match = false;
    SearchView searchView;
    TextView txtEmployeeName, txtEmployeeNo;
    LinearLayout LtEmpDetails, LtEnrollFp;
    LinearLayout finger_collect, employee_list;
    AlertDialog employee_diag;
    TextView messagebox;
    TextView handbox;
    LinearLayout LeftFingers, RightFingers;
    Button exit;
    Button leftHand, rightHand;
    FingerView currentFingerView;
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
    private ProgressDialog mProgressDialog;
    private FingerprintScanner mScanner;
    private FingerprintTask mTask;
    private int mId;
    private ImageView fingerpic;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_ERROR: {
                    mInformation.setTextColor(getResources().getColor(R.color.error_text_color));
                    mDetails.setTextColor(getResources().getColor(R.color.error_details_text_color));
                    mInformation.setText(((Bundle) msg.obj).getString("information"));
                    mDetails.setText(((Bundle) msg.obj).getString("details"));
                    messagebox.setTextColor(getResources().getColor(R.color.error_text_color));
                    messagebox.setText(((Bundle) msg.obj).getString("information", "") + "\n"
                            + ((Bundle) msg.obj).getString("details", ""));
                    break;
                }
                case MSG_SHOW_INFO: {
                    mInformation.setTextColor(getResources().getColor(R.color.information_text_color));
                    mDetails.setTextColor(getResources().getColor(R.color.information_details_text_color));
                    mInformation.setText(((Bundle) msg.obj).getString("information", ""));
                    mDetails.setText(((Bundle) msg.obj).getString("details", ""));
                    messagebox.setTextColor(getResources().getColor(R.color.information_text_color));
                    messagebox.setText(((Bundle) msg.obj).getString("information", "") + "\n"
                            + ((Bundle) msg.obj).getString("details", ""));
                    break;
                }
                case MSG_UPDATE_IMAGE: {
                    fingerpic.setImageBitmap((Bitmap) msg.obj);
                    break;
                }
                case MSG_UPDATE_TEXT: {
                    String[] texts = (String[]) msg.obj;
                    mCaptureTime.setText(texts[0]);
                    mExtractTime.setText(texts[1]);
                    mGeneralizeTime.setText(texts[2]);
                    mVerifyTime.setText(texts[3]);
                    break;
                }
                case MSG_UPDATE_BUTTON: {
                    Boolean enable = (Boolean) msg.obj;
                    mBtnEnroll.setEnabled(enable);
                    mBtnVerify.setEnabled(enable);
                    mBtnIdentify.setEnabled(enable);
                    mBtnClear.setEnabled(enable);
                    mBtnShow.setEnabled(enable);
                    break;
                }
                case MSG_UPDATE_SN: {
                    mSN.setText((String) msg.obj);
                    break;
                }
                case MSG_UPDATE_FW_VERSION: {
                    mFwVersion.setText((String) msg.obj);
                    break;
                }
                case MSG_SHOW_PROGRESS_DIALOG: {
                    String[] info = (String[]) msg.obj;
                    mProgressDialog.setTitle(info[0]);
                    mProgressDialog.setMessage(info[1]);
                    mProgressDialog.show();
                    break;
                }
                case MSG_DISMISS_PROGRESS_DIALOG: {
                    mProgressDialog.dismiss();
                    break;
                }
            }
        }
    };
    private FingerView rfinger1;
    private FingerView rfinger2;
    private FingerView rfinger3;
    private FingerView rfinger4;
    private FingerView rfinger5;
    private FingerView lfinger1;
    private FingerView lfinger2;
    private FingerView lfinger3;
    private FingerView lfinger4;
    private FingerView lfinger5;
    private Timer timer;

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
        setContentView(R.layout.activity_fingerprint_employee);
        setupToolbar();
        dbhelper = new DBHelper(getApplicationContext());
        mScanner = FingerprintScanner.getInstance(this);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        messagebox = findViewById(R.id.messagebox);
        handbox = findViewById(R.id.handbox);
        mInformation = findViewById(R.id.tv_info);
        mDetails = findViewById(R.id.tv_details);
        mSN = findViewById(R.id.tv_fps_sn);
        mFwVersion = findViewById(R.id.tv_fps_fw);
        mCaptureTime = findViewById(R.id.captureTime);
        mExtractTime = findViewById(R.id.extractTime);
        mGeneralizeTime = findViewById(R.id.generalizeTime);
        mVerifyTime = findViewById(R.id.verifyTime);
        mFingerprintImage = findViewById(R.id.fingerimage);

        mBtnEnroll = findViewById(R.id.bt_enroll);
        mBtnVerify = findViewById(R.id.bt_verify);
        mBtnIdentify = findViewById(R.id.bt_identify);
        mBtnClear = findViewById(R.id.bt_clear);
        mBtnShow = findViewById(R.id.bt_show);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);

        enableControl(false);

        updateSingerTestText(-1, -1, -1, -1);


        txtEmployeeNo = findViewById(R.id.txtEmployeeNo);

        txtEmployeeName = findViewById(R.id.txtEmployeeName);


        LeftFingers = findViewById(R.id.LeftFingers);
        RightFingers = findViewById(R.id.RightFingers);

        fingerpic = findViewById(R.id.fingerpic);
        finger_collect = findViewById(R.id.finger_collect);

        rfinger1 = findViewById(R.id.rfinger1);
        rfinger1.setImageResources(R.drawable.f1_normal,
                R.drawable.f1_selected, R.drawable.f1_reading,
                R.drawable.f1_registed, R.drawable.f1_selected_registed);


        rfinger2 = findViewById(R.id.rfinger2);
        rfinger2.setImageResources(R.drawable.f2_normal,
                R.drawable.f2_selected, R.drawable.f2_reading,
                R.drawable.f2_registed, R.drawable.f2_selected_registed);

        rfinger3 = findViewById(R.id.rfinger3);
        rfinger3.setImageResources(R.drawable.f3_normal,
                R.drawable.f3_selected, R.drawable.f3_reading,
                R.drawable.f3_registed, R.drawable.f3_selected_registed);
        rfinger4 = findViewById(R.id.rfinger4);
        rfinger4.setImageResources(R.drawable.f4_normal,
                R.drawable.f4_selected, R.drawable.f4_reading,
                R.drawable.f4_registed, R.drawable.f4_selected_registed);
        rfinger5 = findViewById(R.id.rfinger5);
        rfinger5.setImageResources(R.drawable.f5_normal,
                R.drawable.f5_selected, R.drawable.f5_reading,
                R.drawable.f5_registed, R.drawable.f5_selected_registed);


        lfinger1 = findViewById(R.id.lfinger1);
        lfinger1.setImageResources(R.drawable.f1_normal,
                R.drawable.f1_selected, R.drawable.f1_reading,
                R.drawable.f1_registed, R.drawable.f1_selected_registed);
        lfinger2 = findViewById(R.id.lfinger2);
        lfinger2.setImageResources(R.drawable.f2_normal,
                R.drawable.f2_selected, R.drawable.f2_reading,
                R.drawable.f2_registed, R.drawable.f2_selected_registed);
        lfinger3 = findViewById(R.id.lfinger3);
        lfinger3.setImageResources(R.drawable.f3_normal,
                R.drawable.f3_selected, R.drawable.f3_reading,
                R.drawable.f3_registed, R.drawable.f3_selected_registed);
        lfinger4 = findViewById(R.id.lfinger4);
        lfinger4.setImageResources(R.drawable.f4_normal,
                R.drawable.f4_selected, R.drawable.f4_reading,
                R.drawable.f4_registed, R.drawable.f4_selected_registed);
        lfinger5 = findViewById(R.id.lfinger5);
        lfinger5.setImageResources(R.drawable.f5_normal,
                R.drawable.f5_selected, R.drawable.f5_reading,
                R.drawable.f5_registed, R.drawable.f5_selected_registed);

        leftHand = findViewById(R.id.leftHand);
        leftHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftHand.setVisibility(View.GONE);
                rightHand.setVisibility(View.VISIBLE);
                LeftFingers.setVisibility(View.VISIBLE);
                RightFingers.setVisibility(View.GONE);
                handbox.setText("Place Left Hand Fingers");
            }
        });

        rightHand = findViewById(R.id.rightHand);
        rightHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftHand.setVisibility(View.VISIBLE);
                rightHand.setVisibility(View.GONE);
                LeftFingers.setVisibility(View.GONE);
                RightFingers.setVisibility(View.VISIBLE);
                handbox.setText("Place Right Hand Fingers");
            }
        });

        exit = findViewById(R.id.exitfragment);
        exit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                employee_list.setVisibility(View.VISIBLE);
                finger_collect.setVisibility(View.GONE);


            }
        });


        employee_list = findViewById(R.id.employee_list);


        listEmployees = this.findViewById(R.id.lvEmployee);
        listEmployees.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                CaptureNo = 0;
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                textEmployee = selectedView.findViewById(R.id.tv_name);
                textEmployeeNo = selectedView.findViewById(R.id.tv_number);
                // txtUsername = selectedView.findViewById(R.id.txtUserName);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                // Intent intent = new Intent(Activity_ListStock.this, UpdateStock.class);
                // intent.putExtra("accountid", textAccountId.getText().toString());
                // startActivity(intent);
                showUpdateUserDialog();
            }
        });
        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search Employee No ...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String EmployeeCode = constraint.toString();
                        return dbhelper.SearchSpecificEmployee(EmployeeCode);

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
                        String EmployeeCode = constraint.toString();
                        return dbhelper.SearchEmployee(EmployeeCode);

                    }
                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        //searchView.requestFocus();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Employees Bio-Data");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void showUpdateUserDialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_employee, null);
        dialogBuilder.setView(dialogView);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if (CaptureNo == 0) {
            accountId = textAccountId.getText().toString();
            dialogBuilder.setTitle("Employee");
        } else if (CaptureNo == 1) {
            accountId = EmployeeNo;
            dialogBuilder.setTitle("Employee");
        } else {
        }

        emID = dialogView.findViewById(R.id.emID);
        emID.setEnabled(false);
        emID.setFocusable(false);
        emName = dialogView.findViewById(R.id.emName);
        emName.setEnabled(false);
        emName.setFocusable(false);
        emIDNo = dialogView.findViewById(R.id.emIDNo);
        emIDNo.setEnabled(false);
        emIDNo.setFocusable(false);
        emPickerNo = dialogView.findViewById(R.id.emPickerNo);
        emPickerNo.setEnabled(false);
        emPickerNo.setFocusable(false);
        emCardID = dialogView.findViewById(R.id.emCardID);
        emCardID.setEnabled(false);
        emCardID.setFocusable(false);

        LtEmpDetails = dialogView.findViewById(R.id.LtEmpDetails);
        LtEnrollFp = dialogView.findViewById(R.id.LtEnrollFp);

        btn_enroll = dialogView.findViewById(R.id.btn_enroll);
        btn_enroll.setText("Enroll Fingerprints");
        btn_enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                employee_list.setVisibility(View.GONE);
                finger_collect.setVisibility(View.VISIBLE);
                employee_diag.dismiss();
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("EmployeeNo", emID.getText().toString());
                edit.commit();
                edit.putString("EmployeeName", emName.getText().toString());
                edit.commit();

                txtEmployeeNo.setText(emID.getText().toString());

                txtEmployeeName.setText(emName.getText().toString());


                leftHand.setVisibility(View.VISIBLE);
                rightHand.setVisibility(View.GONE);
                LeftFingers.setVisibility(View.GONE);
                RightFingers.setVisibility(View.VISIBLE);

                rfinger1.setSelected(false);
                rfinger1.setRegisted(false);
                rfinger1.setReading(false);
                rfinger1.setEnabled(true);

                rfinger2.setSelected(false);
                rfinger2.setRegisted(false);
                rfinger2.setReading(false);
                rfinger2.setEnabled(true);

                rfinger3.setSelected(false);
                rfinger3.setRegisted(false);
                rfinger3.setReading(false);
                rfinger3.setEnabled(true);

                rfinger4.setSelected(false);
                rfinger4.setRegisted(false);
                rfinger4.setReading(false);
                rfinger4.setEnabled(true);

                rfinger5.setSelected(false);
                rfinger5.setRegisted(false);
                rfinger5.setReading(false);
                rfinger5.setEnabled(true);

                lfinger1.setSelected(false);
                lfinger1.setRegisted(false);
                lfinger1.setReading(false);
                lfinger1.setEnabled(true);

                lfinger2.setSelected(false);
                lfinger2.setRegisted(false);
                lfinger2.setReading(false);
                lfinger2.setEnabled(true);

                lfinger3.setSelected(false);
                lfinger3.setRegisted(false);
                lfinger3.setReading(false);
                lfinger3.setEnabled(true);

                lfinger4.setSelected(false);
                lfinger4.setRegisted(false);
                lfinger4.setReading(false);
                lfinger4.setEnabled(true);

                lfinger5.setSelected(false);
                lfinger5.setRegisted(false);
                lfinger5.setReading(false);
                lfinger5.setEnabled(true);


                dbhelper = new DBHelper(getApplicationContext());
                SQLiteDatabase db = dbhelper.getReadableDatabase();

                Cursor employee = null;


                employee = db.query(Database.FINGERPRINT_TABLE_NAME, null,
                        " femID = ?", new String[]{emID.getText().toString()}, null, null, null);

                //startManagingCursor(accounts);
                if (employee.getCount() > 0) {
                    while (employee.moveToNext()) {
                        // update view
                        FingerNo = Integer.parseInt(employee.getString(employee
                                .getColumnIndex(Database.FEM_FINGERNO)));
                        switch (FingerNo) {
                            case 1:
                                rfinger1.setRegisted(true);
                                rfinger1.setSelected(true);
                                rfinger1.setEnabled(false);

                                break;
                            case 2:

                                rfinger2.setRegisted(true);
                                rfinger2.setSelected(true);
                                rfinger2.setEnabled(false);
                                break;
                            case 3:

                                rfinger3.setRegisted(true);
                                rfinger3.setSelected(true);
                                rfinger3.setEnabled(false);

                                break;
                            case 4:

                                rfinger4.setRegisted(true);
                                rfinger4.setSelected(true);
                                rfinger4.setEnabled(false);
                                break;
                            case 5:

                                rfinger5.setRegisted(true);
                                rfinger5.setSelected(true);
                                rfinger5.setEnabled(false);
                                break;
                            case 6:
                                lfinger1.setRegisted(true);
                                lfinger1.setSelected(true);
                                lfinger1.setEnabled(false);
                                break;
                            case 7:

                                lfinger2.setRegisted(true);
                                lfinger2.setSelected(true);
                                lfinger2.setEnabled(false);
                                break;
                            case 8:
                                lfinger3.setRegisted(true);
                                lfinger3.setSelected(true);
                                lfinger3.setEnabled(false);

                                break;
                            case 9:

                                lfinger4.setRegisted(true);
                                lfinger4.setSelected(true);
                                lfinger4.setEnabled(false);
                                break;
                            case 10:

                                lfinger5.setRegisted(true);
                                lfinger5.setSelected(true);
                                lfinger5.setEnabled(false);

                                break;

                            default:

                                break;

                        }
                        //  txtdesc.setVisibility(View.VISIBLE);
                    }
                }


            }
        });

        String EmNo = null;
        if (CaptureNo == 0) {
            EmNo = textEmployeeNo.getText().toString();
        } else if (CaptureNo == 1) {
            EmNo = EmployeeNo;


        } else {
        }


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();

        Cursor employee = null;


        employee = db.query(Database.FINGERPRINT_TABLE_NAME, null,
                " femID = ?", new String[]{EmNo}, null, null, null);

        //startManagingCursor(accounts);
        if (employee.getCount() > 0) {
            if (employee.moveToFirst()) {
                // update view
                btn_enroll.setText("Fingerprints");

                //  txtdesc.setVisibility(View.VISIBLE);
            }
        }


        Cursor account = null;
        if (CaptureNo == 0) {
            account = db.query(Database.EM_TABLE_NAME, null,
                    " _id = ?", new String[]{accountId}, null, null, null);
        } else if (CaptureNo == 1) {
            account = db.query(Database.EM_TABLE_NAME, null,
                    " emID = ?", new String[]{accountId}, null, null, null);
        } else {
        }
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            emID.setText(account.getString(account
                    .getColumnIndex(Database.EM_ID)));
            emName.setText(account.getString(account
                    .getColumnIndex(Database.EM_NAME)));
            emIDNo.setText(account.getString(account
                    .getColumnIndex(Database.EM_IDNO)));

            emPickerNo.setText(account.getString(account
                    .getColumnIndex(Database.EM_PICKERNO)));

            emCardID.setText(account.getString(account
                    .getColumnIndex(Database.EM_CARDID)));


        }
        account.close();
        db.close();
        dbhelper.close();


        btn_svAgt = dialogView.findViewById(R.id.btn_svAgt);
        btn_svAgt.setVisibility(View.GONE);

        if (CaptureNo == 0) {
            dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do something with edt.getText().toString();
                    // deleteEmployee();
                    dialog.dismiss();

                }
            });
         /*  dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                    updateEmployee();
                    getdata();



                }
            });*/

        } else {
            dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do something with edt.getText().toString();


                }
            });

        }
        employee_diag = dialogBuilder.create();
        employee_diag.show();
    }

    public void updateEmployee() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.EM_ID, emID.getText().toString());
            values.put(Database.EM_NAME, emName.getText().toString());
            values.put(Database.EM_IDNO, emIDNo.getText().toString());
            values.put(Database.EM_PICKERNO, emPickerNo.getText().toString());
            values.put(Database.EM_CARDID, emCardID.getText().toString());


            long rows = db.update(Database.EM_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            db.close();
            if (rows > 0) {
                Toast.makeText(this, "Updated Employee Successfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Employee!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteEmployee() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this employee?")
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
            int rows = db.delete(Database.EM_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if (rows == 1) {
                Toast.makeText(this, "Employee Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            } else {
                Toast.makeText(this, "Could not delete employee!", Toast.LENGTH_LONG).show();
            }
            //}

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
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
                Toast.makeText(this, "no task records", Toast.LENGTH_LONG).show();
            }
            String[] from = {Database.ROW_ID, Database.EM_ID, Database.EM_NAME, Database.EM_TEAM};
            int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_name, R.id.tv_pickerno};


            ca = new SimpleCursorAdapter(this, R.layout.employee_list, accounts, from, to);

            listEmployees = this.findViewById(R.id.lvEmployee);
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

        openDevice();

    }

    @Override
    protected void onPause() {
        closeDevice();

        super.onPause();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.rfinger1:
                FingerNo = 1;
                enroll();

                break;
            case R.id.rfinger2:
                FingerNo = 2;
                enroll();
                break;
            case R.id.rfinger3:
                FingerNo = 3;
                enroll();
                break;
            case R.id.rfinger4:
                FingerNo = 4;
                enroll();
                break;
            case R.id.rfinger5:
                FingerNo = 5;
                enroll();
                break;
            case R.id.lfinger1:
                FingerNo = 6;
                enroll();
                break;
            case R.id.lfinger2:
                FingerNo = 7;
                enroll();
                break;
            case R.id.lfinger3:
                FingerNo = 8;
                enroll();
                break;
            case R.id.lfinger4:
                FingerNo = 9;
                enroll();
                break;
            case R.id.lfinger5:
                FingerNo = 10;
                enroll();
                break;
            case R.id.bt_enroll:
                enroll();
                break;
            case R.id.bt_verify:
                verify();
                break;
            case R.id.bt_identify:
                identify();
                break;
            case R.id.bt_clear:
                ClearFingerprintDB();
                break;
            case R.id.bt_show:
                showFingerprintImage();
                break;
        }
    }

    public void ClearFingerprintDB() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                EmployeeFingerprintsAratek.this);
        // Setting Dialog Title
        alertDialog.setTitle("Clear Database?");
        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to clear the Fingerprint Database?");

        // Setting Positive "Yes" Button
        alertDialog.setNegativeButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = dbhelper.getWritableDatabase();

                        db.delete(Database.FINGERPRINT_TABLE_NAME, null, null);
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FINGERPRINT_TABLE_NAME + "'");
                        clearFingerprintDatabase();


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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            // startActivity(intent);
            closeDevice();
            finish();
            Intent mIntent = new Intent(EmployeeFingerprintsAratek.this, ImportFingerPrintsActivity.class);
            startActivity(mIntent);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void updateFingerprintImage(FingerprintImage fi) {
        byte[] fpBmp = null;
        Bitmap bitmap;
//        fi.convert2Wsq();
        if (fi == null || (fpBmp = fi.convert2Bmp()) == null || (bitmap = BitmapFactory.decodeByteArray(fpBmp, 0, fpBmp.length)) == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nofinger);
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_IMAGE, bitmap));
    }

    private void updateSingerTestText(long captureTime, long extractTime, long generalizeTime, long verifyTime) {
        String[] texts = new String[4];
        if (captureTime < 0) {
            texts[0] = getString(R.string.not_done);
        } else if (captureTime < 1) {
            texts[0] = "< 1ms";
        } else {
            texts[0] = captureTime + "ms";
        }

        if (extractTime < 0) {
            texts[1] = getString(R.string.not_done);
        } else if (extractTime < 1) {
            texts[1] = "< 1ms";
        } else {
            texts[1] = extractTime + "ms";
        }

        if (generalizeTime < 0) {
            texts[2] = getString(R.string.not_done);
        } else if (generalizeTime < 1) {
            texts[2] = "< 1ms";
        } else {
            texts[2] = generalizeTime + "ms";
        }

        if (verifyTime < 0) {
            texts[3] = getString(R.string.not_done);
        } else if (verifyTime < 1) {
            texts[3] = "< 1ms";
        } else {
            texts[3] = verifyTime + "ms";
        }

        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_TEXT, texts));
    }

    private void enableControl(boolean enable) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_BUTTON, enable));
    }

    private void openDevice() {
        new Thread() {
            @Override
            public void run() {
                synchronized (EmployeeFingerprintsAratek.this) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.preparing_device));
                    int error;
                    if ((error = mScanner.powerOn()) != FingerprintScanner.RESULT_OK) {
                        showError(getString(R.string.fingerprint_device_power_on_failed), getFingerprintErrorString(error));
                    }
                    if ((error = mScanner.open()) != FingerprintScanner.RESULT_OK) {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SN, getString(R.string.fps_sn, "null")));
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FW_VERSION, getString(R.string.fps_fw, "null")));
                        showError(getString(R.string.fingerprint_device_open_failed), getFingerprintErrorString(error));
                    } else {
                        Result res = mScanner.getSN();
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SN, Terminal.getSdkVersion()));
                        res = mScanner.getFirmwareVersion();
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FW_VERSION, getString(R.string.fps_fw, res.data)));
                        showInformation(getString(R.string.fingerprint_device_open_success), null);
                        enableControl(true);
                       /* Result has=mScanner.hasFinger();
                        showInformation(getString(R.string.fingerprint_device_open_success), String.valueOf(has.arg1)+has.data);
                       timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Result has=mScanner.hasFinger();
                                        if(has.data=="true"){
                                            showInformation(getString(R.string.fingerprint_device_open_success), String.valueOf(has.arg1)+has.data);

                                        }else{

                                            showInformation(getString(R.string.fingerprint_device_open_success), String.valueOf(has.arg1)+has.data);
                                            dismissProgressDialog();
                                        }

                                    }
                                });
                            }
                        }, 6000, 1000);*/

                        //identify();
                    }
                    if ((error = Bione.initialize(EmployeeFingerprintsAratek.this, FP_DB_PATH)) != Bione.RESULT_OK) {
                        showError(getString(R.string.algorithm_initialization_failed), getFingerprintErrorString(error));
                    }
                    Log.i(TAG, "Fingerprint algorithm version: " + Bione.getVersion());
                    dismissProgressDialog();
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeDevice();
        finish();
        Intent mIntent = new Intent(EmployeeFingerprintsAratek.this, ImportFingerPrintsActivity.class);
        startActivity(mIntent);
    }

    private void closeDevice() {
        new Thread() {
            @Override
            public void run() {
                synchronized (EmployeeFingerprintsAratek.this) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.closing_device));
                    enableControl(false);
                    int error;
                    if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
                        mTask.cancel(false);
                        mTask.waitForDone();
                    }
                    if ((error = mScanner.close()) != FingerprintScanner.RESULT_OK) {
                        showError(getString(R.string.fingerprint_device_close_failed), getFingerprintErrorString(error));
                    } else {
                        showInformation(getString(R.string.fingerprint_device_close_success), null);
                    }
                    if ((error = mScanner.powerOff()) != FingerprintScanner.RESULT_OK) {
                        showError(getString(R.string.fingerprint_device_power_off_failed), getFingerprintErrorString(error));
                    }
                    if ((error = Bione.exit()) != Bione.RESULT_OK) {
                        showError(getString(R.string.algorithm_cleanup_failed), getFingerprintErrorString(error));
                    }
                    dismissProgressDialog();
                }
            }
        }.start();
    }

    private void enroll() {
        mTask = new FingerprintTask();
        mTask.execute("enroll");
    }

    private void verify() {

        mTask = new FingerprintTask();
        mTask.execute("verify");

    }

    private void identify() {
        mTask = new FingerprintTask();
        mTask.execute("identify");
        CaptureNo = 1;
    }

    private void clearFingerprintDatabase() {
        int error = Bione.clear();
        if (error == Bione.RESULT_OK) {
            showInformation(getString(R.string.clear_fingerprint_database_success), null);
        } else {
            showError(getString(R.string.clear_fingerprint_database_failed), getFingerprintErrorString(error));
        }
    }

    private void showFingerprintImage() {
        mTask = new FingerprintTask();
        mTask.execute("show");
    }

    private void showProgressDialog(String title, String message) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_PROGRESS_DIALOG, new String[]{title, message}));
    }

    private void dismissProgressDialog() {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_DISMISS_PROGRESS_DIALOG));
    }

    private void showError(String info, String details) {
        Bundle bundle = new Bundle();
        bundle.putString("information", info);
        bundle.putString("details", details);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_ERROR, bundle));
    }

    private void showInformation(String info, String details) {
        Bundle bundle = new Bundle();
        bundle.putString("information", info);
        bundle.putString("details", details);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_INFO, bundle));
    }

    private String getFingerprintErrorString(int error) {
        int strid;
        switch (error) {
            case FingerprintScanner.RESULT_OK:
                strid = R.string.operation_successful;
                break;
            case FingerprintScanner.RESULT_FAIL:
                strid = R.string.error_operation_failed;
                break;
            case FingerprintScanner.WRONG_CONNECTION:
                strid = R.string.error_wrong_connection;
                break;
            case FingerprintScanner.DEVICE_BUSY:
                strid = R.string.error_device_busy;
                break;
            case FingerprintScanner.DEVICE_NOT_OPEN:
                strid = R.string.error_device_not_open;
                break;
            case FingerprintScanner.TIMEOUT:
                strid = R.string.error_timeout;
                break;
            case FingerprintScanner.NO_PERMISSION:
                strid = R.string.error_no_permission;
                break;
            case FingerprintScanner.WRONG_PARAMETER:
                strid = R.string.error_wrong_parameter;
                break;
            case FingerprintScanner.DECODE_ERROR:
                strid = R.string.error_decode;
                break;
            case FingerprintScanner.INIT_FAIL:
                strid = R.string.error_initialization_failed;
                break;
            case FingerprintScanner.UNKNOWN_ERROR:
                strid = R.string.error_unknown;
                break;
            case FingerprintScanner.NOT_SUPPORT:
                strid = R.string.error_not_support;
                break;
            case FingerprintScanner.NOT_ENOUGH_MEMORY:
                strid = R.string.error_not_enough_memory;
                break;
            case FingerprintScanner.DEVICE_NOT_FOUND:
                strid = R.string.error_device_not_found;
                break;
            case FingerprintScanner.DEVICE_REOPEN:
                strid = R.string.error_device_reopen;
                break;
            case FingerprintScanner.NO_FINGER:
                strid = R.string.error_no_finger;
                break;
            case Bione.INITIALIZE_ERROR:
                strid = R.string.error_algorithm_initialization_failed;
                break;
            case Bione.INVALID_FEATURE_DATA:
                strid = R.string.error_invalid_feature_data;
                break;
            case Bione.BAD_IMAGE:
                mId = -1;
                strid = R.string.error_bad_image;
                break;
            case Bione.NOT_MATCH:
                strid = R.string.error_not_match;
                break;
            case Bione.LOW_POINT:
                strid = R.string.error_low_point;
                break;
            case Bione.NO_RESULT:
                strid = R.string.error_no_result;
                break;
            case Bione.OUT_OF_BOUND:
                strid = R.string.error_out_of_bound;
                break;
            case Bione.DATABASE_FULL:
                strid = R.string.error_database_full;
                break;
            case Bione.LIBRARY_MISSING:
                strid = R.string.error_library_missing;
                break;
            case Bione.UNINITIALIZE:
                strid = R.string.error_algorithm_uninitialize;
                break;
            case Bione.REINITIALIZE:
                strid = R.string.error_algorithm_reinitialize;
                break;
            case Bione.REPEATED_ENROLL:
                strid = R.string.error_repeated_enroll;
                break;
            case Bione.NOT_ENROLLED:
                strid = R.string.error_not_enrolled;
                break;
            default:
                strid = R.string.error_other;
                break;
        }
        return getString(strid);
    }

    @SuppressLint("StringFormatMatches")
    public void showEmployee() {
        SQLiteDatabase db = dbhelper.getReadableDatabase();

        if (mId < 0) {
            mId = -1;
            return;
        }
        Cursor accounts = db.rawQuery("select * from " + Database.FINGERPRINT_TABLE_NAME + " where " + Database.FEM_FINGERPRINT + "=" + mId, null);
        if (accounts.getCount() > 0) {
            while (accounts.moveToNext()) {

                EmployeeNo = accounts.getString(accounts.getColumnIndex(Database.FEM_ID));
                showInformation(getString(R.string.fingerprint_match), getString(R.string.matched_id, mId) + " Employee No: " + EmployeeNo);

            }
        }
    }

    private class FingerprintTask extends AsyncTask<String, Integer, Void> {
        private boolean mIsDone = false;

        @Override
        protected void onPreExecute() {
            enableControl(false);
        }


        @Override
        protected Void doInBackground(String... params) {
            long startTime, captureTime = -1, extractTime = -1, generalizeTime = -1, verifyTime = -1;
            FingerprintImage fi = null;
            byte[] fpFeat = null, fpTemp = null;
            Result res;

            do {
                if (params[0].equals("show") || params[0].equals("enroll") || params[0].equals("verify") || params[0].equals("identify")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.press_finger));
                    int capRetry = 0;
                    mScanner.prepare();
                    do {
                        startTime = System.currentTimeMillis();
                        res = mScanner.capture();
                        captureTime = System.currentTimeMillis() - startTime;

                        fi = (FingerprintImage) res.data;
                        int quality;
                        if (fi != null) {
                            quality = Bione.getFingerprintQuality(fi);
                            Log.i(TAG, "Fingerprint image quality is " + quality);
                            if (quality < 50 && capRetry < 3 && !isCancelled()) {
                                capRetry++;
                                continue;
                            }
                        }

                        if (res.error != FingerprintScanner.NO_FINGER || isCancelled()) {
                            break;
                        }
                    } while (true);
                    mScanner.finish();
                    if (isCancelled()) {
                        break;
                    }
                    if (res.error != FingerprintScanner.RESULT_OK) {
                        mId = -1;
                        showError(getString(R.string.capture_image_failed), getFingerprintErrorString(res.error));
                        break;
                    }
                    updateFingerprintImage(fi);
                }

                if (params[0].equals("show")) {
                    showInformation(getString(R.string.capture_image_success), null);
                } else if (params[0].equals("enroll")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.enrolling));
                } else if (params[0].equals("verify")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.verifying));
                } else if (params[0].equals("identify")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.identifying));
                }

                if (params[0].equals("enroll") || params[0].equals("verify") || params[0].equals("identify")) {
                    startTime = System.currentTimeMillis();
                    res = Bione.extractFeature(fi);
                    extractTime = System.currentTimeMillis() - startTime;
                    if (res.error != Bione.RESULT_OK) {
                        showError(getString(R.string.enroll_failed_because_of_extract_feature), getFingerprintErrorString(res.error));
                        break;
                    }
                    fpFeat = (byte[]) res.data;
                }

                if (params[0].equals("enroll")) {
                    startTime = System.currentTimeMillis();
                    res = Bione.makeTemplate(fpFeat, fpFeat, fpFeat);
                    generalizeTime = System.currentTimeMillis() - startTime;
                    if (res.error != Bione.RESULT_OK) {
                        showError(getString(R.string.enroll_failed_because_of_make_template), getFingerprintErrorString(res.error));
                        break;
                    }
                    fpTemp = (byte[]) res.data;

                    int id = Bione.getFreeID();
                    if (id < 0) {
                        showError(getString(R.string.enroll_failed_because_of_get_id), getFingerprintErrorString(id));
                        break;
                    }
                    int ret = Bione.enroll(id, fpTemp);
                    if (ret != Bione.RESULT_OK) {
                        showError(getString(R.string.enroll_failed_because_of_error), getFingerprintErrorString(ret));
                        break;
                    }

                    switch (FingerNo) {
                        case 1:

                            s_femID = prefs.getString("EmployeeNo", "");
                            s_emName = prefs.getString("EmployeeName", "");
                            s_femPickerNo = "";
                            s_emFingerNo = "1";
                            mId = id;
                            s_emFingerprintb64 = Base64.encodeToString(fpTemp, 0, fpTemp.length, Base64.DEFAULT);
                            s_emFingerprinthex = ByteArrayToHexString(fpTemp, fpTemp.length);
                            dbhelper.AddFP(s_femID, s_femPickerNo, s_emFingerNo, String.valueOf(mId), s_emFingerprintb64, s_emFingerprinthex);


                            // dbhelper.AddFP(s_femID, s_femPickerNo, s_emFingerNo,Captured);

                          /*  if (success) {


                                //Toast.makeText(EmployeeFingerprintsAratek.this," Fingerprint Saved successfully!! " +String.valueOf(mId), Toast.LENGTH_LONG).show();
                                Log.i(" Fingerprint Saved: ", String.valueOf(mId));

                            }*/


                            break;
                        case 2:
                            s_femID = prefs.getString("EmployeeNo", "");
                            s_emName = prefs.getString("EmployeeName", "");
                            s_femPickerNo = "";
                            s_emFingerNo = "2";
                            mId = id;
                            s_emFingerprintb64 = Base64.encodeToString(fpTemp, 0, fpTemp.length, Base64.DEFAULT);
                            s_emFingerprinthex = ByteArrayToHexString(fpTemp, fpTemp.length);
                            dbhelper.AddFP(s_femID, s_femPickerNo, s_emFingerNo, String.valueOf(mId), s_emFingerprintb64, s_emFingerprinthex);

                            break;
                        case 3:
                            s_femID = prefs.getString("EmployeeNo", "");
                            s_emName = prefs.getString("EmployeeName", "");
                            s_femPickerNo = "";
                            s_emFingerNo = "3";
                            mId = id;
                            s_emFingerprintb64 = Base64.encodeToString(fpTemp, 0, fpTemp.length, Base64.DEFAULT);
                            s_emFingerprinthex = ByteArrayToHexString(fpTemp, fpTemp.length);
                            dbhelper.AddFP(s_femID, s_femPickerNo, s_emFingerNo, String.valueOf(mId), s_emFingerprintb64, s_emFingerprinthex);


                            break;
                        case 4:
                            s_femID = prefs.getString("EmployeeNo", "");
                            s_emName = prefs.getString("EmployeeName", "");
                            s_femPickerNo = "";
                            s_emFingerNo = "4";
                            mId = id;
                            s_emFingerprintb64 = Base64.encodeToString(fpTemp, 0, fpTemp.length, Base64.DEFAULT);
                            s_emFingerprinthex = ByteArrayToHexString(fpTemp, fpTemp.length);
                            dbhelper.AddFP(s_femID, s_femPickerNo, s_emFingerNo, String.valueOf(mId), s_emFingerprintb64, s_emFingerprinthex);


                            break;
                        case 5:
                            s_femID = prefs.getString("EmployeeNo", "");
                            s_emName = prefs.getString("EmployeeName", "");
                            s_femPickerNo = "";
                            s_emFingerNo = "5";
                            mId = id;
                            s_emFingerprintb64 = Base64.encodeToString(fpTemp, 0, fpTemp.length, Base64.DEFAULT);
                            s_emFingerprinthex = ByteArrayToHexString(fpTemp, fpTemp.length);
                            dbhelper.AddFP(s_femID, s_femPickerNo, s_emFingerNo, String.valueOf(mId), s_emFingerprintb64, s_emFingerprinthex);

                            break;
                        case 6:
                            s_femID = prefs.getString("EmployeeNo", "");
                            s_emName = prefs.getString("EmployeeName", "");
                            s_femPickerNo = "";
                            s_emFingerNo = "6";
                            mId = id;
                            s_emFingerprintb64 = Base64.encodeToString(fpTemp, 0, fpTemp.length, Base64.DEFAULT);
                            s_emFingerprinthex = ByteArrayToHexString(fpTemp, fpTemp.length);
                            dbhelper.AddFP(s_femID, s_femPickerNo, s_emFingerNo, String.valueOf(mId), s_emFingerprintb64, s_emFingerprinthex);


                            break;
                        case 7:
                            s_femID = prefs.getString("EmployeeNo", "");
                            s_emName = prefs.getString("EmployeeName", "");
                            s_femPickerNo = "";
                            s_emFingerNo = "7";
                            mId = id;
                            s_emFingerprintb64 = Base64.encodeToString(fpTemp, 0, fpTemp.length, Base64.DEFAULT);
                            s_emFingerprinthex = ByteArrayToHexString(fpTemp, fpTemp.length);
                            dbhelper.AddFP(s_femID, s_femPickerNo, s_emFingerNo, String.valueOf(mId), s_emFingerprintb64, s_emFingerprinthex);


                            break;
                        case 8:
                            s_femID = prefs.getString("EmployeeNo", "");
                            s_emName = prefs.getString("EmployeeName", "");
                            s_femPickerNo = "";
                            s_emFingerNo = "8";
                            mId = id;
                            s_emFingerprintb64 = Base64.encodeToString(fpTemp, 0, fpTemp.length, Base64.DEFAULT);
                            s_emFingerprinthex = ByteArrayToHexString(fpTemp, fpTemp.length);
                            dbhelper.AddFP(s_femID, s_femPickerNo, s_emFingerNo, String.valueOf(mId), s_emFingerprintb64, s_emFingerprinthex);

                            break;
                        case 9:
                            s_femID = prefs.getString("EmployeeNo", "");
                            s_emName = prefs.getString("EmployeeName", "");
                            s_femPickerNo = "";
                            s_emFingerNo = "9";
                            mId = id;
                            s_emFingerprintb64 = Base64.encodeToString(fpTemp, 0, fpTemp.length, Base64.DEFAULT);
                            s_emFingerprinthex = ByteArrayToHexString(fpTemp, fpTemp.length);
                            dbhelper.AddFP(s_femID, s_femPickerNo, s_emFingerNo, String.valueOf(mId), s_emFingerprintb64, s_emFingerprinthex);

                            break;
                        case 10:
                            s_femID = prefs.getString("EmployeeNo", "");
                            s_emName = prefs.getString("EmployeeName", "");
                            s_femPickerNo = "";
                            s_emFingerNo = "10";
                            mId = id;
                            s_emFingerprintb64 = Base64.encodeToString(fpTemp, 0, fpTemp.length, Base64.DEFAULT);
                            s_emFingerprinthex = ByteArrayToHexString(fpTemp, fpTemp.length);
                            dbhelper.AddFP(s_femID, s_femPickerNo, s_emFingerNo, String.valueOf(mId), s_emFingerprintb64, s_emFingerprinthex);


                            break;

                        default:

                            break;

                    }
                    byte[] mMatData = Base64.decode(s_emFingerprintb64, Base64.DEFAULT);
                    byte[] mMatData1 = HexStringToByteArray(s_emFingerprinthex);

                    //Toast.makeText(getApplicationContext(),fpTemp.toString(),Toast.LENGTH_LONG).show();
                    //   showInformation(getString(R.string.enroll_success), getString(R.string.enrolled_id, id)+"\nOriginal Byte: "+fpTemp.toString()+"\nBase64: "+mMatData.toString()+"\nHexArray: "+mMatData1.toString());
                    showInformation(getString(R.string.enroll_success), getString(R.string.enrolled_id, id));
                } else if (params[0].equals("verify")) {
                    startTime = System.currentTimeMillis();
                    res = Bione.verify(mId, fpFeat);

                    verifyTime = System.currentTimeMillis() - startTime;
                    if (res.error != Bione.RESULT_OK) {
                        showError(getString(R.string.verify_failed_because_of_error), getFingerprintErrorString(res.error));
                        break;
                    }
                    if ((Boolean) res.data) {

                        showInformation(getString(R.string.fingerprint_match), getString(R.string.fingerprint_similarity, res.arg1) + mId);

                    } else {
                        showError(getString(R.string.fingerprint_not_match), getString(R.string.fingerprint_similarity, res.arg1));
                    }
                } else if (params[0].equals("identify")) {
                    startTime = System.currentTimeMillis();
                    int id = Bione.identify(fpFeat);
                    verifyTime = System.currentTimeMillis() - startTime;
                    if (id < 0) {
                        mId = id;

                        showError(getString(R.string.identify_failed_because_of_error), getFingerprintErrorString(id));
                        break;
                    }
                    mId = id;
                    showEmployee();
                    // showInformation(getString(R.string.identify_match), getString(R.string.matched_id, id));
                }
            } while (false);

            updateSingerTestText(captureTime, extractTime, generalizeTime, verifyTime);
            enableControl(true);
            dismissProgressDialog();
            mIsDone = true;
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            if (CaptureNo == 1) {
                if (mId < 0) {
                    mId = -1;
                    return;
                }
                showUpdateUserDialog();
            }

            dbhelper = new DBHelper(getApplicationContext());
            SQLiteDatabase db = dbhelper.getReadableDatabase();

            Cursor employee = null;


            employee = db.query(Database.FINGERPRINT_TABLE_NAME, null,
                    " femID = ?", new String[]{emID.getText().toString()}, null, null, null);

            //startManagingCursor(accounts);
            if (employee.getCount() > 0) {
                while (employee.moveToNext()) {
                    // update view
                    FingerNo = Integer.parseInt(employee.getString(employee
                            .getColumnIndex(Database.FEM_FINGERNO)));
                    switch (FingerNo) {
                        case 1:
                            rfinger1.setRegisted(true);
                            rfinger1.setSelected(true);
                            rfinger1.setEnabled(false);

                            break;
                        case 2:

                            rfinger2.setRegisted(true);
                            rfinger2.setSelected(true);
                            rfinger2.setEnabled(false);
                            break;
                        case 3:

                            rfinger3.setRegisted(true);
                            rfinger3.setSelected(true);
                            rfinger3.setEnabled(false);

                            break;
                        case 4:

                            rfinger4.setRegisted(true);
                            rfinger4.setSelected(true);
                            rfinger4.setEnabled(false);
                            break;
                        case 5:

                            rfinger5.setRegisted(true);
                            rfinger5.setSelected(true);
                            rfinger5.setEnabled(false);
                            break;
                        case 6:
                            lfinger1.setRegisted(true);
                            lfinger1.setSelected(true);
                            lfinger1.setEnabled(false);
                            break;
                        case 7:

                            lfinger2.setRegisted(true);
                            lfinger2.setSelected(true);
                            lfinger2.setEnabled(false);
                            break;
                        case 8:
                            lfinger3.setRegisted(true);
                            lfinger3.setSelected(true);
                            lfinger3.setEnabled(false);

                            break;
                        case 9:

                            lfinger4.setRegisted(true);
                            lfinger4.setSelected(true);
                            lfinger4.setEnabled(false);
                            break;
                        case 10:

                            lfinger5.setRegisted(true);
                            lfinger5.setSelected(true);
                            lfinger5.setEnabled(false);

                            break;

                        default:

                            break;

                    }
                    //  txtdesc.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onCancelled() {
        }

        public void waitForDone() {
            while (!mIsDone) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
