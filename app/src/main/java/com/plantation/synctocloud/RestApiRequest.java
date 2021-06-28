package com.plantation.synctocloud;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RestApiRequest {

    private final String TAG;
    private final Context _context;
    public String _URL;
    SharedPreferences mSharedPrefs;
    SharedPreferences prefs;
    String CompanyID, UserID, DeviceID, SerialNumber;
    String access_token, token_type, expires_in, userName, issued, expires;
    SimpleDateFormat dateFormatA;
    String dateFormat;
    String Id, Title, Message;
    ConnectivityManager connectivityManager;
    boolean connected = false;
    private String _TOKEN;
    private String _licenseKey;

    public RestApiRequest(Context ctx) {
        _URL = null;
        _TOKEN = null;
        _licenseKey = null;

        TAG = "RestApiRequest";

        _context = ctx;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        prefs = PreferenceManager.getDefaultSharedPreferences(_context);

        if (mSharedPrefs.getString("internetAccessModes", null).equals("WF")) {
            if (mSharedPrefs.getString("coPort", "").equals("")) {
                _URL = mSharedPrefs.getString("portalURL", null) + "/" +
                        mSharedPrefs.getString("coApp", null);
            } else {
                _URL = mSharedPrefs.getString("portalURL", "") + ":"
                        + mSharedPrefs.getString("coPort", "") + "/" +
                        mSharedPrefs.getString("coApp", "");
            }
        } else {
            if (mSharedPrefs.getString("coPort", "").equals("")) {
                _URL = mSharedPrefs.getString("mdportalURL", null) + "/" +
                        mSharedPrefs.getString("coApp", null);
            } else {
                _URL = mSharedPrefs.getString("mdportalURL", "") + ":"
                        + mSharedPrefs.getString("coPort", "") + "/" +
                        mSharedPrefs.getString("coApp", "");

            }
        }
        // dateFormat="E, dd MMM yyyy HH:mm:ss Z";
        dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        dateFormatA = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        _TOKEN = mSharedPrefs.getString("token", null);
        _licenseKey = mSharedPrefs.getString("licenseKey", null);

        CompanyID = mSharedPrefs.getString("company_prefix", "");
        UserID = prefs.getString("user", "");
        DeviceID = mSharedPrefs.getString("terminalID", "");
        SerialNumber = getIMEIDeviceId();

    }

    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) _context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;


        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v(TAG, e.toString());
        }
        return connected;
    }

    public String getAllowedDevice(String URL, String Token, String Terminal) {

        try {
            String result;
            Response response;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("http://" + URL + "/api/MasterData/Weighingkits?Estate=0&Factory=0&$select=RecordIndex,InternalSerial,ExternalSerial,AllocFactory&$filter=InternalSerial eq '" + Terminal + "'")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + Token)
                    .build();


            response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("RestApiRequest", response.body().string());
            result = responseBodyCopy.string();

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long token_hours() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat input = new SimpleDateFormat(dateFormat);
        Date token_time = null;
        long difference;
        try {
            token_time = input.parse(mSharedPrefs.getString("start_time", dateFormatA.format(cal.getTime())));
            // token_time = input.parse("2020-11-30T22:50:45Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i("TOKEN_HOURS ", token_time + " Current Date " + new Date());
        difference = new Date().getTime() - token_time.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        //long elapsedDays = different / daysInMilli;
        //different = different % daysInMilli;

        long elapsedHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;

        long elapsedMinutes = difference / minutesInMilli;
        difference = difference % minutesInMilli;

        long elapsedSeconds = difference / secondsInMilli;
        Log.e("======= Hours", " :: " + elapsedHours);
        Log.e("======= min", " :: " + elapsedMinutes);
        System.out.printf("%d hours, %d minutes, %d seconds%n", elapsedHours, elapsedMinutes, elapsedSeconds);
        return elapsedHours;
    }

    public String getMainToken() {

        try {

            String result;
            Response response;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();

            JSONObject IOTjsonObject = new JSONObject();
            IOTjsonObject.put("CompanyID", CompanyID);
            IOTjsonObject.put("UserID", UserID);
            IOTjsonObject.put("DeviceID", DeviceID);
            IOTjsonObject.put("SerialNumber", SerialNumber);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, IOTjsonObject.toString());
            Request request = new Request.Builder()
                    .url(_URL + "/api/Purchases/Token")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("RestApiRequest", response.body().string());
            result = responseBodyCopy.string();

            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("access_token") && !jsonObject.isNull("access_token")) {
                access_token = jsonObject.getString("access_token");
                token_type = jsonObject.getString("token_type");
                expires_in = jsonObject.getString("expires_in");
                userName = jsonObject.getString("userName");
                issued = jsonObject.getString(".issued");
                expires = jsonObject.getString(".expires");
                Log.i("INFO", access_token + "" + token_type + "" + expires_in);
                Calendar cal = Calendar.getInstance();
                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.putString("token", access_token);
                edit.commit();
                edit.putString("expires_in", expires_in);
                edit.commit();
                edit.putString("expires", expires);
                edit.commit();
                edit.putString("start_time", dateFormatA.format(cal.getTime()));
                edit.commit();
                return access_token;
            } else if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                Id = jsonObject.getString("Id");
                Title = jsonObject.getString("Title");
                Message = result;

                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.remove("token");
                edit.commit();
                edit.remove("expires_in");
                edit.commit();
                edit.remove("expires");
                edit.commit();
                return Id;
            }
            return result;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String getToken() {

        try {
            String Username = "IOT";
            String Password = "Join#iot@1618";

            String result = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "Grant_type=password&username=" + Username + "&password=" + Password + "");
            Request request = new Request.Builder()
                    .url(_URL + "/token")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("RestApiRequest", response.body().string());
            result = responseBodyCopy.string();
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("access_token") && !jsonObject.isNull("access_token")) {
                access_token = jsonObject.getString("access_token");
                token_type = jsonObject.getString("token_type");
                expires_in = jsonObject.getString("expires_in");
                userName = jsonObject.getString("userName");
                issued = jsonObject.getString(".issued");
                expires = jsonObject.getString(".expires");
                Log.i("INFO", access_token + "" + token_type + "" + expires_in);
                Calendar cal = Calendar.getInstance();
                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.putString("token", access_token);
                edit.commit();
                edit.putString("expires_in", expires_in);
                edit.commit();
                edit.putString("expires", expires);
                edit.commit();
                edit.putString("start_time", dateFormatA.format(cal.getTime()));
                edit.commit();
                return access_token;
            } else if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                Id = jsonObject.getString("Id");
                Title = jsonObject.getString("Title");
                Message = result;

                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.remove("token");
                edit.commit();
                edit.remove("expires_in");
                edit.commit();
                edit.remove("expires");
                edit.commit();
                return Id;
            }
            return result;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            String Server = "-8080";
            Log.e("SoapApiRequest", e.toString());
            Log.e("Server Response", e.toString());
            e.printStackTrace();
            return Server;
        }
    }

    public String getIMEIDeviceId() {

        String deviceId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(_context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) _context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (_context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
            }
            assert mTelephony != null;
            if (mTelephony.getDeviceId() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceId = mTelephony.getImei();
                } else {
                    deviceId = mTelephony.getDeviceId();
                }
            } else {
                deviceId = Settings.Secure.getString(_context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        Log.d("deviceId", deviceId);
        return deviceId;
    }

    public String CreateBatch(String batchInfo) {

        try {

            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();
                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, batchInfo);
            Request request = new Request.Builder()
                    .url(_URL + "/api/Farmlabor/OpenBatch")
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("CreateBatch", response.body().string());
            return responseBodyCopy.string();


        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("CreateBatch", ex.toString());
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String DeletePurchasesBatch(int BatchIndex) {

        try {
            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();

                }
            }
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(_URL + "/api/Farmlabor/DeleteBatch?Id=" + BatchIndex)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("DeleteBatch", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("DeleteBatch", ex.toString());
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String CloseOutgrowersPurchasesBatch(int BatchIndex, String stringCloseTime, String totalWeight) {

        try {
            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();

                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(_URL + "/api/Farmlabor/CloseBatch?Id=" + BatchIndex + "&time=" + stringCloseTime + "&weight=" + totalWeight)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("CloseBatch", response.body().string());
            return responseBodyCopy.string();


        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("CloseBatch", ex.toString());
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String SendReceiptSMS(String ReceiptNo, String tKg) {
        try {
            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();

                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(_URL + "/api/Purchases/Sendsms?Receipt=" + ReceiptNo + "&Kg=" + tKg)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("SendSMS", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("SendSMS", ex.toString());
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String postWeighment(String Id, String WeightInfo) {

        try {
            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();

                }
            }


            Log.i("ServerBatchID", Id + " WeightInfo " + WeightInfo);
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, WeightInfo);
            Request request = new Request.Builder()
                    .url(_URL + "/api/Farmlabor/Weighment?Id=" + Id)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("postWeighment", response.body().string());
            return responseBodyCopy.string();

        } catch (IOException var5) {
            Log.e("postWeighment", "Error posting weighment");
            var5.printStackTrace();
            //var1 = (String)var3;
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }


    }

    public String VerifyRecord(String Id, String WeightInfo) {

        try {
            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();

                }
            }


            Log.i("ServerBatchID", Id + " WeightInfo " + WeightInfo);
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, WeightInfo);
            Request request = new Request.Builder()
                    .url(_URL + "/api/Farmlabor/Verify?Id=" + Id)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("VerifyRecord", response.body().string());
            return responseBodyCopy.string();

        } catch (IOException var5) {
            Log.e("VerifyRecord", "Error posting weighment");
            var5.printStackTrace();
            //var1 = (String)var3;
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }


    }

    public String StartDispatch(String DeliveryInfo) {
        try {
            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();

                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, DeliveryInfo);
            Request request = new Request.Builder()
                    .url(_URL + "/api/Farmlabor/Newdispatch")
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("StartDispatch", response.body().string());
            return responseBodyCopy.string();

        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("StartDispatch", ex.toString());
            String Server = "-8080";
            return Server;
        }
    }

    public String UpdateDelivery(int Delivery, String DeliveryInfo) {
        try {
            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();

                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, DeliveryInfo);
            Request request = new Request.Builder()
                    .url(_URL + "/api/Purchases/UpdateDelivery?Id=" + Delivery)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("UpdateDelivery", response.body().string());
            return responseBodyCopy.string();

        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("UpdateDelivery", ex.toString());
            String Server = "-8080";
            return Server;
        }
    }

    public String DeliverBatch(int Delivery, String BatchNo) {

        try {
            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();

                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(_URL + "/api/Farmlabor/Despatch?Id=" + Delivery + "&BatchNo=" + BatchNo)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("DeliverBatch", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("DeliverBatch", ex.toString());
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String CompleteDispatch(String Delivery) {

        try {
            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();

                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(_URL + "/api/Farmlabor/Dispatch?Id=" + Delivery)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("CompleteDispatch", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("CompleteDispatch", ex.toString());
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

}
