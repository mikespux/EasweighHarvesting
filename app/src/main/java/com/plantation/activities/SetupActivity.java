package com.plantation.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.plantation.R;
import com.plantation.preferences.CloudSetup;
import com.plantation.preferences.CompanyDetails;
import com.plantation.preferences.OtherSettings;
import com.plantation.preferences.ScaleSetup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 8/19/2015.
 */
public class SetupActivity extends AppCompatActivity {

    static SharedPreferences mSharedPrefs, prefs;
    public Intent mIntent = null;
    public Toolbar toolbar;
    ViewPager view_pager;
    TabLayout tab_layout;

    String cachedDeviceAddress;
    Button btn_pairscale, btn_pairprinter, btn_pairreader;
    AlertDialog b;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        initComponent();


    }

    private void initComponent() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        view_pager = findViewById(R.id.view_pager);
        setupViewPager(view_pager);

        tab_layout = findViewById(R.id.tab_layout);
        tab_layout.setupWithViewPager(view_pager);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(CompanyDetails.newInstance(), "General Info");
        adapter.addFragment(ScaleSetup.newInstance(), "Scale Setup");
        adapter.addFragment(CloudSetup.newInstance(), "Cloud Setup");
        adapter.addFragment(OtherSettings.newInstance(), "Other Settings");
        viewPager.setAdapter(adapter);
    }

    /**
     * method initializer
     */

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        cachedDeviceAddress = pref.getString("address", "");
        if (mSharedPrefs.getString("terminalID", "").equals("0")) {
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("Please Enter Terminal ID");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }
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
        } else if (mSharedPrefs.getString("scaleVersion", "").equals("EW15")) {
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

                    mIntent = new Intent(SetupActivity.this, PairedDeviceListActivity.class);
                    startActivity(mIntent);


                }
            });
            btn_pairprinter = dialogView.findViewById(R.id.btn_pairprinter);
            if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
                // go back to milkers activity
                Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                btn_pairprinter.setVisibility(View.GONE);

                //return;
            } else {
                btn_pairprinter.setVisibility(View.VISIBLE);
            }

            btn_pairprinter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    mIntent = new Intent(SetupActivity.this, PrintTestActivity.class);
                    startActivity(mIntent);


                }
            });

            dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do something with edt.getText().toString();


                }
            });
            dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            });
            b = dialogBuilder.create();
            b.show();

            b.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    cachedDeviceAddress = pref.getString("address", "");
                    if (cachedDeviceAddress.equals("")) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please Pair Scale");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        return;
                    }
                    b.dismiss();
                    finish();
                    mIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mIntent);
                    Boolean wantToCloseDialog = false;
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (wantToCloseDialog)
                        b.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        } else if (mSharedPrefs.getString("scaleVersion", "").equals("EW11")) {
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

                    mIntent = new Intent(SetupActivity.this, PairedDeviceListActivity.class);
                    startActivity(mIntent);
                }
            });
            btn_pairprinter = dialogView.findViewById(R.id.btn_pairprinter);
            if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
                // go back to milkers activity
                Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                btn_pairprinter.setVisibility(View.GONE);

                //return;
            } else {
                btn_pairprinter.setVisibility(View.VISIBLE);
            }

            btn_pairprinter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mIntent = new Intent(SetupActivity.this, PrintTestActivity.class);
                    startActivity(mIntent);


                }
            });

            dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do something with edt.getText().toString();


                }
            });
            dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            });
            b = dialogBuilder.create();
            b.show();
            b.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cachedDeviceAddress = pref.getString("address", "");
                    if (cachedDeviceAddress.equals("")) {
                        Context context = getApplicationContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText("Please Pair Scale");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        return;
                    }
                  /*  if (cachedDeviceAddress.toString().equals("")) {
                        Context context = dialogView.getContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Pair Scale ...");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        return;
                    }

                    if (mSharedPrefs.getBoolean("enablePrinting", false) == true) {
                        // go back to milkers activity

                        if (prefs.getString("mDevice", "").equals("")) {
                            Context context = dialogView.getContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                            text.setText("Please Pair Printer ...");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            return;
                        }
                        return;
                    }*/
                   /* SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SetupActivity.this);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("user");
                    edit.remove("pass");
                    edit.commit();*/
                    b.dismiss();
                    finish();
                    mIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mIntent);
                    Boolean wantToCloseDialog = false;
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (wantToCloseDialog)
                        b.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        } else {
            if (!cachedDeviceAddress.equals("")) {
                finish();
                return;
            }
           /* SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SetupActivity.this);
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("user");
            edit.remove("pass");
            edit.commit();*/
            finish();
            mIntent = new Intent(SetupActivity.this, MainActivity.class);
            startActivity(mIntent);
        }
        return;


    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


}
