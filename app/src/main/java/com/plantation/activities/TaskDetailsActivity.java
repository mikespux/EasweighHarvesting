package com.plantation.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
public class TaskDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    ListView listTasks;
    public SimpleCursorAdapter ca;
    Button btAddTasks, btn_svTask;
    EditText tkcode, tkname, tktype;
    CheckBox overtime, multiple;
    String accountId;
    TextView textAccountId;
    Boolean success = true;


    SearchView searchView;
    String s_tkcode, s_tkname, s_tktype, s_overtime, s_multiple;
    Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listtasks);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tasks");

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
        btAddTasks = findViewById(R.id.btAddTasks);
        btAddTasks.setVisibility(View.GONE);
        btAddTasks.setOnClickListener(v -> showAddTask());
        listTasks = this.findViewById(R.id.lvTasks);
        listTasks.setOnItemClickListener((parent, selectedView, arg2, arg3) -> {
            textAccountId = selectedView.findViewById(R.id.txtAccountId);
            Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
            showUpdateTask();

        });

        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search Task Code ...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String TaskCode = constraint.toString();
                        return dbhelper.SearchSpecificTask(TaskCode);

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
                        String TaskCode = constraint.toString();
                        return dbhelper.SearchTask(TaskCode);

                    }
                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        searchView.requestFocus();


    }
    public void showAddTask() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Task");
        tkcode = dialogView.findViewById(R.id.task_code);
        tkname = dialogView.findViewById(R.id.task_name);
        tktype = dialogView.findViewById(R.id.task_type);
        overtime = dialogView.findViewById(R.id.overtime);
        multiple = dialogView.findViewById(R.id.multiple);


        btn_svTask = dialogView.findViewById(R.id.btn_svTask);
        btn_svTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_tkcode = tkcode.getText().toString();
                    s_tkname = tkname.getText().toString();
                    s_tktype = tktype.getText().toString();

                    if (overtime.isChecked()) {
                        s_overtime = "1";
                    } else {
                        s_overtime = "0";
                    }
                    if (multiple.isChecked()) {
                        s_multiple = "1";
                    } else {
                        s_multiple = "0";
                    }

                    if (s_tkcode.equals("") || s_tkname.equals("") || s_tktype.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checktask = dbhelper.CheckTask(s_tkcode);
                    //Check for duplicate id number
                    if (checktask.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Task already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddTask(s_tkcode, s_tkname, s_tktype, s_overtime, s_multiple, "");
                    if (success) {


                        Toast.makeText(TaskDetailsActivity.this, "Task Saved successfully!!", Toast.LENGTH_LONG).show();

                        tkcode.setText("");
                        tkname.setText("");
                        tktype.setText("");
                        overtime.setChecked(false);
                        multiple.setChecked(false);

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(TaskDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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

    public void showUpdateTask() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Tasks");
        accountId = textAccountId.getText().toString();

        tkcode = dialogView.findViewById(R.id.task_code);
        tkcode.setEnabled(false);
        tkname = dialogView.findViewById(R.id.task_name);
        tktype = dialogView.findViewById(R.id.task_type);
        overtime = dialogView.findViewById(R.id.overtime);
        multiple = dialogView.findViewById(R.id.multiple);

        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.TASK_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            tkcode.setText(account.getString(account
                    .getColumnIndex(Database.TK_ID)));
            tkname.setText(account.getString(account
                    .getColumnIndex(Database.TK_NAME)));
            tktype.setText(account.getString(account
                    .getColumnIndex(Database.TK_TYPE)));

            int otime, mtime;
            otime = Integer.parseInt(account.getString(account.getColumnIndex(Database.TK_OT)));
            mtime = Integer.parseInt(account.getString(account.getColumnIndex(Database.TK_MT)));
            overtime.setChecked(otime == 1);
            multiple.setChecked(mtime == 1);


        }
        account.close();
        db.close();
        dbhelper.close();



        btn_svTask = dialogView.findViewById(R.id.btn_svTask);
        btn_svTask.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteTask();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateTask();
                getdata();



            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
    public void updateTask() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command
            s_tkcode = tkcode.getText().toString();
            s_tkname = tkname.getText().toString();
            s_tktype = tktype.getText().toString();

            if (overtime.isChecked()) {
                s_overtime = "1";
            } else {
                s_overtime = "0";
            }
            if (multiple.isChecked()) {
                s_multiple = "1";
            } else {
                s_multiple = "0";
            }
            ContentValues values = new ContentValues();
            values.put(Database.TK_NAME, s_tkname);
            values.put(Database.TK_TYPE, s_tktype);
            values.put(Database.TK_OT, s_overtime);
            values.put(Database.TK_MT, s_multiple);

            long rows = db.update(Database.TASK_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            db.close();
            if (rows > 0) {
                Toast.makeText(this, "Updated Task Successfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Task!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this task?")
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
            int rows = db.delete(Database.TASK_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if (rows == 1) {
                Toast.makeText(this, "Task Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            } else
                Toast.makeText(this, "Could not delete task!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        getdata();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {
            int ROWID = 0;
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.TASK_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);
            if (accounts.getCount() == 0) {
                Toast.makeText(this, "no task records", Toast.LENGTH_LONG).show();
            }
            String[] from = {Database.ROW_ID, Database.TK_ID, Database.TK_NAME, Database.TK_TYPE, Database.TK_OT, Database.TK_MT};
            int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_name, R.id.tv_type, R.id.tv_ot, R.id.tv_multiple};


            ca = new SimpleCursorAdapter(this, R.layout.task_list, accounts, from, to);

            listTasks = this.findViewById(R.id.lvTasks);
            listTasks.setAdapter(ca);
            listTasks.setTextFilterEnabled(true);
            dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        // mIntent = new Intent(FarmerDetailsActivity.this,MainActivity.class);
        //startActivity(mIntent);
        return;
    }


}
