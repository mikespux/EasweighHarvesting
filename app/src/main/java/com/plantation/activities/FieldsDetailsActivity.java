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
public class FieldsDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddField, btn_svField;
    ListView listFields;
    EditText fd_code, dv_code;
    String s_fd_code, s_dv_code;
    String accountId;
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
        getSupportActionBar().setTitle("Fields");

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
        btAddField = findViewById(R.id.btAddUser);
        btAddField.setVisibility(View.GONE);
        btAddField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddField();
            }
        });
        listFields = this.findViewById(R.id.lvUsers);
        listFields.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                // Intent intent = new Intent(Activity_ListStock.this, UpdateStock.class);
                // intent.putExtra("accountid", textAccountId.getText().toString());
                // startActivity(intent);
                showUpdateFields();
            }
        });


    }

    public void showAddField() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_field, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Fields");
        fd_code = dialogView.findViewById(R.id.fd_code);
        dv_code = dialogView.findViewById(R.id.dv_code);
        // company = (EditText) dialogView.findViewById(R.id.company);


        btn_svField = dialogView.findViewById(R.id.btn_svField);
        btn_svField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_fd_code = fd_code.getText().toString();
                    s_dv_code = dv_code.getText().toString();
                    // s_company = company.getText().toString();


                    if (s_fd_code.equals("") || s_dv_code.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkField = dbhelper.CheckField(s_fd_code);
                    //Check for duplicate id number
                    if (checkField.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Field already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddField(s_fd_code, s_dv_code, "");
                    if (success) {


                        Toast.makeText(FieldsDetailsActivity.this, "Field Saved successfully!!", Toast.LENGTH_LONG).show();

                        fd_code.setText("");
                        dv_code.setText("");
                        // company.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(FieldsDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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

    public void showUpdateFields() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_field, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Fields");
        accountId = textAccountId.getText().toString();

        fd_code = dialogView.findViewById(R.id.fd_code);
        fd_code.setEnabled(false);
        dv_code = dialogView.findViewById(R.id.dv_code);
        // company = (EditText) dialogView.findViewById(R.id.company);

        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.FIELD_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            fd_code.setText(account.getString(account
                    .getColumnIndex(Database.FD_ID)));
            dv_code.setText(account.getString(account
                    .getColumnIndex(Database.FD_DIVISION)));


        }
        account.close();
        db.close();
        dbhelper.close();


        btn_svField = dialogView.findViewById(R.id.btn_svField);
        btn_svField.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteField();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateField();
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

    public void updateField() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.FD_ID, fd_code.getText().toString());
            values.put(Database.FD_DIVISION, dv_code.getText().toString());


            long rows = db.update(Database.FIELD_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            db.close();
            if (rows > 0) {
                Toast.makeText(this, "Updated Field Successfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Field!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteField() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this field?")
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
            int rows = db.delete(Database.FIELD_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if (rows == 1) {
                Toast.makeText(this, "Field Deleted Successfully!", Toast.LENGTH_LONG).show();

                getdata();
            } else {
                Toast.makeText(this, "Could not delete field!", Toast.LENGTH_LONG).show();
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
            Cursor accounts = db.query(true, Database.FIELD_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);

            String[] from = {Database.ROW_ID, Database.FD_ID, Database.FD_DIVISION};
            int[] to = {R.id.txtAccountId, R.id.txtUserName, R.id.txtUserType};

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
