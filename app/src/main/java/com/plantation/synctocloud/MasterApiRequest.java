package com.plantation.synctocloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.plantation.data.DBHelper;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MasterApiRequest {

    private final String TAG;
    DBHelper dbHelper;
    public String _URL;
    Context _context;

    SharedPreferences mSharedPrefs;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;
    String Server = "";

    String CRecordIndex;
    String ERecordIndex = "";
    String FRecordIndex, FryPrefix, FryTitle, FryCapacity;

    String _TOKEN;


    public MasterApiRequest(Context ctx) {
        _URL = null;
        _TOKEN = null;

        TAG = "RestApiRequest";

        _context = ctx;
        dbHelper = new DBHelper(_context);
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

        _TOKEN = mSharedPrefs.getString("token", null);
        // _TOKEN = new RestApiRequest(_context).getToken();
        if (_TOKEN == null || _TOKEN.equals("")) {
            _TOKEN = new RestApiRequest(_context).getToken();
        } else {
            long token_hours = new RestApiRequest(_context).token_hours();
            if (token_hours >= 23) {
                _TOKEN = new RestApiRequest(_context).getToken();

            }
        }

        CRecordIndex = prefs.getString("CRecordIndex", null);
        ERecordIndex = prefs.getString("ERecordIndex", null);


    }


    public String getFactories(String CRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Factories?Co=" + CRecordIndex)
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("factoriesresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getFactories", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getFactories", ex.toString());
            return ex.getMessage();
        }
    }

    public String getTransporters(String CRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Transporters?Co=" + CRecordIndex + "&$select=RecordIndex,TptCode,TptName")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("transresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getTransporters", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getTransporters", ex.toString());
            return ex.getMessage();
        }
    }

    public String getClerks(String ERecordIndex, String DRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Fieldclerks?Estate=" + ERecordIndex + "&Div=" + DRecordIndex + "&$select=Recordindex,KitUserNumber,KitUserName,EmpNo,EPFNo,EStaffName,WkuDivision,AccessLevel")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("getclerksresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getClerks", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getClerks", ex.toString());
            return ex.getMessage();
        }
    }

    public String getDivisions(String ERecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Divisions?est=" + ERecordIndex)
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("getdivresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getDivisions", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getDivisions", ex.toString());
            return ex.getMessage();
        }
    }

    public String getFields(String ERecordIndex, String DRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Fields?Estate=" + ERecordIndex + "&div=" + DRecordIndex + "&$select=Recordindex,FieldNumber,FieldName,FieldSize,FieldDivision,edCode,edEstate,EstatePrefix")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("fieldsresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getFields", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getFields", ex.toString());
            return ex.getMessage();
        }
    }

    public String getBlocks(String ERecordIndex, String DRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Blocks?Estate=" + ERecordIndex + "&div=" + DRecordIndex + "&field=0&$select=Recordindex,BlockNumber,BlockName,BlockSize,FieldNumber,FieldDivision,edEstate")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("blocksresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getBlocks", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getBlocks", ex.toString());
            return ex.getMessage();
        }
    }

    public String getTeams(String DRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Teams?Div=" + DRecordIndex)
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("teamsresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getTeams", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getTeams", ex.toString());
            return ex.getMessage();
        }
    }

    public String getTeamMembers(String TMRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/Employees/TeamMembers?team=" + TMRecordIndex)
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("tmembersresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getTeamMembers", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getTeamMembers", ex.toString());
            return ex.getMessage();
        }
    }

    public String getEmployees(String ERecordIndex, String DRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/Employees/EstateStaff?estate=" + ERecordIndex + "&division=" + DRecordIndex)
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("employeeresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getEmployees", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getEmployees", ex.toString());
            return ex.getMessage();
        }
    }

    public String getMachines(String ERecordIndex, String DRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Harvesters?Estate=" + ERecordIndex + "&Div=" + DRecordIndex)
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("getmachineresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getMachines", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getMachines", ex.toString());
            return ex.getMessage();
        }
    }

    public String getPluckingCodes() {
        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/Employees/Pluckingcodes?$select=Recordindex,ctcode,ctTitle,ctActivityType,ctAllowMultiple,ctAllowOT")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("jobcoderesponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getJobCodes", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getJobCodes", ex.toString());
            return ex.getMessage();
        }
    }

    public String getCrops(String CRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Crops?Co=" + CRecordIndex + "&$select=RecordIndex,MpCode,MpDescription")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("getcropsresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getCrops", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getCrops", ex.toString());
            return ex.getMessage();
        }
    }

    public String getGrades(String PRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Grades?Crop=" + PRecordIndex + "&$select=RecordIndex,pgdRef,pgdName,MpCode")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("gradesresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getGrades", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getGrades", ex.toString());
            return ex.getMessage();
        }
    }

    public String getVarieties(String PRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Varieties?Crop=" + PRecordIndex + "&$select=RecordIndex,vtrRef,vrtName,MpCode")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("varietyresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getVarieties", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getVarieties", ex.toString());
            return ex.getMessage();
        }
    }

    public String getCostCenters(String CRecordIndex, String DRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(_URL + "/api/Company/Costcenters?co=" + CRecordIndex + "&div=" + DRecordIndex)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("centersresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getCostC", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getCostC", ex.toString());
            return ex.getMessage();
        }
    }
}
