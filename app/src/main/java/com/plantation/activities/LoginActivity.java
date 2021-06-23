package com.plantation.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;

import java.io.File;


/**
 * Created by Michael on 9/24/2015.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    static SharedPreferences mSharedPrefs, prefs;
    private final String TAG = "App";
    DBHelper dbhelper;
    String systembasedate;
    int count;
    int usercount;
    File FolderPath1, FolderPath2, FolderPath3, FolderPath4;
    File root;
    private TextInputLayout usernameWrapper, passwordWrapper;
    private ProgressBar mProgress;
    private Button signInBtn;
    private Snackbar snackbar;
    private DevicePolicyManager devicePolicyManager = null;
    private ComponentName demoDeviceAdmin = null;
    private int ACTIVATION_REQUEST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        demoDeviceAdmin = new ComponentName(this, DeviceAdmin.class);
        Log.e("DeviceAdminActive==", "" + demoDeviceAdmin);

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);// adds new device administrator
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, demoDeviceAdmin);//ComponentName of the administrator component.
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Disable app");//dditional explanation
        startActivityForResult(intent, ACTIVATION_REQUEST);


        initView();
        initToolbar();
        setupProgressBar();
        setupSnackBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

            } else {
                CreateFolders();

            }
        } else {
            CreateFolders();
        }
    }

    public void CreateFolders() {

        root = Environment.getExternalStorageDirectory();
        FolderPath1 = new File(new File(root, "Easyweigh"), "Masters");

        if (!FolderPath1.exists()) {
            if (!FolderPath1.mkdirs()) {
                Log.d("App", "failed to create directory");
                Toast.makeText(LoginActivity.this, "failed to create Masters directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        FolderPath2 = new File(root, "/Easyweigh/Exports");

        if (!FolderPath2.exists()) {
            if (!FolderPath2.mkdirs()) {
                Log.d("App", "failed to create directory");
                Toast.makeText(LoginActivity.this, "failed to create Exports directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        FolderPath3 = new File(root, "/Easyweigh/Apks");

        if (!FolderPath3.exists()) {
            if (!FolderPath3.mkdirs()) {
                Log.d("App", "failed to create directory");
                Toast.makeText(LoginActivity.this, "failed to create Apks directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        FolderPath4 = new File(root, "/Easyweigh/Fingerprints");

        if (!FolderPath4.exists()) {
            if (!FolderPath4.mkdirs()) {
                Log.d("App", "failed to create directory");
                Toast.makeText(LoginActivity.this, "failed to create Fingerprints directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);

    }

    @Override
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        backButtonHandler();
        return;
    }

    public void backButtonHandler() {

        Intent intent = new Intent(android.content.Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(android.content.Intent.CATEGORY_HOME);
        finish();
        startActivity(intent);
    }

    public void setupSnackBar() {
        snackbar = Snackbar.make(findViewById(R.id.ParentLayoutLogin), getString(R.string.LoginError), Snackbar.LENGTH_LONG);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#FF5252"));
    }

    public void setupProgressBar() {
        mProgress = findViewById(R.id.progress_bar);
        mProgress.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF5252"),
                PorterDuff.Mode.SRC_IN);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initView() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        signInBtn = findViewById(R.id.signInBtn);
        signInBtn.setVisibility(View.VISIBLE);
        TextView registerBtn = findViewById(R.id.register);
        usernameWrapper = findViewById(R.id.usernameWrapper);
        passwordWrapper = findViewById(R.id.passwordWrapper);
        findViewById(R.id.forgotBtn).setOnClickListener(this);
        signInBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        dbhelper = new DBHelper(LoginActivity.this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor accounts = db.query(true, Database.EM_TABLE_NAME, null, null, null, null, null, null, null, null);
        count = accounts.getCount();

        Cursor users = db.query(true, Database.OPERATORSMASTER_TABLE_NAME, null, null, null, null, null, null, null, null);
        usercount = users.getCount();
        if (usercount == 0) {
            String DefaultUsers = "INSERT INTO " + Database.OPERATORSMASTER_TABLE_NAME + " ("
                    + Database.USERIDENTIFIER + ", "
                    + Database.CLERKNAME + ", "
                    + Database.USERPWD + ", "
                    + Database.ACCESSLEVEL + ") Values ('OCTAGON', 'ODS', '1234', '1')";

            db.execSQL(DefaultUsers);

        }
        // Toast.makeText(LoginActivity.this, String.valueOf(count), Toast.LENGTH_LONG).show();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(LoginActivity.this);

        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("count", String.valueOf(count));
        edit.apply();


    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == R.id.signInBtn) {
            String userName = usernameWrapper.getEditText().getText().toString().trim();
            String userPassword = passwordWrapper.getEditText().getText().toString().trim();
            if (userName.length() <= 0) {
                usernameWrapper.setError("Enter a valid Username");
            } else if (userPassword.length() < 3) {
                passwordWrapper.setError("Invalid Password");
            } else {
                usernameWrapper.setErrorEnabled(false);
                passwordWrapper.setErrorEnabled(false);


                if (dbhelper.UserLogin(userName, userPassword)) {
                    // save user data
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(LoginActivity.this);

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("user", userName);
                    edit.commit();

                    edit.putString("pass", userPassword);
                    edit.commit();
                    mProgress.setVisibility(View.VISIBLE);
                    signInBtn.setVisibility(View.GONE);

                    Cursor d = dbhelper.getAccessLevel(userName);
                    String full_name = d.getString(1);
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Successfully Logged In " + full_name);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    // Toast.makeText(LoginActivity.this, "Successfully Logged In", Toast.LENGTH_LONG).show();
                    systembasedate = prefs.getString("basedate", "");
                    if (systembasedate.equals("") && mSharedPrefs.getString("scaleVersion", "").equals("") && count == 0) {
                        finish();
                        Intent login = new Intent(getApplicationContext(), SplashActivity.class);
                        startActivity(login);

                        return;
                    }

                    finish();
                    Intent login = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(login);
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Username/Password", Toast.LENGTH_LONG).show();
                    snackbar.show();
                }
                dbhelper.close();


            }
        } else if (view.getId() == R.id.register) {


        } else if (view.getId() == R.id.forgotBtn) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.insertEmail));
            final EditText emailInput = new EditText(this);
            alert.setView(emailInput);
            alert.setPositiveButton(getString(R.string.rest), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String email = emailInput.getText().toString().trim();


                }
            });
            alert.setNegativeButton(getString(R.string.cancel), null);
            alert.show();
        }
    }

    public void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        String uname = prefs.getString("user", "");
        String pass = prefs.getString("pass", "");

        String userName = uname;
        String userPassword = pass;
        usernameWrapper.getEditText().setText(uname);

        try {
            if (userName.length() > 0 && userPassword.length() > 0) {
                DBHelper dbUser = new DBHelper(LoginActivity.this);


                if (dbUser.UserLogin(userName, userPassword)) {


                    finish();
                    Intent login = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(login);
                } else {

                }
                dbUser.close();
            }

        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
}
