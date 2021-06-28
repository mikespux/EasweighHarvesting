package com.plantation.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.soap.SoapRequest;
import com.plantation.synctocloud.RestApiRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.modificator.waterwave_progress.WaterWaveProgress;


public class UploadActivity extends AppCompatActivity {
	static SharedPreferences prefs;
	public Toolbar toolbar;
	public SimpleCursorAdapter ca;
	Button btnUpload, btnCancel, btnSignOff;
	int accesslevel = 0;
	int useridentifier = 1;
	String username, userpass;
	String user_level;
	DBHelper dbhelper;
	SoapRequest request;
	String batchInfo;
	String batchNo, BatchNumber, deviceID, stringOpenDate, deliveryNoteNo, Weight, dipatchedTime, userID, userID2, stringOpenTime, weighingSession,
			closedb, stringCloseTime, factory, tractorNo, trailerNo, TransporterCode, DelivaryNo, Co_prefix, Current_User, UserName;
	String weighmentInfo, taskInfo;
	String totalWeight;
	String BatchNo;
	String BatchDte;
	String ColDate, Time, DataDevice, BatchNO, TaskCode, EmployeeNo;
	String WorkerNo, FieldClerk, ProduceCode, TaskUnits, TaskType;
	String VarietyCode, GradeCode, EstateCode, DivisionCode, Project;
	String GrossTotal, TareWeight, Crates;
	String UnitPrice, RecieptNo, WeighmentNo, NetWeight, FieldCode, Block;
	String CheckinMethod, CheckoutMethod, CheckoutTime;
	String serverBatchID;
	String shedCode;
	String returnValue;
	String TicketNo, DNoteNo, DelDate, Factory, Transporter, Vehicle, Tractor, ArrivalTime, FieldWt, GrossWt, TareWt,
			RejectWt, QualityScore, DepartureTime, CoPrefix, InternalSerial, UserIdentifier, CloudID;
	String DeliveryNo;
	String DeliveryInfo;
	SimpleDateFormat dateTimeFormat;
	SimpleDateFormat timeFormat;
	SimpleDateFormat timeFormat1;
	SimpleDateFormat dateFormat;
	SimpleDateFormat dateOnlyFormat;
	SimpleDateFormat BatchDateFormat;
	ArcProgress arcProgress;
	WaterWaveProgress waveProgress;
	EditText etFrom, etTo, etFarmerNo;
	String fromDate, toDate, farmerNo;
	String condition = " _id > 0 ";
	String condition1 = " _id > 0 ";
	AlertDialog b;
	SQLiteDatabase db;
	Intent mIntent;
	ListView listReciepts;
	String BatchDate;
	TextView textBatchNo, textBatchDate, textDelNo, textDelivaryNo, textStatus;
	String cond;
	SearchView searchView;
	int closed1 = 1;
	int cloudid = 0;
	String DelNo;
	String error, errorNo;
	Cursor curBatchNames, batches;
	Cursor produce;
	Cursor tasks;
	String BatchCloudID, SignOffInfo;
	private final String TAG = "Vik";
	private String checkListReturnValue;
	private SharedPreferences mSharedPrefs;
	private final int totalRecords = 0;
	private int progressStatus = 0;
	private int count = 0;
	String Id, Title, Message;
	private TextView textView, txtFNo;
	private Button pickFrom, pickTo;
	private Button btnSearchReceipt;
	private Button btnFilter;
	String VId, VTitle, VMessage;
	String BatchDel;
	private String soapResponse, serverBatchNo, BatchID;
	private String restApiResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
		/*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);*/
        setupToolbar();
        initializer();
    }

	public void setupToolbar() {
		toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(R.string.title_upload);

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
	}

	public void onBackPressed() {
		//Display alert message when back button has been pressed
		finish();

		return;
	}

	public void initializer() {
		dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		BatchDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
		timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		timeFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs = PreferenceManager.getDefaultSharedPreferences(UploadActivity.this);
		dbhelper = new DBHelper(getApplicationContext());
		db = dbhelper.getReadableDatabase();
		request = new SoapRequest(getApplicationContext());
		btnFilter = (Button) findViewById(R.id.btnFilter);
		btnFilter.setVisibility(View.GONE);
		btnFilter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new Restart().execute();
				showSearchReceipt();
			}
		});
		btnUpload = (Button) findViewById(R.id.btnUpload);
		btnUpload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (!checkList()) {
					return;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
				builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Upload Data?</font>"))
						.setCancelable(false)
						.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								new AsyncCallWS().execute();

							}
						})
						.setPositiveButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();

							}
						});
				AlertDialog alert2 = builder.create();
				alert2.show();

			}
		});
		btnSignOff = (Button) findViewById(R.id.btnSignOff);
		username = prefs.getString("user", "");
		Cursor d = dbhelper.getAccessLevel(username);
		user_level = d.getString(accesslevel);
		if (user_level.equals("2")) {
			btnSignOff.setVisibility(View.GONE);
		} else {
			btnSignOff.setVisibility(View.VISIBLE);
		}
		btnSignOff.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listReciepts.performItemClick(listReciepts.getAdapter().getView(0, null, null), 0, listReciepts.getAdapter().getItemId(0));
				DelNo = textDelNo.getText().toString();
				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
				builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Are you sure you want to SignOff Batch <b>(" + DelNo + ")</b>?</font>"))
						.setCancelable(false)
						.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								ContentValues values = new ContentValues();
								values.put(Database.BatCloudID, serverBatchNo);
								long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
										Database.DeliveryNoteNumber + " = ?", new String[]{DelNo});

								if (rows > 0) {

								}
								Toast.makeText(UploadActivity.this, "Batch " + DelNo + " Signed Off Successfully !!!", Toast.LENGTH_LONG).show();
								new Restart().execute();


							}
						})
						.setPositiveButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();

							}
						});
				AlertDialog alert2 = builder.create();
				alert2.show();
			}
		});
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();

			}
		});
		listReciepts = (ListView) this.findViewById(R.id.lvReciepts);

		listReciepts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View selectedView, int position, long arg3) {


				if (position == 0) {

					textBatchNo = (TextView) selectedView.findViewById(R.id.tv_reciept);
					textBatchDate = (TextView) selectedView.findViewById(R.id.tv_date);
					textDelNo = (TextView) selectedView.findViewById(R.id.tv_number);
					textDelivaryNo = (TextView) selectedView.findViewById(R.id.tv_device);
					Log.d("Accounts", "Selected Account Id : " + textBatchNo.getText().toString());

					//Toast.makeText(UploadActivity.this,textBatchDate.getText().toString()+ textBatchNo.getText().toString(), Toast.LENGTH_LONG).show();
				} else {

					//Toast.makeText(UploadActivity.this,"empty", Toast.LENGTH_LONG).show();
				}
				//showRecieptDetails();


			}


		});


		if (!checkList()) {
			finish();
			return;
		}

		String selectQuery = "SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE SignedOff=1";
		Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() <= 0) {
            Toast.makeText(UploadActivity.this, "No Batch Dispatched to Upload!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        cursor.close();
        //showSearchReceipt();

    }

    private boolean checkList() {

        try {
            if (mSharedPrefs.getBoolean("cloudServices", false)) {
                try {
                    if (mSharedPrefs.getString("internetAccessModes", null).equals(null)) {
                        Toast.makeText(getApplicationContext(), "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
                        return false;

                    }
                    try {
                        if (mSharedPrefs.getString("licenseKey", null).equals(null) || mSharedPrefs.getString("licenseKey", null).equals(XmlPullParser.NO_NAMESPACE)) {
                            //this.checkListReturnValue = "License key not found!";
                            // Toast.makeText(getApplicationContext(), "License key not found!", Toast.LENGTH_LONG).show();
                            //  return false;
                        }
                        try {
                            if (!mSharedPrefs.getString("portalURL", null).equals(null) && !mSharedPrefs.getString("portalURL", null).equals(XmlPullParser.NO_NAMESPACE)) {
                                return true;
                            }
                            //this.checkListReturnValue = "Portal URL not configured!";
                            Toast.makeText(getApplicationContext(), "Portal URL not configured!", Toast.LENGTH_LONG).show();
                            return false;
                        } catch (Exception e) {
                            //this.checkListReturnValue = "Portal URL not configured!";
                            Toast.makeText(getApplicationContext(), "Portal URL not configured!", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    } catch (Exception e2) {
                        //this.checkListReturnValue = "License key not found!";
                        Toast.makeText(getApplicationContext(), "License key not found!", Toast.LENGTH_LONG).show();
                        return false;
                    }

                } catch (Exception e3) {
                    e3.printStackTrace();
                    //this.checkListReturnValue = "Cloud Services not enabled!";
                    Toast.makeText(getApplicationContext(), "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            Toast.makeText(getApplicationContext(), "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
            return false;


            //this.checkListReturnValue = "Cloud Services not enabled!";

        } catch (Exception e4) {
            e4.printStackTrace();
            //this.checkListReturnValue = "Cloud Services not enabled!";
            Toast.makeText(getApplicationContext(), "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
            return false;
        }

    }

	public void showSearchReceipt() {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.dialog_search_batches, null);
		dialogBuilder.setView(dialogView);
		dialogBuilder.setCancelable(false);
		dialogBuilder.setTitle("Search Batches");
		etFrom = (EditText) dialogView.findViewById(R.id.edtFromDate);
		etTo = (EditText) dialogView.findViewById(R.id.edtToDate);
		etFarmerNo = (EditText) dialogView.findViewById(R.id.edtFarmerNo);

		Date date = new Date(getDate());
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		etFrom.setText(format1.format(date));
		etTo.setText(format1.format(date));

		pickFrom = (Button) dialogView.findViewById(R.id.btnFrom);
		pickFrom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();
				newFragment.show(getFragmentManager(), "datePicker");

			}
		});

		pickTo = (Button) dialogView.findViewById(R.id.btnTo);
		pickTo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment2();
				newFragment.show(getFragmentManager(), "datePicker");

			}
		});


		btnSearchReceipt = (Button) dialogView.findViewById(R.id.btn_SearchReceipt);
		btnSearchReceipt.setVisibility(View.VISIBLE);
		btnSearchReceipt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fromDate = etFrom.getText().toString();
				toDate = etTo.getText().toString();
				farmerNo = etFarmerNo.getText().toString();

				SharedPreferences.Editor edit = prefs.edit();
				edit.putString("fromDate", fromDate);
				edit.commit();
				edit.putString("toDate", toDate);
				edit.commit();
				edit.putString("farmerNo", farmerNo);
				edit.commit();

				if (fromDate.length() > 0)
					condition += " and  " + Database.BatchDate + " >= '" + fromDate + "'";
				else
					new Restart().execute();

				if (toDate.length() > 0)
					condition += " and  " + Database.BatchDate + " <= '" + toDate + "'";

				if (closed1 > 0)
					condition += " and  " + Database.Closed + " = '" + closed1 + "'";

				//getSearch();
				ca.getFilter().filter(condition);
				ca.setFilterQueryProvider(new FilterQueryProvider() {

					@Override
					public Cursor runQuery(CharSequence constraint) {
						String reciept = constraint.toString();
						return dbhelper.SearchBatchByDate(reciept);
					}
				});

				b.dismiss();
			}
		});

		dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				return keyCode == KeyEvent.KEYCODE_BACK;
			}
		});
		dialogBuilder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//pass
				//getdata();
				finish();
			}
		});
		b = dialogBuilder.create();
		b.show();

	}

	public void showRecieptDetails() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.activity_listclosedbatches, null);
		dialogBuilder.setView(dialogView);
		dialogBuilder.setTitle("All Weighment Receipts");
		dbhelper = new DBHelper(this);
		db = dbhelper.getReadableDatabase();

		BatchNo = textBatchNo.getText().toString();
		String dbtBatchOn = textBatchDate.getText().toString() + " 00:00:00";
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = fmt.parse(dbtBatchOn);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
		BatchDate = format1.format(date);
		if (BatchDate.length() > 0)
			cond += " and  " + Database.CollDate + " = '" + BatchDate + "'";

		if (BatchNo.length() > 0)
			cond += " and  " + Database.BatchNumber + " = '" + BatchNo + "'";

		searchView = (SearchView) dialogView.findViewById(R.id.searchView);
		searchView.setQueryHint("Search Farmer No ...");
		searchView.setVisibility(View.GONE);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				ca.getFilter().filter(query);
				ca.setFilterQueryProvider(new FilterQueryProvider() {

					@Override
					public Cursor runQuery(CharSequence constraint) {
						String FarmerNo = constraint.toString();
						return dbhelper.SearchSpecificOnR(FarmerNo, cond);

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
						String FarmerNo = constraint.toString();
						return dbhelper.SearchOnR(FarmerNo);

					}
				});
				//Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
				return false;
			}
		});


		Cursor delivery = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
				+ Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNumber + " ='" + BatchNo + "'", null);
		TextView txtStatus = (TextView) dialogView.findViewById(R.id.textStatus);

		if (delivery.getCount() == 0) {
			txtStatus.setVisibility(View.VISIBLE);
			searchView.setVisibility(View.GONE);
		} else {
			//Toast.makeText(this, "records found", Toast.LENGTH_LONG).show();}


			final DecimalFormat df = new DecimalFormat("#0.0#");
			Cursor c = db.rawQuery("select " +
					"" + Database.DataCaptureDevice +
					",COUNT(" + Database.ROW_ID + ")" +
					",SUM(" + Database.Tareweight + ")" +
					",SUM(" + Database.NetWeight + ")" +
					" from FarmersProduceCollection WHERE "
					+ Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNumber + " ='" + BatchNo + "'", null);
			if (c != null) {

				c.moveToFirst();
				txtStatus.setVisibility(View.VISIBLE);
				txtStatus.setText("Weighments: " + df.format(c.getDouble(1)) + "\n" +
						"Net Weight: " + df.format(c.getDouble(3)) + " Kgs.");

			}
			c.close();

		}
		while (delivery.moveToNext()) {
			String[] from = {Database.ROW_ID, Database.EmployeeNo, Database.NetWeight};
			int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_phone};


			ca = new SimpleCursorAdapter(dialogView.getContext(), R.layout.z_list, delivery, from, to);

			ListView listBatches = (ListView) dialogView.findViewById(R.id.lvUsers);
			listBatches.setAdapter(ca);
			listBatches.setTextFilterEnabled(true);
			//db.close();
			//dbhelper.close();
		}


		dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {


			}
		});
		dialogBuilder.setNegativeButton("Upload", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		b = dialogBuilder.create();
		b.show();
		b.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
		b.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.BLUE);
		b.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
		b.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);

		b.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!checkList()) {
					return;
				}


				AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
				builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Upload Data?</font>"))
						.setCancelable(false)
						.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								new AsyncCallWS().execute();

							}
						})
						.setPositiveButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								b.dismiss();

							}
						});
				final AlertDialog alert2 = builder.create();
				alert2.show();
				Boolean wantToCloseDialog = false;
				//Do stuff, possibly set wantToCloseDialog to true then...
				if (wantToCloseDialog)
					b.dismiss();
				//else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onStart() {
		super.onStart();
		getdata();


	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void getdata() {

		try {
		/*	ContentValues values = new ContentValues();
			values.put(Database.BatCloudID, 0);

			long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
					"DeliveryNoteNumber = ?", new String[]{"40022020020401"});
			if(rows>0){

			}*/

            SQLiteDatabase db = dbhelper.getReadableDatabase();

//			Cursor delivery = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
//					+ Database.Closed + " ='" + closed1 + "' and " + Database.BatCloudID + " ='" + cloudid + "' and SignedOff=1", null);
            Cursor delivery = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null);
            if (delivery.getCount() > 0) {

				String[] from = {Database.ROW_ID, Database.DeliveryNoteNumber, Database.DelivaryNO,
						Database.BatchNumber, Database.BatchDate, Database.NoOfWeighments, Database.TotalWeights, Database.NoOfTasks};
				int[] to = {R.id.txtAccountId, R.id.tv_number, R.id.tv_device, R.id.tv_reciept, R.id.tv_date, R.id.txtWeigments, R.id.txtTotalKgs, R.id.txtTasks};


                ca = new SimpleCursorAdapter(this, R.layout.upload_list, delivery, from, to);

                listReciepts = (ListView) this.findViewById(R.id.lvReciepts);

				listReciepts.setAdapter(ca);
				ca.notifyDataSetChanged();
				listReciepts.setTextFilterEnabled(true);
				listReciepts.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				listReciepts.performItemClick(listReciepts.getAdapter().getView(0, null, null), 0, listReciepts.getAdapter().getItemId(0));

				//db.close();
				//dbhelper.close();


			} else {

				new NoReceipt().execute();
			}
		} catch (Exception ex) {
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private String getDate() {

		//A string to hold the current date
		String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

		//Return the current date
		return currentDateTimeString;
	}

	@SuppressLint("ValidFragment")
	public class DatePickerFragment extends DialogFragment
			implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getApplicationContext(), this, year, month, day);
        }

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			// edtBaseDate.setText(view.getYear() + "/" + view.getMonth() + "/" + view.getDayOfMonth());

			// Create a Date variable/object with user chosen date
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(0);
			cal.set(year, month, day, 0, 0, 0);
			Date chosenDate = cal.getTime();
			SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
			etFrom.setText(format1.format(chosenDate));
		}
	}

	@SuppressLint("ValidFragment")
	public class DatePickerFragment2 extends DialogFragment
			implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getApplicationContext(), this, year, month, day);
        }

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			// edtBaseDate.setText(view.getYear() + "/" + view.getMonth() + "/" + view.getDayOfMonth());
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(0);
			cal.set(year, month, day, 0, 0, 0);
			Date chosenDate = cal.getTime();
			SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
			etTo.setText(format1.format(chosenDate));
		}
	}

	private class AsyncCallWS extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute");

			waveProgress = (WaterWaveProgress) findViewById(R.id.waterWaveProgress1);
			waveProgress.setShowProgress(true);
			waveProgress.setProgress(0);
			waveProgress.animateWave();

			arcProgress = (ArcProgress) findViewById(R.id.arc_progress);
			arcProgress.setVisibility(View.VISIBLE);
			arcProgress.setProgress(0);

			textStatus = (TextView) findViewById(R.id.textStatus);
			textStatus.setVisibility(View.VISIBLE);

			listReciepts.setVisibility(View.GONE);
			btnFilter.setVisibility(View.GONE);
			listReciepts.performItemClick(listReciepts.getAdapter().getView(0, null, null), 0, listReciepts.getAdapter().getItemId(0));
			//soapResponse = new SoapRequest(UploadActivity.this).DoDataSourceTest();
			//Toast.makeText(getApplicationContext(),soapResponse,Toast.LENGTH_LONG);
		}

		@Override
		protected String doInBackground(String... aurl) {
			Log.i(TAG, "doInBackground");
			try {
				try {


					BatchNo = textBatchNo.getText().toString();
					DelNo = textDelivaryNo.getText().toString();
					String dbtBatchOn = textBatchDate.getText().toString() + " 00:00:00";
					String dbtBatchOn1 = textBatchDate.getText().toString();
					SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
					Date date = null;
					try {
						date = fmt.parse(dbtBatchOn);
					} catch (ParseException e) {
						e.printStackTrace();
					}
                    SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                    BatchDate = format1.format(date);
                    SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMdd");
                    BatchDte = format2.format(date);

                    if (dbtBatchOn1.length() > 0)
                        condition += " and  " + Database.BatchDate + " = '" + dbtBatchOn1 + "'";

                    if (BatchNo.length() > 0)
                        condition += " and  " + Database.BatchNumber + " = '" + BatchNo + "'";
//
//					if (closed1 > 0)
//						condition += " and  " + Database.Closed + " = '" + closed1 + "'";
//					condition += " and  " + Database.BatCloudID + " = '" + cloudid + "'"; and SignedOff=1


                    SQLiteDatabase db = dbhelper.getReadableDatabase();
                    batches = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where " + condition + "", null);
                    count = batches.getCount();

                    while (batches.moveToNext()) {
						Date openTime = dateTimeFormat.parse(batches.getString(batches.getColumnIndex(Database.BatchDate)) +
								" " +
								batches.getString(batches.getColumnIndex(Database.OpeningTime)));
						Date closeTime = dateTimeFormat.parse(batches.getString(batches.getColumnIndex(Database.BatchDate)) +
								" " +
								batches.getString(batches.getColumnIndex(Database.ClosingTime)));
						batchNo = batches.getString(batches.getColumnIndex(Database.BatchNumber));
						deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
						stringOpenDate = dateFormat.format(openTime);
						deliveryNoteNo = batches.getString(batches.getColumnIndex(Database.DeliveryNoteNumber));
						userID = batches.getString(batches.getColumnIndex(Database.Userid));
						stringOpenTime = timeFormat.format(openTime);
						if (batches.getString(batches.getColumnIndex(Database.BatchSession)) == null) {
							weighingSession = "1";
                        } else {
                            weighingSession = batches.getString(batches.getColumnIndex(Database.BatchSession));
                        }
                        closedb = batches.getString(batches.getColumnIndex(Database.Closed));
                        stringCloseTime = timeFormat.format(closeTime);
                        if (batches.getString(batches.getColumnIndex(Database.BatCloudID)) == null) {

                            BatchID = "0";
                        } else {
                            BatchID = batches.getString(batches.getColumnIndex(Database.BatCloudID));

                        }

                        if (batches.getString(batches.getColumnIndex(Database.BEstate)) == null) {

                            EstateCode = "";
                        } else {
                            EstateCode = batches.getString(batches.getColumnIndex(Database.BEstate));

                        }
                        if (batches.getString(batches.getColumnIndex(Database.BDivision)) == null) {
                            DivisionCode = "";
						} else {
							DivisionCode = batches.getString(batches.getColumnIndex(Database.BDivision));

						}
						if (batches.getString(batches.getColumnIndex(Database.TotalWeights)) == null) {
							Weight = "0";
						} else {
							Weight = batches.getString(batches.getColumnIndex(Database.TotalWeights));
						}

						if (batches.getString(batches.getColumnIndex(Database.Dispatched)) == null) {
							dipatchedTime = stringCloseTime;
						} else {
							dipatchedTime = batches.getString(batches.getColumnIndex(Database.Dispatched));
						}
						BatchDate = BatchDateFormat.format(closeTime);

						factory = batches.getString(batches.getColumnIndex(Database.Factory));
						if (batches.getString(batches.getColumnIndex(Database.Transporter)) == null) {
							TransporterCode = "";
						} else {
							TransporterCode = batches.getString(batches.getColumnIndex(Database.Transporter));
						}
						tractorNo = batches.getString(batches.getColumnIndex(Database.Tractor));
						trailerNo = batches.getString(batches.getColumnIndex(Database.Trailer));

						if (batches.getString(batches.getColumnIndex(Database.DelivaryNO)) == null) {
							DelivaryNo = "";
						} else {
							DelivaryNo = batches.getString(batches.getColumnIndex(Database.DelivaryNO));
						}
						Co_prefix = mSharedPrefs.getString("company_prefix", "");
						Current_User = prefs.getString("user", "");


						//	batch.append("1"+ ",");
						//	batch.append(stringCloseTime + ",");
						//	batch.append(Weight + ",");
						//	batch.append(dipatchedTime + ",");
						//	batch.append(factory + ",");
						//	batch.append(tractorNo + ",");
						//	batch.append(trailerNo + ",");
						//	batch.append(DelivaryNo + ",");

						//Which column you want to upload

						StringBuilder batch = new StringBuilder();

						batch.append(batchNo + ",");
						batch.append(deviceID + ",");
						batch.append(userID + ",");
						batch.append(deliveryNoteNo + ",");
						batch.append(stringOpenTime + ",");
						batch.append(Co_prefix + ",");
						batch.append(EstateCode + ",");
						batch.append(DivisionCode);

                        batchInfo = batch.toString();

                        batches.moveToNext();

                        progressStatus++;
                        publishProgress("" + progressStatus);

                    }
                    batches.close();
                    //request.createBatch(batchInfo);
					Log.i("BatchInfo", batchInfo);
					restApiResponse = new RestApiRequest(getApplicationContext()).CreateBatch(batchInfo);
                    error = restApiResponse;
                    try {

                        JSONObject jsonObject = new JSONObject(restApiResponse);
                        Message = jsonObject.getString("Message");
                        if (Message.equals("Authorization has been denied for this request.")) {
                            Id = "-1";
                            count = 0;
                            SharedPreferences.Editor edit = mSharedPrefs.edit();
                            edit.remove("token");
                            edit.commit();
                            edit.remove("expires_in");
                            edit.commit();
                            edit.remove("expires");
                            edit.commit();
                            return null;
                        }
                        if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                            Id = jsonObject.getString("Id");
                            Title = jsonObject.getString("Title");
                            Message = jsonObject.getString("Message");

                            Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
                            try {
                                if (Integer.valueOf(Id).intValue() > 0) {
                                    errorNo = "0";
                                    serverBatchNo = Id;
                                    ContentValues values = new ContentValues();
                                    values.put(Database.BatCloudID, serverBatchNo);
                                    long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                                            Database.DeliveryNoteNumber + " = ?", new String[]{deliveryNoteNo});

                                    if (rows > 0) {

                                        SharedPreferences.Editor edit = prefs.edit();
                                        edit.putString("serverBatchNo", serverBatchNo);
                                        edit.commit();
                                    }
                                }
                                if (Integer.valueOf(Id).intValue() < 0) {
                                    if (Integer.valueOf(Id).intValue() == -3313) {
                                        errorNo = "-3313";
                                        serverBatchNo = BatchID;
                                        Log.i("serverBatchNo", serverBatchNo);
                                    } else {
                                        error = Id;

                                        return null;

                                    }


                                }
                                //System.out.println(value);}
                            } catch (NumberFormatException e) {
                                //value = 0; // your default value
                                Id = "-1";
                                return null;

                            }
                        } else {
                            Id = "-1";
                            Title = "";
                            error = restApiResponse;
                            Message = restApiResponse;
                            count = 0;
                            return null;

                        }
                    } catch (JSONException e) {
                        Id = "-8080";
                        Title = "";
                        error = restApiResponse;
                        Message = restApiResponse;
                        count = 0;
                        e.printStackTrace();
                        return null;

                    }


                    produce = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
                            + Database.CollDate + " ='" + stringOpenDate + "' and " + Database.DataCaptureDevice + " ='" + deliveryNoteNo + "' and " + Database.CloudID + " <='" + cloudid + "' ", null);
                    count = count + produce.getCount();
                    //csvWrite.writeNext(produce.getColumnNames());
                    while (produce.moveToNext()) {

                        ColDate = produce.getString(produce.getColumnIndex(Database.CollDate));
						String dbtTransOn = ColDate + " 00:00:00";
						SimpleDateFormat frmt = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
						Date date1 = null;
						try {
							date1 = frmt.parse(dbtTransOn);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd");
						String TransDate = format3.format(date1);

						Time = produce.getString(produce.getColumnIndex(Database.CaptureTime));
						BatchNo = produce.getString(produce.getColumnIndex(Database.BatchNo));
						DataDevice = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
						TaskCode = produce.getString(produce.getColumnIndex(Database.TaskCode));
						EmployeeNo = produce.getString(produce.getColumnIndex(Database.EmployeeNo));
						ProduceCode = produce.getString(produce.getColumnIndex(Database.DeliveredProduce));


						if (produce.getString(produce.getColumnIndex(Database.ProduceVariety)) == null) {
							VarietyCode = "";
						} else {
							VarietyCode = produce.getString(produce.getColumnIndex(Database.ProduceVariety));
						}
						if (produce.getString(produce.getColumnIndex(Database.ProduceGrade)) == null) {
							GradeCode = "";
						} else {

							GradeCode = produce.getString(produce.getColumnIndex(Database.ProduceGrade));
						}

						EstateCode = produce.getString(produce.getColumnIndex(Database.SourceEstate));
						DivisionCode = produce.getString(produce.getColumnIndex(Database.SourceDivision));
						FieldCode = produce.getString(produce.getColumnIndex(Database.SourceField));
						if (FieldCode.equals("Select ...")) {
							FieldCode = "";
						} else {
							FieldCode = produce.getString(produce.getColumnIndex(Database.SourceField));
						}
						Block = produce.getString(produce.getColumnIndex(Database.SourceBlock));
						if (Block.equals("Select ...")) {
							Block = "";
						} else {
							Block = produce.getString(produce.getColumnIndex(Database.SourceBlock));
						}
						NetWeight = produce.getString(produce.getColumnIndex(Database.NetWeight));
						TareWeight = produce.getString(produce.getColumnIndex(Database.Tareweight));

						if (produce.getString(produce.getColumnIndex(Database.BagCount)) == null) {
							Crates = "1";

						} else {
							Crates = produce.getString(produce.getColumnIndex(Database.BagCount));
						}

						UnitPrice = produce.getString(produce.getColumnIndex(Database.UnitPrice));
						WeighmentNo = produce.getString(produce.getColumnIndex(Database.LoadCount));
						RecieptNo = produce.getString(produce.getColumnIndex(Database.DataCaptureDevice)) + produce.getString(produce.getColumnIndex(Database.ReceiptNo));
						FieldClerk = produce.getString(produce.getColumnIndex(Database.FieldClerk));
						CheckinMethod = produce.getString(produce.getColumnIndex(Database.UsedSmartCard));

						Co_prefix = mSharedPrefs.getString("company_prefix", "");
						Current_User = prefs.getString("user", "");

						StringBuilder sb = new StringBuilder();
						sb.append("2" + ",");
						sb.append(TransDate + ",");
						sb.append(DataDevice + ",");
						sb.append(Time + ",");
						sb.append(FieldClerk + ",");
						sb.append(ProduceCode + ",");
						sb.append(EstateCode + ",");
                        sb.append(DivisionCode + ",");
                        sb.append(FieldCode + ",");
                        sb.append(Block + ",");
                        sb.append(TaskCode + ",");
                        sb.append(EmployeeNo + ",");
                        sb.append(NetWeight + ",");
                        sb.append(TareWeight + ",");
                        sb.append(Crates + ",");
                        sb.append(RecieptNo + ",");
                        sb.append(BatchNo + ",");
                        sb.append(WeighmentNo + ",");
                        sb.append(VarietyCode + ",");
                        sb.append(GradeCode + ",");
                        sb.append(Co_prefix + ",");
                        sb.append(Current_User + ",");
                        sb.append(CheckinMethod + ",");
                        sb.append(CheckinMethod + ",");
                        sb.append("3");

                        weighmentInfo = sb.toString();

                        try {


                            if (Integer.valueOf(errorNo) == -3313) {
                                serverBatchNo = BatchID;
                                Log.i("serverBatchNo", serverBatchNo);
                                restApiResponse = new RestApiRequest(getApplicationContext()).VerifyRecord(serverBatchNo, weighmentInfo);
                            } else {
                                restApiResponse = new RestApiRequest(getApplicationContext()).postWeighment(serverBatchNo, weighmentInfo);
                            }


                            JSONObject jsonObject = new JSONObject(restApiResponse);

                            Id = jsonObject.getString("Id");
                            Title = jsonObject.getString("Title");
                            Message = jsonObject.getString("Message");

                            Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);

                            if (Integer.valueOf(Id).intValue() > 0) {
//								Cursor checkcloudid = dbhelper.CheckWeighmentCloudID(Id);
//								//Check for duplicate checkcloudid number
//								if (checkcloudid.getCount() > 0)
//								{
//									// Toast.makeText(getApplicationContext(), "checkcloudid already exists",Toast.LENGTH_SHORT).show();
//
//								}
//								else {
//									ContentValues values = new ContentValues();
//									values.put(Database.CloudID, Id);
//									long rows = db.update(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, values,
//											Database.FarmerNo + " = ? AND " + Database.LoadCount + " = ? AND " + Database.DataCaptureDevice + " = ? AND "
//													+ Database.ReceiptNo + " = ?", new String[]{FarmerNo, UnitCount, BatchSerial, SessionNo});
//
//									if (rows > 0) {
//										Log.i("success:", Id);
//
//									}
//								}

                            }
                            if (Integer.valueOf(Id).intValue() < 0) {

                                return null;
                            }


                        } catch (NumberFormatException | JSONException e) {
                            Id = "-8080";
                            Title = "";
                            error = restApiResponse;
                            Message = restApiResponse;
                            e.printStackTrace();
                            returnValue = e.toString();
                            Log.i("Catch Exc:", returnValue);
                        }


                        progressStatus++;
						publishProgress("" + progressStatus);

					}


					produce.close();




				} catch (Exception e) {
					e.printStackTrace();
					returnValue = e.toString();
				}
				Cursor batch = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where "
						+ Database.DelivaryNO + " ='" + DelNo + "' and  " + Database.Closed + " = '" + closed1 + "'" +
						" and " + Database.BatCloudID + " = '" + cloudid + "'", null);
				count = batch.getCount();
				if (batch.getCount() > 0) {
					batch.moveToFirst();
					while (!batch.isAfterLast()) {
						totalWeight = batch.getString(batch.getColumnIndex(Database.TotalWeights));

						Date openTime = dateTimeFormat.parse(batch.getString(batch.getColumnIndex(Database.BatchDate)) +
								" " +
								batch.getString(batch.getColumnIndex(Database.OpeningTime)));
						Date closeTime = dateTimeFormat.parse(batch.getString(batch.getColumnIndex(Database.BatchDate)) +
								" " +
								batch.getString(batch.getColumnIndex(Database.ClosingTime)));
						// + "00:00:00");
						BatchCloudID = batch.getString(batch.getColumnIndex(Database.BatCloudID));
						deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
						stringOpenDate = dateFormat.format(openTime);
						deliveryNoteNo = batch.getString(batch.getColumnIndex(Database.DeliveryNoteNumber));
						userID = batch.getString(batch.getColumnIndex(Database.Userid));
						stringOpenTime = timeFormat.format(openTime);
						if (batch.getString(batch.getColumnIndex(Database.BatchSession)) == null) {
							weighingSession = "1";
						} else {
							weighingSession = batch.getString(batch.getColumnIndex(Database.BatchSession));
						}
						//closed = batch.getString(batch.getColumnIndex(Database.Closed));
						stringCloseTime = timeFormat.format(closeTime);
						factory = batch.getString(batch.getColumnIndex(Database.Factory));
						if (batch.getString(batch.getColumnIndex(Database.Transporter)) == null) {
							TransporterCode = "";
						} else {
							TransporterCode = batch.getString(batch.getColumnIndex(Database.Transporter));
						}
						tractorNo = batch.getString(batch.getColumnIndex(Database.Tractor));
						trailerNo = batch.getString(batch.getColumnIndex(Database.Trailer));

						if (batch.getString(batch.getColumnIndex(Database.DelivaryNO)) == null) {
							DelivaryNo = "";
						} else {
							DelivaryNo = batch.getString(batch.getColumnIndex(Database.DelivaryNO));
						}
						Co_prefix = mSharedPrefs.getString("company_prefix", "");
						Current_User = prefs.getString("user", "");
						//BatchSerial = batch.getString(batch.getColumnIndex(Database.DeliveryNoteNumber));
						serverBatchNo = BatchCloudID;
						StringBuilder sb = new StringBuilder();
						sb.append(serverBatchNo + ",");
						sb.append(stringCloseTime + ",");
						sb.append(totalWeight);
						SignOffInfo = sb.toString();

						batch.moveToNext();

                        progressStatus++;
                        publishProgress("" + progressStatus);

                    }
                    batch.close();


                    if (Integer.valueOf(errorNo) == -3313) {
                        serverBatchNo = BatchID;
                    } else {
                        serverBatchNo = prefs.getString("serverBatchNo", "");
                    }
					//restApiResponse = new RestApiRequest(getApplicationContext()).CloseOutgrowersPurchasesBatch(Integer.parseInt(serverBatchNo), SignOffInfo);
                    error = restApiResponse;

                    try {

                        JSONObject jsonObject = new JSONObject(restApiResponse);
                        Message = jsonObject.getString("Message");
                        if (Message.equals("Authorization has been denied for this request.")) {
                            Id = "-1";
                            SharedPreferences.Editor edit = mSharedPrefs.edit();
                            edit.remove("token");
                            edit.commit();
                            edit.remove("expires_in");
                            edit.commit();
                            edit.remove("expires");
                            edit.commit();
                            return null;
                        }
                        if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                            Id = jsonObject.getString("Id");
                            Title = jsonObject.getString("Title");


                            Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
                            try {

                                if (Integer.valueOf(Id).intValue() < 0) {
                                    if (Integer.valueOf(Id).intValue() == -3411) {
                                        errorNo = "-3411";

                                    } else {
                                        error = Id;

                                        return null;

                                    }
                                }
                                //System.out.println(value);}
                            } catch (NumberFormatException e) {
                                //value = 0; // your default value
                                return null;

                            }
                        } else {
                            Id = "-1";
                            Title = "";
                            Message = restApiResponse;
                            return null;

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
				} else {

					//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

				}


				//Cursor delivery = db.rawQuery("SELECT * FROM " + Database.Fmr_FactoryDeliveries + " where " + condition1 + "", null);
				Cursor delivery = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where " + Database.DeliveryNoteNumber + " = '" + deliveryNoteNo + "'", null);
				count = delivery.getCount();
				if (delivery.getCount() > 0) {
					delivery.moveToFirst();
					while (!delivery.isAfterLast()) {

						//	TicketNo= delivery.getString(delivery.getColumnIndex(Database.FdWeighbridgeTicket));
						DelivaryNo = delivery.getString(delivery.getColumnIndex(Database.DelivaryNO));
						deliveryNoteNo = delivery.getString(delivery.getColumnIndex(Database.DeliveryNoteNumber));

						Date deldate = dateTimeFormat.parse(delivery.getString(delivery.getColumnIndex(Database.BatchDate)) +
								" " + "00:00:00");
						DelDate = dateFormat.format(deldate);
						Factory = delivery.getString(delivery.getColumnIndex(Database.Factory));

						if (delivery.getString(delivery.getColumnIndex(Database.Transporter)) == null) {
							Transporter = "";
						} else {
							Transporter = delivery.getString(delivery.getColumnIndex(Database.Transporter));
						}
						if (delivery.getString(delivery.getColumnIndex(Database.Trailer)) == null) {
							Vehicle = "";
						} else {
							Vehicle = delivery.getString(delivery.getColumnIndex(Database.Trailer));
							//	Vehicle="KAA807Y";
						}


						if (delivery.getString(delivery.getColumnIndex(Database.Tractor)) == null) {
							Tractor = "";
						} else {
							Tractor = delivery.getString(delivery.getColumnIndex(Database.Tractor));
							//Tractor="KAA119X";

						}
						EstateCode = delivery.getString(delivery.getColumnIndex(Database.BEstate));

						Date deptime = dateTimeFormat.parse(delivery.getString(delivery.getColumnIndex(Database.BatchDate)) +
								" " +
								delivery.getString(delivery.getColumnIndex(Database.ClosingTime)));
						//
						DepartureTime = timeFormat1.format(deptime);
						CloudID = delivery.getString(delivery.getColumnIndex(Database.BatCloudID));

						CoPrefix = mSharedPrefs.getString("company_prefix", "");
						InternalSerial = mSharedPrefs.getString("terminalID", "");
						UserIdentifier = prefs.getString("user", "");


						StringBuilder del = new StringBuilder();

						del.append(DelivaryNo + ",");
						del.append(DelDate + ",");
						del.append(Factory + ",");
						del.append(Transporter + ",");
						del.append(Vehicle + ",");
						del.append(Tractor + ",");
						del.append(DepartureTime + ",");
						del.append(CoPrefix + ",");
						del.append(EstateCode + ",");
						del.append(UserIdentifier + ",");
						del.append("0");
						DeliveryInfo = del.toString();

						StringBuilder sb = new StringBuilder();

						sb.append(deliveryNoteNo);

						BatchNumber = sb.toString();


                        delivery.moveToNext();


                        progressStatus++;
                        publishProgress("" + progressStatus);
                    }
                    delivery.close();
                    //request.createBatch(DeliveryInfo);


                    SharedPreferences.Editor edit = prefs.edit();

                    restApiResponse = new RestApiRequest(getApplicationContext()).StartDispatch(DeliveryInfo);
                    error = restApiResponse;
                    try {

                        JSONObject jsonObject = new JSONObject(restApiResponse);

                        Message = jsonObject.getString("Message");
                        if (Message.equals("Authorization has been denied for this request.")) {
                            Id = "-1";

                            edit.remove("token");
                            edit.commit();
                            edit.remove("expires_in");
                            edit.commit();
                            edit.remove("expires");
                            edit.commit();
                            return null;
                        }

                        Id = jsonObject.getString("Id");
                        Title = jsonObject.getString("Title");


                        Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
                        try {
                            if (Integer.valueOf(Id).intValue() > 0) {

                                DeliveryNo = Id;
                                edit.putString("DeliveryNo", DeliveryNo);
                                edit.commit();
                                Log.i("Delivery:", DeliveryNo);

                            }
                            if (Integer.valueOf(Id).intValue() < 0) {

                                DeliveryNo = Id;
                                error = Message;
                                return null;
                            }


                            //System.out.println(value);}
                        } catch (NumberFormatException e) {
                            //value = 0; // your default value
                            return null;

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    StringBuilder bc = new StringBuilder();

                    bc.append(deliveryNoteNo);

                    BatchDel = bc.toString();


                    restApiResponse = new RestApiRequest(getApplicationContext()).DeliverBatch(Integer.parseInt(DeliveryNo), BatchDel);
                    error = restApiResponse;
                    Log.i("DBatch Response 0 ", error);
                    Log.i("DBatch Response 1 ", BatchDel);

                    try {

                        JSONObject jsonObject = new JSONObject(restApiResponse);

                        Message = jsonObject.getString("Message");
                        if (Message.equals("Authorization has been denied for this request.")) {
                            Id = "-1";
                            edit.remove("token");
                            edit.commit();
                            edit.remove("expires_in");
                            edit.commit();
                            edit.remove("expires");
                            edit.commit();
                            return null;
                        }
                        Id = jsonObject.getString("Id");
                        Title = jsonObject.getString("Title");

                        Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
                        try {
                            if (Integer.valueOf(Id).intValue() > 0) {


                                Log.i("Delivery:", DeliveryNo);
                                Log.i("DBatch Response 0 ", Id);
                                Log.i("DBatch Response 1 ", BatchDel);
                            }
                            if (Integer.valueOf(Id).intValue() < 0) {

                                error = Message;
                                return null;
                            }

                            count = count + 1;
                            progressStatus++;
                            publishProgress("" + progressStatus);
                            //System.out.println(value);}
                        } catch (NumberFormatException e) {
                            //value = 0; // your default value
                            DeliveryNo = restApiResponse;
                            //return null;

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                    //Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

                }


                DeliveryNo = prefs.getString("DeliveryNo", "");
                restApiResponse = new RestApiRequest(getApplicationContext()).CompleteDispatch(DeliveryNo);

                try {

                    JSONObject jsonObject = new JSONObject(restApiResponse);
                    Message = jsonObject.getString("Message");
                    if (Message.equals("Authorization has been denied for this request.")) {
                        Id = "-1";
                        SharedPreferences.Editor edit = mSharedPrefs.edit();
                        edit.remove("token");
                        edit.commit();
                        edit.remove("expires_in");
                        edit.commit();
                        edit.remove("expires");
                        edit.commit();
                        return null;
                    }
                    Id = jsonObject.getString("Id");
                    Title = jsonObject.getString("Title");


                    Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
                    try {
                        if (Integer.valueOf(Id).intValue() > 0) {


                            Log.i("CompleteDispatch ID", Id);
                            Log.i("CompleteDispatch M", Message);

                        }
                        if (Integer.valueOf(Id).intValue() < 0) {

                            error = Message;
                            return null;
                        }
                        count = count + 1;
                        progressStatus++;
                        publishProgress("" + progressStatus);

                        //System.out.println(value);}
                    } catch (NumberFormatException e) {

                        return null;

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } catch (Exception e) {
				e.printStackTrace();
				returnValue = e.toString();
			}


			return null;
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			Log.i(TAG, "onProgressUpdate");
			waveProgress.setProgress(Integer.parseInt(progress[0]));
			waveProgress.setMaxProgress(count);
			arcProgress.setProgress(Integer.parseInt(progress[0]));
			arcProgress.setMax(count);
			arcProgress.setBottomText("Uploading ...");

			textStatus.setText("Uploading... " + Integer.parseInt(progress[0]) + "/" + count + " Records");
		}


		@Override
		protected void onPostExecute(String unused) {
			Log.i(TAG, "onPostExecute");


			try {

				if (Integer.valueOf(soapResponse).intValue() > 0) {
					returnValue = soapResponse;
					ContentValues values = new ContentValues();
					values.put(Database.BatCloudID, serverBatchNo);
					long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
							Database.DelivaryNO + " = ?", new String[]{DelNo});

					if (rows > 0) {
						Toast.makeText(UploadActivity.this, "Data Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
						new Restart().execute();
					}

					return;
				}
			} catch (NumberFormatException e) {
				errorNo = prefs.getString("errorNo", "");

				if (errorNo.equals("-8080")) {
					Toast.makeText(UploadActivity.this, "Server Not Available !!", Toast.LENGTH_LONG).show();
					finish();
					return;
				} else if (errorNo.equals("-1011")) {
					try {

						soapResponse = new SoapRequest(UploadActivity.this).VerifyBatch(serverBatchNo, totalWeight);
						returnValue = soapResponse;

						ContentValues values = new ContentValues();
						values.put(Database.BatCloudID, 1);
						long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
								Database.DeliveryNoteNumber + " = ?", new String[]{DelNo});

						if (rows > 0) {

						}
						Toast.makeText(UploadActivity.this, "Data Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
						new Restart().execute();
					} catch (NumberFormatException err) {
						if (Integer.valueOf(returnValue).intValue() < 0) {
							Toast.makeText(UploadActivity.this, error, Toast.LENGTH_LONG).show();
							finish();
						}
					}


				} else {

					Toast.makeText(UploadActivity.this, error, Toast.LENGTH_LONG).show();
					finish();
				}
			}


			//b.dismiss();
		}

	}

	private class Restart extends AsyncTask<Void, Void, String> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(Void... params) {
			finish();

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


			return "";
		}

		@Override
		protected void onPostExecute(String result) {


			mIntent = new Intent(getApplicationContext(), UploadActivity.class);
			startActivity(mIntent);
		}
	}

	private class NoReceipt extends AsyncTask<Void, Void, String> {


		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(Void... params) {


			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


			return "";
		}

		@Override
		protected void onPostExecute(String result) {


			finish();
			Context context = getApplicationContext();
			LayoutInflater inflater = getLayoutInflater();
			View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
			TextView text = (TextView) customToastroot.findViewById(R.id.toast);
			text.setText("Nothing Found To Upload");
			Toast customtoast = new Toast(context);
			customtoast.setView(customToastroot);
			customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
			customtoast.setDuration(Toast.LENGTH_LONG);
			customtoast.show();
			//Toast.makeText(getApplicationContext(), "Nothing Found To Upload", Toast.LENGTH_LONG).show();
		}
	}
}
