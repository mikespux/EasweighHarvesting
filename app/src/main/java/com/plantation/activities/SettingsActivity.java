package com.plantation.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.plantation.R;
import com.plantation.adapters.SettingsAdapter;
import com.plantation.data.SettingsItem;
import com.plantation.helpers.DividerItemDecoration;
import com.plantation.helpers.RecyclerTouchListener;
import com.plantation.preferences.PreferenceCompanySettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Orenge 8/19/2015.
 */
public class SettingsActivity extends AppCompatActivity {
    static SharedPreferences mSharedPrefs, prefs;
    public RecyclerView settingsList;
    public SettingsAdapter settingsAdapter;
    public Intent mIntent = null;
    public Toolbar toolbar;

    public static List<SettingsItem> getData() {
        List<SettingsItem> data = new ArrayList<>();
        int[] icons = {R.mipmap.ic_settings_black_24dp, R.mipmap.ic_useradd, R.mipmap.ic_agent, R.mipmap.ic_factory, R.mipmap.ic_produce, R.mipmap.ic_zone, R.mipmap.ic_divisions, R.mipmap.ic_route, R.mipmap.ic_shed, R.mipmap.ic_tasks, R.mipmap.ic_transporter, R.mipmap.ic_machine, R.mipmap.ic_capitalp};
        String[] titles = {"Company Setup", "Users", "Employees", "Factories", "Commodity", "Estates", "Divisions", "Fields", "Blocks", "Tasks", "Transporters", "Machines", "Capital Projects"};

        for (int i = 0; i < titles.length && i < icons.length; i++) {

            SettingsItem current = new SettingsItem();
            current.iconId = icons[i];
            current.title = titles[i];
            data.add(current);
        }

        return data;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializer();


    }

    /**
     * method initializer
     */
    public void initializer() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        settingsList = findViewById(R.id.settingsList);
        settingsAdapter = new SettingsAdapter(this, getData());
        settingsList.setHasFixedSize(true);
        settingsList.setAdapter(settingsAdapter);
        settingsList.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, null);
        settingsList.addItemDecoration(itemDecoration);

        // this is the default; this call is actually only necessary with custom ItemAnimators
        settingsList.setItemAnimator(new DefaultItemAnimator());

        settingsList.addOnItemTouchListener(new RecyclerTouchListener(this, settingsList, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                switch (position - 1) {
                    case 0:
                        mIntent = new Intent(SettingsActivity.this, PreferenceCompanySettings.class);//Overall Settings
                        break;
                    case 1:
                        mIntent = new Intent(SettingsActivity.this, UserDetailsActivity.class);//user Details
                        break;
                    case 2:
                        finish();
                        mIntent = new Intent(SettingsActivity.this, EmployeeFingerPrints.class);//Employee Details
                        break;
                    case 3:
                        mIntent = new Intent(SettingsActivity.this, FactoryDetailsActivity.class);//Factory Details
                        break;
                    case 4:
                        mIntent = new Intent(SettingsActivity.this, ProduceDetailsActivity.class);//Commodity details
                        break;
                    case 5:
                        mIntent = new Intent(SettingsActivity.this, EstatesDetailsActivity.class);//Estate details
                        break;
                    case 6:
                        mIntent = new Intent(SettingsActivity.this, DivisionsDetailsActivity.class);//Divisions details
                        break;
                    case 7:
                        mIntent = new Intent(SettingsActivity.this, FieldsDetailsActivity.class);//Fields Details
                        break;
                    case 8:
                        mIntent = new Intent(SettingsActivity.this, BlocksDetailsActivity.class);//Blocks Details
                        break;
                    case 9:
                        mIntent = new Intent(SettingsActivity.this, TaskDetailsActivity.class);//TasksDetails
                        break;
                    case 10:
                        mIntent = new Intent(SettingsActivity.this, TransporterDetailsActivity.class);//Transporter Details
                        break;
                    case 11:
                        mIntent = new Intent(SettingsActivity.this, MachineDetailsActivity.class);//Machine Details
                        break;
                    case 12:
                        mIntent = new Intent(SettingsActivity.this, CapitalPDetailsActivity.class);//Capital Project Details
                        break;


                    default:
                        break;
                }

                if (mIntent != null) {
                    startActivity(mIntent);
                }

            }

            @Override
            public void onLongClick(View view, int position) {
                //add whatever you want like StartActivity.
            }
        }));
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        mIntent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mIntent);
        return;

    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);

    }


}
