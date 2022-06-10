package com.plantation.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.plantation.R;
import com.plantation.connector.P25Connector;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.services.EasyWeighService;
import com.plantation.synctocloud.RestApiRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Michael on 30/06/2016.
 */
public class ScaleEasyWeighActivity extends AppCompatActivity {
    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";
    public static final String DR_150 = "DR-150";
    public static final String WEIGH_AND_TARE = "Discrete";
    public static final String FILLING = "Incremental";
    public static final String FINGERPRINT = "FingerPrint";
    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    public static final String BOTH = "Both";
    public static final String DEVICE_NAME = "device_name";
    public static final int TARE_SCALE = 0;
    public static final int ZERO_SCALE = 12;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_WRITE = 3;
    public static final int READING_PROBE = 6;
    public static final int REQUEST_DEVICEADDRESS = 101;
    public static final String TOAST = "toast";
    public static final int COMPLETE_FAILURE = 404;
    public static final String TAG = "Weighing";
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String SCALE_VERSION = "scaleVersion";
    public static String cachedDeviceAddress;
    public static String DEVICE_TYPE = "device_type";
    public static ProgressDialog mConnectingDlg;
    public static BluetoothAdapter mBluetoothAdapter;
    public static P25Connector mConnector;
    public static AlertDialog weigh;
    public static Button btnReconnect;
    static TextView tvError, tvStability, tvMemberName, tvShowMemberNo, tvShowGrossTotal, tvWeighingAccumWeigh, tvWeighingTareWeigh,
            tvNet, tvTare, tvUnitsCount, tvShowTotalKgs, tvAvgCWeight, tvGross, tvNetWeightAccepted, tvGrossAccepted, txtKgs, txtScaleConn, txtPrinterConn;
    static TextView tvECrates, tvBatchCrates;
    static TextView tvsavedReading, tvSavedNet, tvSavedTare, tvSavedUnits, tvSavedTotal;
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;
    static Double GROSS_KG = 0.0;
    static Context _ctx;
    static Activity _activity;
    static double[] weighments = null;//new double[1000]; //Array to hold readings
    static boolean stopRefreshing = false;
    static double tareWeight = 0.0, setTareWeight = 0.0, totalTareWeight = 0.0, setMoisture = 0.0, totalMoisture = 0.0;
    static double netWeight = 0.0, totalNetWeight = 0.0, rtotalTareWeight = 0.0;
    static ProgressDialog mProcessDialog;
    static Double myGross = 0.0;
    static Double netweight = 0.0;
    static String errorNo;
    static String stableReadingCounter;
    static Button btn_accept, btn_next, btn_print, btn_reconnect;


    public static AlertDialog dialog_accept;


    public Toolbar toolbar;
    // Local Bluetooth adapter
    //private BluetoothAdapter mBluetoothAdapter = null;
    public SimpleCursorAdapter ca;
    Intent mIntent;
    DBHelper dbhelper;
    ListView listEmployees;
    String accountId;
    TextView textAccountId, textEmployee, textEmployeeNo;
    Boolean success = true;
    TableRow trCrates;
    Typeface font;
    String sheds;
    SearchView searchView;
    int gotConsignmentUniqueid = 0;
    double grossWeight = 0.0, totalGrossWeight = 0.0;
    //double cumulativeWeight;
    Date mWeighmentTime;
    boolean sameFarmer = false;
    SimpleDateFormat mDateFormat;
    boolean firstWeighment = false;
    TextView tv_number;
    //UUID for creating sessions
    UUID uuid = UUID.randomUUID();
    String weighingSession = "";
    int WeightCount = 0;
    LinearLayout lt_accept, lt_nprint;
    Message msg;
    Bundle b;
    int BCrates = 0;
    int ECrates = 0;
    int MaxBatchCrates = 0;
    double ETotalKg = 0.0;
    int RecNo = 1;
    int WeighNo = 1;
    String SessionNo, BatchID, BatchDate, BatchNo, BatchCrates, Crates, EmployeeCrates, EmployeeTotalKg, ColDate, Time, TerminalID, DataDevice, BatchNumber, EmployeeNo;
    String FieldClerk, TaskCode, TaskType, ProduceCode, VarietyCode, GradeCode;
    String Estate, Division, Field, Block, CheckinMethod;
    String EstateCode, DivisionCode, FieldCode, Co_prefix, Current_User;
    String BatchSerial;
    String NetWeight, TareWeight, UnitCount;
    String UnitPrice, RecieptNo, WeighmentNo;
    String newGross, newNet, newTare;
    int weighmentCounts = 0;

    String Id, Title, sMessage;

    // socket represents the open connection.
    BluetoothSocket mBTSocket = null;
    EasyWeighService resetConn;
    SQLiteDatabase db;
    DecimalFormat formatter, formatInt;
    String ScaleConn;
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
    String error, weighmentInfo;
    String returnValue;
    int cloudid = 0;
    ImageView c_refresh, c_success, c_error;
    Cursor produce;


    private String restApiResponse, serverBatchNo;
    private int progressStatus = 0, count = 0;

    public static Handler mHandler;
    static {
        mHandler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                    case TARE_SCALE:
                        break;
                    case ZERO_SCALE:
                        break;
                    case MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case EasyWeighService.STATE_CONNECTED:
                                ScaleEasyWeighActivity.mProcessDialog.setMessage("Connected to Scale");
                                //Toast.makeText(_ctx.getApplicationContext(), "Connected ...", Toast.LENGTH_SHORT).show();
                                break;
                            case EasyWeighService.STATE_CONNECTING:
                                mProcessDialog.setMessage("Attempting Connection to scale");

                                //Toast.makeText(getApplicationContext(), "Connecting ...", Toast.LENGTH_SHORT).show();
                                break;
                            case EasyWeighService.STATE_LISTEN:
                            case EasyWeighService.STATE_NONE:
                                break;
                        }
                        break;
                    case REQUEST_DEVICEADDRESS: //Device Address Not found call DeviceListActivity
                        if (mProcessDialog != null && mProcessDialog.isShowing()) {
                            mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
                        }

                        _activity.finish();
                        Intent intentDeviceList = new Intent(_ctx.getApplicationContext(), DeviceListActivity.class);
                        _activity.startActivityForResult(intentDeviceList, 1);

                        break;
                    case MESSAGE_READ:
                        try {
                            txtScaleConn.setText("Scale Connected");
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putString("scalec", "Scale Connected");
                            edit.commit();


                            byte[] readBuf = (byte[]) msg.obj;
                            // construct a string from the valid bytes in the buffer
                            String readMessage = new String(readBuf, 0, msg.arg1);

                            Log.i(TAG, "Returned Message" + readMessage);


                            //Convert message to ascii byte array
                            byte[] messageBytes = stringToBytesASCII(readMessage);

                            String thisWeighment = "";
                            if (mSharedPrefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15)) {
                                thisWeighment = getReading(messageBytes, readMessage);

                            } else {
                                thisWeighment = getReading(messageBytes);
                            }

                            final DecimalFormat df = new DecimalFormat("#0.0#");
                            final DecimalFormat tdf = new DecimalFormat("#0.000#");
                            final DecimalFormat f = new DecimalFormat("#.#");
                            double myDouble;

                            //thisWeighment = newFormatReading[0]; //overriding weighment
                            //tareWeight = Double.parseDouble(newFormatReading[1]);

                            Log.i(TAG, "New Format Reading is " + thisWeighment);
                            Log.i(TAG, "Weighment is " + thisWeighment);
                            Log.i(TAG, "Tare Weight is " + tareWeight);

                            if (thisWeighment != null && !thisWeighment.isEmpty()) {
                                try {
                                    myDouble = Double.parseDouble(thisWeighment);
                                } catch (NumberFormatException e) {
                                    myDouble = Double.parseDouble(thisWeighment.replace("W  ", ""));
                                    Log.i(TAG, "NumberFormatException " + e.getMessage() + " " + myDouble);
                                }

                                GROSS_KG = myDouble;
                                txtKgs.setText(df.format(GROSS_KG));

                                prefs = PreferenceManager.getDefaultSharedPreferences(_ctx.getApplicationContext());

                                int WeightCount = prefs.getInt("WeightCount", 0);
                                if (WeightCount <= Integer.parseInt(stableReadingCounter)) {

                                    weighments[WeightCount] = Double.parseDouble(df.format(GROSS_KG));

                                    Log.i("WeightCount", "Weight[" + WeightCount + "]: " + weighments[WeightCount]);

                                    if (WeightCount == (Integer.parseInt(stableReadingCounter) - 1)) {

                                        Log.i("WeightCount", "List:" + Arrays.toString(weighments));

                                    }
                                }

                                if (weigh.isShowing()) {

                                    if (myDouble == 100.0) {

                                        Context context = _ctx.getApplicationContext();
                                        LayoutInflater inflater = _activity.getLayoutInflater();
                                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                        TextView text = customToastroot.findViewById(R.id.toast);
                                        text.setText("OVER WEIGHT");
                                        Toast customtoast = new Toast(context);
                                        customtoast.setView(customToastroot);
                                        customtoast.setGravity(Gravity.TOP | Gravity.TOP, 0, 0);
                                        customtoast.setDuration(Toast.LENGTH_SHORT);
                                        //  customtoast.show();
                                        tvWeighingTareWeigh.setText("-");
                                        tvShowGrossTotal.setText("-");
                                        tvShowTotalKgs.setText("-");
                                        tvError.setVisibility(View.VISIBLE);
                                        tvStability.setVisibility(View.GONE);
                                        tvMemberName.setVisibility(View.GONE);
                                        btn_accept.setEnabled(false);

                                        return;
                                    } else {

                                        //tvError.setVisibility(View.GONE);
                                        tvMemberName.setVisibility(View.VISIBLE);
                                        btn_accept.setEnabled(true);

                                    }

                                    edit.putString("tvGross", df.format(myDouble));
                                    edit.commit();



                                    if (mSharedPrefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15)) {
                                        f.setRoundingMode(RoundingMode.HALF_EVEN);
                                        totalMoisture = Double.parseDouble(f.format((setMoisture / 100) * myDouble));
                                        tvWeighingTareWeigh.setText(df.format(totalMoisture + tareWeight));
                                        netWeight = myDouble - totalMoisture;
                                        edit.putString("tvNetWeight", df.format(netWeight));
                                        edit.apply();
                                        tvShowGrossTotal.setText(df.format(myDouble));
                                        tvShowTotalKgs.setText(df.format(netWeight));

                                    } else {
                                        f.setRoundingMode(RoundingMode.HALF_EVEN);
                                        totalMoisture = Double.parseDouble(f.format((setMoisture / 100) * myDouble));
                                        tvWeighingTareWeigh.setText(tdf.format(totalMoisture + tareWeight));
                                        netWeight = myDouble - totalMoisture;
                                        edit.putString("tvNetWeight", tdf.format(netWeight));
                                        edit.apply();
                                        tvShowGrossTotal.setText(tdf.format(myDouble));
                                        tvShowTotalKgs.setText(tdf.format(netWeight));
                                    }

                                    if (netWeight <= 0.0) {
                                        edit.putString("tvNetWeight", "0.0");
                                        edit.apply();
                                        tvShowTotalKgs.setText("0.0");
                                        tvStability.setVisibility(View.GONE);
                                    }


                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                        }

                        break;
                    case READING_PROBE:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);

                        //Convert message to ascii byte array
                        byte[] messageBytes = stringToBytesASCII(readMessage);

                        String thisWeighment = "";
                        if (mSharedPrefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15)) {
                            thisWeighment = getReading(messageBytes, readMessage);
                        } else {
                            thisWeighment = getReading(messageBytes);
                        }

                        if (!thisWeighment.equals("0.0")) {
                            //resend tare command

                            Message msg2 = Message.obtain(null, EasyWeighService.TARE_SCALE);
                            Message msg3 = Message.obtain(null, EasyWeighService.ZERO_SCALE);

                        }

                        break;
                    case MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                        mProcessDialog.setMessage("Connected to " + mConnectedDeviceName);

                        if (mProcessDialog != null && mProcessDialog.isShowing()) {
                            mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
                        }

                        // now send R for 3 seconds

                        try {
                            Message msg2 = Message.obtain(null, EasyWeighService.INIT_WEIGHING);
                            mEasyWeighService.send(msg2);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                        break;
                    case MESSAGE_TOAST:
                        //mProcessDialog.setMessage("Unable To Connect to Device");
                        if (mProcessDialog == null) {
                            mProcessDialog = new ProgressDialog(_ctx.getApplicationContext());
                        }

                        mProcessDialog.setMessage(msg.getData().getString(TOAST));

                        if (mProcessDialog != null && mProcessDialog.isShowing()) {
                            mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
                        }
                        if (msg.getData().getString(TOAST).equals("Unable to connect scale")) {
                            Context context = _ctx.getApplicationContext();
                            LayoutInflater inflater = _activity.getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText(msg.getData().getString(TOAST));
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            if (mBluetoothAdapter != null) {
                                if (mBluetoothAdapter.isDiscovering()) {
                                    mBluetoothAdapter.cancelDiscovery();
                                }
                            }


                        }
                        if (msg.getData().getString(TOAST).equals("Scale Disconnected")) {
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putString("scalec", "Scale Disconnected");
                            edit.commit();
                            Context context = _ctx.getApplicationContext();
                            LayoutInflater inflater = _activity.getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText(msg.getData().getString(TOAST));
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            if (mBluetoothAdapter != null) {
                                if (mBluetoothAdapter.isDiscovering()) {
                                    mBluetoothAdapter.cancelDiscovery();
                                }
                            }


                            txtScaleConn.setVisibility(View.GONE);
                            // SessionSave();

                        }
                        // mProcessDialog.show();
                        // Toast.makeText(_ctx.getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_LONG).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    sleep(1500);
                                    return;
                                } catch (InterruptedException localInterruptedException) {
                                    localInterruptedException.printStackTrace();
                                    return;
                                } finally {
                                    _activity.finish();

                                }
                            }
                        }
                                .start();
                        break;
                    case COMPLETE_FAILURE:
                        //Something is terribly wrong
                        Toast.makeText(_ctx.getApplicationContext(), "Something is terribly Wrong", Toast.LENGTH_SHORT).show();
                        _activity.finish();
                        break;
                }
            }
        };
    }

    private static final Messenger mMessenger = new Messenger(mHandler);
    // Name of the connected device
    private static String mConnectedDeviceName = null;
    private static Messenger mEasyWeighService;
    private static boolean mEasyWeighServiceBound;
    // Sets up communication with {@link EasyWeighService}
    private static final ServiceConnection scaleConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mEasyWeighServiceBound = true;

            Bundle myBundle = new Bundle();
            myBundle.putInt(DEVICE_TYPE, 1);

            Message msg = Message.obtain(null, EasyWeighService.MSG_REG_CLIENT);

            msg.setData(myBundle);

            msg.replyTo = mMessenger;

            mEasyWeighService = new Messenger(service);
            //mPrinterService = new Messenger(service);

            try {
                mEasyWeighService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to register client to service.");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mEasyWeighService = null;

            mEasyWeighServiceBound = false;
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) ==
                        BluetoothAdapter.STATE_ON) {
                    initialize();
                }
            }
        }
    };
    //AlertDialog.Builder dialogBuilder;
    AlertDialog.Builder dialogAccept;
    private Boolean dialogShownOnce = false;

    public static boolean allWeightsTheSame(double[] array) {
        if (array.length == 0) {
            return true;
        } else {
            double first = array[0];
            for (double element : array) {
                if (element != first) {
                    return false;
                }
            }
            return true;
        }
    }

    public static byte[] stringToBytesASCII(String str) {
        byte[] b = new byte[str.length()];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) str.charAt(i);
        }
        return b;
    }

    static String getReading(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length);
        for (int i = 0; i < data.length; ++i) {
            if (data[i] < 6) {
                throw new IllegalArgumentException();
            } else if (data[i] >= 46 && data[i] <= 57) {
                sb.append((char) data[i]); //I believe this is an accurate reading
            }
        }
        //return sb.toString();
        return sb.toString().replaceAll("I", "");
    }

    static String getReading(byte[] data, String message) {
        String returnValue = "";
        try {
            for (int i = 0; i < data.length; ++i) {
                if (message.length() >= 10 && message.length() <= 11) {
                    throw new IllegalArgumentException();
                } else if (data[i] >= 46 && data[i] <= 57) {
                    String[] parts = message.trim().split(",");

                    returnValue = parts[0];
                    if (parts.length == 2) {
                        tareWeight = Double.parseDouble(parts[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public static void initialize() {
        Log.d(TAG, "setup Service()");

        try {
            Intent intent = new Intent(ScaleEasyWeighActivity._ctx, EasyWeighService.class);
            //Intent printingIntent = new Intent(this,PrintingService.class);

            Bundle myBundle = new Bundle();
            Log.i(TAG, "Am Passing this address to Service " + cachedDeviceAddress);
            myBundle.putString(EXTRA_DEVICE_ADDRESS, cachedDeviceAddress);

            Log.i(TAG, "Scale Version " + mSharedPrefs.getString("scaleVersion", "EW15"));
            //get scale version
            if (mSharedPrefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15)) {
                myBundle.putString(SCALE_VERSION, EASYWEIGH_VERSION_15);
            } else {
                myBundle.putString(SCALE_VERSION, EASYWEIGH_VERSION_11);
            }

            intent.putExtras(myBundle); //add Bundle to intent

            _ctx.startService(intent);

            _ctx.bindService(intent, scaleConnection, Context.BIND_AUTO_CREATE);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static String getAddress() {
        return cachedDeviceAddress;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        firstWeighment = true; //ensure receipt no is not incremented on first weighment
        weighingSession = uuid.toString();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ScaleEasyWeighActivity.this);
        prefs = PreferenceManager.getDefaultSharedPreferences(ScaleEasyWeighActivity.this);

        stableReadingCounter = mSharedPrefs.getString("stabilityReadingCounter", "3");
        weighments = new double[Integer.parseInt(stableReadingCounter)];
        //send print request
        resetConn = new EasyWeighService();
        msg = Message.obtain(null, EasyWeighService.PRINT_REQUEST);


        try {
            setContentView(R.layout.activity_listemployee_weigh);
            _ctx = this;
            _activity = this;

            //first get scale version
            cachedDeviceAddress = pref.getString("address", "");
            mEasyWeighServiceBound = false;
            registerReceiver(mReceiver, initIntentFilter());

            if (mSharedPrefs.getString("weighingAlgorithm", "Incremental").equals(FILLING)) {
                // tvTareWeight.setText("0.0");
                //first connection so we send tare command
                mHandler.sendEmptyMessage(ScaleEasyWeighActivity.TARE_SCALE);
            } else {
                mHandler.sendEmptyMessage(ScaleEasyWeighActivity.ZERO_SCALE);
                //setTareWeight = Double.parseDouble(mSharedPrefs.getString("tareWeight", "1.2"));
                //tvWeighingTareWeigh.setText(String.valueOf(setTareWeight));
            }

            //weighmentCounts = mSharedPrefs.getInt("weighmentCounts", 0);
            weighmentCounts = 1;
            if (!mSharedPrefs.getBoolean("enablePrinting", false)) {
                // go back to milkers activity
                //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
            } else {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }


        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        setupToolbar();
        initializer();

    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Employee Weigh");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void initializer() {


        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();
        formatter = new DecimalFormat("0000");
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        font = Typeface.createFromAsset(getApplicationContext().getAssets(), "LCDM2B__.TTF");
        txtKgs = this.findViewById(R.id.txtKGS);
        txtKgs.setText("0.0");
        txtKgs.setVisibility(View.GONE);
        txtScaleConn = this.findViewById(R.id.txtScaleConn);
        ScaleConn = txtScaleConn.getText().toString();

        txtPrinterConn = this.findViewById(R.id.txtPrinterConn);
        btnReconnect = this.findViewById(R.id.btnReconnect);
        btnReconnect.setOnClickListener(v -> {

            //connect();
        });

        listEmployees = this.findViewById(R.id.listEmployees);
        listEmployees.setOnItemClickListener((parent, selectedView, arg2, arg3) -> {
            textAccountId = selectedView.findViewById(R.id.txtAccountId);
            textEmployee = selectedView.findViewById(R.id.tv_name);
            textEmployeeNo = selectedView.findViewById(R.id.tv_number);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("Employee", textEmployee.getText().toString());
            edit.putString("EmployeeNo", textEmployeeNo.getText().toString());
            edit.apply();
            Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
            Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
            //first get scale version

            myGross = Double.parseDouble(txtKgs.getText().toString());
            if (myGross > 0) {
                Context context = ScaleEasyWeighActivity.this;
                LayoutInflater inflater = ScaleEasyWeighActivity.this.getLayoutInflater();
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
            showWeighDialog();
        });


        if (!mSharedPrefs.getBoolean("enforcePickerNo", false)) {

            searchView = findViewById(R.id.searchView);
            searchView.requestFocus();
            searchView.setQueryHint("Search Employee No ...");
            if (!mSharedPrefs.getBoolean("enableAlphaNumeric", false)) {
                searchView.setInputType(InputType.TYPE_CLASS_PHONE);
            } else {
                searchView.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            }
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    ca.getFilter().filter(query);
                    ca.setFilterQueryProvider(constraint -> {
                        String EmployeeCode = constraint.toString();
                        return dbhelper.SearchSpecificEmployee(EmployeeCode);

                    });
                    // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    ca.getFilter().filter(newText);
                    ca.setFilterQueryProvider(constraint -> {
                        String EmployeeCode = constraint.toString();
                        return dbhelper.SearchEmployee(EmployeeCode);

                    });
                    //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                    return false;
                }
            });
        } else {
            searchView = findViewById(R.id.searchView);
            searchView.requestFocus();
            searchView.setQueryHint("Search Picker No ...");
            if (!mSharedPrefs.getBoolean("enableAlphaNumeric", false)) {
                searchView.setInputType(InputType.TYPE_CLASS_PHONE);
            } else {
                searchView.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            }
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    ca.getFilter().filter(query);
                    ca.setFilterQueryProvider(constraint -> {
                        String PickerNo = constraint.toString();
                        return dbhelper.SearchSpecificPicker(PickerNo);

                    });
                    // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    ca.getFilter().filter(newText);
                    ca.setFilterQueryProvider(constraint -> {
                        String PickerNo = constraint.toString();
                        return dbhelper.SearchEmployeePicker(PickerNo);

                    });
                    //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                    return false;
                }
            });
        }


    }

    public void getWeighdata() {

        Date date = new Date(getDate());
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
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
    }

    public void showWeighDialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView;
        if (mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {
            dialogView = inflater.inflate(R.layout.dialog_weigh_tea, null);
        } else {
            dialogView = inflater.inflate(R.layout.dialog_weigh, null);
        }
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        //dialogBuilder.setTitle("------- Weigh Produce -------");
        final SQLiteDatabase db = dbhelper.getReadableDatabase();

        spGrade = dialogView.findViewById(R.id.spGrade);
        spBlock = dialogView.findViewById(R.id.spBlock);
        BlockList();
        Grade();
        c_error = dialogView.findViewById(R.id.c_error);
        c_success = dialogView.findViewById(R.id.c_success);
        c_refresh = dialogView.findViewById(R.id.c_refresh);
        Glide.with(dialogView.getContext()).load(R.drawable.ic_refresh).into(c_refresh);

        tvError = dialogView.findViewById(R.id.tvError);
        tvStability = dialogView.findViewById(R.id.tvStability);

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
        tvBatchCrates = dialogView.findViewById(R.id.tvBatchCrates);
        getWeighdata();

        lt_accept = dialogView.findViewById(R.id.lt_accept);
        lt_nprint = dialogView.findViewById(R.id.lt_nprint);
        btn_accept = dialogView.findViewById(R.id.btn_accept);
        btn_next = dialogView.findViewById(R.id.btn_next);
        btn_next.setOnClickListener(v -> {
            myGross = Double.parseDouble(txtKgs.getText().toString());
            if (myGross > 0) {
                Context context = ScaleEasyWeighActivity.this;
                LayoutInflater inflater1 = ScaleEasyWeighActivity.this.getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Remove Load!\nTo Continue ...");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                return;
            }
            dialogShownOnce = false;
            btn_accept.setVisibility(View.VISIBLE);
            btn_next.setVisibility(View.GONE);
            c_refresh.setVisibility(View.GONE);
            c_success.setVisibility(View.GONE);
            c_error.setVisibility(View.GONE);
        });

        btn_print = dialogView.findViewById(R.id.btn_print);

        btn_accept.setOnClickListener(v -> {

            if (!mSharedPrefs.getBoolean("enableAdmin", false)) {
                String username = prefs.getString("user", "");

                Cursor d = dbhelper.getAccessLevel(username);
                String user_level = d.getString(0);

                if (user_level.equals("1")) {

                    Context context = getApplicationContext();
                    LayoutInflater inflater12 = getLayoutInflater();
                    View customToastroot = inflater12.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Sorry! Administrator Cannot Weigh\nPlease Login as a Clerk ...");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }

            }

            if (mSharedPrefs.getBoolean("enforceBlock", false)) {

                if (spBlock.getSelectedItem().equals("Select ...")) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater12 = getLayoutInflater();
                    View customToastroot = inflater12.inflate(R.layout.red_toast, null);
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

            if (mSharedPrefs.getBoolean("enforceGrade", false)) {
                if (spGrade.getSelectedItem().equals("Select ...")) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater12 = getLayoutInflater();
                    View customToastroot = inflater12.inflate(R.layout.red_toast, null);
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
                LayoutInflater inflater12 = getLayoutInflater();
                View customToastroot = inflater12.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Gross Total Cannot be 0.0, Please Request For Gross Reading");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();

                return;

            }

            if (mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {
            } else {

                netweight = Double.parseDouble(tvShowGrossTotal.getText().toString());
                if (netweight <= Double.parseDouble(mSharedPrefs.getString("minCRange", "0"))) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater12 = getLayoutInflater();
                    View customToastroot = inflater12.inflate(R.layout.red_toast, null);
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
                    LayoutInflater inflater12 = getLayoutInflater();
                    View customToastroot = inflater12.inflate(R.layout.red_toast, null);
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
            }

            if (!allWeightsTheSame(weighments)) {
                Log.i("WeightCount", "Not Equal:" + Arrays.toString(weighments));
                tvStability.setVisibility(View.VISIBLE);
                // tvStability.setText("UnStable:"+  Arrays.toString(weighments));

                return;

            } else {
                Log.i("WeightCount", "Readings Equal:" + Arrays.toString(weighments));
                Context context = _activity;
                LayoutInflater inflater12 = _activity.getLayoutInflater();
                View customToastroot = inflater12.inflate(R.layout.blue_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Reading Stable:" + Arrays.toString(weighments));
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                //customtoast.show();
                tvStability.setVisibility(View.GONE);
            }

            EmployeeNo = prefs.getString("EmployeeNo", "");
            Date date = new Date(getDate());
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            ColDate = format1.format(date);

            dialogAccept = new AlertDialog.Builder(v.getContext());
            // Setting Dialog Title
            LayoutInflater inflater12 = getLayoutInflater();
            final View dialogView1;
            if (mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {
                dialogView1 = inflater12.inflate(R.layout.dialog_grossweight_tea, null);
            } else {
                dialogView1 = inflater12.inflate(R.layout.dialog_grossweight, null);
            }
            dialogAccept.setView(dialogView1);
            dialogAccept.setCancelable(false);
            dialogAccept.setTitle("Accept Reading?");

            // Setting Dialog Message
            //dialogBuilder.setMessage("Are you sure you want to accept the gross reading?");
            if (mSharedPrefs.getString("cMode", "HT").equals("HT")) {

                tvGross = dialogView1.findViewById(R.id.txtGross);
                tvGross.setTypeface(font);

                tvUnitsCount = dialogView1.findViewById(R.id.tvUnitsCount);
                tvUnitsCount.setTypeface(font);

                trCrates = dialogView1.findViewById(R.id.trCrates);
                trCrates.setOnClickListener(v1 -> showCratesDialog());

                tvWeighingTareWeigh = dialogView1.findViewById(R.id.tvWeighingTareWeigh);
                tvWeighingTareWeigh.setTypeface(font);

                tvShowTotalKgs = dialogView1.findViewById(R.id.tvShowTotalKgs);
                tvShowTotalKgs.setTypeface(font);


                tvAvgCWeight = dialogView1.findViewById(R.id.tvAvgCWeight);
                tvAvgCWeight.setTypeface(font);


                tvGross.setText(tvShowGrossTotal.getText().toString());


                if (tvShowGrossTotal.getText().equals("0.0")) {

                    tvGross.setText("0 KG");

                }


                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("Gross", tvShowGrossTotal.getText().toString());
                edit.putString("Crates", tvUnitsCount.getText().toString());
                edit.putString("Net", tvShowTotalKgs.getText().toString());
                edit.putString("Tare", tvWeighingTareWeigh.getText().toString());
                edit.apply();
                tvWeighingTareWeigh.setText(prefs.getString("Tare", "0.0"));


                final DecimalFormat df = new DecimalFormat("#0.0#");

                double grossValue = 0.0;
                double tare = 0.0;
                grossValue = Double.parseDouble(tvGross.getText().toString());
                if (grossValue >= Double.parseDouble(mSharedPrefs.getString("minCRange", "18"))) {
                    double cratesno = grossValue / Double.parseDouble(mSharedPrefs.getString("minCRange", "18"));
                    int crates = (int) cratesno;
                    double avgCWeight = grossValue / crates;
                    tare = crates * setTareWeight;
                    //tare=setTareWeight;

                    // tvWeighingTareWeigh.setText(df.format(tare));
                    tvWeighingTareWeigh.setText(prefs.getString("Tare", "0.0"));


                    tvUnitsCount.setText(String.valueOf(crates));

                    double NetTotal = 0.0;
                    NetTotal = grossValue - tare;
                    tvShowTotalKgs.setText(df.format(NetTotal));
                    double truncatedDouble = new BigDecimal(avgCWeight).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                    tvAvgCWeight.setText(df.format(truncatedDouble));
                    tvWeighingTareWeigh.setText(df.format(totalMoisture + tareWeight));

                    tvNet = dialogView1.findViewById(R.id.tvNet);
                    tvNet.setTypeface(font);
                    tvNet.setText(df.format(netWeight));

                    tvTare = dialogView1.findViewById(R.id.tvTare);
                    tvTare.setTypeface(font);
                    tvTare.setText(df.format(totalMoisture + tareWeight));
                }


            } else {

                tvGross = dialogView1.findViewById(R.id.txtGross);
                tvGross.setTypeface(font);
                tvGross.setText(tvShowGrossTotal.getText().toString());

                final DecimalFormat df = new DecimalFormat("#0.0#");

                double grossValue = 0.0;
                grossValue = Double.parseDouble(tvGross.getText().toString());

                tvShowTotalKgs.setText(df.format(grossValue));

                if (tvShowGrossTotal.getText().equals("0.0")) {

                    tvGross.setText("0 KG");

                }
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("Gross", tvShowGrossTotal.getText().toString());
                edit.putString("Crates", tvUnitsCount.getText().toString());
                edit.putString("Net", tvShowTotalKgs.getText().toString());
                edit.putString("Tare", tvWeighingTareWeigh.getText().toString());
                edit.apply();

            }

            ///Setting Negative "Yes" Button
            dialogAccept.setNegativeButton("YES",
                    (dialog, which) -> {
                    });
            // Setting Positive "NO" Button
            dialogAccept.setPositiveButton("NO",
                    (dialog, which) -> {
                        dialogShownOnce = false;

                    });
            // Showing Alert Message
            dialog_accept = dialogAccept.create();
            if (!dialog_accept.isShowing() && !dialogShownOnce) {
                dialog_accept.show();
                dialogShownOnce = true;

                dialog_accept.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog_accept.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
                dialog_accept.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.BLUE);
                dialog_accept.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v12 -> {
                    if (mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {


                    } else {
                        double avgW = Double.parseDouble(tvAvgCWeight.getText().toString());
                        double minW = Double.parseDouble(mSharedPrefs.getString("minCRange", "18.0"));
                        double maxW = Double.parseDouble(mSharedPrefs.getString("maxCRange", "20.0"));
                        if (minW > avgW) {
                            Context context = getApplicationContext();
                            LayoutInflater inflater121 = getLayoutInflater();
                            View customToastroot = inflater121.inflate(R.layout.red_toast, null);
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
                            LayoutInflater inflater121 = getLayoutInflater();
                            View customToastroot = inflater121.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Unacceptable Average Crate Weight! should be less than " + mSharedPrefs.getString("maxCRange", "18"));
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            return;
                        }
                    }

                    dbhelper = new DBHelper(getApplicationContext());
                    SQLiteDatabase db1 = dbhelper.getReadableDatabase();
                    formatter = new DecimalFormat("0000");
                    formatInt = new DecimalFormat("000");

                    BatchSerial = prefs.getString("DeliverNoteNumber", "");

                    Calendar cal = Calendar.getInstance();
                    Date date1 = new Date(getDate());
                    SimpleDateFormat format11 = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                    ColDate = format11.format(date1);
                    Time = format2.format(cal.getTime());
                    FieldClerk = prefs.getString("user", "");
                    EmployeeNo = prefs.getString("EmployeeNo", "");
                    TaskCode = prefs.getString("taskCode", "");
                    // TaskType = prefs.getString("taskType", "2");
                    TaskType = "2";
                    ProduceCode = prefs.getString("produceCode", "");
                    VarietyCode = prefs.getString("varietyCode", "");
               /* if (spGrade.getSelectedItem().equals("Select ...")) {

                    GradeCode="";
                }else{

                    GradeCode=gradeid;
                }*/

                    GradeCode = prefs.getString("gradeCode", "");

                    Estate = prefs.getString("estateCode", "");
                    Division = prefs.getString("divisionCode", " ");
                    Field = prefs.getString("fieldCode", "");
                    if (Field.equals("Select ...")) {
                        Field = "";
                    } else {
                        Field = prefs.getString("fieldCode", "");
                    }
                    Block = spBlock.getSelectedItem().toString();
                    if (Block.equals("Select ...")) {
                        Block = "";
                    } else {
                        Block = spBlock.getSelectedItem().toString();
                    }

                    //NetWeight=prefs.getString("Net", "");


                    if (mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {

                        Crates = tvUnitsCount.getText().toString();
                        NetWeight = prefs.getString("Net", "0.0");
                        TareWeight = prefs.getString("Tare", "0.0");

                    } else {
                        Crates = tvUnitsCount.getText().toString();
                        NetWeight = tvNet.getText().toString();
                        TareWeight = tvTare.getText().toString();

                    }
                    UnitPrice = "0";


                    BatchCrates = tvUnitsCount.getText().toString();


                    Cursor trans = db1.rawQuery("select * from EmployeeProduceCollection where " + Database.DataCaptureDevice + " = '" + BatchSerial + "'", null);
                    if (trans.getCount() > 0) {
                        Cursor trans1 = db1.rawQuery("select MAX(ReceiptNo) from EmployeeProduceCollection where " + Database.DataCaptureDevice + " = '" + BatchSerial + "'", null);
                        if (trans1 != null) {

                            trans1.moveToFirst();

                            RecNo = Integer.parseInt(trans1.getString(0)) + 1;
                            RecieptNo = formatter.format(RecNo);

                        }
                        trans1.close();
                    } else {
                        RecieptNo = formatter.format(RecNo);


                    }
                    Cursor transb = db1.rawQuery("select * from EmployeeProduceCollection where "
                            + Database.EmployeeNo + " = '" + EmployeeNo + "' and "
                            + Database.CollDate + " = '" + ColDate + "'", null);
                    if (transb.getCount() > 0) {
                        Cursor trans2 = db1.rawQuery("select MAX(LoadCount) from EmployeeProduceCollection where "
                                + Database.EmployeeNo + " = '" + EmployeeNo + "' and "
                                + Database.CollDate + " = '" + ColDate + "'", null);

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
                    Cursor c1 = db1.rawQuery("select _id,BatchCrates from FarmersSuppliesConsignments where "
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
                    long rows = db1.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
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

                    } else {
                        CheckinMethod = "3";
                    }


                    dbhelper.AddEmployeeTrans(ColDate, Time, BatchSerial, BatchNo, EmployeeNo,
                            FieldClerk, TaskCode, TaskType, ProduceCode,
                            VarietyCode, GradeCode, Estate, Division, Field, Block,
                            NetWeight, TareWeight, Crates,
                            UnitPrice, RecieptNo, WeighmentNo, CheckinMethod);
                    dialogShownOnce = false;
                    if (mSharedPrefs.getBoolean("realtimeServices", false)) {
                        // Method to Send Weighments to Cloud.
                        new WeighmentsToCloud().execute();
                    }
                    Context context = getApplicationContext();
                    LayoutInflater inflater121 = getLayoutInflater();
                    View customToastroot = inflater121.inflate(R.layout.white_red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    // text.setText("Saved Successfully: " + NetWeight + " Kgs\n" + "Crates: " + Crates + "");
                    text.setText("Saved Successfully: " + NetWeight + " Kgs\nBags: " + Crates + "\nTare: " + TareWeight);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    dialog_accept.dismiss();

                    if (!mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {
                        weigh.dismiss();
                    }
                    getWeighdata();
                    //weigh.dismiss();
                    btn_accept.setVisibility(View.GONE);
                    btn_next.setVisibility(View.VISIBLE);

                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                });
            }

        });

        dialogBuilder.setNegativeButton("Back", (dialog, whichButton) -> {
        });
        dialogBuilder.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        weigh = dialogBuilder.create();
        weigh.show();
        weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);
        weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> exitWeighingWindow());


    }

    public void exitWeighingWindow() {
        dialogShownOnce = false;
        if (weigh.isShowing()) {
            weigh.dismiss();
        }
        if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {
            finish();
            mIntent = new Intent(ScaleEasyWeighActivity.this, CardWeighActivity.class);
            startActivity(mIntent);

        } else if (mSharedPrefs.getString("vModes", "Both").equals(BOTH)) {
            finish();
            mIntent = new Intent(ScaleEasyWeighActivity.this, BothCardWeighActivity.class);
            startActivity(mIntent);
        }
    }

    public void showMachineWeighDialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView;
        dialogView = inflater.inflate(R.layout.dialog_weigh_machine, null);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        //dialogBuilder.setTitle("------- Weigh Produce -------");


        c_error = dialogView.findViewById(R.id.c_error);
        c_success = dialogView.findViewById(R.id.c_success);
        c_refresh = dialogView.findViewById(R.id.c_refresh);
        Glide.with(dialogView.getContext()).load(R.drawable.ic_refresh).into(c_refresh);

        tvError = dialogView.findViewById(R.id.tvError);
        tvStability = dialogView.findViewById(R.id.tvStability);

        tvShowMemberNo = dialogView.findViewById(R.id.tvMemberNo);
        tvShowMemberNo.setText(prefs.getString("MachineNo", ""));

        tvMemberName = dialogView.findViewById(R.id.tvMemberNameShow);


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
        tvBatchCrates = dialogView.findViewById(R.id.tvBatchCrates);
        getWeighdata();


        lt_accept = dialogView.findViewById(R.id.lt_accept);
        lt_nprint = dialogView.findViewById(R.id.lt_nprint);
        btn_accept = dialogView.findViewById(R.id.btn_accept);
        btn_next = dialogView.findViewById(R.id.btn_next);
        btn_next.setOnClickListener(v -> {
            myGross = Double.parseDouble(txtKgs.getText().toString());
            if (myGross > 0) {
                Context context = ScaleEasyWeighActivity.this;
                LayoutInflater inflater12 = ScaleEasyWeighActivity.this.getLayoutInflater();
                View customToastroot = inflater12.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Remove Load!\nTo Continue ...");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                return;
            }
            dialogShownOnce = false;
            btn_accept.setVisibility(View.VISIBLE);
            btn_next.setVisibility(View.GONE);
            c_refresh.setVisibility(View.GONE);
            c_success.setVisibility(View.GONE);
            c_error.setVisibility(View.GONE);
        });

        btn_print = dialogView.findViewById(R.id.btn_print);

        btn_accept.setOnClickListener(v -> {

            if (!mSharedPrefs.getBoolean("enableAdmin", false)) {
                String username = prefs.getString("user", "");

                Cursor d = dbhelper.getAccessLevel(username);
                String user_level = d.getString(0);

                if (user_level.equals("1")) {

                    Context context = getApplicationContext();
                    LayoutInflater inflater1 = getLayoutInflater();
                    View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Sorry! Administrator Cannot Weigh\nPlease Login as a Clerk ...");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }

            }


            if (tvShowGrossTotal.getText().equals("0.0")) {
                Context context = getApplicationContext();
                LayoutInflater inflater1 = getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Gross Total Cannot be 0.0, Please Request For Gross Reading");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();

                return;

            }


            if (!allWeightsTheSame(weighments)) {
                Log.i("WeightCount", "Not Equal:" + Arrays.toString(weighments));
                tvStability.setVisibility(View.VISIBLE);
                // tvStability.setText("UnStable:"+  Arrays.toString(weighments));

                return;

            } else {
                Log.i("WeightCount", "Readings Equal:" + Arrays.toString(weighments));
                Context context = _activity;
                LayoutInflater inflater1 = _activity.getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.blue_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Reading Stable:" + Arrays.toString(weighments));
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                //customtoast.show();
                tvStability.setVisibility(View.GONE);
            }


            dialogAccept = new AlertDialog.Builder(v.getContext());
            final View dialogView1;
            dialogView1 = getLayoutInflater().inflate(R.layout.dialog_grossweight_tea, null);
            dialogAccept.setView(dialogView1);
            dialogAccept.setCancelable(false);
            dialogAccept.setTitle("Accept Reading?");
            // Setting Dialog Message
            //dialogBuilder.setMessage("Are you sure you want to accept the gross reading?");


            tvGross = dialogView1.findViewById(R.id.txtGross);
            tvGross.setTypeface(font);
            tvGross.setText(tvShowGrossTotal.getText().toString());

            final DecimalFormat df = new DecimalFormat("#0.0#");

            Double grossValue = 0.0;
            grossValue = Double.parseDouble(tvGross.getText().toString());

            tvShowTotalKgs.setText(df.format(grossValue));
            if (tvShowGrossTotal.getText().equals("0.0")) {

                tvGross.setText("0 KG");

            }
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("Gross", tvShowGrossTotal.getText().toString());
            edit.putString("Crates", tvUnitsCount.getText().toString());
            edit.putString("Net", tvShowTotalKgs.getText().toString());
            edit.putString("Tare", tvWeighingTareWeigh.getText().toString());
            edit.apply();

            ///Setting Negative "Yes" Button
            dialogAccept.setNegativeButton("YES",
                    (dialog, which) -> {
                    });
            // Setting Positive "NO" Button
            dialogAccept.setPositiveButton("NO",
                    (dialog, which) -> {
                        dialogShownOnce = false;

                    });
            // Showing Alert Message
            dialog_accept = dialogAccept.create();
            if (!dialog_accept.isShowing() && !dialogShownOnce) {
                dialog_accept.show();
                dialogShownOnce = true;

                dialog_accept.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog_accept.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
                dialog_accept.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.BLUE);
                dialog_accept.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v1 -> {

                    dbhelper = new DBHelper(getApplicationContext());
                    SQLiteDatabase db = dbhelper.getReadableDatabase();
                    formatter = new DecimalFormat("0000");
                    formatInt = new DecimalFormat("000");

                    BatchSerial = prefs.getString("DeliverNoteNumber", "");
                    Calendar cal = Calendar.getInstance();
                    Date date = new Date(getDate());
                    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                    ColDate = format1.format(date);
                    Time = format2.format(cal.getTime());
                    FieldClerk = prefs.getString("user", "");
                    EmployeeNo = prefs.getString("MachineNo", "");
                    TaskCode = prefs.getString("taskCode", "");
                    TaskType = "5";
                    ProduceCode = prefs.getString("produceCode", "");
                    VarietyCode = prefs.getString("varietyCode", "");
                    GradeCode = prefs.getString("gradeCode", "");
                    Estate = prefs.getString("estateCode", "");
                    Division = prefs.getString("divisionCode", " ");
                    Field = prefs.getString("fieldCode", "");
                    if (Field.equals("Select ...")) {
                        Field = "";
                    } else {
                        Field = prefs.getString("fieldCode", "");
                    }

                    Block = "";

                    Crates = tvUnitsCount.getText().toString();
                    NetWeight = prefs.getString("Net", "");
                    TareWeight = prefs.getString("Tare", "");

                    UnitPrice = "0";


                    BatchCrates = tvUnitsCount.getText().toString();


                    Cursor trans = db.rawQuery("select * from EmployeeProduceCollection where " + Database.DataCaptureDevice + " = '" + BatchSerial + "'", null);
                    if (trans.getCount() > 0) {
                        Cursor trans1 = db.rawQuery("select MAX(ReceiptNo) from EmployeeProduceCollection where " + Database.DataCaptureDevice + " = '" + BatchSerial + "'", null);
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
                            + Database.CollDate + " = '" + ColDate + "'", null);
                    if (transb.getCount() > 0) {
                        Cursor trans2 = db.rawQuery("select MAX(LoadCount) from EmployeeProduceCollection where "
                                + Database.EmployeeNo + " = '" + EmployeeNo + "' and "
                                + Database.CollDate + " = '" + ColDate + "'", null);

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

                    } else {
                        CheckinMethod = "3";
                    }


                    dbhelper.AddEmployeeTrans(ColDate, Time, BatchSerial, BatchNo, EmployeeNo,
                            FieldClerk, TaskCode, TaskType, ProduceCode,
                            VarietyCode, GradeCode, Estate, Division, Field, Block,
                            NetWeight, TareWeight, Crates,
                            UnitPrice, RecieptNo, WeighmentNo, CheckinMethod);
                    dialogShownOnce = false;
                    if (mSharedPrefs.getBoolean("realtimeServices", false)) {
                        // Method to Send Weighments to Cloud.
                        new WeighmentsToCloud().execute();

                    }
                    Context context = getApplicationContext();
                    LayoutInflater inflater11 = getLayoutInflater();
                    View customToastroot = inflater11.inflate(R.layout.white_red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    // text.setText("Saved Successfully: " + NetWeight + " Kgs\n" + "Crates: " + Crates + "");
                    text.setText("Saved Successfully: " + NetWeight + " Kgs\nBags: " + Crates + "\nTare: " + TareWeight);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    dialog_accept.dismiss();

                    getWeighdata();
                    //weigh.dismiss();
                    btn_accept.setVisibility(View.GONE);
                    btn_next.setVisibility(View.VISIBLE);

                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (!mSharedPrefs.getString("cMode", "Tea").equals("Tea")) {

                        weigh.dismiss();
                    }
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                });
            }

        });


        dialogBuilder.setNegativeButton("Back", (dialog, whichButton) -> {
            //pass
            finish();

        });
        dialogBuilder.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        weigh = dialogBuilder.create();
        weigh.show();
        weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);
        weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            weigh.dismiss();
            finish();
        });


    }


    private void BlockList() {
        blockdata.clear();
        Field = prefs.getString("fieldCode", "");
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select bkID,bkFiled from blocks where bkFiled='" + Field + "'", null);
        blockdata.add("Select ...");
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

        blockadapter = new ArrayAdapter<>(ScaleEasyWeighActivity.this, R.layout.spinner_item_min, blockdata);
        blockadapter.setDropDownViewResource(R.layout.spinner_item_min);
        blockadapter.notifyDataSetChanged();
        spBlock.setAdapter(blockadapter);
        spBlock.setSelection(prefs.getInt("spSelection", 0));
        spBlock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String blockName = parent.getItemAtPosition(position).toString();
                SharedPreferences.Editor edit = prefs.edit();
                edit.putInt("spSelection", position);
                edit.commit();
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
        Cursor c = db.rawQuery("select pgdRef,pgdName from ProduceGrades where pgdProduce= '" + ProduceCode + "'", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    grade = c.getString(c.getColumnIndex("pgdName"));
                    gradedata.add(grade);

                } while (c.moveToNext());
            }
        }


        gradeadapter = new ArrayAdapter<>(this, R.layout.spinner_item_min, gradedata);
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
                edit.apply();

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
        dialogBasedate.setPositiveButton("Update", (dialog, whichButton) -> {
            //do something with edt.getText().toString();


        });

        final AlertDialog bc = dialogBasedate.create();
        bc.show();
        bc.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int crate = Integer.parseInt(edtCrates.getText().toString());
            if (crate == 0) {
                Toast.makeText(getApplicationContext(), "Please a Valid Crate Number!", Toast.LENGTH_LONG).show();
                return;
            }
            tvUnitsCount.setText(edtCrates.getText().toString());

            final DecimalFormat df = new DecimalFormat("#0.0#");
            double grossValue = 0.0;
            double tare = 0.0;
            double NetTotal = 0.0;

            grossValue = Double.parseDouble(tvGross.getText().toString());
            int crates = Integer.parseInt(edtCrates.getText().toString());
            double avgCWeight = grossValue / crates;
            //tare = crates * setTareWeight;
            tare = setTareWeight;
            NetTotal = grossValue - tare;

            tvWeighingTareWeigh.setText(df.format(tare));
            tvShowTotalKgs.setText(df.format(NetTotal));
            Double truncatedDouble = new BigDecimal(avgCWeight).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            tvAvgCWeight.setText(df.format(truncatedDouble));

            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("Gross", tvShowGrossTotal.getText().toString());
            edit.putString("Crates", tvUnitsCount.getText().toString());
            edit.putString("Net", tvShowTotalKgs.getText().toString());
            edit.putString("Tare", tvWeighingTareWeigh.getText().toString());
            edit.apply();
            bc.dismiss();
            //Do stuff, possibly set wantToCloseDialog to true then...
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove("spSelection");
        edit.apply();
        if (mSharedPrefs.getString("vModes", "FingerPrint").equals(FINGERPRINT)) {
            getEmployee();
            searchView.setVisibility(View.GONE);
            if (EasyWeighService.mState != EasyWeighService.STATE_CONNECTED) {
                mProcessDialog = new ProgressDialog(this);
                mProcessDialog.setTitle("Please Wait");
                mProcessDialog.setMessage("Attempting Connection to Scale ...");
                mProcessDialog.setCancelable(false);
                mProcessDialog.show();
            }

        } else if (mSharedPrefs.getString("vModes", "Card").equals(CARD)) {
            getEmployee();
            searchView.setVisibility(View.GONE);
            if (EasyWeighService.mState != EasyWeighService.STATE_CONNECTED) {
                mProcessDialog = new ProgressDialog(this);
                mProcessDialog.setTitle("Please Wait");
                mProcessDialog.setMessage("Attempting Connection to Scale ...");
                mProcessDialog.setCancelable(false);
                mProcessDialog.show();
            }

        } else if (mSharedPrefs.getString("vModes", "Both").equals(BOTH)) {
            getEmployee();
            searchView.setVisibility(View.GONE);
            if (EasyWeighService.mState != EasyWeighService.STATE_CONNECTED) {
                mProcessDialog = new ProgressDialog(this);
                mProcessDialog.setTitle("Please Wait");
                mProcessDialog.setMessage("Attempting Connection to Scale ...");
                mProcessDialog.setCancelable(false);
                mProcessDialog.show();
            }

        } else if (mSharedPrefs.getString("vModes", "Manual").equals(MANUAL)) {
            if (prefs.getString("taskType", "1").equals("5")) {
                searchView.setVisibility(View.GONE);
                showMachineWeighDialog();
            } else {
                getdata();
            }
            if (EasyWeighService.mState != EasyWeighService.STATE_CONNECTED) {
                mProcessDialog = new ProgressDialog(this);
                mProcessDialog.setTitle("Please Wait");
                mProcessDialog.setMessage("Attempting Connection to Scale ...");
                mProcessDialog.setCancelable(false);
                mProcessDialog.show();
            }

        } else {
            getdata();
            if (EasyWeighService.mState != EasyWeighService.STATE_CONNECTED) {
                mProcessDialog = new ProgressDialog(this);
                mProcessDialog.setTitle("Please Wait");
                mProcessDialog.setMessage("Attempting Connection to Scale ...");
                mProcessDialog.setCancelable(false);
                mProcessDialog.show();
            }
        }

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


            listEmployees.setAdapter(ca);
            listEmployees.setTextFilterEnabled(true);
            //dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void getEmployee() {
        try {
            EmployeeNo = prefs.getString("FpEmployeeNo", "");
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, Database.EM_ID + "='" + EmployeeNo + "'", null, null, null, null, null, null);
            if (accounts.getCount() == 0) {
                Toast.makeText(this, "no records", Toast.LENGTH_LONG).show();
            }
            String[] from = {Database.ROW_ID, Database.EM_ID, Database.EM_NAME, Database.EM_PICKERNO};
            int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_name, R.id.tv_pickerno};


            ca = new SimpleCursorAdapter(this, R.layout.employee_list, accounts, from, to);

            listEmployees.setAdapter(ca);
            listEmployees.setTextFilterEnabled(true);
            listEmployees.performItemClick(listEmployees.getAdapter().getView(0, null, null), 0, listEmployees.getAdapter().getItemId(0));
            //dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (!mSharedPrefs.getBoolean("enablePrinting", false)) {
            // go back to milkers activity
            //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
        } else {
            if (mConnector != null) {
                txtPrinterConn.setVisibility(View.VISIBLE);

            }
        }

        sameFarmer = false;

        //setTareWeight = 0.0;
        setTareWeight = Double.parseDouble(mSharedPrefs.getString("tareWeight", "0"));
        stopRefreshing = false;
        setMoisture = Double.parseDouble(mSharedPrefs.getString("moisture", "0.0"));
        if (mEasyWeighServiceBound) {
            Message msg = Message.obtain(null, EasyWeighService.READING_PROBE);
            Bundle b = new Bundle();

            try {
                weighingSession = uuid.toString();
                weighmentCounts = 1;

                firstWeighment = false;
                stopRefreshing = false;

                msg.setData(b);
                mEasyWeighService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                weighmentCounts = 1;

                tvUnitsCount.setText(String.valueOf(weighmentCounts));
            } catch (Exception e) {
                e.printStackTrace();


            }
        } else {
            initialize();
        }

    }

    // Intent filter and broadcast receive to handle Bluetooth on event.
    private IntentFilter initIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    @Override
    public synchronized void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind from WeighingActivity service and Unregister receiver
        try {
            if (mEasyWeighServiceBound) unbindService(scaleConnection);
            unregisterReceiver(mReceiver);
            if (mProcessDialog != null && mProcessDialog.isShowing()) {
                mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ScaleEasyWeighActivity.this);
                    cachedDeviceAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    cachedDeviceAddress = pref.getString("address", "");
                    if (cachedDeviceAddress == null) {
                        Toast.makeText(getBaseContext(), "Please select scale....", Toast.LENGTH_LONG).show();
                        // finish();
                    } else {
                        //Send Message to Servive with new address

                        Message msg = Message.obtain(null, EasyWeighService.RETRY);
                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_DEVICE_ADDRESS, cachedDeviceAddress);

                        try {
                            msg.setData(bundle);
                            mEasyWeighService.send(msg);
                        } catch (RemoteException e) {
                            Log.w(TAG, "Unable to register client to service.");
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                try {
                    // When the request to enable Bluetooth returns
                    if (resultCode == Activity.RESULT_OK) {
                        // Bluetooth is now enabled, so set up a chat session
                        //initialize();

                        //	cachedDeviceAddress = mDbHelper.getLastUsedDevice();

                        Message msg = Message.obtain(null, EasyWeighService.RETRY);
                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_DEVICE_ADDRESS, cachedDeviceAddress);

                        if (cachedDeviceAddress != null || !cachedDeviceAddress.isEmpty()) { //first check we have an address
                            try {
                                mProcessDialog.setMessage("Attempting Connection ...");
                                mProcessDialog.show();
                                msg.setData(bundle);
                                mEasyWeighService.send(msg);
                            } catch (RemoteException e) {
                                Log.w(TAG, "Unable to register client to service.");
                                e.printStackTrace();
                            }
                        } else { //Try Each and every other address in DB
                            mProcessDialog.setMessage("Unable to connect to Default Device");
                            ScaleEasyWeighActivity.this.finish();
                            Intent intentDeviceList = new Intent(getApplicationContext(), DeviceListActivity.class);
                            startActivityForResult(intentDeviceList, 1);
                        }

                    } else {
                        // User did not enable Bluetooth or an error occurred
                        Log.d(TAG, "BT not enabled");
                        Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                        //mProcessDialog.dismiss();
                        finish();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "After Request BT " + e);
                }

                break;
        }
    }



    @Override
    public void onBackPressed() {
        //do nothing
        finish();
        // resetConn.stop();
        super.onBackPressed();
    }

    private String getDate() {
        //Return the current date
        return DateFormat.getDateInstance().format(new Date());
    }

    public class WeighmentsToCloud extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            c_refresh.setVisibility(View.VISIBLE);
            c_success.setVisibility(View.GONE);
            c_error.setVisibility(View.GONE);

        }

        @Override
        protected String doInBackground(String... aurl) {
            Log.i(TAG, "doInBackground");
            try {
                db = dbhelper.getReadableDatabase();
                serverBatchNo = prefs.getString("serverBatchNo", "");
                BatchSerial = prefs.getString("DeliverNoteNumber", "");
                produce = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                        + Database.CollDate + " ='" + ColDate + "' and " + Database.DataCaptureDevice + " ='" + BatchSerial + "'  and " + Database.CloudID + "<='" + cloudid + "'", null);
                count = produce.getCount();
                if (count > 0) {
                    //weighments.moveToFirst();
                    count = count + produce.getCount();
                    while (produce.moveToNext()) {
                        ColDate = produce.getString(produce.getColumnIndex(Database.CollDate));
                        Time = produce.getString(produce.getColumnIndex(Database.CaptureTime));
                        BatchNo = produce.getString(produce.getColumnIndex(Database.BatchNo));
                        DataDevice = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
                        TaskCode = produce.getString(produce.getColumnIndex(Database.TaskCode));
                        EmployeeNo = produce.getString(produce.getColumnIndex(Database.EmployeeNo));
                        ProduceCode = produce.getString(produce.getColumnIndex(Database.DeliveredProduce));


                        if (produce.getString(produce.getColumnIndex(Database.ProduceVariety)) == null) {
                            VarietyCode = "";
                        } else {
                            VarietyCode = produce.getString(produce.getColumnIndex(Database.ProduceVariety));
                        }
                        if (produce.getString(produce.getColumnIndex(Database.ProduceGrade)) == null) {
                            GradeCode = "";
                        } else {

                            GradeCode = produce.getString(produce.getColumnIndex(Database.ProduceGrade));
                        }

                        EstateCode = produce.getString(produce.getColumnIndex(Database.SourceEstate));
                        DivisionCode = produce.getString(produce.getColumnIndex(Database.SourceDivision));
                        FieldCode = produce.getString(produce.getColumnIndex(Database.SourceField));
                        if (FieldCode.equals("Select ...") || FieldCode.equals("")) {
                            FieldCode = "";
                        } else {
                            FieldCode = produce.getString(produce.getColumnIndex(Database.SourceField));
                        }
                        if (produce.getString(produce.getColumnIndex(Database.SourceBlock)) != null) {
                            Block = produce.getString(produce.getColumnIndex(Database.SourceBlock));
                            if (Block.equals("Select ...")) {
                                Block = "";
                            } else {
                                Block = produce.getString(produce.getColumnIndex(Database.SourceBlock));
                            }
                        } else {
                            Block = "";
                        }

                        NetWeight = produce.getString(produce.getColumnIndex(Database.NetWeight));
                        TareWeight = produce.getString(produce.getColumnIndex(Database.Tareweight));

                        if (produce.getString(produce.getColumnIndex(Database.BagCount)) == null) {
                            Crates = "1";

                        } else {
                            Crates = produce.getString(produce.getColumnIndex(Database.BagCount));
                        }

                        UnitPrice = produce.getString(produce.getColumnIndex(Database.UnitPrice));
                        WeighmentNo = produce.getString(produce.getColumnIndex(Database.LoadCount));
                        RecieptNo = produce.getString(produce.getColumnIndex(Database.DataCaptureDevice)) + produce.getString(produce.getColumnIndex(Database.ReceiptNo));
                        SessionNo = produce.getString(produce.getColumnIndex(Database.ReceiptNo));
                        FieldClerk = produce.getString(produce.getColumnIndex(Database.FieldClerk));
                        CheckinMethod = produce.getString(produce.getColumnIndex(Database.UsedSmartCard));

                        Co_prefix = mSharedPrefs.getString("company_prefix", "");
                        Current_User = prefs.getString("user", "");
                        TaskType = produce.getString(produce.getColumnIndex(Database.TaskType));


                        StringBuilder wm = new StringBuilder();
                        wm.append(TaskType + ",");
                        wm.append(ColDate + ",");
                        wm.append(DataDevice + ",");
                        wm.append(Time + ",");
                        wm.append(FieldClerk + ",");
                        wm.append(ProduceCode + ",");
                        wm.append(EstateCode + ",");
                        wm.append(DivisionCode + ",");
                        wm.append(FieldCode + ",");
                        wm.append(Block + ",");
                        wm.append(TaskCode + ",");
                        wm.append(EmployeeNo + ",");
                        wm.append(NetWeight + ",");
                        wm.append(TareWeight + ",");
                        wm.append(Crates + ",");
                        wm.append(RecieptNo + ",");
                        wm.append(BatchNo + ",");
                        wm.append(WeighmentNo + ",");
                        wm.append(VarietyCode + ",");
                        wm.append(GradeCode + ",");
                        wm.append(Co_prefix + ",");
                        wm.append(Current_User + ",");
                        wm.append(CheckinMethod + ",");
                        wm.append("4");

                        weighmentInfo = wm.toString();

                        try {


                            restApiResponse = new RestApiRequest(getApplicationContext()).postWeighment(serverBatchNo, weighmentInfo);
                            JSONObject jsonObject = new JSONObject(restApiResponse);

                            Id = jsonObject.getString("Id");
                            Title = jsonObject.getString("Title");
                            sMessage = jsonObject.getString("Message");

                            Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + sMessage);

                            if (Integer.parseInt(Id) > 0) {
                                Cursor checkcloudid = dbhelper.CheckWeighmentCloudID(Id);
                                //Check for duplicate checkcloudid number
                                if (checkcloudid.getCount() > 0) {
                                    // Toast.makeText(getApplicationContext(), "checkcloudid already exists",Toast.LENGTH_SHORT).show();

                                } else {
                                    ContentValues values = new ContentValues();
                                    values.put(Database.CloudID, Id);
                                    long rows = db.update(Database.EM_PRODUCE_COLLECTION_TABLE_NAME, values,
                                            Database.EmployeeNo + " = ? AND " + Database.LoadCount + " = ? AND " + Database.DataCaptureDevice + " = ? AND "
                                                    + Database.ReceiptNo + " = ?", new String[]{EmployeeNo, WeighmentNo, BatchSerial, SessionNo});

                                    if (rows > 0) {
                                        Log.i("success:", Id);

                                    }
                                }

                            }
                            if (Integer.parseInt(Id) < 0) {

                                return null;
                            }


                        } catch (NumberFormatException | JSONException e) {
                            Id = "-8080";
                            Title = "";
                            sMessage = restApiResponse;
                            e.printStackTrace();
                            returnValue = e.toString();
                            Log.i("Catch Exc:", returnValue);
                        }

                        progressStatus++;
                        publishProgress("" + progressStatus);

                    }


                    produce.close();

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
            c_refresh.setVisibility(View.VISIBLE);
            c_success.setVisibility(View.GONE);
            c_error.setVisibility(View.GONE);


        }

        @Override
        protected void onPostExecute(String unused) {

            try {

                if (Integer.parseInt(Id) > 0) {

                    c_refresh.setVisibility(View.GONE);
                    c_success.setVisibility(View.VISIBLE);
                    c_error.setVisibility(View.GONE);
                } else if (Integer.parseInt(Id) < 0) {

                    //Toast.makeText(getApplicationContext(), Id+" "+sMessage, Toast.LENGTH_LONG).show();

                }


            } catch (NumberFormatException e) {

                c_refresh.setVisibility(View.GONE);
                c_success.setVisibility(View.GONE);
                c_error.setVisibility(View.VISIBLE);
                // Toast.makeText(_activity, error, Toast.LENGTH_LONG).show();
                return;

            }

        }
    }


}
