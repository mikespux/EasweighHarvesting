package com.plantation.fingerprintreader;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.plantation.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintImage;
import cn.com.aratek.fp.FingerprintScanner;
import cn.com.aratek.util.Result;

@SuppressLint("HandlerLeak")
public class FingerCollectActivity extends BaseActivity implements
        OnClickListener {
    // ==========================常量==============================
    private final static int INT_COLLECT_FAIL_MEG = 1;
    private final static int INT_COLLECT_READ_MEG = 2;
    private final static int INT_COLLECT_SUCCESS_MEG = 3;
    private final static int INT_COLLECT_REPEAT_MEG = 5;
    private final static int START_THREAD = 4;
    private final static int INT_VERIFY_NUM = 50;
    static FingerCollectActivity fingerCollectActivity;
    // ==========================控件==============================
    // 指位图像集合.
    private final Map<String, FingerView> mapFingerView = new HashMap<String, FingerView>();
    // ==============================数据========================================
    // 指纹数据.
    private final Map<String, byte[]> mapFprintFeature = null;
    // 指纹线程开关.
    public boolean fpThreadONOrOff = true;
    // 线程休眠时间
    public int sleepTime = 100;
    LinearLayout LeftFingers, RightFingers;
    FPThread fpThread;
    Boolean isPressed = false;
    // 提示导航.
    private ImageView program;
    // 提示信息.
    private TextView messagebox;
    // 指纹图像.
    private ImageView fingerpic;
    // 右手指位图.
    private FingerView rfinger1;
    private FingerView rfinger2;
    private FingerView rfinger3;
    private FingerView rfinger4;
    private FingerView rfinger5;
    private FingerView lfinger1;
    private FingerView lfinger2;
    private FingerView lfinger3;
    private FingerView lfinger4;
    private FingerView lfinger5;
    // 返回采集.
    private Button backCollect;
    // 删除指纹
    private Button deleteFinger;
    // 当前指位图.
    private FingerView currentFingerView;
    // 删除确认弹出框.
    private Builder fpDelConfim;
    private Builder fpSaveConfim;

    // ===============================业务类============================================
    // 当前指位.
    private String currentFP = null;
    // 当前指纹特征.
    private byte[] currentFpFeature = null;
    // 当前指纹图像Bitmap.
    private Bitmap currentFpImage = null;
    /**
     * 信息提示进程.
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INT_COLLECT_READ_MEG:// 读取指纹
                    MediaPlayerHelper.payMedia(FingerCollectActivity.this,
                            R.raw.fpreading);
                    messagebox.setText(getString(R.string.read_finger));
                    program.setImageDrawable(getResources().getDrawable(
                            R.drawable.p2));
                    break;
                case INT_COLLECT_FAIL_MEG:// 采集失败
                    fPScannerHelper.lightOff();
                    MediaPlayerHelper.payMedia(FingerCollectActivity.this,
                            R.raw.collectfail);
                    messagebox.setText(getString(R.string.collect_fail));
                    program.setImageDrawable(getResources().getDrawable(
                            R.drawable.p1));
                    fingerpic.setImageBitmap(currentFpImage);
                    currentFingerView.setSelected(false);
                    currentFingerView.setRegisted(false);
                    currentFingerView = null;
//				currentFpImage = null;
                    currentFP = null;
                    currentFpFeature = null;
                    break;
                case INT_COLLECT_SUCCESS_MEG:// 采集成功
                    fPScannerHelper.lightOff();
                    MediaPlayerHelper.payMedia(FingerCollectActivity.this,
                            R.raw.collectsuccess);
                    messagebox.setText(getString(R.string.collect_success));
                    program.setImageDrawable(getResources().getDrawable(
                            R.drawable.p3));
                    fingerpic.setImageBitmap(currentFpImage);

                    currentFingerView.setRegisted(true);
                    break;
                case INT_COLLECT_REPEAT_MEG:
                    messagebox.setText(getString(R.string.repeat_collection));
                    program.setImageDrawable(getResources().getDrawable(
                            R.drawable.p1));
                    break;
                case START_THREAD:
                    runFPThread();
                    break;
            }
        }
    };
    // 应用程序.
    private PrjApp prjApp;

    public static FingerCollectActivity getContext() {
        return fingerCollectActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_finger_collect);
        // 获取应用上下文对象.
//		prjApp = ((PrjApp) getApplicationContext());
        // 指纹获取对象.
        // fPScannerHelper.fPScanner = prjApp.getfPScannerHelper.fPScanner();

        // 初始化页面控件.
        initControl();

        // 指纹数据.
	/*	mapFprintFeature = prjApp.getMapFprintFeature() == null ? new HashMap<String, byte[]>()
				: prjApp.getMapFprintFeature();
		Iterator<String> fpKeyItr = mapFprintFeature.keySet().iterator();
		while (fpKeyItr.hasNext()) {
			mapFingerView.get(fpKeyItr.next()).setRegisted(true);
		}
		fingerCollectActivity = this;*/
    }

    @Override
    protected void onResume() {
        super.onResume();
		/*isPressed = false;
		if (FingerprintTapActivity.fingerprintTapActivity.currTap == 1) {
			fpThreadONOrOff = true;
		}*/
        mHandler.sendEmptyMessageDelayed(START_THREAD, 1000);
        if (!isPressed && fpThreadONOrOff) {
            MediaPlayerHelper.payMedia(FingerCollectActivity.this,
                    R.raw.fpselect);
        }
    }

    @Override
    protected void onPause() {
        // 完成清理、断电操作
        Log.e("hecl", "onPause");
        sleepTime = 0;
        fpThreadONOrOff = false;
        isPressed = true;
        fPScannerHelper.lightOff();
        super.onPause();
    }

    private void initControl() {

        // 提示导航.
        program = findViewById(R.id.program);

        // 提示信息.
        messagebox = findViewById(R.id.messagebox);

        // 指纹图像
        fingerpic = findViewById(R.id.fingerpic);

        // 右手指位图.
        LeftFingers = findViewById(R.id.LeftFingers);
        RightFingers = findViewById(R.id.RightFingers);
        rfinger1 = findViewById(R.id.rfinger1);
        rfinger1.setImageResources(R.drawable.f1_normal,
                R.drawable.f1_selected, R.drawable.f1_reading,
                R.drawable.f1_registed, R.drawable.f1_selected_registed);
        rfinger2 = findViewById(R.id.rfinger2);
        rfinger2.setImageResources(R.drawable.f2_normal,
                R.drawable.f2_selected, R.drawable.f2_reading,
                R.drawable.f2_registed, R.drawable.f2_selected_registed);
        rfinger3 = findViewById(R.id.rfinger3);
        rfinger3.setImageResources(R.drawable.f3_normal,
                R.drawable.f3_selected, R.drawable.f3_reading,
                R.drawable.f3_registed, R.drawable.f3_selected_registed);
        rfinger4 = findViewById(R.id.rfinger4);
        rfinger4.setImageResources(R.drawable.f4_normal,
                R.drawable.f4_selected, R.drawable.f4_reading,
                R.drawable.f4_registed, R.drawable.f4_selected_registed);
        rfinger5 = findViewById(R.id.rfinger5);
        rfinger5.setImageResources(R.drawable.f5_normal,
                R.drawable.f5_selected, R.drawable.f5_reading,
                R.drawable.f5_registed, R.drawable.f5_selected_registed);


        lfinger1 = findViewById(R.id.lfinger1);
        lfinger1.setImageResources(R.drawable.f1_normal,
                R.drawable.f1_selected, R.drawable.f1_reading,
                R.drawable.f1_registed, R.drawable.f1_selected_registed);
        lfinger2 = findViewById(R.id.lfinger2);
        lfinger2.setImageResources(R.drawable.f2_normal,
                R.drawable.f2_selected, R.drawable.f2_reading,
                R.drawable.f2_registed, R.drawable.f2_selected_registed);
        lfinger3 = findViewById(R.id.lfinger3);
        lfinger3.setImageResources(R.drawable.f3_normal,
                R.drawable.f3_selected, R.drawable.f3_reading,
                R.drawable.f3_registed, R.drawable.f3_selected_registed);
        lfinger4 = findViewById(R.id.lfinger4);
        lfinger4.setImageResources(R.drawable.f4_normal,
                R.drawable.f4_selected, R.drawable.f4_reading,
                R.drawable.f4_registed, R.drawable.f4_selected_registed);
        lfinger5 = findViewById(R.id.lfinger5);
        lfinger5.setImageResources(R.drawable.f5_normal,
                R.drawable.f5_selected, R.drawable.f5_reading,
                R.drawable.f5_registed, R.drawable.f5_selected_registed);
		/*mapFingerView.put(ConstantBusi.FP_INFO.R1.getId(), rfinger1);
		mapFingerView.put(ConstantBusi.FP_INFO.R2.getId(), rfinger2);
		mapFingerView.put(ConstantBusi.FP_INFO.R3.getId(), rfinger3);
		mapFingerView.put(ConstantBusi.FP_INFO.R4.getId(), rfinger4);
		mapFingerView.put(ConstantBusi.FP_INFO.R5.getId(), rfinger5);*/

        // 返回采集.
        backCollect = findViewById(R.id.backCollect);
        backCollect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanUI();
            }
        });
        deleteFinger = findViewById(R.id.leftHand);
        deleteFinger.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LeftFingers.setVisibility(View.VISIBLE);
                RightFingers.setVisibility(View.GONE);
				/*if (currentFingerView == null
						|| !currentFingerView.getRegisted()) {
					showDialog(getString(R.string.delete_finger),
							getString(R.string.select_registed_finger));

				} else if (currentFingerView.getRegisted()) {
					fpDelConfim.create().show();

				}*/
            }
        });

        // 删除确认弹出框.
        fpDelConfim = new Builder(FingerCollectActivity.this);
        fpDelConfim.setMessage(getString(R.string.sure_to_delete));
        fpDelConfim.setTitle(getString(R.string.delete_finger_info));
        fpDelConfim.setPositiveButton(getString(R.string.delete_confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mapFprintFeature.remove(currentFP);
                        prjApp.setMapFprintFeature(mapFprintFeature);

                        currentFingerView.setSelected(false);
                        currentFingerView.setRegisted(false);
                        currentFingerView.setReading(false);

                        program.setImageDrawable(getResources().getDrawable(
                                R.drawable.p1));
                        messagebox
                                .setText(getString(R.string.select_collect_finger));
                        fingerpic.setImageDrawable(getResources().getDrawable(
                                R.drawable.pic_finger));
                        currentFingerView = null;
                        currentFpImage = null;
                        currentFP = null;
                        currentFpFeature = null;
                    }
                });

        fpDelConfim.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentFingerView.setSelected(false);
                        dialog.dismiss();
                    }
                });

    }

    public void cleanUI() {
        MediaPlayerHelper.payMedia(FingerCollectActivity.this,
                R.raw.fpselect);
        program.setImageDrawable(getResources().getDrawable(
                R.drawable.p1));
        messagebox.setText(getString(R.string.select_collect_finger));
        fingerpic.setImageDrawable(getResources().getDrawable(
                R.drawable.pic_finger));
        if (currentFingerView != null) {
            currentFingerView.setSelected(false);
        }
        currentFingerView = null;
        currentFpImage = null;
        currentFP = null;
        currentFpFeature = null;
        fPScannerHelper.lightOff();
    }

    void showDialog(String title, String message) {
        AlertDialog alertDialog = new Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
		/*String clickFP = "";
		isPressed = true;
		switch (v.getId()) {
		case R.id.rfinger1:
			if (v.getId() == R.id.rfinger1) {
				clickFP = ConstantBusi.FP_INFO.R1.getId();
			}
		case R.id.rfinger2:
			if (v.getId() == R.id.rfinger2) {
				clickFP = ConstantBusi.FP_INFO.R2.getId();
			}
		case R.id.rfinger3:
			if (v.getId() == R.id.rfinger3) {
				clickFP = ConstantBusi.FP_INFO.R3.getId();
			}
		case R.id.rfinger4:
			if (v.getId() == R.id.rfinger4) {
				clickFP = ConstantBusi.FP_INFO.R4.getId();
			}
		case R.id.rfinger5:
			// ================指纹线程配合的代码===START============
			if (v.getId() == R.id.rfinger5) {
				clickFP = ConstantBusi.FP_INFO.R5.getId();
			}

			if (currentFP != null) {
				if (currentFP.equals(clickFP)) {
					if (currentFingerView.getRegisted()) {
						return;
					}
				} else {
					currentFingerView.setSelected(false);
					program.setImageDrawable(getResources().getDrawable(
							R.drawable.p1));
				}
			}

			currentFP = clickFP;
			currentFingerView = (FingerView) v;
			currentFingerView.setSelected(true);
			fingerpic.setImageDrawable(getResources().getDrawable(
					R.drawable.pic_finger));
			if (currentFingerView.getRegisted()) {
				return;
			} else {
				messagebox.setText(getString(R.string.please_pressing)
						+ ConstantBusi.FP_INFO.getValueById(currentFP) + "！");

				MediaPlayerHelper.payMedia(FingerCollectActivity.this,
						ConstantBusi.FP_INFO.getVoiceFileNameById(currentFP));
				// ================指纹线程配合的代码===END==============
			}
			fPScannerHelper.lightOn();
		}*/
    }

    public void runFPThread() {
        // 启动指纹线程.
        if (fpThreadONOrOff) {
            sleepTime = 1000;
            fpThreadONOrOff = true;
            fpThread = new FPThread();
            fpThread.start();
        }

    }

    private void collectFP() {
        if (!fPScannerHelper.isOpen) {
            return;
        }
        if (!fpThreadONOrOff) {
            return;
        }
        if (!fpThreadONOrOff) {
            return;
        }
        Result res = fPScannerHelper.fPScanner.capture();

        if (res.error == FingerprintScanner.RESULT_OK
                && currentFP != null
                && !mapFprintFeature.containsKey(currentFP)) {
            mHandler.sendEmptyMessage(INT_COLLECT_READ_MEG);
            if (!fpThreadONOrOff) {
                return;
            }
            FingerprintImage fi = (FingerprintImage) res.data;
            byte[] fpBmp = null;
            Bitmap bitmap = null;
            if (fi != null && (fpBmp = fi.convert2Bmp()) != null && (bitmap = BitmapFactory.decodeByteArray(fpBmp, 0, fpBmp.length)) != null) {
                currentFpImage = bitmap;
            } else {
                mHandler.sendEmptyMessage(INT_COLLECT_FAIL_MEG);
                return;
            }
            res = Bione.extractFeature(fi);
            if (res.error != Bione.RESULT_OK) {
                mHandler.sendEmptyMessage(INT_COLLECT_FAIL_MEG);
                return;
            } else {
                currentFpFeature = (byte[]) res.data;
            }
            if (isExistence(currentFpFeature)) {
                mHandler.sendEmptyMessage(INT_COLLECT_REPEAT_MEG);
            } else {

                mapFprintFeature.put(currentFP, currentFpFeature);
                prjApp.setMapFprintFeature(mapFprintFeature);
                mHandler.sendEmptyMessage(INT_COLLECT_SUCCESS_MEG);
//				registerFinger(0, currentFpFeature);
            }
            return;
        }
    }

    private boolean isExistence(byte[] fp) {
        Iterator<String> fpKeyItr = mapFprintFeature.keySet().iterator();
        String fpKey = null;
        Result res;
        while (fpKeyItr.hasNext()) {
            fpKey = fpKeyItr.next();
            res = Bione.verify(fp, mapFprintFeature.get(fpKey));
            if (res.error == Bione.RESULT_OK) {
                return (Boolean) res.data;
            }
        }
        return false;
    }

    public boolean registerFinger(int finger, byte[] data) {

        if (data != null && data.length != 0) {
            File sdDir = null;
            try {
                boolean sdCardExist = Environment.getExternalStorageState()
                        .equals(Environment.MEDIA_MOUNTED);
                if (sdCardExist) {
                    // 获取根目录
                    sdDir = Environment.getExternalStorageDirectory();
                    File ff = new File(sdDir.getName() + "/aratek");
                    if (!ff.isDirectory()) {
                        ff.mkdirs();
                    }
                }
                File f = new File(sdDir.getName() + "/aratek/" + "finger.txt");
                System.out.println(f.getPath());
                FileOutputStream out = new FileOutputStream(f, false);
                out.write(data);
                out.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private class FPThread extends Thread {
        public void run() {

            while (fpThreadONOrOff) {
                try {
                    if (fpThreadONOrOff) {
                        //collectFP();
                        sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    Log.i("FPThread", "FPThread sleep", e);
                }

            }
        }
    }

}
