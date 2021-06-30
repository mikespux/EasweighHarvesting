package com.plantation.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;

/**
 * Created by Michael on 30/06/2016.
 */
public class DivisionsDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddDivisions, btn_svDivision;
    ListView listDivisions;
    EditText dv_code, dv_name, es_code;
    String s_dv_code, s_dv_name, s_es_code;
    String accountId, Divisioncode;
    TextView textAccountId;
    Boolean success = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Divisions");

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

        dbhelper = new DBHelper(getApplicationContext());
        btAddDivisions = findViewById(R.id.btAddUser);
        btAddDivisions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDivision();
            }
        });
        listDivisions = this.findViewById(R.id.lvUsers);
        listDivisions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                // Intent intent = new Intent(Activity_ListStock.this, UpdateStock.class);
                // intent.putExtra("accountid", textAccountId.getText().toString());
                // startActivity(intent);
                showUpdateDivision();
            }
        });


    }

    public void showAddDivision() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_division, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Divisions");
        dv_code = dialogView.findViewById(R.id.dv_code);
        dv_name = dialogView.findViewById(R.id.dv_name);
        es_code = dialogView.findViewById(R.id.es_code);


        btn_svDivision = dialogView.findViewById(R.id.btn_svDivision);
        btn_svDivision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_dv_code = dv_code.getText().toString();
                    s_dv_name = dv_name.getText().toString();
                    s_es_code = es_code.getText().toString();


                    if (s_dv_code.equals("") || s_dv_name.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkDivision = dbhelper.CheckDivision(s_dv_code);
                    //Check for duplicate id number
                    if (checkDivision.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Division already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddDivision(s_dv_code, s_dv_name, s_es_code);
                    if (success) {


                        Toast.makeText(DivisionsDetailsActivity.this, "Division Saved successfully!!", Toast.LENGTH_LONG).show();

                        dv_code.setText("");
                        dv_name.setText("");
                        es_code.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(DivisionsDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
                    }

                }

            }
        });

        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                getdata();

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                getdata();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showUpdateDivision() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_division, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Divisions");
        accountId = textAccountId.getText().toString();


        dv_code = dialogView.findViewById(R.id.dv_code);
        dv_code.setEnabled(false);
        dv_name = dialogView.findViewById(R.id.dv_name);
        es_code = dialogView.findViewById(R.id.es_code);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.DIVISIONS_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            dv_code.setText(account.getString(account
                    .getColumnIndex(Database.DV_ID)));
            dv_name.setText(account.getString(account
                    .getColumnIndex(Database.DV_NAME)));
            es_code.setText(account.getString(account
                    .getColumnIndex(Database.DV_ESTATE)));


        }
        account.close();
        db.close();
        dbhelper.close();


        btn_svDivision = dialogView.findViewById(R.id.btn_svDivision);
        btn_svDivision.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteDivision();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateDivision();
                getdata();


            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        getdata();
    }

    public void updateDivision() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.DV_ID, dv_code.getText().toString());
            values.put(Database.DV_NAME, dv_name.getText().toString());
            values.put(Database.DV_ESTATE, es_code.getText().toString());


            long rows = db.update(Database.DIVISIONS_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            db.close();
            if (rows > 0) {
                Toast.makeText(this, "Updated Division Successfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Division!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteDivision() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Division?")
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
           /* Cursor c=db.rawQuery("select fzCode from Divisions where _id= '"+accountId+"' ", null);
            if(c!=null)
            {
                c.moveToFirst();
                Divisioncode= c.getString(c.getColumnIndex("fzCode"));
            }
            c.close();

            Cursor c1=db.rawQuery("select * from CollectionCenters where MccDivision= '" + Divisioncode + "' ", null);
            if(c1.getCount() > 0){
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Could not delete Division! ,Because its related in sheds");
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(this, "Could not delete Division! ,Because its related in sheds", Toast.LENGTH_LONG).show();
                c1.close();
            }*/
            // else{

            int rows = db.delete(Database.DIVISIONS_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if (rows == 1) {
                Toast.makeText(this, "Division Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            } else {
                Toast.makeText(this, "Could not delete Division!", Toast.LENGTH_LONG).show();
            }
            // }

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {
            int ROWID = 0;
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.DIVISIONS_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);

            String[] from = {Database.ROW_ID, Database.DV_ID, Database.DV_NAME};
            int[] to = {R.id.txtAccountId, R.id.tvCode, R.id.txtUserType};

            @SuppressWarnings("deprecation")
            SimpleCursorAdapter ca = new SimpleCursorAdapter(this, R.layout.userlist, accounts, from, to);

            ListView listusers = this.findViewById(R.id.lvUsers);
            listusers.setAdapter(ca);
            dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


}
