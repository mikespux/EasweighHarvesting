package com.plantation.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.fragments.TabsFragment;
import com.plantation.preferences.PreferenceURLSettings;
import com.plantation.services.EasyWeighService;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String TRANCELL_TI500 = "TI-500";
    public static final String FINGERPRINT = "FingerPrint";
    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    public static final String BOTH = "Both";
    static SharedPreferences mSharedPrefs;
    private final int DATE_DIALOG_ID = 1;
    public int count;
    DBHelper dbhelper;
    int accesslevel = 0;
    int useridentifier = 1;
    MenuItem item;
    Menu nav_Menu;
    Intent mIntent;
    TextView userid;
    Button btn_pairscale, btn_pairprinter, btn_pairreader;
    EasyWeighService resetConn;
    SharedPreferences prefs;
    String systembasedate;
    Button pickDate;
    AlertDialog b, changepass;
    String user_level, defaultpass;
    String username, userpass;
    EditText edtOldPass, edtNewPass, edtConfirmPass;
    Button btnChangePass;
    CheckBox checkID, checkPhoneNo, checkVisiblePass;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private Toolbar toolbar;
    private Fragment mFragment;
    private LocalDate systemdate, currentdate;
    private EditText edtBaseDate;
    private final DatePickerDialog.OnDateSetListener mDatelistener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int month, int day) {
                    if (!validatePastDate(view.getContext(), day, month + 1, year)) {
                        return;
                    }
                    if (!validateFutureDate(view.getContext(), day, month + 1, year)) {
                        return;
                    }
                    edtBaseDate.setText(String.format("%d-%d-%d", year, month + 1, day));

                }
            };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if (mSharedPrefs.getString("vModes", "FingerPrint").toString().equals(FINGERPRINT)){
        //}else{
        resetConn = new EasyWeighService();
        // }

        prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        dbhelper = new DBHelper(getApplicationContext());

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        View header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                displayView(menuItem.getItemId());
                //Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
                return false;
            }
        });

        userid = header.findViewById(R.id.userid);
        username = prefs.getString("user", "");
        Cursor d = dbhelper.getAccessLevel(username);
        user_level = d.getString(accesslevel);
        String user_fullname = d.getString(useridentifier);
        SharedPreferences.Editor edit1 = prefs.edit();
        edit1.putString("fullname", user_fullname);
        edit1.apply();

        // Toast.makeText(MainActivity.this, prefs.getString("count", ""), Toast.LENGTH_LONG).show();
        if (user_level.equals("2")) {
            userid.setText("Welcome " + user_fullname + "\n" + "(Clerk)");
        } else {
            userid.setText("Welcome " + user_fullname + "\n" + "(Manager)");
        }

        defaultpass = d.getString(2);
        if (defaultpass != null) {
            if (defaultpass.equals("0")) {
                changePassword();
                Toast.makeText(getApplicationContext(), "Change the Default Password", Toast.LENGTH_LONG).show();
                return;
            }
        }
//        long expiry_days = dbhelper.expiry_days(username);
//        if (expiry_days >= 29) {
//            changePassword();
//            Context context = getApplicationContext();
//            LayoutInflater inflater1 = getLayoutInflater();
//            View customToastroot = inflater1.inflate(R.layout.red_toast, null);
//            TextView text = customToastroot.findViewById(R.id.toast);
//            text.setText("Password Expired. Please Change it!");
//            Toast customtoast = new Toast(context);
//            customtoast.setView(customToastroot);
//            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
//            customtoast.setDuration(Toast.LENGTH_LONG);
//            customtoast.show();
//            //Toast.makeText(getApplicationContext(), "Password Expired. Please Change!",Toast.LENGTH_LONG).show();
//            return;
//        }
        //TABS VIEW
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.container, new TabsFragment()).commit();
        systembasedate = prefs.getString("basedate", "");

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, null, null, null, null, null, null, null);
        count = accounts.getCount();

        if (count == 0) {
            finish();
            Intent login = new Intent(getApplicationContext(), SyncMastersActivity.class);
            startActivity(login);
            return;
        }
        if (systembasedate.equals("")) {


            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_basedate, null);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setCancelable(false);
            dialogBuilder.setTitle("App Base Date");
            edtBaseDate = dialogView.findViewById(R.id.editText);
            edtBaseDate.setEnabled(false);
            // edtBaseDate.setText(prefs.getString("basedate",""));

            pickDate = dialogView.findViewById(R.id.btnDate);
            pickDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(DATE_DIALOG_ID);

                }
            });

            dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {


                }
            });

            dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Toast.makeText(MainActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                        return true;
                    }
                    return false;
                }
            });


            b = dialogBuilder.create();

            if (user_level.equals("1")) {
                b.show();
            }
            if (user_level.equals("2")) {
                Context context = getApplicationContext();
                LayoutInflater inflater1 = getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Contact the Administrator to Solve Base Date Error!!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                new LogOut().execute();
                return;
            }
            b.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (edtBaseDate.getText().length() == 0) {
                        Toast.makeText(MainActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                        return;
                    }
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("basedate", edtBaseDate.getText().toString());
                    edit.commit();
                    b.dismiss();
                    SQLiteDatabase db = dbhelper.getReadableDatabase();
                    Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, null, null, null, null, null, null, null);
                    count = accounts.getCount();
                    if (count == 0) {
                        finish();
                        mIntent = new Intent(MainActivity.this, ImportMasterActivity.class);
                        startActivity(mIntent);
                        Toast.makeText(MainActivity.this, edtBaseDate.getText().toString() + " saved successfully", Toast.LENGTH_LONG).show();


                        return;
                    }
                    if (count > 0) {

                        finish();
                        mIntent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(mIntent);
                        Toast.makeText(MainActivity.this, edtBaseDate.getText().toString() + " saved successfully", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Boolean wantToCloseDialog = false;
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (wantToCloseDialog)
                        b.dismiss();
                }
            });
            return;

        } else {
            systemdate = LocalDate.parse(prefs.getString("basedate", ""));
            currentdate = new LocalDate();


            if (Days.daysBetween(currentdate, systemdate).getDays() >= 1) {
                if (user_level.equals("1")) {
                    Toast.makeText(MainActivity.this, "Current Base Date is:" + systemdate + " and should not be greater than Phone Date, Please Reset", Toast.LENGTH_LONG).show();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Reset PhoneDate and Login to Reset Base Date");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("basedate");
                    edit.commit();
                    new LogOut().execute();
                    //
                    startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                    return;
                }
                if (user_level.equals("2")) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Contact the Administrator to Solve Base Date Error!!");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    new LogOut().execute();
                    return;
                }

            } else if (Days.daysBetween(systemdate, currentdate).getDays() >= 7) {


                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_basedate, null);
                dialogBuilder.setView(dialogView);
                dialogBuilder.setCancelable(false);
                dialogBuilder.setTitle("App Base Date");
                edtBaseDate = dialogView.findViewById(R.id.editText);
                edtBaseDate.setEnabled(false);
                edtBaseDate.setText(prefs.getString("basedate", ""));

                pickDate = dialogView.findViewById(R.id.btnDate);
                pickDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(DATE_DIALOG_ID);

                    }
                });

                dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //do something with edt.getText().toString();


                    }
                });

                dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            Toast.makeText(MainActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        return false;
                    }
                });
           /* dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                    getdata();
                }
            });*/
                b = dialogBuilder.create();
                if (user_level.equals("1")) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater2 = getLayoutInflater();
                    View customToastroot = inflater2.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Current Base Date is: '" + systemdate + "' and should not be less than 7 days the Phone Date, Please Reset");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    b.show();
                }
                if (user_level.equals("2")) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater1 = getLayoutInflater();
                    View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Contact the Administrator to Solve Base Date Error!!");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    new LogOut().execute();
                    return;
                }
                b.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (edtBaseDate.getText().length() == 0) {
                            Toast.makeText(MainActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                            return;
                        }
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("basedate", edtBaseDate.getText().toString());
                        edit.commit();
                        b.dismiss();

                        SQLiteDatabase db = dbhelper.getReadableDatabase();
                        Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, null, null, null, null, null, null, null);
                        count = accounts.getCount();
                        if (count == 0) {
                            finish();
                            mIntent = new Intent(MainActivity.this, SyncMastersActivity.class);
                            startActivity(mIntent);
                            Toast.makeText(MainActivity.this, edtBaseDate.getText().toString() + " saved successfully", Toast.LENGTH_LONG).show();


                            return;
                        }
                        if (count > 0) {

                            finish();
                            mIntent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(mIntent);
                            Toast.makeText(MainActivity.this, edtBaseDate.getText().toString() + " saved successfully", Toast.LENGTH_LONG).show();
                            return;
                        }


                        Boolean wantToCloseDialog = false;
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog)
                            b.dismiss();
                    }
                });
                //Toast.makeText(MainActivity.this,"Current date is more than 5 days set date or reset Base date", Toast.LENGTH_LONG).show();

            } else {

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("basedate", currentdate.toString());
                edit.commit();

                if (prefs.getString("DeliverNoteNumber", "").equals("")
                        || prefs.getString("DeliverNoteNumber", "").equals("No Batch Opened")) {
                    Context context1 = getApplicationContext();
                    LayoutInflater inflater1 = getLayoutInflater();
                    View customToastroot1 = inflater1.inflate(R.layout.white_red_toast, null);
                    TextView text1 = customToastroot1.findViewById(R.id.toast);
                    text1.setText("Please Open Batch To Proceed ...");
                    Toast customtoast1 = new Toast(context1);
                    customtoast1.setView(customToastroot1);
                    customtoast1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast1.setDuration(Toast.LENGTH_LONG);
                    // customtoast1.show();
                }
                //Toast.makeText(MainActivity.this, currentdate.toString()+" updated successfully", Toast.LENGTH_LONG).show();

            }
        }
        enableBT();
    }

    public boolean validatePastDate(Context mContext, int day, int month, int year) {
        final Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH) + 1;
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        if (day > currentDay && year == currentYear && month == currentMonth) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select today's date not future date ", Toast.LENGTH_LONG).show();
            return false;
        } else if (month > currentMonth && year == currentYear) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select valid month", Toast.LENGTH_LONG).show();
            return false;
        } else if (year > currentYear) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select valid year", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public boolean validateFutureDate(Context mContext, int day, int month, int year) {
        final Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH) + 1;
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        if (day < currentDay && year == currentYear && month == currentMonth) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select today's date not past date ", Toast.LENGTH_LONG).show();
            return false;
        } else if (month < currentMonth && year == currentYear) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select valid month", Toast.LENGTH_LONG).show();
            return false;
        } else if (year < currentYear) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select valid year", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);


        switch (id) {
            case DATE_DIALOG_ID:
                //start changes...
                DatePickerDialog dialog = new DatePickerDialog(this, mDatelistener, year, month, day);
                dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis());


                return dialog;
            //end changes...
        }

        return null;
        // Create a new instance of DatePickerDialog and return it
        //return new DatePickerDialog(getActivity(), this, year, month, day);
    }


    /**
     * method to display items of drawer
     *
     * @param id
     */
    private void displayView(int id) {
        switch (id) {
            case R.id.navigation_item_home:
                mFragment = new TabsFragment();
                break;
            case R.id.navigation_item_import:
                finish();
                mIntent = new Intent(MainActivity.this, SyncMastersActivity.class);
                startActivity(mIntent);
                break;
            case R.id.navigation_mode_settings:
                mIntent = new Intent(MainActivity.this, PreferenceURLSettings.class);
                startActivity(mIntent);
                break;
            case R.id.navigation_item_paired:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_pair_devices, null);
                dialogBuilder.setView(dialogView);
                dialogBuilder.setTitle("Pair Devices");

                btn_pairscale = dialogView.findViewById(R.id.btn_pairscale);
                btn_pairscale.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mIntent = new Intent(MainActivity.this, PairedDeviceListActivity.class);
                        startActivity(mIntent);
                    }
                });
                btn_pairprinter = dialogView.findViewById(R.id.btn_pairprinter);
                btn_pairprinter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mIntent = new Intent(MainActivity.this, PrintTestActivity.class);
                        startActivity(mIntent);


                    }
                });



                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //do something with edt.getText().toString();


                    }
                });

                b = dialogBuilder.create();
                b.show();
                break;
            case R.id.nav_basedate:

                AlertDialog.Builder dialogBasedate = new AlertDialog.Builder(this);
                LayoutInflater inflater1 = this.getLayoutInflater();
                final View dialogView1 = inflater1.inflate(R.layout.dialog_basedate, null);
                dialogBasedate.setView(dialogView1);
                dialogBasedate.setCancelable(true);
                dialogBasedate.setTitle("App Base Date");
                edtBaseDate = dialogView1.findViewById(R.id.editText);
                edtBaseDate.setEnabled(false);
                edtBaseDate.setText(prefs.getString("basedate", ""));

                pickDate = dialogView1.findViewById(R.id.btnDate);
                pickDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(DATE_DIALOG_ID);

                    }
                });

                dialogBasedate.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //do something with edt.getText().toString();


                    }
                });

                dialogBasedate.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            Toast.makeText(MainActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        return false;
                    }
                });
           /* dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                    getdata();
                }
            });*/
                b = dialogBasedate.create();
                b.show();
                b.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (edtBaseDate.getText().length() == 0) {
                            Toast.makeText(MainActivity.this, "Please set Base Date", Toast.LENGTH_LONG).show();
                            return;
                        }
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("basedate", edtBaseDate.getText().toString());
                        edit.commit();
                        b.dismiss();

                        SQLiteDatabase db = dbhelper.getReadableDatabase();
                        Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, null, null, null, null, null, null, null);
                        count = accounts.getCount();
                        if (count == 0) {
                            finish();
                            mIntent = new Intent(MainActivity.this, SyncMastersActivity.class);
                            startActivity(mIntent);
                            Toast.makeText(MainActivity.this, edtBaseDate.getText().toString() + " saved successfully", Toast.LENGTH_LONG).show();


                            return;
                        }
                        if (count > 0) {

                            finish();
                            mIntent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(mIntent);
                            Toast.makeText(MainActivity.this, edtBaseDate.getText().toString() + " saved successfully", Toast.LENGTH_LONG).show();
                            return;
                        }


                        Boolean wantToCloseDialog = false;
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog)
                            b.dismiss();
                    }
                });
                break;
            case R.id.navigation_reports:
                mIntent = new Intent(MainActivity.this, HarvestReportsActivity.class);
                startActivity(mIntent);
                break;
            case R.id.ChangePass:
                changePassword();
                break;

            case R.id.SignOutItem:
                new LogOut().execute();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove("user");
                edit.remove("pass");
                edit.commit();
                break;
        }
        if (mFragment != null && mIntent == null) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.container, mFragment).commit();
        } else {
            //startActivity(mIntent);
            mIntent = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        dbhelper = new DBHelper(getApplicationContext());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String username = prefs.getString("user", "");

        Cursor d = dbhelper.getAccessLevel(username);
        String user_level = d.getString(accesslevel);
        //Toast.makeText(MainActivity.this, user_level, Toast.LENGTH_LONG).show();(
        if (user_level.equals("2")) {
            item = menu.findItem(R.id.action_settings);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;


            case R.id.action_settings:
                finish();
                Intent mIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(mIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void changePassword() {


        AlertDialog.Builder dialogFarmerSearch = new AlertDialog.Builder(this);
        LayoutInflater inflater1 = this.getLayoutInflater();
        final View dialogView = inflater1.inflate(R.layout.dialog_change_password, null);
        dialogFarmerSearch.setView(dialogView);
        dialogFarmerSearch.setCancelable(true);
        dialogFarmerSearch.setTitle("Change Password");

        edtOldPass = dialogView.findViewById(R.id.edtOldPass);
        edtNewPass = dialogView.findViewById(R.id.edtNewPass);
        edtConfirmPass = dialogView.findViewById(R.id.edtConfirmPass);
        checkVisiblePass = dialogView.findViewById(R.id.checkVisiblePass);
        checkVisiblePass.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //do stuff
            if (checkVisiblePass.isChecked()) {
                edtOldPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                edtNewPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                edtConfirmPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {

                edtOldPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                edtNewPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                edtConfirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        btnChangePass = dialogView.findViewById(R.id.btnChangePass);
        btnChangePass.setOnClickListener(v -> {
            Cursor d = dbhelper.getPassword(username);
            userpass = d.getString(0);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            //Toast.makeText(v.getContext(),"UserName: "+username+" Pass: "+userpass,Toast.LENGTH_SHORT).show();

            String opassword = edtOldPass.getText().toString();
            String password = edtNewPass.getText().toString();
            String cpassword = edtConfirmPass.getText().toString();
            if (opassword.length() < 4) {
                edtOldPass.setError("Invalid Password Length");
                return;
            }
            if (password.length() < 4) {
                edtNewPass.setError("Invalid Password Length");
                return;
            }
            if (cpassword.length() < 4) {
                edtConfirmPass.setError("Invalid Password Length");
                return;
            }

            if (!opassword.equals(userpass)) {
                Toast.makeText(getApplicationContext(), "Invalid Old Password", Toast.LENGTH_LONG).show();
                return;
            }
            if (password.equals(userpass)) {
                Toast.makeText(getApplicationContext(), "Please Enter a New Password", Toast.LENGTH_LONG).show();
                return;
            }
            if (!cpassword.equals(password)) {
                Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
                return;
            }
            Calendar cal = Calendar.getInstance();
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 30);
            Date expDate = c.getTime();

            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            ContentValues values = new ContentValues();
            values.put(Database.USERPWD, password);
            values.put(Database.USERCLOUDID, 1);
            //values.put(Database.PWDSETDATE, "2021-07-15 10:00:12");
            values.put(Database.PWDSETDATE, dateTimeFormat.format(cal.getTime()));
            values.put(Database.PWDEXPDATE, dateTimeFormat.format(expDate));

            long rows = db.update(Database.OPERATORSMASTER_TABLE_NAME, values,
                    "ClerkName COLLATE NOCASE = ?", new String[]{username});

            db.close();
            if (rows > 0) {
                Toast.makeText(getApplicationContext(), "Updated Password Successfully!", Toast.LENGTH_LONG).show();
                edtOldPass.setText("");
                edtNewPass.setText("");
                edtConfirmPass.setText("");
                new LogOut().execute();

            } else {
                Toast.makeText(getApplicationContext(), "Sorry! Could not update Password!",
                        Toast.LENGTH_LONG).show();
            }

        });

        dialogFarmerSearch.setPositiveButton("Cancel", (dialog, whichButton) -> {


        });


           /* dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                    getdata();
                }
            });*/
        AlertDialog changepass = dialogFarmerSearch.create();
        changepass.show();
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        backButtonHandler();
        return;
    }


    public void backButtonHandler() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MainActivity.this);
        // Setting Dialog Title
        alertDialog.setTitle("Close Easyway?");
        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to close the application?");

        // Setting Positive "Yes" Button
        alertDialog.setNegativeButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new LogOut().execute();
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

    public void onStart() {
        super.onStart();
        dbhelper = new DBHelper(getApplicationContext());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String username = prefs.getString("user", "");
        navigationView = findViewById(R.id.navigation_view);
        nav_Menu = navigationView.getMenu();

        Cursor d = dbhelper.getAccessLevel(username);
        user_level = d.getString(accesslevel);
        //Toast.makeText(MainActivity.this, user_level, Toast.LENGTH_LONG).show();(
        if (user_level.equals("2")) {

            //nav_Menu.findItem(R.id.navigation_item_search).setVisible(false);
            nav_Menu.findItem(R.id.navigation_item_import).setVisible(false);
            nav_Menu.findItem(R.id.nav_basedate).setVisible(false);

        }

    }

    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    public void disableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
    }

    private class LogOut extends AsyncTask<Void, Void, String> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this,
                    getString(R.string.please_wait),
                    getString(R.string.logging_out),
                    true);
        }

        @Override
        protected String doInBackground(Void... params) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("pass");
            edit.commit();
            edit.remove("tvConn");
            edit.commit();

            try {
                Thread.sleep(1000);
                resetConn.stop();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return "";
        }

        @Override
        protected void onPostExecute(String result) {

            dialog.dismiss();
            disableBT();
            finish();
            /*Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addCategory(android.content.Intent.CATEGORY_HOME);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/
        }
    }
}
