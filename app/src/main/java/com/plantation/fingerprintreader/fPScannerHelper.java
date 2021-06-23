package com.plantation.fingerprintreader;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.plantation.R;

import cn.com.aratek.dev.Terminal;
import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintScanner;


public class fPScannerHelper {
    private static final String FP_DB_PATH = "/sdcard/fp.db";
    public static boolean isOpen = false;
    public static FingerprintScanner fPScanner;
    static Context context;
    static Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                int error;
                if ((error = fPScanner.powerOn()) != FingerprintScanner.RESULT_OK) {
                    Toast.makeText(
                            context,
                            context.getString(R.string.fp_scanner_fail_on)
                                    + error, Toast.LENGTH_SHORT)
                            .show();
                } else if ((error = fPScanner.open()) != FingerprintScanner.RESULT_OK) {
                    if (error != -1013) {
                        Toast.makeText(
                                context,
                                context.getString(R.string.fp_scanner_fail_open)
                                        + error, Toast.LENGTH_SHORT)
                                .show();
                    }

                }
                if ((error = Bione.initialize(context, FP_DB_PATH)) != Bione.RESULT_OK) {
                    Toast.makeText(
                            context,
                            context.getString(R.string.algorithm_initialization_failed)
                                    + error, Toast.LENGTH_SHORT)
                            .show();
                }
                isOpen = true;

            } else if (msg.what == 1) {
                if (isOpen) {
                    int error;
                    if ((error = fPScanner.close()) != FingerprintScanner.RESULT_OK) {
                        Toast.makeText(
                                context,
                                context.getString(R.string.fp_scanner_fail_close)
                                        + error, Toast.LENGTH_SHORT)
                                .show();
                    } else if ((error = fPScanner.powerOff()) != FingerprintScanner.RESULT_OK) {
                        Toast.makeText(
                                context,
                                context.getString(R.string.fp_scanner_fail_off)
                                        + error, Toast.LENGTH_SHORT)
                                .show();
                    }
                    if ((error = Bione.exit()) != Bione.RESULT_OK) {
                        Toast.makeText(
                                context,
                                context.getString(R.string.algorithm_cleanup_failed)
                                        + error, Toast.LENGTH_SHORT)
                                .show();
                    }

                }
                isOpen = false;
            }

        }
    };
    private static boolean isLightOn = false;

    static {
        isOpen = false;
        fPScanner = FingerprintScanner.getInstance();
    }

    public static boolean isLight() {
        return isLightOn;
    }

    public static void lightOn() {
        if (!isLightOn) {
            Log.e("fPScannerHelper", "lightOn");
            fPScanner.prepare();
            isLightOn = true;
        }
    }

    public static void lightOff() {
        Log.e("fPScannerHelper", "lightOff");
        fPScanner.finish();
        isLightOn = false;
    }

    public static void open(Context cont) {
        context = cont;
        System.out.println("SDK版本：" + Terminal.getSdkVersion());
        mHandler.sendEmptyMessageDelayed(0, 500);
    }

    public static void close(Context cont) {
        context = cont;
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }
}
