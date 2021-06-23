/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 *
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.plantation.vsp.serialdevice;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.plantation.R;
import com.plantation.trancell.BleBaseActivity;
import com.plantation.vsp.misc.DataManipulation;

import java.text.DecimalFormat;


public class SerialActivity extends BleBaseActivity implements
        SerialManagerUiCallback {
    DataManipulation Reading;
    private Button mBtnSend;
    private ScrollView mScrollViewConsoleOutput;
    private EditText mInputBox;
    private TextView mValueConsoleOutputTv;
    private TextView mValueRxCounterTv;
    private TextView mValueTxCounterTv;
    private SerialManager mSerialManager;

    private boolean isPrefClearTextAfterSending = false;
    private boolean isPrefSendCR = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_serial);
        Reading = new DataManipulation();
        mSerialManager = new SerialManager(this, this);
        setBleBaseDeviceManager(mSerialManager);

        //initialiseDialogAbout(getResources().getString(R.string.about_serial));
        initialiseDialogFoundDevices("VSP");
    }

    @Override
    public void bindViews() {
        super.bindViews();

        mBtnSend = findViewById(R.id.btnSend);
        mScrollViewConsoleOutput = findViewById(R.id.scrollViewConsoleOutput);
        mInputBox = findViewById(R.id.inputBox);
        mValueConsoleOutputTv = findViewById(R.id.valueConsoleOutputTv);
        mValueRxCounterTv = findViewById(R.id.valueRxCounterTv);
        mValueTxCounterTv = findViewById(R.id.valueTxCounterTv);
    }

    @Override
    public void setListeners() {
        super.setListeners();

        mBtnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // to send data to module
                String data = mInputBox.getText().toString();
                if (data != null) {
                    mBtnSend.setEnabled(false);
                    if (mValueConsoleOutputTv.getText().length() <= 0) {
                        mValueConsoleOutputTv.append(">");
                    } else {
                        mValueConsoleOutputTv.append("\n\n>");
                    }

                    if (isPrefSendCR == true) {
                        mSerialManager.startDataTransfer(data + "\r");
                    } else if (isPrefSendCR == false) {
                        mSerialManager.startDataTransfer(data);
                    }

                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus()
                                    .getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    if (isPrefClearTextAfterSending == true) {
                        mInputBox.setText("");
                    } else {
                        // do not clear the text from the editText
                    }
                }
            }
        });
    }

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.serial, menu);
		getActionBar().setIcon(R.drawable.icon_serial);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{

		case R.id.action_clear:
			mValueConsoleOutputTv.setText("");
			mSerialManager.clearRxAndTxCounter();

			mValueRxCounterTv.setText("0");
			mValueTxCounterTv.setText("0");

			break;
		}
		return super.onOptionsItemSelected(item);
	}*/

    @Override
    public void onUiVspServiceFound(final boolean found) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBtnSend.setEnabled(found);
            }
        });
    }

    @Override
    public void onUiSendDataSuccess(final String dataSend) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mValueConsoleOutputTv.append(dataSend);
                mValueTxCounterTv.setText("" + mSerialManager.getTxCounter());
                mScrollViewConsoleOutput.smoothScrollTo(0,
                        mValueConsoleOutputTv.getBottom());
            }
        });
    }

    @Override
    public void onUiReceiveData(final String dataReceived, final String Reading) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                double value;
                final DecimalFormat df = new DecimalFormat("#0.0#");
                try {


                    value = new Double(Reading);
                    //System.out.println(value);}
                } catch (NumberFormatException e) {
                    value = 0; // your default value
                }


                mValueConsoleOutputTv.append(df.format(value));


                mValueRxCounterTv.setText("" + mSerialManager.getRxCounter());
                mScrollViewConsoleOutput.smoothScrollTo(0,
                        mValueConsoleOutputTv.getBottom());
            }
        });
    }

    @Override
    public void onUiUploaded() {
        mBtnSend.setEnabled(true);
    }

    @Override
    protected void loadPref() {
        super.loadPref();
        isPrefClearTextAfterSending = mSharedPreferences.getBoolean(
                "pref_clear_text_after_sending", false);
        isPrefSendCR = mSharedPreferences.getBoolean(
                "pref_append_/r_at_end_of_data", true);
    }
}