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
public class BlocksDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddBlock, btn_svBlock;
    ListView listBlocks;
    EditText fd_code, bk_code;
    String s_fd_code, s_bk_code;
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
        getSupportActionBar().setTitle("Blocks");

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
        btAddBlock = findViewById(R.id.btAddUser);
        btAddBlock.setVisibility(View.GONE);
        btAddBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBlock();
            }
        });
        listBlocks = this.findViewById(R.id.lvUsers);
        listBlocks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                // Intent intent = new Intent(Activity_ListStock.this, UpdateStock.class);
                // intent.putExtra("accountid", textAccountId.getText().toString());
                // startActivity(intent);
                showUpdateBlock();
            }
        });


    }

    public void showAddBlock() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_block, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Blocks");
        fd_code = dialogView.findViewById(R.id.fd_code);
        bk_code = dialogView.findViewById(R.id.bk_code);
        // company = (EditText) dialogView.findViewById(R.id.company);


        btn_svBlock = dialogView.findViewById(R.id.btn_svBlock);
        btn_svBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_fd_code = fd_code.getText().toString();
                    s_bk_code = bk_code.getText().toString();
                    // s_company = company.getText().toString();


                    if (s_fd_code.equals("") || s_bk_code.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkBlock = dbhelper.CheckBlock(s_fd_code);
                    //Check for duplicate id number
                    if (checkBlock.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Block already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddBlock(s_fd_code, s_bk_code, "");
                    if (success) {


                        Toast.makeText(BlocksDetailsActivity.this, "Block Saved successfully!!", Toast.LENGTH_LONG).show();

                        fd_code.setText("");
                        bk_code.setText("");
                        // company.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(BlocksDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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

    public void showUpdateBlock() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_block, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Blocks");
        accountId = textAccountId.getText().toString();

        fd_code = dialogView.findViewById(R.id.fd_code);
        fd_code.setEnabled(false);
        bk_code = dialogView.findViewById(R.id.bk_code);
        // company = (EditText) dialogView.findViewById(R.id.company);

        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.BLOCK_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            fd_code.setText(account.getString(account
                    .getColumnIndex(Database.BK_ID)));
            bk_code.setText(account.getString(account
                    .getColumnIndex(Database.BK_FIELD)));


        }
        account.close();
        db.close();
        dbhelper.close();


        btn_svBlock = dialogView.findViewById(R.id.btn_svBlock);
        btn_svBlock.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteBlock();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateBlock();
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

    public void updateBlock() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.BK_ID, fd_code.getText().toString());
            values.put(Database.BK_FIELD, bk_code.getText().toString());


            long rows = db.update(Database.BLOCK_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            db.close();
            if (rows > 0) {
                Toast.makeText(this, "Updated Block Successfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Block!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteBlock() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Block?")
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
            int rows = db.delete(Database.BLOCK_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if (rows == 1) {
                Toast.makeText(this, "Block Deleted Successfully!", Toast.LENGTH_LONG).show();

                getdata();
            } else {
                Toast.makeText(this, "Could not delete Block!", Toast.LENGTH_LONG).show();
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
            Cursor accounts = db.query(true, Database.BLOCK_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);

            String[] from = {Database.ROW_ID, Database.BK_ID, Database.BK_FIELD};
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
