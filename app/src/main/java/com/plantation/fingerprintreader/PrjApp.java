package com.plantation.fingerprintreader;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.DataOutputStream;
import java.util.Map;

@SuppressLint("HandlerLeak")
public class PrjApp extends Application {
    // 指纹SDK封装对象.
    //private FingerprintScanner fPScanner;

    public static Context appContext;
    //指纹数据.
    private Map<String, byte[]> mapFprintFeature = null;
    private int pos;

    /*public FingerprintScanner getfPScanner() {
        return fPScanner;
    }*/
    public static int execRootCmdSilent(String cmd) {
        try {
            Process proc = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(proc.getOutputStream());
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            proc.waitFor();
            return proc.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -99;
        }
    }

    public void onCreate() {
        super.onCreate();

        appContext = this;

//		execRootCmdSilent("mount -t usbfs -o devmode=0666 none /proc/bus/usb");
		/*fPScanner = FingerprintScanner.getInstance();
		if (!fPScanner.powerOn()) {
			Log.i("指纹仪上电失败！错误码：" + fPScanner.getLastError(),"");
		} else if (!fPScanner.open()) {
			Log.i("指纹仪打开失败！错误码：" + fPScanner.getLastError(),"");
		}
		Log.i("初始化系统参数：", "执行完成！");*/
    }

    public void onTerminate() {
        super.onTerminate();
        Log.i("退出系统释放资源：", "释放完成！");
    }

    public Map<String, byte[]> getMapFprintFeature() {
        return mapFprintFeature;
    }

    public void setMapFprintFeature(Map<String, byte[]> mapFprintFeature) {
        this.mapFprintFeature = mapFprintFeature;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

}
