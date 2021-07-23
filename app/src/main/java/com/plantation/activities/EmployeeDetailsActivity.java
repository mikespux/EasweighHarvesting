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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SearchView;
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
public class EmployeeDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddAgt, btn_svAgt;
    ListView listEmployees;
    EditText emID, emName, emIDNo, emPickerNo, emCardID;
    String s_emID, s_emName, s_emPickerNo, s_emIDNo, s_emCardID;
    String accountId;
    TextView textAccountId;
    Boolean success = true;
    public SimpleCursorAdapter ca;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employees);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Employees");

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
        btAddAgt = findViewById(R.id.btAddUser);
        btAddAgt.setVisibility(View.GONE);
        btAddAgt.setOnClickListener(v -> showAddUserDialog());
        listEmployees = this.findViewById(R.id.lvEmployee);
        listEmployees.setOnItemClickListener((parent, selectedView, arg2, arg3) -> {
            textAccountId = selectedView.findViewById(R.id.txtAccountId);
            Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
            showUpdateUserDialog();
        });

        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search Employee No ...");
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
        searchView.requestFocus();

    }

    public void showAddUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_employee, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Employees");
        emID = dialogView.findViewById(R.id.emID);
        emName = dialogView.findViewById(R.id.emName);
        emIDNo = dialogView.findViewById(R.id.emIDNo);
        emPickerNo = dialogView.findViewById(R.id.emPickerNo);
        emCardID = dialogView.findViewById(R.id.emCardID);


        btn_svAgt = dialogView.findViewById(R.id.btn_svAgt);
        btn_svAgt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_emID = emID.getText().toString();
                    s_emName = emName.getText().toString();
                    s_emIDNo = emIDNo.getText().toString();
                    s_emCardID = emCardID.getText().toString();
                    s_emPickerNo = emPickerNo.getText().toString();

                    if (s_emID.equals("") || s_emName.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkEmployee = dbhelper.CheckEM(s_emID);
                    //Check for duplicate id number
                    if (checkEmployee.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Employee already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddEM(s_emID, s_emName, s_emIDNo, s_emCardID, s_emPickerNo, "", "");
                    if (success) {


                        Toast.makeText(EmployeeDetailsActivity.this, "Employee Saved successfully!!", Toast.LENGTH_LONG).show();

                        emID.setText("");
                        emName.setText("");
                        emIDNo.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(EmployeeDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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

    public void showUpdateUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_employee, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Employees");
        accountId = textAccountId.getText().toString();

        emID = dialogView.findViewById(R.id.emID);
        emID.setEnabled(false);
        emName = dialogView.findViewById(R.id.emName);
        emIDNo = dialogView.findViewById(R.id.emIDNo);
        emPickerNo = dialogView.findViewById(R.id.emPickerNo);
        emCardID = dialogView.findViewById(R.id.emCardID);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.EM_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            emID.setText(account.getString(account
                    .getColumnIndex(Database.EM_ID)));
            emName.setText(account.getString(account
                    .getColumnIndex(Database.EM_NAME)));
            emIDNo.setText(account.getString(account
                    .getColumnIndex(Database.EM_IDNO)));

            emPickerNo.setText(account.getString(account
                    .getColumnIndex(Database.EM_PICKERNO)));

            emCardID.setText(account.getString(account
                    .getColumnIndex(Database.EM_CARDID)));


        }
        account.close();
        db.close();
        dbhelper.close();


        btn_svAgt = dialogView.findViewById(R.id.btn_svAgt);
        btn_svAgt.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteEmployee();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateEmployee();
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

    public void updateEmployee() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.EM_ID, emID.getText().toString());
            values.put(Database.EM_NAME, emName.getText().toString());
            values.put(Database.EM_IDNO, emIDNo.getText().toString());
            values.put(Database.EM_PICKERNO, emPickerNo.getText().toString());
            values.put(Database.EM_CARDID, emCardID.getText().toString());


            long rows = db.update(Database.EM_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            db.close();
            if (rows > 0) {
                Toast.makeText(this, "Updated Employee Successfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Employee!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteEmployee() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this employee?")
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
           /* Cursor c=db.rawQuery("select fzCode from employees where _id= '"+accountId+"' ", null);
            if(c!=null)
            {
                c.moveToFirst();
                employeecode= c.getString(c.getColumnIndex("fzCode"));
            }
            c.close();

            Cursor c1=db.rawQuery("select * from CollectionCenters where MccEmployee= '" + employeecode + "' ", null);
            if(c1.getCount() > 0){
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Could not delete employee! ,Because its related in sheds");
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(this, "Could not delete employee! ,Because its related in sheds", Toast.LENGTH_LONG).show();
                c1.close();
            }
            else{*/

            int rows = db.delete(Database.EM_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if (rows == 1) {
                Toast.makeText(this, "Employee Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            } else {
                Toast.makeText(this, "Could not delete employee!", Toast.LENGTH_LONG).show();
            }
            //}

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
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

            listEmployees = this.findViewById(R.id.lvEmployee);
            listEmployees.setAdapter(ca);
            listEmployees.setTextFilterEnabled(true);
            //dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


}
