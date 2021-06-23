package com.plantation.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.services.SerialService;
import com.plantation.trancell.WeighBaseActivity;
import com.plantation.vsp.serialdevice.SerialManager;
import com.plantation.vsp.serialdevice.SerialManagerUiCallback;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;


public class ScaleSerialWeighActivity extends WeighBaseActivity implements
        SerialManagerUiCallback {
    public static final String TRANCELL_TI500 = "TI-500";
    public static final String FINGERPRINT = "FingerPrint";
    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    public static SerialManager mSerialManager;
    public static boolean isPrefClearTextAfterSending = false;
    public static boolean isPrefSendCR = true;
    public static BluetoothAdapter mBluetoothAdapter;
    public static AlertDialog weigh;
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;
    static TextView tvMemberName, tvShowMemberNo, tvShowGrossTotal, tvWeighingAccumWeigh, tvWeighingTareWeigh,
            tvUnitsCount, tvShowTotalKgs, tvAvgCWeight, tvGross, tvNetWeightAccepted, tvGrossAccepted, txtKgs, txtScaleConn, txtPrinterConn;
    static TextView tvECrates, tvBatchCrates;
    static TextView tvsavedReading, tvSavedNet, tvSavedTare, tvSavedUnits, tvSavedTotal;
    static Double myGross = 0.0;
    static Double netweight = 0.0;
    static boolean stopRefreshing = false;
    static double setTareWeight = 0.0;
    static Context _ctx;
    static Activity _activity;
    static int SessionBgs;
    public Toolbar toolbar;
    public SimpleCursorAdapter ca;
    DBHelper dbhelper;
    SQLiteDatabase db;
    DecimalFormat formatter, formatInt;
    TextView tv_number, txtFarmer;
    TableRow trCrates;
    EditText etShowGrossTotal;
    Typeface font;
    String SessionID, SessionNo, SessionDate, SessionTime, SessionDevice;
    String SessionFarmerNo, SessionBags, SessionNet, SessionTare;
    String SessionField, SessionBlock, SessionGrade;
    int BCrates = 0;
    int ECrates = 0;
    int MaxBatchCrates = 0;
    double ETotalKg = 0.0;
    int RecNo = 1;
    int WeighNo = 1;
    String BatchID, BatchDate, BatchNo, BatchCrates, Crates, EmployeeCrates, EmployeeTotalKg, ColDate, Time, DataDevice, BatchNumber, EmployeeNo;
    String FieldClerk, TaskCode, ProduceCode, VarietyCode, GradeCode;
    String Estate, Division, Field, Block;
    String NetWeight, TareWeight, UnitCount;
    String UnitPrice, RecieptNo, WeighmentNo, CheckinMethod;
    String newGross, newNet, newTare;
    int weighmentCounts = 0;
    String weighingSession = "";
    Button btn_accept, btn_next, btn_print, btn_reconnect;
    LinearLayout lt_accept, lt_nprint;
    SimpleDateFormat dateTimeFormat;
    String blocks, blockNo, manageid;
    ArrayList<String> blockdata = new ArrayList<String>();
    ArrayAdapter<String> blockadapter;
    Spinner spBlock;
    String grade, gradeid;
    ArrayList<String> gradedata = new ArrayList<String>();
    ArrayAdapter<String> gradeadapter;
    Spinner spGrade;
    EditText edtCrates;
    TextView tvConnection;
    ListView listEmployees;
    String accountId;
    TextView textAccountId, textEmployee, textEmployeeNo;
    Boolean success = true;
    SearchView searchView;
    private Button mBtnSend;
    private ScrollView mScrollViewConsoleOutput;
    private EditText mInputBox;
    private TextView mValueConsoleOutputTv;
    private TextView mValueRxCounterTv;
    private TextView mValueTxCounterTv;
    private Timer timer;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_listemployee);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSerialManager = new SerialManager(this, this);
        setBleBaseDeviceManager(mSerialManager);

        //initialiseDialogAbout(getResources().getString(R.string.about_serial));
        initialiseDialogFoundDevices("VSP");
        initializer();
        _ctx = this;
        _activity = this;
    }


    @Override
    public void bindViews() {
        super.bindViews();

        mBtnSend = findViewById(R.id.btnSend);
        mBtnScan = findViewById(R.id.btnScan);
        mScrollViewConsoleOutput = findViewById(R.id.scrollViewConsoleOutput);
        mInputBox = findViewById(R.id.inputBox);
        mValueConsoleOutputTv = findViewById(R.id.valueConsoleOutputTv);
        mValueRxCounterTv = findViewById(R.id.valueRxCounterTv);
        mValueTxCounterTv = findViewById(R.id.valueTxCounterTv);
    }

    public void initializer() {
        txtKgs = this.findViewById(R.id.txtKGS);
        txtKgs.setText("0.0");
        txtKgs.setVisibility(View.GONE);
        tvConnection = this.findViewById(R.id.tvConnection);
        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();
        formatter = new DecimalFormat("0000");
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ScaleSerialWeighActivity.this);
        prefs = PreferenceManager.getDefaultSharedPreferences(ScaleSerialWeighActivity.this);
        setTareWeight = Double.parseDouble(mSharedPrefs.getString("tareWeight", "0"));

        showWeigh();
        weigh.dismiss();

        listEmployees = this.findViewById(R.id.lvEmployee);
        listEmployees.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                textEmployee = selectedView.findViewById(R.id.tv_name);
                textEmployeeNo = selectedView.findViewById(R.id.tv_number);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("Employee", textEmployee.getText().toString());
                edit.commit();
                edit.putString("EmployeeNo", textEmployeeNo.getText().toString());
                edit.commit();
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                myGross = Double.parseDouble(txtKgs.getText().toString());
                if (myGross > 0) {
                    Context context = ScaleSerialWeighActivity.this;
                    LayoutInflater inflater = ScaleSerialWeighActivity.this.getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Remove Load!\nTo Continue ...");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }
                showWeigh();


            }
        });
        if (!mSharedPrefs.getBoolean("enforcePickerNo", false) == true) {

            searchView = findViewById(R.id.searchView);
            searchView.requestFocus();
            searchView.setQueryHint("Search Employee No ...");
            if (!mSharedPrefs.getBoolean("enableAlphaNumeric", false) == true) {
                searchView.setInputType(InputType.TYPE_CLASS_PHONE |
                        InputType.TYPE_CLASS_PHONE);
            } else {
                searchView.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            }
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
        } else {
            searchView = findViewById(R.id.searchView);
            searchView.requestFocus();
            searchView.setQueryHint("Search Picker No ...");
            if (!mSharedPrefs.getBoolean("enableAlphaNumeric", false) == true) {
                searchView.setInputType(InputType.TYPE_CLASS_PHONE |
                        InputType.TYPE_CLASS_PHONE);
            } else {
                searchView.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            }
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    ca.getFilter().filter(query);
                    ca.setFilterQueryProvider(new FilterQueryProvider() {

                        @Override
                        public Cursor runQuery(CharSequence constraint) {
                            String PickerNo = constraint.toString();
                            return dbhelper.SearchSpecificPicker(PickerNo);

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
                            String PickerNo = constraint.toString();
                            return dbhelper.SearchEmployeePicker(PickerNo);

                        }
                    });
                    //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                    return false;
                }
            });
        }


    }

    @Override
    public void setListeners() {
        super.setListeners();

        mBtnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // to send data to module
                String data = mInputBox.getText().toString();
                if (data != null) {
                    mBtnSend.setEnabled(false);
                    if (mValueConsoleOutputTv.getText().length() <= 0) {
                        mValueConsoleOutputTv.append(">");
                    } else {
                        mValueConsoleOutputTv.append("\n\n>");
                    }

                    if (isPrefSendCR == true) {
                        mSerialManager.startDataTransfer(data + "\r");
                    } else if (isPrefSendCR == false) {
                        mSerialManager.startDataTransfer(data);
                    }

                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus()
                                    .getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    if (isPrefClearTextAfterSending == true) {
                        mInputBox.setText("");
                    } else {
                        // do not clear the text from the editText
                    }
                }
            }
        });
    }


    public void onResume() {
        super.onResume();

		/*Intent intent = new Intent(ScaleSerialWeighActivity.this, SerialService.class);
		startService(intent);*/

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
            // go back to milkers activity
            //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
        } else {
			/*if (mConnector != null) {
				txtPrinterConn.setVisibility(View.VISIBLE);

			}*/
        }
        setTareWeight = Double.parseDouble(mSharedPrefs.getString("tareWeight", "0"));


    }


    public void showWeigh() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_weigh, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        //dialogBuilder.setTitle("------- Weigh Produce -------");
        SQLiteDatabase db = dbhelper.getReadableDatabase();

        spGrade = dialogView.findViewById(R.id.spGrade);
        spBlock = dialogView.findViewById(R.id.spBlock);
        BlockList();
        Grade();

        tvShowMemberNo = dialogView.findViewById(R.id.tvMemberNo);
        tvShowMemberNo.setText(prefs.getString("EmployeeNo", ""));

        tvMemberName = dialogView.findViewById(R.id.tvMemberNameShow);
        tvMemberName.setText(prefs.getString("Employee", ""));

        tvWeighingAccumWeigh = dialogView.findViewById(R.id.tvWeighingAccumWeigh);
        tvWeighingAccumWeigh.setTypeface(font);

        tvWeighingTareWeigh = dialogView.findViewById(R.id.tvWeighingTareWeigh);
        tvWeighingTareWeigh.setTypeface(font);
        tvWeighingTareWeigh.setText(String.valueOf(setTareWeight));

        tvShowTotalKgs = dialogView.findViewById(R.id.tvShowTotalKgs);
        tvShowTotalKgs.setTypeface(font);

        tvsavedReading = dialogView.findViewById(R.id.tvvGross);
        tvSavedNet = dialogView.findViewById(R.id.tvvTotalKgs);
        tvSavedTare = dialogView.findViewById(R.id.tvTareWeight);
        tvSavedUnits = dialogView.findViewById(R.id.tvvcount);
        tvSavedTotal = dialogView.findViewById(R.id.tvAccumWeight);

        tvUnitsCount = dialogView.findViewById(R.id.tvUnitsCount);
        tvUnitsCount.setTypeface(font);

        tvUnitsCount.setText(String.valueOf(weighmentCounts));
        tvShowGrossTotal = dialogView.findViewById(R.id.tvShowGrossTotal);
        tvShowGrossTotal.setTypeface(font);
        tvShowGrossTotal.setText("0.0");
        tvGrossAccepted = dialogView.findViewById(R.id.tvGrossAccepted);
        tvGrossAccepted.setTypeface(font);
        tvNetWeightAccepted = dialogView.findViewById(R.id.tvNetWeightAccepted);
        tvNetWeightAccepted.setTypeface(font);

        tvECrates = dialogView.findViewById(R.id.tvECrates);
        Date date = new Date(getDate());
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        ColDate = format1.format(date);
        Cursor ec = db.rawQuery("select sum(BagCount)from EmployeeProduceCollection where "
                + Database.CollDate + " = '" + ColDate + "' and " + Database.EmployeeNo + " = '" + tvShowMemberNo.getText().toString() + "'", null);
        if (ec != null) {

            ec.moveToFirst();
            if (ec.getString(0) != null) {
                ECrates = Integer.parseInt(ec.getString(0));
                EmployeeCrates = String.valueOf(ECrates);
            } else {
                EmployeeCrates = "0";
            }

        }
        ec.close();
        tvECrates.setText(EmployeeCrates);

        Cursor tw = db.rawQuery("select sum(NetWeight)from EmployeeProduceCollection where "
                + Database.CollDate + " = '" + ColDate + "' and " + Database.EmployeeNo + " = '" + tvShowMemberNo.getText().toString() + "'", null);
        if (tw != null) {

            tw.moveToFirst();
            if (tw.getString(0) != null) {
                ETotalKg = Double.parseDouble(tw.getString(0));
                EmployeeTotalKg = String.valueOf(ETotalKg);
            } else {
                EmployeeTotalKg = "0.0";
            }

        }
        tw.close();
        tvWeighingAccumWeigh.setText(EmployeeTotalKg);

        BatchDate = prefs.getString("BatchON", "");
        BatchNo = prefs.getString("BatchNumber", "");
        tvBatchCrates = dialogView.findViewById(R.id.tvBatchCrates);
        Cursor bc = db.rawQuery("select BatchCrates from FarmersSuppliesConsignments where "
                + Database.BatchDate + " = '" + BatchDate + "' and " + Database.BatchNumber + " = '" + BatchNo + "'", null);
        if (bc != null) {

            bc.moveToFirst();
            if (bc.getString(0) != null) {
                BCrates = Integer.parseInt(bc.getString(0));
                BatchCrates = String.valueOf(BCrates);
            } else {
                BatchCrates = "0";
            }

        }
        bc.close();
        tvBatchCrates.setText(BatchCrates);

        lt_accept = dialogView.findViewById(R.id.lt_accept);
        lt_nprint = dialogView.findViewById(R.id.lt_nprint);
        btn_accept = dialogView.findViewById(R.id.btn_accept);
        btn_next = dialogView.findViewById(R.id.btn_next);

        btn_print = dialogView.findViewById(R.id.btn_print);

        btn_accept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!mSharedPrefs.getBoolean("enforceBlock", false) == true) {

                    //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                } else {
                    if (spBlock.getSelectedItem().equals("Select ...")) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Block");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getApplicationContext(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (!mSharedPrefs.getBoolean("enforceGrade", false) == true) {

                    //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                } else {
                    if (spGrade.getSelectedItem().equals("Select ...")) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Grade");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getApplicationContext(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if (tvShowGrossTotal.getText().equals("0.0")) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Gross Total Cannot be 0.0, Please Request For Gross Reading");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();

                    return;

                }

                netweight = Double.parseDouble(tvShowGrossTotal.getText().toString());
                if (netweight <= Double.parseDouble(mSharedPrefs.getString("minCRange", "0"))) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Unacceptable Gross Weight! should be greater than" + Double.parseDouble(mSharedPrefs.getString("minCRange", "0")));
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
                    return;

                }
                MaxBatchCrates = Integer.parseInt(mSharedPrefs.getString("maxBatchCrates", "0"));
                if (Integer.parseInt(tvBatchCrates.getText().toString()) > MaxBatchCrates) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText(mSharedPrefs.getString("maxBatchCrates", "0") + " Maximum Crates Exhausted for this Batch!\nClose and Open a New Batch");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
                    return;

                }


                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
                // Setting Dialog Title
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_grossweight, null);
                dialogBuilder.setView(dialogView);
                dialogBuilder.setTitle("Accept Reading?");
                // Setting Dialog Message
                //dialogBuilder.setMessage("Are you sure you want to accept the gross reading?");

                tvGross = dialogView.findViewById(R.id.txtGross);
                tvGross.setTypeface(font);

                tvUnitsCount = dialogView.findViewById(R.id.tvUnitsCount);
                tvUnitsCount.setTypeface(font);

                trCrates = dialogView.findViewById(R.id.trCrates);
                trCrates.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showCratesDialog();
                    }
                });

                tvWeighingTareWeigh = dialogView.findViewById(R.id.tvWeighingTareWeigh);
                tvWeighingTareWeigh.setTypeface(font);

                tvShowTotalKgs = dialogView.findViewById(R.id.tvShowTotalKgs);
                tvShowTotalKgs.setTypeface(font);

                tvAvgCWeight = dialogView.findViewById(R.id.tvAvgCWeight);
                tvAvgCWeight.setTypeface(font);


                tvGross.setText(tvShowGrossTotal.getText().toString());

                final DecimalFormat df = new DecimalFormat("#0.0#");

                Double grossValue = 0.0;
                Double tare = 0.0;
                grossValue = Double.parseDouble(tvGross.getText().toString());
                if (grossValue >= Double.parseDouble(mSharedPrefs.getString("minCRange", "18"))) {
                    Double cratesno = grossValue / Double.parseDouble(mSharedPrefs.getString("minCRange", "18"));
                    int crates = cratesno.intValue();
                    Double avgCWeight = grossValue / crates;
                    tare = crates * setTareWeight;
                    //tare=setTareWeight;

                    tvWeighingTareWeigh.setText(df.format(tare));
                    tvUnitsCount.setText(String.valueOf(crates));

                    Double NetTotal = 0.0;
                    NetTotal = grossValue - tare;
                    tvShowTotalKgs.setText(df.format(NetTotal));
                    Double truncatedDouble = new BigDecimal(avgCWeight).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                    tvAvgCWeight.setText(df.format(truncatedDouble));
                }
                if (tvShowGrossTotal.getText().equals("0.0")) {

                    tvGross.setText("0 KG");

                } else {

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("Gross", tvShowGrossTotal.getText().toString());
                    edit.commit();
                    edit.putString("Crates", tvUnitsCount.getText().toString());
                    edit.commit();
                    edit.putString("Net", tvShowTotalKgs.getText().toString());
                    edit.commit();
                    edit.putString("Tare", tvWeighingTareWeigh.getText().toString());
                    edit.commit();


                }

                ///Setting Negative "Yes" Button
                dialogBuilder.setNegativeButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                // Setting Positive "NO" Button
                dialogBuilder.setPositiveButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                            }
                        });
                // Showing Alert Message
                final AlertDialog weights = dialogBuilder.create();
                weights.show();
                weights.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                weights.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
                weights.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
                weights.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.BLUE);
                weights.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Double avgW = Double.parseDouble(tvAvgCWeight.getText().toString());
                        Double minW = Double.parseDouble(mSharedPrefs.getString("minCRange", "18.0"));
                        Double maxW = Double.parseDouble(mSharedPrefs.getString("maxCRange", "20.0"));
                        if (minW > avgW) {
                            Context context = getApplicationContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Unacceptable Average Crate Weight! should be greater than " + mSharedPrefs.getString("minCRange", "18"));
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            return;

                        }
                        if (maxW < avgW) {
                            Context context = getApplicationContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Unacceptable Average Crate Weight! should be less than " + mSharedPrefs.getString("maxCRange", "18"));
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            return;

                        }

                        dbhelper = new DBHelper(getApplicationContext());
                        SQLiteDatabase db = dbhelper.getReadableDatabase();
                        formatter = new DecimalFormat("0000");
                        formatInt = new DecimalFormat("000");
                        Date BatchD = null;
                        try {
                            BatchD = dateTimeFormat.parse(prefs.getString("BatchON", "") + " 00:00:00");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        SimpleDateFormat format0 = new SimpleDateFormat("yyyyMMdd");
                        BatchDate = format0.format(BatchD);
                        BatchNo = prefs.getString("BatchNumber", "");
                        DataDevice = mSharedPrefs.getString("terminalID", "") + BatchDate + BatchNo;

                        Calendar cal = Calendar.getInstance();
                        Date date = new Date(getDate());
                        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                        SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                        ColDate = format1.format(date);
                        Time = format2.format(cal.getTime());
                        FieldClerk = prefs.getString("user", "");
                        EmployeeNo = prefs.getString("EmployeeNo", "");
                        TaskCode = prefs.getString("taskCode", "");
                        ProduceCode = prefs.getString("produceCode", "");
                        VarietyCode = prefs.getString("varietyCode", " ");

						/*if (spGrade.getSelectedItem().equals("Select ...")) {

							GradeCode="";
						}else{

							GradeCode=gradeid;
						}*/

                        GradeCode = gradeid;
                        Estate = prefs.getString("estateCode", "");
                        Division = prefs.getString("divisionCode", " ");
                        //Field=prefs.getString("fieldCode", "");
                        if (prefs.getString("fieldCode", "").equals("Select ...")) {
                            Field = "";
                        } else {
                            Field = prefs.getString("fieldCode", "");
                        }
                        if (spBlock.getSelectedItem().equals("Select ...")) {
                            Block = "";
                        } else {
                            Block = spBlock.getSelectedItem().toString();
                        }

                        //NetWeight=prefs.getString("Net", "");
                        NetWeight = tvShowTotalKgs.getText().toString();
                        TareWeight = prefs.getString("Tare", "");

                        Crates = prefs.getString("Crates", "");
                        UnitPrice = "0";


                        BatchCrates = tvUnitsCount.getText().toString();


                        Cursor trans = db.rawQuery("select * from EmployeeProduceCollection where " + Database.DataCaptureDevice + " = '" + DataDevice + "'", null);
                        if (trans.getCount() > 0) {
                            Cursor trans1 = db.rawQuery("select MAX(ReceiptNo) from EmployeeProduceCollection where " + Database.DataCaptureDevice + " = '" + DataDevice + "'", null);
                            if (trans1 != null) {

                                trans1.moveToFirst();

                                RecNo = Integer.parseInt(trans1.getString(0)) + 1;
                                RecieptNo = formatter.format(RecNo);

                            }
                            trans1.close();
                        } else {
                            RecieptNo = formatter.format(RecNo);


                        }
                        Cursor transb = db.rawQuery("select * from EmployeeProduceCollection where "
                                + Database.EmployeeNo + " = '" + EmployeeNo + "' and "
                                + Database.DataCaptureDevice + " = '" + DataDevice + "'", null);
                        if (transb.getCount() > 0) {
                            Cursor trans2 = db.rawQuery("select MAX(LoadCount) from EmployeeProduceCollection where "
                                    + Database.EmployeeNo + " = '" + EmployeeNo + "' and "
                                    + Database.DataCaptureDevice + " = '" + DataDevice + "'", null);

                            if (trans2 != null) {

                                trans2.moveToFirst();

                                WeighNo = Integer.parseInt(trans2.getString(0)) + 1;
                                WeighmentNo = formatInt.format(WeighNo);


                            }
                            trans2.close();
                        } else {
                            WeighmentNo = formatInt.format(1);
                        }

                        BatchDate = prefs.getString("BatchON", "");
                        Cursor c1 = db.rawQuery("select _id,BatchCrates from FarmersSuppliesConsignments where "
                                + Database.BatchDate + " = '" + BatchDate + "' and " + Database.BatchNumber + " = '" + BatchNo + "'", null);
                        if (c1 != null) {

                            c1.moveToFirst();
                            BatchID = String.valueOf(c1.getInt(0));
                            if (c1.getString(1) != null) {
                                BCrates = Integer.parseInt(c1.getString(1)) + Integer.parseInt(Crates);
                                BatchCrates = String.valueOf(BCrates);
                            } else {
                                BCrates = Integer.parseInt(Crates);
                                BatchCrates = String.valueOf(BCrates);
                            }
                        }
                        c1.close();
                        ContentValues values = new ContentValues();
                        values.put(Database.BatchCrates, BatchCrates);
                        long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                                "_id = ?", new String[]{BatchID});

                        //db.close();
                        if (rows > 0) {
                            // Toast.makeText(getApplicationContext(), "Updated Total KGs Successfully!",Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Sorry! Could not update BatchCrates!",
                                    Toast.LENGTH_LONG).show();
                        }

                        if (mSharedPrefs.getString("vModes", "FingerPrint").equals(FINGERPRINT)) {
                            CheckinMethod = "1";

                        } else if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {
                            CheckinMethod = "2";

                        } else if (mSharedPrefs.getString("vModes", "Manual").equals(MANUAL)) {
                            CheckinMethod = "3";

                        }

                        dbhelper.AddEmployeeTrans(ColDate, Time, DataDevice, BatchNo, EmployeeNo,
                                FieldClerk, TaskCode, ProduceCode,
                                VarietyCode, GradeCode, Estate, Division, Field, Block,
                                NetWeight, TareWeight, Crates,
                                UnitPrice, RecieptNo, WeighmentNo, CheckinMethod);

                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        //text.setText("Saved Successfully: " + NetWeight + " Kgs\n" + "Crates: " + Crates + "");
                        text.setText("Saved Successfully: " + NetWeight + " Kgs");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        weights.dismiss();
                        weigh.dismiss();

                        Boolean wantToCloseDialog = false;
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog)
                            weights.dismiss();
                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                    }
                });

            }
        });

        dialogBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                //getdata();
                // weighmentCounts = 1;

            }
        });
        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        weigh = dialogBuilder.create();
        weigh.show();
        weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);
        weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                weigh.dismiss();
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                if (wantToCloseDialog)
                    weigh.dismiss();
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });


    }

    private void BlockList() {
        blockdata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select bkID,bkFiled from blocks", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    blocks = c.getString(c.getColumnIndex("bkID"));
                    blockdata.add(blocks);

                } while (c.moveToNext());
            }
        }
        c.close();
        //db.close();
        //dbhelper.close();

        blockadapter = new ArrayAdapter<String>(ScaleSerialWeighActivity.this, R.layout.spinner_item_min, blockdata);
        blockadapter.setDropDownViewResource(R.layout.spinner_item_min);
        blockadapter.notifyDataSetChanged();
        spBlock.setAdapter(blockadapter);
        //spBlock.setSelection(prefs.getInt("spinnerSelection", 0));
        spBlock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String blockName = parent.getItemAtPosition(position).toString();

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

    private void Grade() {
        gradedata.clear();
        ProduceCode = prefs.getString("produceCode", "");
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select pgdRef,pgdName from ProduceGrades where pgdProduce= '" + ProduceCode + "' ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    grade = c.getString(c.getColumnIndex("pgdName"));
                    gradedata.add(grade);

                } while (c.moveToNext());
            }
        }


        gradeadapter = new ArrayAdapter<String>(this, R.layout.spinner_item_min, gradedata);
        gradeadapter.setDropDownViewResource(R.layout.spinner_item_min);
        gradeadapter.notifyDataSetChanged();
        spGrade.setAdapter(gradeadapter);
        spGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String GradeName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select pgdRef from ProduceGrades where pgdName= '" + GradeName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    gradeid = c.getString(c.getColumnIndex("pgdRef"));


                }
                SharedPreferences.Editor edit = prefs.edit();

                edit.putString("gradeCode", gradeid);
                edit.commit();
                c.close();
                //db.close();
                //dbhelper.close();
                TextView tv = (TextView) view;


                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
               /* if(disabled.equals("true")) {
                    // Set the disable item text color
                    tv.setBackgroundColor(Color.parseColor("#E3E4ED"));

                }*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    public void showCratesDialog() {
        AlertDialog.Builder dialogBasedate = new AlertDialog.Builder(this);
        LayoutInflater inflater1 = this.getLayoutInflater();
        final View dialogView1 = inflater1.inflate(R.layout.dialog_edit_crate, null);
        dialogBasedate.setView(dialogView1);
        dialogBasedate.setCancelable(true);
        dialogBasedate.setTitle("Update Crates");
        edtCrates = dialogView1.findViewById(R.id.et_crates);

        edtCrates.setText(tvUnitsCount.getText().toString());
        dialogBasedate.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();


            }
        });

        final AlertDialog bc = dialogBasedate.create();
        bc.show();
        bc.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                int crate = Integer.parseInt(edtCrates.getText().toString());
                if (crate == 0) {
                    Toast.makeText(getApplicationContext(), "Please a Valid Crate Number!", Toast.LENGTH_LONG).show();
                    return;
                }
                tvUnitsCount.setText(edtCrates.getText().toString());

                final DecimalFormat df = new DecimalFormat("#0.0#");
                Double grossValue = 0.0;
                Double tare = 0.0;
                Double NetTotal = 0.0;

                grossValue = Double.parseDouble(tvGross.getText().toString());
                int crates = Integer.parseInt(edtCrates.getText().toString());
                Double avgCWeight = grossValue / crates;
                //tare = crates * setTareWeight;
                tare = setTareWeight;
                NetTotal = grossValue - tare;

                tvWeighingTareWeigh.setText(df.format(tare));
                tvShowTotalKgs.setText(df.format(NetTotal));
                Double truncatedDouble = new BigDecimal(avgCWeight).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                tvAvgCWeight.setText(df.format(truncatedDouble));

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("Gross", tvShowGrossTotal.getText().toString());
                edit.commit();
                edit.putString("Crates", tvUnitsCount.getText().toString());
                edit.commit();
                edit.putString("Net", tvShowTotalKgs.getText().toString());
                edit.commit();
                edit.putString("Tare", tvWeighingTareWeigh.getText().toString());
                edit.commit();
                bc.dismiss();
                //Do stuff, possibly set wantToCloseDialog to true then...
                if (wantToCloseDialog)
                    bc.dismiss();
            }
        });
    }

    @Override
    public void onUiVspServiceFound(final boolean found) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBtnSend.setEnabled(found);
            }
        });
    }

    @Override
    public void onUiSendDataSuccess(final String dataSend) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mValueConsoleOutputTv.append(dataSend);
                //txtKgs.setText(dataSend);
//				txtKgs.setText(dataSend.replace("\n", "").replace("\r", ""));
                mValueTxCounterTv.setText("" + mSerialManager.getTxCounter());
                mScrollViewConsoleOutput.smoothScrollTo(0,
                        mValueConsoleOutputTv.getBottom());
            }
        });
    }

    @Override
    public void onUiReceiveData(final String dataReceived, final String Reading) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mBtnScan.setText(R.string.btn_disconnect);
                String GrossReading = dataReceived.replaceAll(" ", "").replaceAll("kg", "").replaceAll("GR", "").replaceAll("\n", "").replaceAll("\r", "");
                String G1, G2, G3, s1, s2;
                Double s3;
                G1 = dataReceived.replaceAll(" ", "");
                G2 = G1.replaceAll("kg", "").replaceAll("GR", "");
                G3 = G2.replaceAll("\r\n", "");
                // s3=Double.parseDouble(G3);

                //mValueConsoleOutputTv.append(GrossReading);
                //mValueRxCounterTv.setText("" + mSerialManager.getRxCounter());
                double value;
                final DecimalFormat df = new DecimalFormat("#0.0#");

                try {


                    value = new Double(Reading);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("Reading", String.valueOf(value));
                    edit.commit();
                    //System.out.println(value);}
                } catch (NumberFormatException e) {
                    //value = 0; // your default value
                    value = Double.parseDouble(prefs.getString("Reading", "0"));
                }
                txtKgs.setText(df.format(value));
                tvShowGrossTotal.setText(df.format(value));
                mScrollViewConsoleOutput.smoothScrollTo(0,
                        mValueConsoleOutputTv.getBottom());
                //mValueConsoleOutputTv.append(df.format(value));


            }
        });
    }

    @Override
    public void onUiDisconnected(final int status) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove("tvConn");
        edit.commit();
        Intent intent = new Intent(ScaleSerialWeighActivity.this, SerialService.class);
        stopService(intent);
        finish();

        Context context = getApplication();
        LayoutInflater inflater = getLayoutInflater();
        View customToastroot = inflater.inflate(R.layout.red_toast, null);
        TextView text = customToastroot.findViewById(R.id.toast);
        text.setText("Scale Disconnected, Reconnect!!");
        Toast customtoast = new Toast(context);
        customtoast.setView(customToastroot);
        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        customtoast.setDuration(Toast.LENGTH_LONG);
        customtoast.show();
        Toast.makeText(getApplication(), "Scale Disconnected, Reconnect!!",
                Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onUiConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBtnScan.setText(R.string.btn_disconnect);
                tvConnection.setVisibility(View.VISIBLE);
                Intent intent = new Intent(ScaleSerialWeighActivity.this, SerialService.class);
                startService(intent);

				/*timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								String data = "P";
								if (data != null) {
									mBtnSend.setEnabled(false);
									if (mValueConsoleOutputTv.getText().length() <= 0) {
										//mValueConsoleOutputTv.append(">");
									} else {
										//mValueConsoleOutputTv.append("\n\n>");
									}

									if (isPrefSendCR == true) {
										try {
											mSerialManager.startDataTransfer(data);

											Thread.sleep(250);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}


									} else if (isPrefSendCR == false) {

										try {
											mSerialManager.startDataTransfer(data);

											Thread.sleep(250);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}

									/*InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

									inputManager.hideSoftInputFromWindow(getCurrentFocus()
													.getWindowToken(),
											InputMethodManager.HIDE_NOT_ALWAYS);

									if (isPrefClearTextAfterSending == true) {
										mInputBox.setText("");
									} else {
										// do not clear the text from the editText
									}
								}
							}
						});
					}
				}, 6000, 1000);*/

            }
        });
        invalidateUI();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    @Override
    public void onUiUploaded() {
        mBtnSend.setEnabled(true);
    }

    @Override
    protected void loadPref() {
        super.loadPref();
        isPrefClearTextAfterSending = mSharedPreferences.getBoolean(
                "pref_clear_text_after_sending", false);
        isPrefSendCR = mSharedPreferences.getBoolean(
                "pref_append_/r_at_end_of_data", true);
    }

    @Override
    public void onBackPressed() {
        mBleBaseDeviceManager.disconnect();
        Intent intent = new Intent(ScaleSerialWeighActivity.this, SerialService.class);
        stopService(intent);
        finish();
    }

    @Override
    protected void onStart() {
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
            String[] from = {Database.ROW_ID, Database.EM_ID, Database.EM_NAME, Database.EM_PICKERNO};
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

    private String getDate() {

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }
}