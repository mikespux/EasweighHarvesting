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

import org.xmlpull.v1.XmlPullParser;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.modificator.waterwave_progress.WaterWaveProgress;

@SuppressWarnings("ALL")
public class UploadTsksandAttActivity extends AppCompatActivity {
	static EditText etDate;
	static EditText etTo;
	static SharedPreferences prefs;
	public Toolbar toolbar;
	public SimpleCursorAdapter ca;
	Button btnCancel, btnSignOff;
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
	SimpleDateFormat dateFormat;
	SimpleDateFormat dateOnlyFormat;
	SimpleDateFormat BatchDateFormat;
	ArcProgress arcProgress;
	WaterWaveProgress waveProgress;
	EditText etFarmerNo;
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
	Cursor attend;
	String BatchCloudID, SignOffInfo;
	String Employee_No, CardNo, AuthMethod, VerMethod, DateTimeIn, DateCheckin, Estate, Division, Rtype, TerminalID, UserID, TimeIn, TimeOut;
	private String TAG = "Vik";
	private String checkListReturnValue;
	private SharedPreferences mSharedPrefs;
	private int progressStatus = 0, count = 0, totalRecords = 0;
	private String soapResponse, serverBatchNo, Attendance;
	private TextView textView, txtFNo;
	private Button pickFrom, pickTo;
	private Button btnUpload;
	private Button btnFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_tasks);
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
		timeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs = PreferenceManager.getDefaultSharedPreferences(UploadTsksandAttActivity.this);
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


		listReciepts = (ListView) this.findViewById(R.id.lvReciepts);
		if (!checkList()) {
			finish();
			return;
		}


		showSearchReceipt();

	}


	private boolean checkList() {
		this.checkListReturnValue = XmlPullParser.NO_NAMESPACE;
		try {
			if (this.mSharedPrefs.getBoolean("cloudServices", false)) {
				try {
					if (this.mSharedPrefs.getString("internetAccessModes", null).toString().equals(null)) {
						Toast.makeText(UploadTsksandAttActivity.this, "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
						return false;

					}
					try {
						if (this.mSharedPrefs.getString("licenseKey", null).equals(null) || this.mSharedPrefs.getString("licenseKey", null).equals(XmlPullParser.NO_NAMESPACE)) {
							//this.checkListReturnValue = "License key not found!";
							Toast.makeText(UploadTsksandAttActivity.this, "License key not found!", Toast.LENGTH_LONG).show();
							return false;
						}
						try {
							if (!this.mSharedPrefs.getString("portalURL", null).equals(null) && !this.mSharedPrefs.getString("portalURL", null).equals(XmlPullParser.NO_NAMESPACE)) {
								return true;
							}
							//this.checkListReturnValue = "Portal URL not configured!";
							Toast.makeText(UploadTsksandAttActivity.this, "Portal URL not configured!", Toast.LENGTH_LONG).show();
							return false;
						} catch (Exception e) {
							//this.checkListReturnValue = "Portal URL not configured!";
							Toast.makeText(UploadTsksandAttActivity.this, "Portal URL not configured!", Toast.LENGTH_LONG).show();
							return false;
						}
					} catch (Exception e2) {
						//this.checkListReturnValue = "License key not found!";
						Toast.makeText(UploadTsksandAttActivity.this, "License key not found!", Toast.LENGTH_LONG).show();
						return false;
					}

				} catch (Exception e3) {
					e3.printStackTrace();
					//this.checkListReturnValue = "Cloud Services not enabled!";
					Toast.makeText(UploadTsksandAttActivity.this, "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
					return false;
				}
			}
			Toast.makeText(UploadTsksandAttActivity.this, "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
			return false;


			//this.checkListReturnValue = "Cloud Services not enabled!";

		} catch (Exception e4) {
			e4.printStackTrace();
			//this.checkListReturnValue = "Cloud Services not enabled!";
			Toast.makeText(UploadTsksandAttActivity.this, "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
			return false;
		}

	}

	public void showSearchReceipt() {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.dialog_upload_tasks, null);
		dialogBuilder.setView(dialogView);
		dialogBuilder.setCancelable(false);
		dialogBuilder.setTitle("Upload");
		etDate = (EditText) dialogView.findViewById(R.id.edtDate);


		Date date = new Date(getDate());
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		etDate.setText(format1.format(date));

		pickFrom = (Button) dialogView.findViewById(R.id.btnDate);
		pickFrom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();
				newFragment.show(getFragmentManager(), "datePicker");

			}
		});


		btnUpload = (Button) dialogView.findViewById(R.id.btn_Upload);
		btnUpload.setVisibility(View.VISIBLE);
		btnUpload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fromDate = etDate.getText().toString();


				SharedPreferences.Editor edit = prefs.edit();
				edit.putString("fromDate", fromDate);
				edit.commit();
				new AsyncCallWS().execute();
			}
		});

		dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {

					return true;
				}
				return false;
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
				ca.getFilter().filter(query.toString());
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
				ca.getFilter().filter(newText.toString());
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
			String from[] = {Database.ROW_ID, Database.EmployeeNo, Database.NetWeight};
			int to[] = {R.id.txtAccountId, R.id.tv_number, R.id.tv_phone};


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


	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void getdata() {

		try {


			SQLiteDatabase db = dbhelper.getReadableDatabase();

			Cursor tasks = db.rawQuery("select * from " + Database.EM_TASK_ALLOCATION_TABLE_NAME + " WHERE "
					+ Database.CloudID + " ='" + cloudid + "'", null);

			Cursor attend = db.rawQuery("select * from " + Database.EM_CHECKIN_TABLE_NAME + " WHERE "
					+ Database.BatCloudID + " ='" + cloudid + "'", null);
			if (tasks.getCount() + attend.getCount() > 0) {


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
	public static class DatePickerFragment extends DialogFragment
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
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			// edtBaseDate.setText(view.getYear() + "/" + view.getMonth() + "/" + view.getDayOfMonth());

			// Create a Date variable/object with user chosen date
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(0);
			cal.set(year, month, day, 0, 0, 0);
			Date chosenDate = cal.getTime();
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
			etDate.setText(format1.format(chosenDate));
		}
	}

	@SuppressLint("ValidFragment")
	public static class DatePickerFragment2 extends DialogFragment
			implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			// edtBaseDate.setText(view.getYear() + "/" + view.getMonth() + "/" + view.getDayOfMonth());
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(0);
			cal.set(year, month, day, 0, 0, 0);
			Date chosenDate = cal.getTime();
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
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
			b.dismiss();

			//soapResponse = new SoapRequest(UploadActivity.this).DoDataSourceTest();
			//Toast.makeText(getApplicationContext(),soapResponse,Toast.LENGTH_LONG);
		}

		@Override
		protected String doInBackground(String... aurl) {
			Log.i(TAG, "doInBackground");
			try {


				String dbtTrans = fromDate + " 00:00:00";
				SimpleDateFormat frm = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date tdate1 = null;
				try {
					tdate1 = frm.parse(dbtTrans);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				SimpleDateFormat form = new SimpleDateFormat("dd/MM/yyyy");
				String TDate = form.format(tdate1);
				tasks = db.rawQuery("select * from " + Database.EM_TASK_ALLOCATION_TABLE_NAME + " WHERE "
						+ Database.CollDate + " ='" + TDate + "'  and " + Database.CloudID + " ='" + cloudid + "'", null);
				count = count + tasks.getCount();
				if (tasks.getCount() == 0) {
					error = "-1";
				}
				//csvWrite.writeNext(tasks.getColumnNames());
				while (tasks.moveToNext()) {


					ColDate = tasks.getString(tasks.getColumnIndex(Database.CollDate));
					String dbtTransOn = ColDate + " 00:00:00";
					SimpleDateFormat frmt = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
					Date date1 = null;
					try {
						date1 = frmt.parse(dbtTransOn);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
					String TransDate = format2.format(date1);

					Time = tasks.getString(tasks.getColumnIndex(Database.CaptureTime));
					DataDevice = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
					EmployeeNo = tasks.getString(tasks.getColumnIndex(Database.EmployeeNo));
					TaskCode = tasks.getString(tasks.getColumnIndex(Database.TaskCode));
					TaskType = tasks.getString(tasks.getColumnIndex(Database.TaskType));
					TaskUnits = tasks.getString(tasks.getColumnIndex(Database.TaskUnits));
					EstateCode = tasks.getString(tasks.getColumnIndex(Database.SourceEstate));
					DivisionCode = tasks.getString(tasks.getColumnIndex(Database.SourceDivision));
					if (tasks.getString(tasks.getColumnIndex(Database.SourceField)) == null) {
						FieldCode = "";
					} else {
						FieldCode = tasks.getString(tasks.getColumnIndex(Database.SourceField));

					}

					//Block=tasks.getString(tasks.getColumnIndex(Database.SourceBlock));

					//RecieptNo =tasks.getString(tasks.getColumnIndex(Database.DataCaptureDevice))+tasks.getString(tasks.getColumnIndex(Database.ReceiptNo));
					FieldClerk = tasks.getString(tasks.getColumnIndex(Database.FieldClerk));
					if (tasks.getString(tasks.getColumnIndex(Database.CheckinMethod)) == null) {
						CheckinMethod = "3";
					} else {
						CheckinMethod = tasks.getString(tasks.getColumnIndex(Database.CheckinMethod));

					}
					if (tasks.getString(tasks.getColumnIndex(Database.CheckoutMethod)) == null) {
						CheckoutMethod = "3";
					} else {
						CheckoutMethod = tasks.getString(tasks.getColumnIndex(Database.CheckoutMethod));

					}

					if (tasks.getString(tasks.getColumnIndex(Database.CheckoutTime)) == null) {
						CheckoutTime = TransDate + " 00:00:00";
					} else {
						CheckoutTime = tasks.getString(tasks.getColumnIndex(Database.CheckoutTime));

					}


					Co_prefix = mSharedPrefs.getString("company_prefix", "").toString();
					Current_User = prefs.getString("user", "");
					Project = "";

					StringBuilder sb = new StringBuilder();
					sb.append("4" + ",");
					sb.append(TransDate + ",");
					sb.append(DataDevice + ",");
					sb.append(Time + ",");
					sb.append(FieldClerk + ",");
					sb.append(EstateCode + ",");
					sb.append(DivisionCode + ",");
					sb.append(FieldCode + ",");
					sb.append(EmployeeNo + ",");
					sb.append(TaskCode + ",");
					sb.append(TaskType + ",");
					sb.append(TaskUnits + ",");
					sb.append(Project + ",");
					sb.append(Co_prefix + ",");
					sb.append(Current_User + ",");
					sb.append(CheckinMethod + ",");
					sb.append(CheckoutTime + ",");
					sb.append(CheckoutMethod);

					taskInfo = sb.toString();

					try {
						soapResponse = new SoapRequest(UploadTsksandAttActivity.this).PostTaskRecord(taskInfo);
						error = soapResponse;
						errorNo = prefs.getString("TaskerrorNo", "");
						if (Integer.valueOf(errorNo).intValue() < 0) {
							if (Integer.valueOf(errorNo).intValue() == -4910) {
								ContentValues values = new ContentValues();
								values.put(Database.CloudID, -4910);
								long rows = db.update(Database.EM_TASK_ALLOCATION_TABLE_NAME, values,
										Database.EmployeeNo + " = ? AND " + Database.CollDate + " = ? AND " + Database.CaptureTime + " = ?",
										new String[]{EmployeeNo, ColDate, Time});

								if (rows > 0) {
									Log.i("error:", soapResponse);

								}
							} else {
								ContentValues values = new ContentValues();
								values.put(Database.CloudID, 0);
								long rows = db.update(Database.EM_TASK_ALLOCATION_TABLE_NAME, values,
										Database.EmployeeNo + " = ? AND " + Database.CollDate + " = ? AND " + Database.CaptureTime + " = ?",
										new String[]{EmployeeNo, ColDate, Time});

								if (rows > 0) {
									Log.i("error:", soapResponse);

								}
								//return null;
							}
						}
						if (Integer.valueOf(errorNo).intValue() > 0) {
							returnValue = soapResponse;
							ContentValues values = new ContentValues();
							values.put(Database.CloudID, returnValue);
							long rows = db.update(Database.EM_TASK_ALLOCATION_TABLE_NAME, values,
									Database.EmployeeNo + " = ? AND " + Database.CollDate + " = ? AND " + Database.CaptureTime + " = ?",
									new String[]{EmployeeNo, ColDate, Time});

							if (rows > 0) {
								Log.i("success:", returnValue);

							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						returnValue = e.toString();
					}


					progressStatus++;
					publishProgress("" + progressStatus);
				}
				//tasks.close();

				attend = db.rawQuery("select * from " + Database.EM_CHECKIN_TABLE_NAME + " WHERE "
						+ Database.Date + " ='" + TDate + "' and " + Database.CloudID + " ='" + cloudid + "'", null);

//				attend = db.rawQuery("select * from " + Database.EM_CHECKIN_TABLE_NAME + " WHERE "
//						+ Database.Date + " ='"+ TDate + "'", null);
				count = count + attend.getCount();
				if (attend.getCount() == 0) {
					error = "-1";
					return null;
				}
				//csvWrite.writeNext(attend.getColumnNames());
				while (attend.moveToNext()) {


					String TransDateTime = attend.getString(attend.getColumnIndex(Database.DateTime));
					String TransDate = attend.getString(attend.getColumnIndex(Database.Date)) + " 00:00:00";

					Employee_No = attend.getString(attend.getColumnIndex(Database.Employee_No));
					CardNo = attend.getString(attend.getColumnIndex(Database.CardNo));
					if (attend.getString(attend.getColumnIndex(Database.AuthMethod)) == null) {
						AuthMethod = "1";
					} else {
						AuthMethod = attend.getString(attend.getColumnIndex(Database.AuthMethod));
					}
					VerMethod = attend.getString(attend.getColumnIndex(Database.Vtype));
					SimpleDateFormat frmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					Date date = null;
					try {
						date = frmt.parse(TransDateTime);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					DateTimeIn = format2.format(date);

					Date date1 = null;
					try {
						date1 = frmt.parse(TransDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd");
					DateCheckin = format3.format(date1);


					Estate = attend.getString(attend.getColumnIndex(Database.Estate));
					Division = attend.getString(attend.getColumnIndex(Database.Division));
					Rtype = attend.getString(attend.getColumnIndex(Database.Rtype));
					TerminalID = attend.getString(attend.getColumnIndex(Database.TerminalID));
					;
					TimeIn = attend.getString(attend.getColumnIndex(Database.TimeIn));
					;
					TimeOut = attend.getString(attend.getColumnIndex(Database.TimeOut));
					;
					UserID = attend.getString(attend.getColumnIndex(Database.UserID));


					Co_prefix = mSharedPrefs.getString("company_prefix", "").toString();
					Current_User = prefs.getString("user", "");
					Project = "";


					StringBuilder sb = new StringBuilder();
					sb.append("5" + ",");
					sb.append(Employee_No + ",");
					sb.append(CardNo + ",");
					sb.append(AuthMethod + ",");
					sb.append(VerMethod + ",");
					sb.append(DateTimeIn + ",");
					sb.append(DateCheckin + ",");
					sb.append(Estate + ",");
					sb.append(Division + ",");
					sb.append(Rtype + ",");
					sb.append(TerminalID + ",");
					sb.append(UserID + ",");
					sb.append(Co_prefix);

					Attendance = sb.toString();


					try {
						soapResponse = new SoapRequest(UploadTsksandAttActivity.this).PostClockingRecord(Attendance);
						error = soapResponse;
						errorNo = prefs.getString("ClockerrorNo", "");
						if (Integer.valueOf(errorNo).intValue() < 0) {

							if (Integer.valueOf(errorNo).intValue() == -4910) {
								ContentValues values = new ContentValues();
								values.put(Database.CloudID, -4910);
								long rows = db.update(Database.EM_CHECKIN_TABLE_NAME, values,
										Database.Employee_No + " = ? AND " + Database.Date + " = ?",
										new String[]{Employee_No, attend.getString(attend.getColumnIndex(Database.Date))});

								if (rows > 0) {
									Log.i("errorUpdated:", soapResponse);

								}
							} else {
								ContentValues values = new ContentValues();
								values.put(Database.CloudID, 0);
								long rows = db.update(Database.EM_CHECKIN_TABLE_NAME, values,
										Database.Employee_No + " = ? AND " + Database.Date + " = ?",
										new String[]{Employee_No, attend.getString(attend.getColumnIndex(Database.Date))});

								if (rows > 0) {
									Log.i("error:", soapResponse);

								}
							}
							//return null;
						}

						if (Integer.valueOf(errorNo).intValue() > 0) {
							returnValue = soapResponse;
							ContentValues values = new ContentValues();
							values.put(Database.CloudID, soapResponse);
							long rows = db.update(Database.EM_CHECKIN_TABLE_NAME, values,
									Database.Employee_No + " = ? AND " + Database.Date + " = ?",
									new String[]{Employee_No, attend.getString(attend.getColumnIndex(Database.Date))});

							if (rows > 0) {
								Log.i("success:", soapResponse);

							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						returnValue = e.toString();
					}
					progressStatus++;
					publishProgress("" + progressStatus);
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

				if (Integer.valueOf(errorNo).intValue() > 0) {
					returnValue = soapResponse;

					Toast.makeText(UploadTsksandAttActivity.this, "Data Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
					new Restart().execute();


					return;
				}
				if (Integer.valueOf(errorNo).intValue() < 0) {

					Toast.makeText(UploadTsksandAttActivity.this, error, Toast.LENGTH_LONG).show();
					finish();

					return;
				}
			} catch (NumberFormatException e) {
				errorNo = prefs.getString("errorNo", "");
				if (error.equals("-1")) {
					Toast.makeText(UploadTsksandAttActivity.this, "No Tasks and Attendance to Upload.", Toast.LENGTH_LONG).show();
					finish();
					return;
				}
				if (errorNo.equals("-8080")) {
					Toast.makeText(UploadTsksandAttActivity.this, "Server Not Available !!", Toast.LENGTH_LONG).show();
					finish();
					return;
				} else {

					Toast.makeText(UploadTsksandAttActivity.this, error, Toast.LENGTH_LONG).show();
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


			mIntent = new Intent(getApplicationContext(), UploadTsksandAttActivity.class);
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
