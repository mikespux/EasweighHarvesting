package com.plantation.soap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class SoapRequest {
    private static final String MY_DB = "com.octagon.easyweigh_preferences";
    private final String CREATE_BATCH_METHOD;
    private final String METHOD_NAMESPACE;
    private final String NAMESPACE;
    private final String POST_WEIGHMENT_METHOD;
    private final String SIGNOFF_BATCH_METHOD;
    private final String TAG;
    private final Context _context;
    HttpTransportSE httpTransport;
    HttpTransportSE httpTransport2;
    SharedPreferences mSharedPrefs;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;
    private String _URL;
    private String _licenseKey;

    public SoapRequest(Context ctx) {
        _URL = null;
        _licenseKey = null;
        NAMESPACE = "http://tempuri.org/IEasyweighService/";
        METHOD_NAMESPACE = "http://tempuri.org/";
        TAG = "SoapRequest";
        CREATE_BATCH_METHOD = "CreateBatch";
        POST_WEIGHMENT_METHOD = "PostWeighment";
        SIGNOFF_BATCH_METHOD = "SignoffBatch";
        _context = ctx;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        _URL = mSharedPrefs.getString("portalURL", null);
        // _URL = mSharedPrefs.getString("portalURL", null);

        if (this.mSharedPrefs.getString("internetAccessModes", null).equals("WF")) {
            _URL = mSharedPrefs.getString("portalURL", null);

        } else {
            _URL = mSharedPrefs.getString("mdportalURL", null);

        }


        _licenseKey = mSharedPrefs.getString("licenseKey", null);
        //_URL = "http://192.168.0.24/Easyweigh/EasywayCloudService.svc/EasyweighService";
        //_licenseKey = "QMZ1Y46KD3";
        httpTransport = new HttpTransportSE(_URL);
        httpTransport2 = new HttpTransportSE(_URL);
    }

    public String DoDataSourceTest() {
        SoapObject request = new SoapObject("http://tempuri.org/", "DoDatasourceTest");
        request.addProperty("Lickey", _licenseKey);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/DoDatasourceTest", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            // Log.i("Response 2 ", response.getProperty(2).toString());

            if (response.getProperty(1).toString().equals("Ok")) {
                // Toast.makeText(_context, response.getProperty(0).toString(), Toast.LENGTH_LONG).show();
                return response.getProperty(0).toString();
            }
            String errorNO = response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("errorNo", errorNO);
            edit.commit();

            return new StringBuilder(response.getProperty(2).toString()).toString();
            //return new StringBuilder(String.valueOf(response.getProperty(0).toString())).append("\r\n").append(response.getProperty(1).toString()).append("\r\n").append(response.getProperty(2).toString()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            String Server = "-8080";
            return Server;
        }
    }


    public String PushAttendanceRecord(String var1, String var2) {
        Log.i("SoapOperations", "Post weighmeninfo " + var2);
        Log.i("SoapOperations", "Post batch id " + var1);
        Object var3 = null;
        SoapObject var4 = new SoapObject("http://tempuri.org/", "PushAttendanceRecord");
        var4.addProperty("Lickey", this._licenseKey);
        var4.addProperty("Batch", var1);
        var4.addProperty("AttendanceInfo", var2);
        SoapSerializationEnvelope var6 = new SoapSerializationEnvelope(110);
        var6.dotNet = true;
        var6.setOutputSoapObject(var4);

        try {
            this.httpTransport.debug = true;
            this.httpTransport.call("http://tempuri.org/IEasyweighService/PushAttendanceRecord", var6);
            SoapObject var7 = (SoapObject) var6.getResponse();
            Log.i("Response 0 ", var7.getProperty(0).toString());
            Log.i("Response 1 ", var7.getProperty(1).toString());
            Log.i("Response 2 ", var7.getProperty(2).toString());
            if (var7.getProperty(1).toString().equals("Ok")) {
                var1 = var7.getProperty(0).toString();
            } else {
                String errorNO = var7.getProperty(0).toString();
                // save user data
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("errorNo", errorNO);
                edit.commit();
                var1 = var7.getProperty(2).toString();

            }
        } catch (Exception var5) {
            Log.e("SoapOperations", "Error posting weighment");
            var5.printStackTrace();
            var1 = (String) var3;
        }

        return var1;
    }

    public String VerifyBatch(String BatchNo, String TotalWeights) {
        SoapObject request = new SoapObject("http://tempuri.org/", "VerifyBatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("BatchNo", BatchNo);
        request.addProperty("TotalWeights", TotalWeights);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/VerifyBatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            if (response.getProperty(1).toString().equals("Ok")) {
                return response.getProperty(0).toString();
            }
            String errorNO = response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("errorNo", errorNO);
            edit.commit();
            return response.getProperty(2).toString();


        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            return ex.getMessage();
        }
    }

    public String createDelivery(String DeliveryInfo) {
        SoapObject request = new SoapObject("http://tempuri.org/", "CreateDelivery");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("DeliveryInfo", DeliveryInfo);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/CreateDelivery", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            edit = prefs.edit();
            if (response.getProperty(1).toString().equals("Ok")) {
                // Toast.makeText(_context, response.getProperty(0).toString(), Toast.LENGTH_LONG).show();
                edit.remove("DelerrorNo");
                edit.commit();
                return response.getProperty(0).toString();


            }
            String errorNO = response.getProperty(0).toString();
            // save user data

            edit.putString("DelerrorNo", errorNO);
            edit.commit();
            return new StringBuilder(response.getProperty(2).toString()).toString();
            //return new StringBuilder(String.valueOf(response.getProperty(0).toString())).append("\r\n").append(response.getProperty(1).toString()).append("\r\n").append(response.getProperty(2).toString()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            String Server = "-8080";
            return Server;
        }
    }

    public String DeliverBatch(String var1, String var2) {
        Log.i("SoapOperations", "Post BatchNo" + var2);
        Log.i("SoapOperations", "Post Delivery " + var1);
        Object var3 = null;
        SoapObject var4 = new SoapObject("http://tempuri.org/", "DeliverBatch");
        var4.addProperty("Lickey", this._licenseKey);
        var4.addProperty("Delivery", var1);
        var4.addProperty("BatchNo", var2);
        SoapSerializationEnvelope var6 = new SoapSerializationEnvelope(110);
        var6.dotNet = true;
        var6.setOutputSoapObject(var4);

        try {
            this.httpTransport.debug = true;
            this.httpTransport.call("http://tempuri.org/IEasyweighService/DeliverBatch", var6);
            SoapObject var7 = (SoapObject) var6.getResponse();
            Log.i("Response 0 ", var7.getProperty(0).toString());
            Log.i("Response 1 ", var7.getProperty(1).toString());
            Log.i("Response 2 ", var7.getProperty(2).toString());
            if (var7.getProperty(1).toString().equals("Ok")) {
                var1 = var7.getProperty(0).toString();
            } else {
                String errorNO = var7.getProperty(0).toString();
                // save user data
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("DelerrorNo", errorNO);
                edit.commit();
                var1 = var7.getProperty(2).toString();

            }
        } catch (Exception var5) {
            Log.e("SoapOperations", "Error posting BatchNo");
            var5.printStackTrace();
            var1 = (String) var3;
        }

        return var1;
    }

    public String SignoffDelivery(String Delivery) {
        SoapObject request = new SoapObject("http://tempuri.org/", "SignoffDelivery");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("Delivery", Delivery);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/SignoffDelivery", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            if (response.getProperty(1).toString().equals("Ok")) {
                return response.getProperty(0).toString();
            }
            String errorNO = response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("DelerrorNo", errorNO);
            edit.commit();
            return response.getProperty(2).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            return ex.getMessage();
        }
    }


    //NEW Method
    public String OpenWeighingBatch(String batchInfo) {
        Log.i("SoapOperations", "Post batchInfo " + batchInfo);
        SoapObject request = new SoapObject("http://tempuri.org/", "OpenWeighingBatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("BatchInfo", batchInfo);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/OpenWeighingBatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());

            if (response.getProperty(1).toString().equals("Ok")) {
                // Toast.makeText(_context, response.getProperty(0).toString(), Toast.LENGTH_LONG).show();
                return response.getProperty(0).toString();
            }
            String errorNO = response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("batcherrorNo", errorNO);
            edit.commit();

            return new StringBuilder(response.getProperty(2).toString()).toString();
            //return new StringBuilder(String.valueOf(response.getProperty(0).toString())).append("\r\n").append(response.getProperty(1).toString()).append("\r\n").append(response.getProperty(2).toString()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            String Server = "-8080";
            return Server;
        }
    }

    public String DeleteWeighingbatch(int BatchIndex) {
        SoapObject request = new SoapObject("http://tempuri.org/", "DeleteWeighingbatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("BatchIndex ", BatchIndex);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/DeleteWeighingbatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            if (response.getProperty(1).toString().equals("Ok")) {
                return response.getProperty(0).toString();
            }
            String errorNO = response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("DeleteerrorNo", errorNO);
            edit.commit();
            return response.getProperty(2).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            return ex.getMessage();
        }
    }

    public String CloseWeighingBatch(String SignOffInfo) {
        SoapObject request = new SoapObject("http://tempuri.org/", "CloseWeighingBatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("SignOffInfo", SignOffInfo);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {

            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/CloseWeighingBatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            edit = prefs.edit();
            if (response.getProperty(1).toString().equals("Ok")) {

                edit.putString("successNo", response.getProperty(0).toString());
                edit.commit();
                return response.getProperty(0).toString();
            }
            String errorNO = response.getProperty(0).toString();
            // save user data


            edit.putString("bcerrorNo", errorNO);
            edit.commit();
            return response.getProperty(2).toString();


        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            return ex.getMessage();
        }
    }

    public String VerifyAttendancePost(String verifyWeighment) {
        SoapObject request = new SoapObject("http://tempuri.org/", "VerifyAttendancePost");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("RecordInfo", verifyWeighment);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/VerifyAttendancePost", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            edit = prefs.edit();
            if (response.getProperty(1).toString().equals("Ok")) {
                // Toast.makeText(_context, response.getProperty(0).toString(), Toast.LENGTH_LONG).show();
                edit.putString("vresponse", response.getProperty(0).toString());
                edit.commit();
                return response.getProperty(0).toString();


            }
            String vresponse = response.getProperty(0).toString();
            // save user data

            edit.putString("vresponse", vresponse);
            edit.commit();
            return new StringBuilder(response.getProperty(2).toString()).toString();

            //return new StringBuilder(String.valueOf(response.getProperty(0).toString())).append("\r\n").append(response.getProperty(1).toString()).append("\r\n").append(response.getProperty(2).toString()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            String Server = "-8080";
            return Server;
        }
    }

    public String PostWeighingRecord(String Batch, String WeighmentInfo) {
        Log.i("SoapOperations", "Post weighmeninfo " + WeighmentInfo);
        Log.i("SoapOperations", "Post batch id " + Batch);
        Object var3 = null;
        SoapObject var4 = new SoapObject("http://tempuri.org/", "PostWeighingRecord");
        var4.addProperty("Lickey", this._licenseKey);
        var4.addProperty("Batch", Batch);
        var4.addProperty("WeighmentInfo", WeighmentInfo);
        SoapSerializationEnvelope var6 = new SoapSerializationEnvelope(110);
        var6.dotNet = true;
        var6.setOutputSoapObject(var4);

        try {
            this.httpTransport.debug = true;
            this.httpTransport.call("http://tempuri.org/IEasyweighService/PostWeighingRecord", var6);
            SoapObject var7 = (SoapObject) var6.getResponse();
            Log.i("Response 0 ", var7.getProperty(0).toString());
            Log.i("Response 1 ", var7.getProperty(1).toString());
            Log.i("Response 2 ", var7.getProperty(2).toString());
            if (var7.getProperty(1).toString().equals("Ok")) {
                Batch = var7.getProperty(0).toString();
            } else {
                String errorNO = var7.getProperty(0).toString();
                // save user data
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("errorNo", errorNO);
                edit.commit();
                Batch = var7.getProperty(0).toString();

            }
        } catch (Exception var5) {
            Log.e("SoapOperations", "Error posting weighment");
            var5.printStackTrace();
            //var1 = (String)var3;
            Batch = "-8080";

        }

        return Batch;
    }

    public String OpenFarmDispatch(String DeliveryInfo) {
        Log.i("SoapOperations", "Post DeliveryInfo " + DeliveryInfo);
        SoapObject request = new SoapObject("http://tempuri.org/", "OpenFarmDispatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("DeliveryInfo", DeliveryInfo);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/OpenFarmDispatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            edit = prefs.edit();
            if (response.getProperty(1).toString().equals("Ok")) {
                // Toast.makeText(_context, response.getProperty(0).toString(), Toast.LENGTH_LONG).show();
                edit.remove("DelerrorNo");
                edit.commit();
                return response.getProperty(0).toString();


            }
            String errorNO = response.getProperty(0).toString();
            // save user data

            edit.putString("DelerrorNo", errorNO);
            edit.commit();
            return new StringBuilder(response.getProperty(2).toString()).toString();
            //return new StringBuilder(String.valueOf(response.getProperty(0).toString())).append("\r\n").append(response.getProperty(1).toString()).append("\r\n").append(response.getProperty(2).toString()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            String Server = "-8080";
            return Server;
        }
    }

    public String PostTaskRecord(String TaskInfo) {
        Log.i("SoapOperations", "Post TaskInfo " + TaskInfo);
        SoapObject request = new SoapObject("http://tempuri.org/", "PostTaskRecord");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("TaskInfo", TaskInfo);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/PostTaskRecord", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            edit = prefs.edit();
            if (response.getProperty(1).toString().equals("Ok")) {
                // Toast.makeText(_context, response.getProperty(0).toString(), Toast.LENGTH_LONG).show();
                edit.putString("TaskerrorNo", response.getProperty(0).toString());
                edit.commit();
                return response.getProperty(0).toString();


            }
            String errorNO = response.getProperty(0).toString();
            // save user data

            edit.putString("TaskerrorNo", errorNO);
            edit.commit();
            return new StringBuilder(response.getProperty(2).toString()).toString();
            //return new StringBuilder(String.valueOf(response.getProperty(0).toString())).append("\r\n").append(response.getProperty(1).toString()).append("\r\n").append(response.getProperty(2).toString()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            String Server = "-8080";
            return Server;
        }
    }

    public String PostClockingRecord(String ClockingInfo) {
        Log.i("SoapOperations", "Post ClockingInfo " + ClockingInfo);
        SoapObject request = new SoapObject("http://tempuri.org/", "PostClockingRecord");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("ClockingInfo", ClockingInfo);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/PostClockingRecord", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            edit = prefs.edit();
            if (response.getProperty(1).toString().equals("Ok")) {
                // Toast.makeText(_context, response.getProperty(0).toString(), Toast.LENGTH_LONG).show();
                edit.putString("ClockerrorNo", response.getProperty(0).toString());
                edit.commit();
                return response.getProperty(0).toString();


            }
            String errorNO = response.getProperty(0).toString();
            // save user data

            edit.putString("ClockerrorNo", errorNO);
            edit.commit();
            return new StringBuilder(response.getProperty(2).toString()).toString();
            //return new StringBuilder(String.valueOf(response.getProperty(0).toString())).append("\r\n").append(response.getProperty(1).toString()).append("\r\n").append(response.getProperty(2).toString()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            String Server = "-8080";
            return Server;
        }
    }

    public String DeliverWeighingBatch(int Delivery, String BatchNo) {
        Log.i("SoapOperations", "Post BatchNo " + BatchNo);
        Log.i("SoapOperations", "Post Delivery " + Delivery);
        SoapObject request = new SoapObject("http://tempuri.org/", "DeliverWeighingBatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("Delivery", Delivery);
        request.addProperty("BatchNo", BatchNo);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/DeliverWeighingBatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            if (response.getProperty(1).toString().equals("Ok")) {
                return response.getProperty(0).toString();
            }
            String errorNO = response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("dlerrorNo", errorNO);
            edit.commit();
            return response.getProperty(2).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            return ex.getMessage();
        }
    }

    public String CloseFarmDispatch(int Delivery) {
        SoapObject request = new SoapObject("http://tempuri.org/", "CloseFarmDispatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("Delivery", Delivery);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/CloseFarmDispatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            if (response.getProperty(1).toString().equals("Ok")) {
                return response.getProperty(0).toString();
            }
            String errorNO = response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("cDelerrorNo", errorNO);
            edit.commit();
            return response.getProperty(2).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            return ex.getMessage();
        }
    }


}
