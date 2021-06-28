package com.plantation.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.activities.PairedDeviceListActivity;


public class PreferenceGeneralSettings extends PreferenceActivity {
    static SharedPreferences mSharedPrefs, prefs;
    Intent mIntent;
    String cachedDeviceAddress;
    Button btn_pairscale, btn_pairprinter;
    AlertDialog b;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_generalsettings);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        root.addView(toolbar, 0);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(R.string.General_settings);
        toolbar.setTitleTextColor(Color.WHITE);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        cachedDeviceAddress = pref.getString("address", "");

        if (mSharedPrefs.getString("scaleVersion", "").equals("")) {
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("Please Select Scale Model to Weigh!!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        } else if (mSharedPrefs.getString("scaleVersion", "").equals("EW15") || mSharedPrefs.getString("scaleVersion", "").equals("EW11")) {
            if (!cachedDeviceAddress.equals("")) {
                finish();
                return;
            }

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_pair_devices, null);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setTitle("Pair Devices");
            dialogBuilder.setCancelable(false);

            btn_pairscale = dialogView.findViewById(R.id.btn_pairscale);
            btn_pairscale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mIntent = new Intent(PreferenceGeneralSettings.this, PairedDeviceListActivity.class);
                    startActivity(mIntent);


                }
            });
            btn_pairprinter = dialogView.findViewById(R.id.btn_pairprinter);
            btn_pairprinter.setVisibility(View.GONE);



            dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do something with edt.getText().toString();
                    if (mSharedPrefs.getString("scaleVersion", "").equals("EW15") || mSharedPrefs.getString("scaleVersion", "").equals("EW11")) {
                        if (!cachedDeviceAddress.equals("")) {
                            Context context = getApplicationContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Please pair Scale Model to Weigh after pressing back button!!");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            dialog.dismiss();
                            return;
                        }
                    }

                }
            });
            dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    //Toast.makeText(PreferenceCompanySettings.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            });
            b = dialogBuilder.create();
            b.show();

            b.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    b.dismiss();
                    finish();

                    Boolean wantToCloseDialog = false;
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (wantToCloseDialog)
                        b.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        } else if (mSharedPrefs.getString("scaleVersion", "").equals("TI-500")) {
            finish();


                  /*  SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PreferenceGeneralSettings.this);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("user");
                    edit.remove("pass");
                    edit.commit();
                    finish();
                    mIntent = new Intent(PreferenceGeneralSettings.this, LoginActivity.class);
                    startActivity(mIntent);*/

        } else {

            finish();
        }

        return;
    }
}
