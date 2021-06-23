package com.plantation.synctocloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.plantation.data.DBHelper;

import java.text.SimpleDateFormat;

public class MasterApiRequest {

    private final String TAG;
    public String _URL;
    Context _context;
    String Server = "";
    SharedPreferences mSharedPrefs;
    SharedPreferences prefs;
    String TRecordIndex, CRecordIndex, FRecordIndex, FryPrefix, FryTitle, FryCapacity;
    String s_tptID, s_tptName;
    DBHelper dbHelper;
    SimpleDateFormat dateFormatA;
    String dateFormat;
    private String _TOKEN;

    public MasterApiRequest(Context ctx) {
        _URL = null;
        _TOKEN = null;

        TAG = "RestApiRequest";

        _context = ctx;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        dateFormatA = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        //  _URL = mSharedPrefs.getString("portalURL", null);

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

        dbHelper = new DBHelper(_context);


    }

//    public String Factories()
//    {
//        HttpHandler sh = new HttpHandler();
//        // Making a request to url and getting response
//        _TOKEN = mSharedPrefs.getString("token", null);
//        if(_TOKEN==null||_TOKEN.equals("")){
//            _TOKEN = new RestApiRequest(_context).getToken();
//        }
//        else{
//            long token_hours=new RestApiRequest(_context).token_hours();
//            if(token_hours>=23){
//                _TOKEN = new RestApiRequest(_context).getToken();
//
//            }
//        }
//        CRecordIndex = prefs.getString("CRecordIndex", null);
//        String url =_URL+"/api/MasterData/Factories?Co="+CRecordIndex+"&$select=RecordIndex,FryPrefix,FryTitle,FryCapacity";
//        String jsonStr = sh.makeServiceCall(url,_TOKEN);
//
//        //  Log.e(TAG, "Response from url: " + jsonStr);
//        if (jsonStr != null) {
//            try {
//
//
//                JSONArray arrayKnownAs = new JSONArray(jsonStr);
//                if (arrayKnownAs.length() > 0) {
//
//
//                    // Do something with object.
//
//                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
//                        JSONObject obj = arrayKnownAs.getJSONObject(i);
//                        FRecordIndex = obj.getString("RecordIndex");
//                        FryPrefix = obj.getString("FryPrefix");
//                        FryTitle = obj.getString("FryTitle");
//                        FryCapacity = obj.getString("FryCapacity");
//                        Cursor checkAgent = dbHelper.CheckFactory(FryPrefix);
//                        //Check for duplicate FryPrefix
//                        if (checkAgent.getCount() > 0) {
//
//                        } else {
//                            dbHelper.AddFactories(FryPrefix, FryTitle, FRecordIndex);
//                        }
//                    }
//                }else{
//                    FRecordIndex="-1";
//                    return FRecordIndex;
//                }
//
//
//            } catch (final JSONException e) {
//                Log.e("TAG", "Json parsing error: " + e.getMessage());
//                return "Json parsing error: " + e.getMessage();
//            }
//
//        } else {
//            Log.e("TAG", "Couldn't get json from server.");
//            return "Couldn't get json from server.";
//        }
//
//        return FRecordIndex;
//    }


//    public String Transporters()
//    {
//        HttpHandler sh = new HttpHandler();
//        // Making a request to url and getting response
//        _TOKEN = mSharedPrefs.getString("token", null);
//        if(_TOKEN==null||_TOKEN.equals("")){
//            _TOKEN = new RestApiRequest(_context).getToken();
//        }
//        else{
//            long token_hours=new RestApiRequest(_context).token_hours();
//            if(token_hours>=23){
//                _TOKEN = new RestApiRequest(_context).getToken();
//
//            }
//        }
//        CRecordIndex = prefs.getString("CRecordIndex", null);
//
//        String url =_URL+"/api/MasterData/Transporters?Co="+CRecordIndex+"&$select=RecordIndex,TptCode,TptName";
//        String jsonStr = sh.makeServiceCall(url,_TOKEN);
//
//
//        //  Log.e(TAG, "Response from url: " + jsonStr);
//        if (jsonStr != null) {
//            try {
//
//
//                SQLiteDatabase db = dbHelper.getWritableDatabase();
//                Cursor routes = db.query(true, Database.TRANSPORTER_TABLE_NAME, null, null, null, null, null, null, null, null);
//                if(routes.getCount()==0) {
//                    String DefaultTransporter = "INSERT INTO " + Database.TRANSPORTER_TABLE_NAME + " ("
//                            + Database.ROW_ID + ", "
//                            + Database.TPT_NAME + ") Values ('0', 'Select ...')";
//                    db.execSQL(DefaultTransporter);
//                }
//
//                JSONArray arrayKnownAs = new JSONArray(jsonStr);
//                // Do something with object.
//                for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
//                    JSONObject obj = arrayKnownAs.getJSONObject(i);
//                    TRecordIndex = obj.getString("RecordIndex");
//                    s_tptID = obj.getString("TptCode");
//                    s_tptName = obj.getString("TptName");
//
//                    Log.i("TRecordIndex", TRecordIndex);
//                    Log.i("s_tptID", s_tptID);
//
//                    Cursor checkRoute =dbHelper.CheckTransporterID(TRecordIndex);
//                    //Check for duplicate shed
//                    if (checkRoute.getCount() > 0) {
//                        // Toast.makeText(getApplicationContext(), "Route already exists",Toast.LENGTH_SHORT).show();
//
//                    }
//                    else {
//                        dbHelper.AddTransporter(s_tptID, s_tptName,TRecordIndex);
//                    }
//
//
//                }
//
//
//
//            } catch (final JSONException e) {
//                Log.e("TAG", "Json parsing error: " + e.getMessage());
//                Server="-8080";
//                Log.e("Server Response", e.toString());
//                e.printStackTrace();
//                return Server;
//            }
//
//        } else {
//            Log.e("TAG", "Couldn't get json from server.");
//
//        }
//        return TRecordIndex;
//    }


}
