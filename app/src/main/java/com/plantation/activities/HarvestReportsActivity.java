package com.plantation.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.plantation.R;
import com.plantation.helpers.CustomListReport;


public class HarvestReportsActivity extends AppCompatActivity {

    static SharedPreferences mSharedPrefs;
    public View mView;
    public Intent mIntent;
    public LinearLayoutManager layoutManager;
    public Context mContext;
    public Toolbar toolbar;
    SharedPreferences prefs;
    ListView list;
    String[] web = {
            "Employee Report", "Batch Report"
            //,"Delivery Report"
    };
    Integer[] imageId = {

            R.drawable.ic_receipt,
            R.drawable.ic_zreport
            //R.drawable.ic_delivary
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.harvest_report);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Harvest Reports");

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
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        CustomListReport adapter = new
                CustomListReport(this, web, imageId);
        list = findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, View view,
                                    int position, long id) {
                if (position == 1) {
                    //Intent myIntent = new Intent(Ali10Activity.this, Hassan.class);
                    //startActivity(myIntent);
                }
                switch (position) {
                    case 0:
                        mIntent = new Intent(getApplicationContext(), EmployeeDetailedRecieptsActivity.class);
                        startActivity(mIntent);
                        break;
                    case 1:
                        mIntent = new Intent(getApplicationContext(), HarvestRecieptsActivity.class);
                        startActivity(mIntent);
                        break;
                    /*case 2:

                        mIntent = new Intent(getApplicationContext(),DeliveryReportActivity.class);
                        startActivity(mIntent);
                        //mIntent = new Intent(getApplicationContext(),EmployeeDetailsActivity.class);//Agent Details
                        break;
*/

                    default:
                        break;
                }


            }
        });

    }


}
