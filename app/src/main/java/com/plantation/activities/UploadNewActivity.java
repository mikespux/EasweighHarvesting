package com.plantation.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.plantation.R;
import com.plantation.data.DBHelper;
import com.plantation.data.Database;
import com.plantation.helpers.Delivery;
import com.plantation.synctocloud.RestApiRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class UploadNewActivity extends AppCompatActivity {
	static SharedPreferences prefs;
	static EditText etDate;
	static EditText etTo;
	private final String TAG = UploadNewActivity.class.getSimpleName();
	public Toolbar toolbar;
	public SimpleCursorAdapter ca;
	Button btnUpload, btnSignOff, btnCancel;
	int accesslevel = 0;
	int useridentifier = 1;
	String username, userpass;
	String user_level;
	DBHelper dbhelper;
	RestApiRequest request;
	String DeliveryInfo;
	String TicketNo, DNoteNo, DelDate, Factory, Transporter, Vehicle, Tractor, Driver, TurnMan, ArrivalTime, FieldWt, GrossWt, TareWt,
			RejectWt, QualityScore, DepartureTime, CoPrefix, InternalSerial, UserIdentifier, CloudID;
	String batchInfo;
	String BatchType, BatchID, batchNo, deviceID, stringOpenDate, deliveryNoteNo, userID, stringOpenTime, weighingSession,
			stringCloseTime, factory, tractorNo, trailerNo, TransporterCode, DelivaryNo, Co_prefix, Current_User, Quality;
	String weighmentInfo;
	String totalWeight, BatchNum;
	String SessionNo, BatchNo, ColDate, Time, DataDevice, BatchNO, TaskCode, EmployeeNo;
	String WorkerNo, FieldClerk, ProduceCode, TaskUnits, TaskType;
	String VarietyCode, GradeCode, EstateCode, DivisionCode, Project;
	String GrossTotal, TareWeight, Crates;
	String UnitPrice, RecieptNo, WeighmentNo, NetWeight, FieldCode, Block;
	String CheckinMethod, CheckoutMethod, CheckoutTime;
	String serverBatchID;
	String shedCode;
	String returnValue;
	SimpleDateFormat dateTimeFormat;
	SimpleDateFormat timeFormat;
	SimpleDateFormat dateFormat;
	SimpleDateFormat dateOnlyFormat;
	ArcProgress arcProgress;

	String condition = " _id > 0 ";
	SQLiteDatabase db;
	Intent mIntent;
	ListView listReciepts;
	String BatchSerial;
	TextView textBatchNo, textBatchDate, textDelNo, textStatus;
	int closed = 1;
	int cloudid = 0;
	String DelNo;
	String error = "", errorNo = "0", BatchDel;
	String Id, Title, Message;
	String VId, VTitle, VMessage;
	AlertDialog b;
	EditText etFarmerNo;
	String fromDate, toDate, farmerNo;
	Cursor delivery;
	Delivery student;
	DeliveryArrayAdapter.DeliveryWrapper DeliveryWrapper = null;
	DeliveryArrayAdapter ArrayAdapter;
	LinearLayout LtUpload;
	DeliveryToCloud asyncTask = new DeliveryToCloud();
	private SharedPreferences mSharedPrefs;
	private String DeliveryNo;
	private int progressStatus = 0, count = 0;
	private String restApiResponse, verifyWeighment, verifyResponse, serverBatchNo, SignOffInfo;
	private Button btnFilter;
	private Button btnSearchReceipt;
	private Button pickFrom, pickTo;

	String OperatorInfo, RowID, sDate, terminalID, machineNo, employeeNo, checkinTime, checkoutTime,
			checkinWeighment, checkoutWeighment, mTaskCode, operator_share, mCompany, mEstate;
	String FuelInfo, mfDate, mfterminalID, mfmachineNo, mfTime, mfLitres, FuelType, mFCompany, mFEstate;

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
		toolbar = findViewById(R.id.app_bar);
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
		dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs = PreferenceManager.getDefaultSharedPreferences(UploadNewActivity.this);

		dbhelper = new DBHelper(getApplicationContext());
		db = dbhelper.getReadableDatabase();
		request = new RestApiRequest(getApplicationContext());

		LtUpload = findViewById(R.id.LtUpload);
		LtUpload.setVisibility(View.GONE);

		btnFilter = findViewById(R.id.btnFilter);
		btnFilter.setVisibility(View.GONE);
		btnUpload = findViewById(R.id.btnUpload);
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

								syncTasks();

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

		btnSignOff = findViewById(R.id.btnSignOff);
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
				builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Are you sure you want to SignOff Delivary <b>(" + DelNo + ")</b>?</font>"))
						.setCancelable(false)
						.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								ContentValues values = new ContentValues();
								values.put(Database.CloudID, 1);
								long rows = db.update(Database.Fmr_FactoryDeliveries, values,
										Database.FdDNoteNum + " = ?", new String[]{DelNo});

								if (rows > 0) {

								}
								Toast.makeText(UploadNewActivity.this, "Delivary " + DelNo + " Signed Off Successfully !!!", Toast.LENGTH_LONG).show();
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

		btnCancel = findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();

			}
		});
		listReciepts = this.findViewById(R.id.lvReciepts);

		listReciepts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View selectedView, int position, long arg3) {


				if (position == 0) {


					textDelNo = selectedView.findViewById(R.id.tv_number);
					Log.d("delivery", "Selected Account Id : " + textDelNo.getText().toString());

					//Toast.makeText(UploadNewActivity.this,textBatchDate.getText().toString()+ textBatchNo.getText().toString(), Toast.LENGTH_LONG).show();
				} else {

					//Toast.makeText(UploadNewActivity.this,"empty", Toast.LENGTH_LONG).show();
				}


			}


		});


		if (!checkList()) {
			finish();
			return;
		}

		String selectQuery = "SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE SignedOff=1";
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.getCount() <= 0) {
			Toast.makeText(UploadNewActivity.this, "No Batch Dispatched to Upload!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		cursor.close();
		showSearchReceipt();
	}

	private String getDate() {

		//A string to hold the current date
		String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

		//Return the current date
		return currentDateTimeString;
	}

	public void showSearchReceipt() {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.dialog_search_uploads, null);
		dialogBuilder.setView(dialogView);
		dialogBuilder.setCancelable(false);
		dialogBuilder.setTitle("Search");
		etDate = dialogView.findViewById(R.id.edtDate);


		Date date = new Date(getDate());
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		etDate.setText(format1.format(date));

		pickFrom = dialogView.findViewById(R.id.btnFrom);
		pickFrom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();
				newFragment.show(getFragmentManager(), "datePicker");

			}
		});


		btnSearchReceipt = dialogView.findViewById(R.id.btn_SearchReceipt);
		btnSearchReceipt.setVisibility(View.VISIBLE);
		btnSearchReceipt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fromDate = etDate.getText().toString();

				SharedPreferences.Editor edit = prefs.edit();
				edit.putString("fromDate", fromDate);
				edit.apply();


				if (fromDate.length() > 0)
					condition += " and  " + Database.FdDate + " = '" + fromDate + "'";


				getdata();
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

	private boolean checkList() {

		try {
			if (this.mSharedPrefs.getBoolean("cloudServices", false)) {
				try {
					if (this.mSharedPrefs.getString("internetAccessModes", null).equals(null)) {
						Toast.makeText(UploadNewActivity.this, "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
						return false;

					}

					try {
						if (!this.mSharedPrefs.getString("portalURL", null).equals(null) && !this.mSharedPrefs.getString("portalURL", null).equals(XmlPullParser.NO_NAMESPACE)) {
							return true;
						}

						Toast.makeText(UploadNewActivity.this, "Portal URL not configured!", Toast.LENGTH_LONG).show();
						return false;
					} catch (Exception e) {

						Toast.makeText(UploadNewActivity.this, "Portal URL not configured!", Toast.LENGTH_LONG).show();
						return false;
					}


				} catch (Exception e3) {
					e3.printStackTrace();

					Toast.makeText(UploadNewActivity.this, "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
					return false;
				}
			}
			Toast.makeText(UploadNewActivity.this, "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
			return false;


		} catch (Exception e4) {
			e4.printStackTrace();

			Toast.makeText(UploadNewActivity.this, "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
			return false;
		}

	}

	private void syncTasks() {
		try {
			if (asyncTask.getStatus() != AsyncTask.Status.RUNNING) {   // check if asyncTasks is running
				asyncTask.cancel(true); // asyncTasks not running => cancel it
				asyncTask = new DeliveryToCloud(); // reset task
				asyncTask.execute(); // execute new task (the same task)
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("MainActivity_TSK", "Error: " + e.toString());
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onStart() {
		super.onStart();
		getdata();
	}

	public void getdata() {

		try {

			SQLiteDatabase db = dbhelper.getReadableDatabase();
			ArrayList<Delivery> arraylist = new ArrayList<Delivery>();

			delivery = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " where " + condition + "", null);
			if (delivery.getCount() > 0) {
				while (delivery.moveToNext()) {

					arraylist.add(new Delivery(delivery.getString(delivery.getColumnIndex(Database.ROW_ID)), delivery.getString(delivery.getColumnIndex(Database.FdDNoteNum)),
							delivery.getString(delivery.getColumnIndex(Database.FdDate)),
							delivery.getString(delivery.getColumnIndex(Database.FdFieldWt)), delivery.getString(delivery.getColumnIndex(Database.CloudID))));
				}

				ArrayAdapter = new DeliveryArrayAdapter(UploadNewActivity.this, R.layout.delivery_upload_list, arraylist);
				listReciepts = this.findViewById(R.id.lvReciepts);

				listReciepts.setAdapter(ArrayAdapter);
				ArrayAdapter.notifyDataSetChanged();
				listReciepts.setTextFilterEnabled(true);

				//db.close();
				//dbhelper.close();


			} else {

				new NoReceipt().execute();
			}
		} catch (Exception ex) {
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
		}
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


	private class DeliveryToCloud extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute");

			arcProgress = findViewById(R.id.arc_progress);
			arcProgress.setVisibility(View.VISIBLE);
			arcProgress.setProgress(0);
			arcProgress.setBottomText("Connecting ...");

			textStatus = findViewById(R.id.textStatus);
			textStatus.setVisibility(View.GONE);

			listReciepts.setVisibility(View.GONE);
			btnFilter.setVisibility(View.GONE);
			listReciepts.performItemClick(listReciepts.getAdapter().getView(0, null, null), 0, listReciepts.getAdapter().getItemId(0));
			count = 0;
		}

		@RequiresApi(api = Build.VERSION_CODES.N)
		@Override
		protected String doInBackground(String... aurl) {
			Log.i(TAG, "doInBackground");

			try {
				SQLiteDatabase db = dbhelper.getReadableDatabase();
				Cursor moperators = db.rawQuery("SELECT * FROM " + Database.MACHINEOP_TABLE_NAME + " where " + Database.MSTATUS + "<'4' and " + Database.MDATE + "='" + DelDate + "'", null);
				count = count + moperators.getCount();
				if (moperators.getCount() > 0) {
					moperators.moveToFirst();
					while (!moperators.isAfterLast()) {


						RowID = moperators.getString(moperators.getColumnIndex(Database.ROW_ID));
						sDate = moperators.getString(moperators.getColumnIndex(Database.MDATE));
						terminalID = moperators.getString(moperators.getColumnIndex(Database.TERMINALID));
						machineNo = moperators.getString(moperators.getColumnIndex(Database.MACHINENUMBER));
						employeeNo = moperators.getString(moperators.getColumnIndex(Database.EMPLOYEENUMBER));
						checkinTime = moperators.getString(moperators.getColumnIndex(Database.CHECKINTIME));
						checkinWeighment = moperators.getString(moperators.getColumnIndex(Database.CHECKINWEIGHMENT));


						if (moperators.getString(moperators.getColumnIndex(Database.CHECKOUTTIME)) == null) {

							checkoutTime = "";
						} else {
							checkoutTime = moperators.getString(moperators.getColumnIndex(Database.CHECKOUTTIME));

						}

						checkinWeighment = moperators.getString(moperators.getColumnIndex(Database.CHECKINWEIGHMENT));

						if (moperators.getString(moperators.getColumnIndex(Database.CHECKOUTWEIGHMENT)) == null) {

							checkoutWeighment = "0";
						} else {
							checkoutWeighment = moperators.getString(moperators.getColumnIndex(Database.CHECKOUTWEIGHMENT));

						}

						if (moperators.getString(moperators.getColumnIndex(Database.MTASKCODE)) == null) {

							mTaskCode = "";
						} else {
							mTaskCode = moperators.getString(moperators.getColumnIndex(Database.MTASKCODE));
						}

						operator_share = "0";
						mCompany = moperators.getString(moperators.getColumnIndex(Database.MCOMPANY));
						mEstate = moperators.getString(moperators.getColumnIndex(Database.MESTATE));

						StringBuilder mop = new StringBuilder();
						mop.append("0" + ",");
						mop.append(sDate + ",");
						mop.append(terminalID + ",");
						mop.append(machineNo + ",");
						mop.append(employeeNo + ",");
						mop.append(checkinTime + ",");
						mop.append(checkinWeighment + ",");
						mop.append(checkoutWeighment + ",");
						mop.append(checkoutTime + ",");
						mop.append(mTaskCode + ",");
						mop.append(operator_share + ",");
						mop.append(mCompany + ",");
						mop.append(mEstate);

						OperatorInfo = mop.toString();

						moperators.moveToNext();

						restApiResponse = new RestApiRequest(getApplicationContext()).MachineOperator(OperatorInfo);
						error = restApiResponse;
						try {

							JSONObject jsonObject = new JSONObject(restApiResponse);

							Message = jsonObject.getString("Message");
							if (Message.equals("Authorization has been denied for this request.")) {
								Id = "-1";
								SharedPreferences.Editor edit = mSharedPrefs.edit();
								edit.remove("token");
								edit.apply();
								edit.remove("expires_in");
								edit.apply();
								edit.remove("expires");
								edit.apply();
							}

							Id = jsonObject.getString("Id");
							Title = jsonObject.getString("Title");


							Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
							try {
								if (Integer.parseInt(Id) > 0) {
									ContentValues values = new ContentValues();
									values.put(Database.MSTATUS, 4);


									long rows = db.update(Database.MACHINEOP_TABLE_NAME, values,
											"_id = ? ", new String[]{RowID});

									if (rows > 0) {

									}

								}
								if (Integer.valueOf(Id).intValue() < 0) {

									error = Message;
								}


								//System.out.println(value);}
							} catch (NumberFormatException e) {
								//value = 0; // your default value


							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
						progressStatus++;
						publishProgress("" + progressStatus);
					}
					moperators.close();
				}

				Cursor mfuel = db.rawQuery("SELECT * FROM " + Database.MACHINEFUEL_TABLE_NAME + " where " + Database.MFSTATUS + "<'4' and " + Database.MFDATE + "='" + DelDate + "'", null);
				count = count + mfuel.getCount();
				if (mfuel.getCount() > 0) {
					mfuel.moveToFirst();
					while (!mfuel.isAfterLast()) {


						RowID = mfuel.getString(mfuel.getColumnIndex(Database.ROW_ID));
						mfDate = mfuel.getString(mfuel.getColumnIndex(Database.MFDATE));
						mfterminalID = mfuel.getString(mfuel.getColumnIndex(Database.MFTERMINALID));
						mfmachineNo = mfuel.getString(mfuel.getColumnIndex(Database.MFMACHINENUMBER));
						mfTime = mfuel.getString(mfuel.getColumnIndex(Database.MFTIME));
						mfLitres = mfuel.getString(mfuel.getColumnIndex(Database.MFLitres));
						FuelType = "";
						mFCompany = mfuel.getString(mfuel.getColumnIndex(Database.MFCOMPANY));
						mFEstate = mfuel.getString(mfuel.getColumnIndex(Database.MFESTATE));

						StringBuilder mf = new StringBuilder();
						mf.append("0" + ",");
						mf.append(mfDate + ",");
						mf.append(mfterminalID + ",");
						mf.append(mfmachineNo + ",");
						mf.append(mfTime + ",");
						mf.append(mfLitres + ",");
						mf.append(FuelType + ",");
						mf.append(mFCompany + ",");
						mf.append(mFEstate);


						FuelInfo = mf.toString();

						mfuel.moveToNext();

						restApiResponse = new RestApiRequest(getApplicationContext()).MachineFueling(FuelInfo);
						error = restApiResponse;
						try {

							JSONObject jsonObject = new JSONObject(restApiResponse);

							Message = jsonObject.getString("Message");
							if (Message.equals("Authorization has been denied for this request.")) {
								Id = "-1";
								SharedPreferences.Editor edit = mSharedPrefs.edit();
								edit.remove("token");
								edit.apply();
								edit.remove("expires_in");
								edit.apply();
								edit.remove("expires");
								edit.apply();
							}

							Id = jsonObject.getString("Id");
							Title = jsonObject.getString("Title");


							Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
							try {
								if (Integer.parseInt(Id) > 0) {
									ContentValues values = new ContentValues();
									values.put(Database.MFSTATUS, 4);


									long rows = db.update(Database.MACHINEFUEL_TABLE_NAME, values,
											"_id = ? ", new String[]{RowID});

									if (rows > 0) {

									}

								}
								if (Integer.valueOf(Id).intValue() < 0) {

									error = Message;
								}


								//System.out.println(value);}
							} catch (NumberFormatException e) {
								//value = 0; // your default value


							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
						progressStatus++;
						publishProgress("" + progressStatus);
					}
					mfuel.close();
				}

				Cursor batches = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where "
						+ Database.DelivaryNO + " ='" + DelNo + "' and  " + Database.Closed + " = '" + closed + "'", null);
				count = batches.getCount();
				if (batches.getCount() > 0) {
					batches.moveToFirst();
					while (!batches.isAfterLast()) {
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
							totalWeight = "0";
						} else {
							totalWeight = batches.getString(batches.getColumnIndex(Database.TotalWeights));
						}

						if (batches.getString(batches.getColumnIndex(Database.Dispatched)) == null) {
							stringCloseTime = stringCloseTime;
						} else {
							stringCloseTime = batches.getString(batches.getColumnIndex(Database.Dispatched));
						}


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
						BatchSerial = batches.getString(batches.getColumnIndex(Database.DeliveryNoteNumber));
						// bNum,PDAid,date,BatchNo,clerk,otime,bsession,BatchType,CoPrefix,UserID
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
								edit.apply();
								edit.remove("expires_in");
								edit.apply();
								edit.remove("expires");
								edit.apply();
								return null;
							}
							if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
								Id = jsonObject.getString("Id");
								Title = jsonObject.getString("Title");
								Message = jsonObject.getString("Message");

								Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
								try {
									if (Integer.valueOf(Id).intValue() > 0) {
										serverBatchNo = Id;
										ContentValues values = new ContentValues();
										values.put(Database.BatCloudID, serverBatchNo);
										long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
												Database.DeliveryNoteNumber + " = ?", new String[]{BatchSerial});

										if (rows > 0) {

											SharedPreferences.Editor edit = prefs.edit();
											edit.putString("serverBatchNo", serverBatchNo);
											edit.apply();
										}
									}
									if (Integer.valueOf(Id).intValue() < 0) {
										if (Integer.valueOf(Id).intValue() == -8) {
											errorNo = "-8";
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
						progressStatus++;
						publishProgress("" + progressStatus);

						ContentValues resetVals = new ContentValues();
						resetVals.put(Database.CloudID, 0);
						long resetrows = db.update(Database.EM_PRODUCE_COLLECTION_TABLE_NAME, resetVals,
								Database.DataCaptureDevice + " = ?"
								, new String[]{BatchSerial});

						if (resetrows > 0) {
							//Toast.makeText(getApplicationContext(), "Weightment Cloud IDs Cleared Successfully!!", Toast.LENGTH_LONG).show();

							Log.i("INFO", "Weightment Cloud IDs Cleared Successfully!!");
						}


						//serverBatchNo=prefs.getString("serverBatchNo", "");
						//BatchSerial = prefs.getString("DeliverNoteNumber", "");

						Cursor produce = db.rawQuery("select * from " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + " WHERE "
								+ Database.DataCaptureDevice + " ='" + BatchSerial + "' and " + Database.CloudID + " ='" + cloudid + "'", null);
						if (produce.getCount() > 0) {
							//weighments.moveToFirst();
							count = count + produce.getCount();
							while (produce.moveToNext()) {
								ColDate = produce.getString(produce.getColumnIndex(Database.CollDate));
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
								if (produce.getString(produce.getColumnIndex(Database.SourceBlock)) != null) {
									Block = produce.getString(produce.getColumnIndex(Database.SourceBlock));
									if (Block.equals("Select ...")) {
										Block = "";
									} else {
										Block = produce.getString(produce.getColumnIndex(Database.SourceBlock));
									}
								} else {
									Block = "";
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
								SessionNo = produce.getString(produce.getColumnIndex(Database.ReceiptNo));
								FieldClerk = produce.getString(produce.getColumnIndex(Database.FieldClerk));
								CheckinMethod = produce.getString(produce.getColumnIndex(Database.UsedSmartCard));

								Co_prefix = mSharedPrefs.getString("company_prefix", "");
								Current_User = prefs.getString("user", "");
								TaskType = produce.getString(produce.getColumnIndex(Database.TaskType));

								StringBuilder wm = new StringBuilder();
								wm.append(TaskType + ",");
								wm.append(ColDate + ",");
								wm.append(DataDevice + ",");
								wm.append(Time + ",");
								wm.append(FieldClerk + ",");
								wm.append(ProduceCode + ",");
								wm.append(EstateCode + ",");
								wm.append(DivisionCode + ",");
								wm.append(FieldCode + ",");
								wm.append(Block + ",");
								wm.append(TaskCode + ",");
								wm.append(EmployeeNo + ",");
								wm.append(NetWeight + ",");
								wm.append(TareWeight + ",");
								wm.append(Crates + ",");
								wm.append(RecieptNo + ",");
								wm.append(BatchNo + ",");
								wm.append(WeighmentNo + ",");
								wm.append(VarietyCode + ",");
								wm.append(GradeCode + ",");
								wm.append(Co_prefix + ",");
								wm.append(Current_User + ",");
								wm.append(CheckinMethod + ",");
								wm.append("3");

								weighmentInfo = wm.toString();

								try {


									if (Integer.valueOf(errorNo) == -8) {
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
										Cursor checkcloudid = dbhelper.CheckWeighmentCloudID(Id);
										//Check for duplicate checkcloudid number
										if (checkcloudid.getCount() > 0) {
											// Toast.makeText(getApplicationContext(), "checkcloudid already exists",Toast.LENGTH_SHORT).show();

										} else {
											ContentValues values = new ContentValues();
											values.put(Database.CloudID, Id);
											long rows = db.update(Database.EM_PRODUCE_COLLECTION_TABLE_NAME, values,
													Database.EmployeeNo + " = ? AND " + Database.LoadCount + " = ? AND " + Database.DataCaptureDevice + " = ? AND "
															+ Database.ReceiptNo + " = ?", new String[]{EmployeeNo, WeighmentNo, BatchSerial, SessionNo});

											if (rows > 0) {
												Log.i("success:", Id);

											}
										}

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

						} else {

							//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

						}

						stringCloseTime = timeFormat.format(closeTime);

						if (Integer.valueOf(errorNo) == -8) {
							serverBatchNo = BatchID;
						} else {
							serverBatchNo = prefs.getString("serverBatchNo", "");
						}
						restApiResponse = new RestApiRequest(getApplicationContext()).CloseOutgrowersPurchasesBatch(Integer.parseInt(serverBatchNo), stringCloseTime, totalWeight);
						error = restApiResponse;

						try {

							JSONObject jsonObject = new JSONObject(restApiResponse);
							Message = jsonObject.getString("Message");
							if (Message.equals("Authorization has been denied for this request.")) {
								Id = "-1";
								SharedPreferences.Editor edit = mSharedPrefs.edit();
								edit.remove("token");
								edit.apply();
								edit.remove("expires_in");
								edit.apply();
								edit.remove("expires");
								edit.apply();
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

						progressStatus++;
						publishProgress("" + progressStatus);


						Cursor machines = db.rawQuery("select * from " + Database.MACHINE_TABLE_NAME + "," + Database.MACHINEOP_TABLE_NAME + "" +
								" where " + Database.MC_ID + "=" + Database.MACHINENUMBER + " and " + Database.MDATE + "='" + DelDate + "' and " + Database.MSTATUS + "='4' group by machineNo", null);

						count = count + machines.getCount();
						if (machines.getCount() > 0) {
							machines.moveToFirst();
							while (!machines.isAfterLast()) {


								sDate = machines.getString(machines.getColumnIndex(Database.MDATE));
								machineNo = machines.getString(machines.getColumnIndex(Database.MACHINENUMBER));
								mCompany = machines.getString(machines.getColumnIndex(Database.MCOMPANY));
								mEstate = machines.getString(machines.getColumnIndex(Database.MESTATE));

								machines.moveToNext();

								restApiResponse = new RestApiRequest(getApplicationContext()).Allocatekilos(sDate, mEstate, machineNo);
								error = restApiResponse;
								try {

									JSONObject jsonObject = new JSONObject(restApiResponse);

									Message = jsonObject.getString("Message");
									if (Message.equals("Authorization has been denied for this request.")) {
										Id = "-1";
										SharedPreferences.Editor edit = mSharedPrefs.edit();
										edit.remove("token");
										edit.apply();
										edit.remove("expires_in");
										edit.apply();
										edit.remove("expires");
										edit.apply();
									}

									Id = jsonObject.getString("Id");
									Title = jsonObject.getString("Title");


									Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
									try {
										if (Integer.parseInt(Id) > 0) {


										}
										if (Integer.valueOf(Id).intValue() < 0) {

											error = Message;
										}


										//System.out.println(value);}
									} catch (NumberFormatException e) {
										//value = 0; // your default value


									}

								} catch (JSONException e) {
									e.printStackTrace();
								}
								progressStatus++;
								publishProgress("" + progressStatus);
							}
							machines.close();
						}


						if (DelNo.length() > 0)
							condition += " and  " + Database.FdDNoteNum + " = '" + DelNo + "'";
//				if (cloudid > 0)
//					condition += " and  " + Database.CloudID + " = '" + cloudid + "'";


						//SQLiteDatabase db = dbhelper.getReadableDatabase();
						Cursor delivery = db.rawQuery("SELECT * FROM " + Database.Fmr_FactoryDeliveries + " where " + condition + "", null);
						count = count + delivery.getCount();
						if (delivery.getCount() > 0) {
							delivery.moveToFirst();
							while (!delivery.isAfterLast()) {

								DNoteNo = delivery.getString(delivery.getColumnIndex(Database.FdDNoteNum));
								DelDate = delivery.getString(delivery.getColumnIndex(Database.FdDate));
								Factory = delivery.getString(delivery.getColumnIndex(Database.FdFactory));

								if (delivery.getString(delivery.getColumnIndex(Database.FdTransporter)) == null) {
									Transporter = "";
								} else {
									Transporter = delivery.getString(delivery.getColumnIndex(Database.FdTransporter));
								}

								if (delivery.getString(delivery.getColumnIndex(Database.FdVehicle)) == null) {
									Vehicle = "";
								} else {
									Vehicle = delivery.getString(delivery.getColumnIndex(Database.FdVehicle));
								}

								if (delivery.getString(delivery.getColumnIndex(Database.FdTractor)) == null) {
									Tractor = "";
								} else {
									Tractor = delivery.getString(delivery.getColumnIndex(Database.FdTractor));
								}

								if (delivery.getString(delivery.getColumnIndex(Database.FdDriver)) == null) {
									Driver = "";
								} else {
									Driver = delivery.getString(delivery.getColumnIndex(Database.FdDriver));
								}

								if (delivery.getString(delivery.getColumnIndex(Database.FdTurnMan)) == null) {
									TurnMan = "";
								} else {
									TurnMan = delivery.getString(delivery.getColumnIndex(Database.FdTurnMan));
								}

								ArrivalTime = delivery.getString(delivery.getColumnIndex(Database.FdArrivalTime));
								FieldWt = delivery.getString(delivery.getColumnIndex(Database.FdFieldWt));
								GrossWt = delivery.getString(delivery.getColumnIndex(Database.FdGrossWt));
								TareWt = delivery.getString(delivery.getColumnIndex(Database.FdTareWt));
								DepartureTime = delivery.getString(delivery.getColumnIndex(Database.FdDepartureTime));
								CloudID = delivery.getString(delivery.getColumnIndex(Database.CloudID));

								CoPrefix = mSharedPrefs.getString("company_prefix", "");
								InternalSerial = mSharedPrefs.getString("terminalID", "");
								UserIdentifier = prefs.getString("user", "");


								StringBuilder del = new StringBuilder();

								del.append(DNoteNo + ",");
								del.append(DelDate + ",");
								del.append(Factory + ",");
								del.append(Transporter + ",");
								del.append(Vehicle + ",");
								del.append(Tractor + ",");
								del.append(DepartureTime + ",");
								del.append(CoPrefix + ",");
								del.append(EstateCode + ",");
								del.append(UserIdentifier + ",");
								del.append("0" + ",");
								del.append(Driver + ",");
								del.append(TurnMan);
								DeliveryInfo = del.toString();
								delivery.moveToNext();

								restApiResponse = new RestApiRequest(getApplicationContext()).StartDispatch(DeliveryInfo);
								error = restApiResponse;
								try {

									JSONObject jsonObject = new JSONObject(restApiResponse);

									Message = jsonObject.getString("Message");
									if (Message.equals("Authorization has been denied for this request.")) {
										Id = "-1";
										SharedPreferences.Editor edit = mSharedPrefs.edit();
										edit.remove("token");
										edit.apply();
										edit.remove("expires_in");
										edit.apply();
										edit.remove("expires");
										edit.apply();
										return null;
									}

									Id = jsonObject.getString("Id");
									Title = jsonObject.getString("Title");


									Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
									try {
										if (Integer.valueOf(Id).intValue() > 0) {

											DeliveryNo = Id;
											SharedPreferences.Editor edit = prefs.edit();
											edit.putString("DeliveryNo", DeliveryNo);
											edit.apply();
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
								progressStatus++;
								publishProgress("" + progressStatus);
							}
							delivery.close();


						}



						restApiResponse = new RestApiRequest(getApplicationContext()).DeliverBatch(Integer.parseInt(DeliveryNo), deliveryNoteNo);
						error = restApiResponse;
						Log.i("DBatch Response 0 ", error);

						try {

							JSONObject jsonObject = new JSONObject(restApiResponse);

							Message = jsonObject.getString("Message");
							if (Message.equals("Authorization has been denied for this request.")) {
								Id = "-1";
								SharedPreferences.Editor edit = mSharedPrefs.edit();
								edit.remove("token");
								edit.apply();
								edit.remove("expires_in");
								edit.apply();
								edit.remove("expires");
								edit.apply();
								return null;
							}
							Id = jsonObject.getString("Id");
							Title = jsonObject.getString("Title");

							Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
							try {
								if (Integer.valueOf(Id).intValue() > 0) {


									Log.i("Delivery:", DeliveryNo);
									Log.i("DBatch Response 0 ", Id);
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


						DeliveryNo = prefs.getString("DeliveryNo", "");
						restApiResponse = new RestApiRequest(getApplicationContext()).CompleteDispatch(DeliveryNo);

						try {

							JSONObject jsonObject = new JSONObject(restApiResponse);
							Message = jsonObject.getString("Message");
							if (Message.equals("Authorization has been denied for this request.")) {
								Id = "-1";
								SharedPreferences.Editor edit = mSharedPrefs.edit();
								edit.remove("token");
								edit.apply();
								edit.remove("expires_in");
								edit.apply();
								edit.remove("expires");
								edit.apply();
								return null;
							}
							Id = jsonObject.getString("Id");
							Title = jsonObject.getString("Title");


							Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
							try {
								if (Integer.parseInt(Id) > 0) {


									Log.i("CompleteDispatch ID", Id);
									Log.i("CompleteDispatch M", Message);

								}
								if (Integer.parseInt(Id) < 0) {

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

					}
					batches.close();


				} else {

					//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

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
			arcProgress.setProgress(Integer.parseInt(progress[0]));
			arcProgress.setMax(count);
			arcProgress.setBottomText("Uploading ...");
			textStatus.setVisibility(View.VISIBLE);
			textStatus.setText("Uploading... " + Integer.parseInt(progress[0]) + "/" + count + " Records");
		}


		@Override
		protected void onPostExecute(String unused) {
			Log.i(TAG, "onPostExecute");

			try {
				if (Integer.parseInt(Id) > 0) {


					ContentValues values = new ContentValues();
					values.put(Database.CloudID, DeliveryNo);
					long rows = db.update(Database.Fmr_FactoryDeliveries, values,
							Database.FdDNoteNum + " = ?", new String[]{DelNo});

					if (rows > 0) {
						Toast.makeText(UploadNewActivity.this, "Data Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
						new Restart().execute();
					}


				} else if (Integer.parseInt(Id) < 0) {
					if (Id.equals("-8080")) {
						Context context = getApplicationContext();
						LayoutInflater inflater = getLayoutInflater();
						View customToastroot = inflater.inflate(R.layout.red_toast, null);
						TextView text = customToastroot.findViewById(R.id.toast);
						text.setText("Server Not Available !!");
						Toast customtoast = new Toast(context);
						customtoast.setView(customToastroot);
						customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
						customtoast.setDuration(Toast.LENGTH_LONG);
						customtoast.show();
						//Toast.makeText(getApplicationContext(), Message, Toast.LENGTH_LONG).show();

						finish();
						return;
					}

					Context context = getApplicationContext();
					LayoutInflater inflater = getLayoutInflater();
					View customToastroot = inflater.inflate(R.layout.red_toast, null);
					TextView text = customToastroot.findViewById(R.id.toast);
					text.setText(Message);
					Toast customtoast = new Toast(context);
					customtoast.setView(customToastroot);
					customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
					customtoast.setDuration(Toast.LENGTH_LONG);
					customtoast.show();
					Toast.makeText(getApplicationContext(), Message, Toast.LENGTH_LONG).show();
					finish();

				}

			} catch (NumberFormatException e) {

				if (error.equals("-8080")) {
					Toast.makeText(UploadNewActivity.this, "Server Not Available !!", Toast.LENGTH_LONG).show();
					finish();
					return;
				} else {
					Toast.makeText(UploadNewActivity.this, error, Toast.LENGTH_LONG).show();
					finish();
				}
			}


		}

	}

	public class DeliveryArrayAdapter extends ArrayAdapter<Delivery> {

		Context context;
		int layoutResourceId;
		ArrayList<Delivery> students;

		public DeliveryArrayAdapter(Context context, int layoutResourceId,
									ArrayList<Delivery> studs) {
			super(context, layoutResourceId, studs);
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.students = studs;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View item = convertView;


			if (item == null) {
				LayoutInflater inflater = ((Activity) context).getLayoutInflater();
				item = inflater.inflate(layoutResourceId, parent, false);
				DeliveryWrapper = new UploadNewActivity.DeliveryArrayAdapter.DeliveryWrapper();
				DeliveryWrapper.id = item.findViewById(R.id.txtAccountId);
				DeliveryWrapper.number = item.findViewById(R.id.tv_number);
				DeliveryWrapper.deldate = item.findViewById(R.id.tv_date);
				DeliveryWrapper.totalkgs = item.findViewById(R.id.txtTotalKgs);
				DeliveryWrapper.Status = item.findViewById(R.id.txtStatus);
				DeliveryWrapper.btnUpload = item.findViewById(R.id.btnUpload);

				item.setTag(DeliveryWrapper);
			} else {
				DeliveryWrapper = (UploadNewActivity.DeliveryArrayAdapter.DeliveryWrapper) item.getTag();
			}

			student = students.get(position);
			DeliveryWrapper.id.setText(student.getID());
			DeliveryWrapper.number.setText(student.getDNoteNo());
			DeliveryWrapper.deldate.setText(student.getDeldate());
			DeliveryWrapper.totalkgs.setText(student.getTotalkgs());

			if (student.getCloudID() != null) {
				if (student.getCloudID().equals("0") || student.getCloudID().equals("")) {
					DeliveryWrapper.Status.setText("Not Uploaded");
					DeliveryWrapper.btnUpload.setText("Upload");
				} else {

					DeliveryWrapper.Status.setText("Uploaded");
					DeliveryWrapper.btnUpload.setText("Re-Upload");
				}
			}


			DeliveryWrapper.btnUpload.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					listReciepts.performItemClick(listReciepts.getAdapter().getView(position, null, null), position, listReciepts.getAdapter().getItemId(0));


					DeliveryWrapper.id.getText().toString();
					//Toast.makeText(getApplicationContext(), DeliveryWrapper.id.getText().toString(), Toast.LENGTH_LONG).show();
					SharedPreferences.Editor edit = prefs.edit();
					edit.putString("_id", DeliveryWrapper.id.getText().toString());
					edit.apply();


					if (!checkList()) {
						return;
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
					builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Upload Data?</font>"))
							.setCancelable(false)
							.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									DelNo = DeliveryWrapper.number.getText().toString();
									DelDate = DeliveryWrapper.deldate.getText().toString();
									syncTasks();

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


			return item;

		}

		private class DeliveryWrapper {
			TextView id;
			TextView number;
			TextView deldate;
			TextView totalkgs;
			TextView Status;
			Button btnUpload;

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


			mIntent = new Intent(getApplicationContext(), UploadNewActivity.class);
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
			TextView text = customToastroot.findViewById(R.id.toast);
			text.setText("Nothing Found To Upload");
			Toast customtoast = new Toast(context);
			customtoast.setView(customToastroot);
			customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
			customtoast.setDuration(Toast.LENGTH_LONG);
			customtoast.show();
		}
	}


}
