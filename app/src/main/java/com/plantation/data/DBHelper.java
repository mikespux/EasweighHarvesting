package com.plantation.data;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "EasyweighHarvesting.db";


    public DBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

    }

    public void createTables(SQLiteDatabase database) {

        //Estates Table
        String estates_table_sql = "create table " + Database.ESTATES_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.ES_ID + " TEXT," +
                Database.ES_NAME + " TEXT," +
                Database.ES_COMPANY + " TEXT," +
                Database.CloudID + " TEXT)";

        //Divisions Table
        String divisions_table_sql = "create table " + Database.DIVISIONS_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.DV_ID + " TEXT," +
                Database.DV_NAME + " TEXT," +
                Database.DV_ESTATE + " TEXT," +
                Database.CloudID + " TEXT)";

        //Fields Table
        String fields_table_sql = "create table " + Database.FIELD_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.FD_ID + " TEXT," +
                Database.FD_DIVISION + " TEXT," +
                Database.CloudID + " TEXT)";

        //Blocks Table
        String blocks_table_sql = "create table " + Database.BLOCK_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.BK_ID + " TEXT," +
                Database.BK_FIELD + " TEXT," +
                Database.CloudID + " TEXT)";

        //Factory Table
        String factory_table_sql = "create table " + Database.FACTORY_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.FRY_PREFIX + " TEXT," +
                Database.FRY_TITLE + " TEXT," +
                Database.CloudID + " TEXT)";

        //Produce Table
        String produce_table_sql = "create table " + Database.PRODUCE_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.MP_CODE + " TEXT," +
                Database.MP_DESCRIPTION + " TEXT," +
                Database.MP_RETAILPRICE + " FLOAT," +
                Database.MP_SALESTAX + " FLOAT," +
                Database.CloudID + " TEXT)";

        //ProduceGrades Table
        String producegrades_table_sql = "create table " + Database.PRODUCEGRADES_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.PG_DREF + " TEXT," +
                Database.PG_DNAME + " TEXT," +
                Database.PG_DPRODUCE + " TEXT," +
                Database.PG_RETAILPRICE + " FLOAT," +
                Database.PG_SALESTAX + " FLOAT," +
                Database.CloudID + " TEXT)";

        //ProduceVarieties Table
        String producevarieties_table_sql = "create table " + Database.PRODUCEVARIETIES_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.VRT_REF + " TEXT," +
                Database.VRT_NAME + " TEXT," +
                Database.VRT_PRODUCE + " TEXT," +
                Database.VRT_RETAILPRICE + " FLOAT," +
                Database.VRT_SALESTAX + " FLOAT," +
                Database.CloudID + " TEXT)";

        //Tasks Table
        String task_table_sql = "create table " + Database.TASK_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.TK_ID + " TEXT," +
                Database.TK_NAME + " TEXT," +
                Database.TK_TYPE + " TEXT," +
                Database.TK_OT + " TEXT," +
                Database.TK_MT + " TEXT," +
                Database.CloudID + " TEXT)";


        //Teams Table
        String teams_table_sql = "create table " + Database.TEAMS_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.TM_CODE + " TEXT," +
                Database.TM_NAME + " TEXT," +
                Database.DG_NUMBER + " TEXT," +
                Database.DG_DIVISION + " TEXT," +
                Database.ED_ESTATE + " TEXT," +
                Database.CloudID + " TEXT)";

        //Employees Table
        String employee_table_sql = "create table " + Database.EM_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.EM_ID + " TEXT," +
                Database.EM_NAME + " TEXT," +
                Database.EM_IDNO + " TEXT," +
                Database.EM_CARDID + " TEXT," +
                Database.EM_PICKERNO + " TEXT," +
                Database.EM_TEAM + " TEXT," +
                Database.CloudID + " TEXT)";

        //Machine Table
        String machine_table_sql = "create table " + Database.MACHINE_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.MC_ID + " TEXT," +
                Database.MC_NAME + " TEXT," +
                Database.CloudID + " TEXT)";

        //Capital Table
        String capital_table_sql = "create table " + Database.CAPITALP_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.CP_ID + " TEXT," +
                Database.CP_NAME + " TEXT," +
                Database.CloudID + " TEXT)";

        //Transporter Table
        String transporter_table_sql = "create table " + Database.TRANSPORTER_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.TPT_ID + " TEXT," +
                Database.TPT_NAME + " TEXT," +
                Database.CloudID + " TEXT)";


        //OperatorsMaster Table
        String operators_master_table_sql = "create table " + Database.OPERATORSMASTER_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.USERIDENTIFIER + " TEXT," +
                Database.CLERKNAME + " TEXT," +
                Database.ACCESSLEVEL + " TEXT," +
                Database.USERPWD + " TEXT," +
                Database.USERCLOUDID + " TEXT)";

        //EmployeeSuppliesConsignments Table
        String employee_batches = "create table " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.DataDevice + " TEXT," +
                Database.Userid + " TEXT," +
                Database.BatchDate + " TEXT," +
                Database.OpeningTime + " TEXT," +
                Database.BatchCount + " TEXT," +
                Database.BatchNumber + " TEXT," +
                Database.BatchSession + " TEXT," +
                Database.BEstate + " TEXT," +
                Database.BDivision + " TEXT," +
                Database.NoOfWeighments + " TEXT," +
                Database.NoOfTasks + " TEXT," +
                Database.BatchCrates + " TEXT," +
                Database.TotalWeights + " TEXT," +
                Database.Closed + " TEXT," +
                Database.ClosingTime + " TEXT," +
                Database.Dispatched + " TEXT," +
                Database.DeliveryNoteNumber + " TEXT," +
                Database.Factory + " TEXT," +
                Database.Transporter + " TEXT," +
                Database.Tractor + " TEXT," +
                Database.Trailer + " TEXT," +
                Database.DelivaryNO + " TEXT," +
                Database.SignedOff + " TEXT," +
                Database.SignedOffTime + " TEXT," +
                Database.BatCloudID + " TEXT)";


        //Session Table
        String session_table_sql = "create table " + Database.SESSION_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.SessionNo + " TEXT," +
                Database.SessionDate + " TEXT," +
                Database.SessionTime + " TEXT," +
                Database.SessionDevice + " TEXT," +
                Database.SessionEmployeeNo + " TEXT," +
                Database.SessionBags + " TEXT," +
                Database.SessionNet + " TEXT," +
                Database.SessionTare + " TEXT," +
                Database.SessionField + " TEXT," +
                Database.SessionBlock + " TEXT," +
                Database.SessionGrade + " TEXT)";

        //EmployeesProduceCollection Table
        String employee_produce_collection_sql = "create table " + Database.EM_PRODUCE_COLLECTION_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.CollDate + " TEXT," +
                Database.CaptureTime + " TEXT," +
                Database.DataCaptureDevice + " TEXT," +
                Database.BatchNo + " TEXT," +
                Database.EmployeeNo + " TEXT," +
                Database.FieldClerk + " TEXT," +
                Database.SourceEstate + " TEXT," +
                Database.SourceDivision + " TEXT," +
                Database.SourceField + " TEXT," +
                Database.SourceBlock + " TEXT," +
                Database.TaskCode + " TEXT," +
                Database.TaskType + " TEXT," +
                Database.DeliveredProduce + " TEXT," +
                Database.ProduceVariety + " TEXT," +
                Database.ProduceGrade + " TEXT," +
                Database.NetWeight + " FLOAT," +
                Database.Tareweight + " FLOAT," +
                Database.BagCount + " INTEGER," +
                Database.UnitPrice + " FLOAT," +
                Database.ReceiptNo + " TEXT," +
                Database.LoadCount + " INTEGER," +
                Database.UsedSmartCard + " TEXT," +
                Database.CloudID + " TEXT)";

        // Machine Operators Table
        String machine_operators_table_sql = "create table " + Database.MACHINEOP_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.MDATE + " TEXT," +
                Database.TERMINALID + " TEXT," +
                Database.MACHINENUMBER + " TEXT," +
                Database.EMPLOYEENUMBER + " TEXT," +
                Database.CHECKINTIME + " TEXT," +
                Database.CHECKOUTTIME + " TEXT," +
                Database.CHECKINWEIGHMENT + " INTEGER," +
                Database.CHECKOUTWEIGHMENT + " INTEGER," +
                Database.MTASKCODE + " TEXT," +
                Database.MCOMPANY + " TEXT," +
                Database.MESTATE + " TEXT," +
                Database.MSTATUS + " TEXT," +
                Database.CloudID + " TEXT)";

        // Machine Fuel Table
        String machine_fuel_table_sql = "create table " + Database.MACHINEFUEL_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.MFDATE + " TEXT," +
                Database.MFTERMINALID + " TEXT," +
                Database.MFMACHINENUMBER + " TEXT," +
                Database.MFTIME + " TEXT," +
                Database.MFLitres + " TEXT," +
                Database.MFTASKCODE + " TEXT," +
                Database.MFCOMPANY + " TEXT," +
                Database.MFESTATE + " TEXT," +
                Database.MFSTATUS + " TEXT," +
                Database.CloudID + " TEXT)";


        // Company Table
        String company_table_sql = "create table " + Database.COMPANY_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.CO_PREFIX + " TEXT," +
                Database.CO_NAME + " TEXT," +
                Database.CO_LETTERBOX + " TEXT," +
                Database.CO_POSTCODE + " TEXT," +
                Database.CO_POSTNAME + " TEXT," +
                Database.CO_POSTREGION + " TEXT," +
                Database.CO_TELEPHONE + " TEXT," +
                Database.CO_ClOUDID + " TEXT)";


        // Delivery Table
        String Delivery_table_sql = "create table " + Database.Fmr_FactoryDeliveries + " ( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.FdWeighbridgeTicket + " TEXT," +
                Database.FdDNoteNum + " TEXT," +
                Database.FdDate + " TEXT," +
                Database.FdFactory + " TEXT," +
                Database.FdTransporter + " TEXT," +
                Database.FdVehicle + " TEXT," +
                Database.FdTractor + " TEXT," +
                Database.FdDriver + " TEXT," +
                Database.FdTurnMan + " TEXT," +
                Database.FdFieldWt + " TEXT," +
                Database.FdArrivalTime + " TEXT," +
                Database.FdGrossWt + " FLOAT," +
                Database.FdTareWt + " FLOAT," +
                Database.FdRejectWt + " FLOAT," +
                Database.FdQualityScore + " FLOAT," +
                Database.FdDepartureTime + " TEXT," +
                Database.FdStatus + " TEXT," +
                Database.CloudID + " TEXT)";

        String DefaultUsers = "INSERT INTO " + Database.OPERATORSMASTER_TABLE_NAME + " ("
                + Database.USERIDENTIFIER + ", "
                + Database.CLERKNAME + ", "
                + Database.USERPWD + ", "
                + Database.ACCESSLEVEL + ") Values ('OCTAGON', 'ODS', '1234', '1')";

        String DefaultEstates = "INSERT INTO " + Database.ESTATES_TABLE_NAME + " ("
                + Database.ROW_ID + ", "
                + Database.ES_NAME + ") Values ('0', 'Select ...')";

        String DefaultDivision = "INSERT INTO " + Database.DIVISIONS_TABLE_NAME + " ("
                + Database.ROW_ID + ", "
                + Database.DV_NAME + ") Values ('0', 'Select ...')";

        String DefaultField = "INSERT INTO " + Database.FIELD_TABLE_NAME + " ("
                + Database.ROW_ID + ", "
                + Database.FD_ID + ") Values ('0', 'Select ...')";

        String DefaultBlock = "INSERT INTO " + Database.BLOCK_TABLE_NAME + " ("
                + Database.ROW_ID + ", "
                + Database.BK_ID + ") Values ('0', 'Select ...')";

        String DefaultComm = "INSERT INTO " + Database.PRODUCE_TABLE_NAME + " ("
                + Database.ROW_ID + ", "
                + Database.MP_DESCRIPTION + ") Values ('0', 'Select ...')";
        String DefaultVariety = "INSERT INTO " + Database.PRODUCEVARIETIES_TABLE_NAME + " ("
                + Database.ROW_ID + ", "
                + Database.VRT_NAME + ") Values ('0', 'Select ...')";
        String DefaultGrade = "INSERT INTO " + Database.PRODUCEGRADES_TABLE_NAME + " ("
                + Database.ROW_ID + ", "
                + Database.PG_DNAME + ") Values ('0', 'Select ...')";
        String DefaultTask = "INSERT INTO " + Database.TASK_TABLE_NAME + " ("
                + Database.ROW_ID + ", "
                + Database.TK_NAME + ") Values ('0', 'Select ...')";
		/*String DefaultEmployee = "INSERT INTO " + Database.EM_TABLE_NAME + " ("
				+ Database.ROW_ID + ", "
				+ Database.EM_NAME + ") Values ('0', 'Select ...')";*/
        String DefaultMachine = "INSERT INTO " + Database.MACHINE_TABLE_NAME + " ("
                + Database.ROW_ID + ", "
                + Database.MC_NAME + ") Values ('0', 'Select ...')";


        try {
            database.execSQL(estates_table_sql);
            database.execSQL(divisions_table_sql);
            database.execSQL(fields_table_sql);
            database.execSQL(blocks_table_sql);
            database.execSQL(factory_table_sql);
            database.execSQL(produce_table_sql);
            database.execSQL(producegrades_table_sql);
            database.execSQL(producevarieties_table_sql);
            database.execSQL(task_table_sql);
            database.execSQL(teams_table_sql);
            database.execSQL(employee_table_sql);
            database.execSQL(machine_table_sql);
            database.execSQL(machine_operators_table_sql);
            database.execSQL(machine_fuel_table_sql);
            database.execSQL(transporter_table_sql);
            database.execSQL(capital_table_sql);
            database.execSQL(operators_master_table_sql);
            database.execSQL(session_table_sql);
            database.execSQL(employee_produce_collection_sql);
            database.execSQL(company_table_sql);
            database.execSQL(employee_batches);
            database.execSQL(Delivery_table_sql);


            Log.d("EasyweighDB", "Tables created!");
            //Defaults
            database.execSQL(DefaultUsers);
            database.execSQL(DefaultEstates);
            database.execSQL(DefaultDivision);
            database.execSQL(DefaultBlock);
            database.execSQL(DefaultField);
            database.execSQL(DefaultComm);
            database.execSQL(DefaultVariety);
            database.execSQL(DefaultGrade);
            database.execSQL(DefaultTask);
            database.execSQL(DefaultMachine);
            //	database.execSQL(DefaultEmployee);


        } catch (Exception ex) {
            Log.d("EasyweighDB", "Error in DBHelper.onCreate() : " + ex.getMessage());
        }
    }


    public long AddCompanyDetails(String co_prefix, String co_name, String co_letterbox, String co_postcode, String co_postname, String co_postregion, String co_telephone) {
        SQLiteDatabase db = this.getReadableDatabase();
        HashMap<String, String> queryValues = new HashMap<String, String>();
        queryValues.put("co_prefix", co_prefix);
        queryValues.put("co_name", co_name);
        queryValues.put("co_letterbox", co_letterbox);
        queryValues.put("co_postcode", co_postcode);
        queryValues.put("co_postname", co_postname);
        queryValues.put("co_posregion", co_postregion);
        queryValues.put("co_telephone", co_telephone);


        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.CO_PREFIX, queryValues.get("co_prefix"));
        initialValues.put(Database.CO_NAME, queryValues.get("co_name"));
        initialValues.put(Database.CO_LETTERBOX, queryValues.get("co_letterbox"));
        initialValues.put(Database.CO_POSTCODE, queryValues.get("co_postcode"));
        initialValues.put(Database.CO_POSTNAME, queryValues.get("co_postname"));
        initialValues.put(Database.CO_POSTREGION, queryValues.get("co_postregion"));
        initialValues.put(Database.CO_TELEPHONE, queryValues.get("co_telephone"));

        return db.insert(Database.COMPANY_TABLE_NAME, null, initialValues);

    }

    /////////////////////////////////////////////////////////////////////
    //USERS FUNCTIONS///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddUsers(String s_etFullName, String s_etNewUserId, String s_etPassword, String s_spUserLevel) {

        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.USERIDENTIFIER, s_etFullName);
        initialValues.put(Database.CLERKNAME, s_etNewUserId);
        initialValues.put(Database.USERPWD, s_etPassword);
        initialValues.put(Database.ACCESSLEVEL, s_spUserLevel);
        initialValues.put(Database.USERCLOUDID, 0);
        return db.insert(Database.OPERATORSMASTER_TABLE_NAME, null, initialValues);

    }

    public Cursor fetchUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.OPERATORSMASTER_TABLE_NAME,
                new String[]{"_id", "ClerkName"},
                "ClerkName" + "='" + username + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    public boolean UserLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + Database.OPERATORSMASTER_TABLE_NAME
                + " WHERE ClerkName=? COLLATE NOCASE AND UserPwd=?", new String[]{username, password});
        if (mCursor != null) {
            return mCursor.getCount() > 0;
        }
        return false;
    }

    /**
     * cursor for viewing password
     */
    public Cursor getPassword(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] allColumns = new String[]{Database.USERPWD, Database.USERIDENTIFIER};
        Cursor c = db.query(Database.OPERATORSMASTER_TABLE_NAME, allColumns, "ClerkName COLLATE NOCASE" + "='" + username + "'", null, null, null, null,
                null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /**
     * cursor for viewing access level
     */
    public Cursor getAccessLevel(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] allColumns = new String[]{Database.ACCESSLEVEL, Database.USERIDENTIFIER, Database.USERCLOUDID};
        Cursor c = db.query(Database.OPERATORSMASTER_TABLE_NAME, allColumns, "ClerkName COLLATE NOCASE" + "='" + username + "'", null, null, null, null,
                null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /////////////////////////////////////////////////////////////////////
    //ESTATE FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddEstate(String s_esID, String s_esName, String s_esCompany, String s_esRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.ES_ID, s_esID);
        initialValues.put(Database.ES_NAME, s_esName);
        initialValues.put(Database.ES_COMPANY, s_esCompany);
        initialValues.put(Database.CloudID, s_esRecordIndex);
        return db.insert(Database.ESTATES_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckEstate(String s_esID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.ESTATES_TABLE_NAME,
                new String[]{"_id", Database.ES_ID},
                Database.ES_ID + "='" + s_esID + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    /////////////////////////////////////////////////////////////////////
    //DIVISIONS FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddDivision(String s_dvID, String s_dvName, String s_dvEstate, String s_dvRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.DV_ID, s_dvID);
        initialValues.put(Database.DV_NAME, s_dvName);
        initialValues.put(Database.DV_ESTATE, s_dvEstate);
        initialValues.put(Database.CloudID, s_dvRecordIndex);
        return db.insert(Database.DIVISIONS_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckDivision(String s_dvID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.DIVISIONS_TABLE_NAME,
                new String[]{"_id", Database.DV_ID},
                Database.DV_ID + "='" + s_dvID + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    /////////////////////////////////////////////////////////////////////
    //FIELDS FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddField(String s_fdID, String s_fdDiv, String s_fdRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.FD_ID, s_fdID);
        initialValues.put(Database.FD_DIVISION, s_fdDiv);
        initialValues.put(Database.CloudID, s_fdRecordIndex);
        return db.insert(Database.FIELD_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckField(String s_fdID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.FIELD_TABLE_NAME,
                new String[]{"_id", Database.FD_ID},
                Database.FD_ID + "='" + s_fdID + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    /////////////////////////////////////////////////////////////////////
    //BLOCK FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddBlock(String s_bkID, String s_bkField, String s_bkRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.BK_ID, s_bkID);
        initialValues.put(Database.BK_FIELD, s_bkField);
        initialValues.put(Database.CloudID, s_bkRecordIndex);
        return db.insert(Database.BLOCK_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckBlock(String s_bkID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.BLOCK_TABLE_NAME,
                new String[]{"_id", Database.BK_ID},
                Database.BK_ID + "='" + s_bkID + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    /////////////////////////////////////////////////////////////////////
    //FACTORY FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddFactories(String s_fryprefix, String s_fryname, String s_recordindex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.FRY_PREFIX, s_fryprefix);
        initialValues.put(Database.FRY_TITLE, s_fryname);
        initialValues.put(Database.CloudID, s_recordindex);
        return db.insert(Database.FACTORY_TABLE_NAME, null, initialValues);

    }


    public Cursor CheckFactory(String s_fryprefix) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.FACTORY_TABLE_NAME,
                new String[]{"_id", Database.FRY_PREFIX},
                Database.FRY_PREFIX + "='" + s_fryprefix + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    /////////////////////////////////////////////////////////////////////
    //PRODUCE FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddProduce(String s_etDgProduceCode, String s_etDgProduceTitle, String s_crRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.MP_CODE, s_etDgProduceCode);
        initialValues.put(Database.MP_DESCRIPTION, s_etDgProduceTitle);
        initialValues.put(Database.MP_DESCRIPTION, s_etDgProduceTitle);
        initialValues.put(Database.CloudID, s_crRecordIndex);
        return db.insert(Database.PRODUCE_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckProduce(String mp_code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.PRODUCE_TABLE_NAME,
                new String[]{"_id", Database.MP_CODE},
                Database.MP_CODE + "='" + mp_code + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    /////////////////////////////////////////////////////////////////////
    //VARIETY FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddVariety(String s_etDgProduceCode, String s_etDgProduceTitle, String produceid, String s_vrRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.VRT_REF, s_etDgProduceCode);
        initialValues.put(Database.VRT_NAME, s_etDgProduceTitle);
        initialValues.put(Database.VRT_PRODUCE, produceid);
        initialValues.put(Database.CloudID, s_vrRecordIndex);
        return db.insert(Database.PRODUCEVARIETIES_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckVariety(String mp_code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.PRODUCEVARIETIES_TABLE_NAME,
                new String[]{"_id", Database.VRT_REF},
                Database.VRT_REF + "='" + mp_code + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    /////////////////////////////////////////////////////////////////////
    //GRADE FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddGrade(String s_etDgProduceCode, String s_etDgProduceTitle, String produceid, String s_grRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.PG_DREF, s_etDgProduceCode);
        initialValues.put(Database.PG_DNAME, s_etDgProduceTitle);
        initialValues.put(Database.PG_DPRODUCE, produceid);
        initialValues.put(Database.CloudID, s_grRecordIndex);
        return db.insert(Database.PRODUCEGRADES_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckGrade(String mp_code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.PRODUCEGRADES_TABLE_NAME,
                new String[]{"_id", Database.PG_DREF},
                Database.PG_DREF + "='" + mp_code + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    /////////////////////////////////////////////////////////////////////
    //TASK FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddTask(String s_tkID, String s_tkName, String s_tkType, String s_tkOT, String s_tkMT, String s_tkRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.TK_ID, s_tkID);
        initialValues.put(Database.TK_NAME, s_tkName);
        initialValues.put(Database.TK_TYPE, s_tkType);
        initialValues.put(Database.TK_OT, s_tkOT);
        initialValues.put(Database.TK_MT, s_tkMT);
        initialValues.put(Database.CloudID, s_tkRecordIndex);
        return db.insert(Database.TASK_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckTask(String s_tkID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.TASK_TABLE_NAME,
                new String[]{"_id", Database.TK_ID},
                Database.TK_ID + "='" + s_tkID + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    public Cursor SearchTask(String TaskCode) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(Database.TASK_TABLE_NAME,
                new String[]{Database.ROW_ID, Database.TK_ID, Database.TK_NAME, Database.TK_TYPE, Database.TK_OT, Database.TK_MT}, Database.TK_ID + " LIKE ?",
                new String[]{"%" + TaskCode + "%"}, null, null, Database.TK_ID + " ASC");

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchSpecificTask(String TaskCode) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(true, Database.TASK_TABLE_NAME, null, Database.TK_ID + "='" + TaskCode + "'", null, null, null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }


    public long AddTeam(String s_tmCode, String s_tmName, String s_dgNumber, String s_dgDivision, String s_edEstate, String s_tmRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.TM_CODE, s_tmCode);
        initialValues.put(Database.TM_NAME, s_tmName);
        initialValues.put(Database.DG_NUMBER, s_dgNumber);
        initialValues.put(Database.DG_DIVISION, s_dgDivision);
        initialValues.put(Database.ED_ESTATE, s_edEstate);
        initialValues.put(Database.CloudID, s_tmRecordIndex);
        return db.insert(Database.TEAMS_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckTeam(String s_dgNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.TEAMS_TABLE_NAME,
                new String[]{"_id", Database.DG_NUMBER},
                Database.DG_NUMBER + "='" + s_dgNumber + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }


    /////////////////////////////////////////////////////////////////////
    //EMPLOYEE FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddEM(String s_emID, String s_emName, String s_emIDNO, String s_emCardNO, String s_emPickerNO, String s_emTeam, String s_emRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.EM_ID, s_emID);
        initialValues.put(Database.EM_NAME, s_emName);
        initialValues.put(Database.EM_IDNO, s_emIDNO);
        initialValues.put(Database.EM_CARDID, s_emCardNO);
        initialValues.put(Database.EM_PICKERNO, s_emPickerNO);
        initialValues.put(Database.EM_TEAM, s_emTeam);
        initialValues.put(Database.CloudID, s_emRecordIndex);
        return db.insert(Database.EM_TABLE_NAME, null, initialValues);

    }


    public Cursor CheckEM(String s_emID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.EM_TABLE_NAME,
                new String[]{"_id", Database.EM_ID},
                Database.EM_ID + "='" + s_emID + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }



    public Cursor SearchEmployee(String EmployeeCode) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(Database.EM_TABLE_NAME,
                new String[]{Database.ROW_ID, Database.EM_ID, Database.EM_NAME, Database.EM_PICKERNO, Database.EM_TEAM}, Database.EM_ID + " LIKE ?",
                new String[]{"%" + EmployeeCode + "%"}, null, null, Database.EM_ID + " ASC");

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchSpecificEmployee(String EmployeeCode) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(true, Database.EM_TABLE_NAME, null, Database.EM_ID + "='" + EmployeeCode + "'", null, null, null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }

    public Cursor SearchEmployeePicker(String PickerNo) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(Database.EM_TABLE_NAME,
                new String[]{Database.ROW_ID, Database.EM_ID, Database.EM_NAME, Database.EM_PICKERNO, Database.EM_TEAM}, Database.EM_PICKERNO + " LIKE ?",
                new String[]{"%" + PickerNo + "%"}, null, null, Database.EM_ID + " ASC");

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchSpecificPicker(String PickerNo) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(true, Database.EM_TABLE_NAME, null, Database.EM_PICKERNO + "='" + PickerNo + "'", null, null, null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchSpecificTeam(String EmployeeTeam) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(true, Database.EM_TABLE_NAME, null, Database.EM_TEAM + "='" + EmployeeTeam + "'", null, null, null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        db.close();
        return myCursor;
    }


    /////////////////////////////////////////////////////////////////////
    //MACHINE FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddMachine(String s_MID, String s_MName, String s_MRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.MC_ID, s_MID);
        initialValues.put(Database.MC_NAME, s_MName);
        initialValues.put(Database.CloudID, s_MRecordIndex);
        return db.insert(Database.MACHINE_TABLE_NAME, null, initialValues);

    }


    public Cursor CheckMachine(String s_MID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.MACHINE_TABLE_NAME,
                new String[]{"_id", Database.MC_ID},
                Database.MC_ID + "='" + s_MID + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    public Cursor SearchMachine(String MachineCode) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(Database.MACHINE_TABLE_NAME,
                new String[]{Database.ROW_ID, Database.MC_ID, Database.MC_NAME}, Database.MC_ID + " LIKE ?",
                new String[]{"%" + MachineCode + "%"}, null, null, Database.MC_ID + " ASC");

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchSpecificMachine(String MachineCode) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(true, Database.MACHINE_TABLE_NAME, null, Database.MC_ID + "='" + MachineCode + "'", null, null, null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }

    public Cursor SearchWMachine(String MachineCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateTimeFormatB = null;
        dateTimeFormatB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String MDate = dateTimeFormatB.format(cal.getTime());
//        Cursor myCursor = db.query(Database.MACHINE_TABLE_NAME,
//                new String[]{Database.ROW_ID, Database.MC_ID, Database.MC_NAME}, Database.MC_ID + " LIKE ?",
//                new String[]{"%" + MachineCode + "%"}, null, null, Database.MC_ID + " ASC");
        Cursor myCursor = db.rawQuery("select * from " + Database.MACHINE_TABLE_NAME + "," + Database.MACHINEOP_TABLE_NAME + "" +
                " where " + Database.MC_ID + "=" + Database.MACHINENUMBER + " and " + Database.MACHINENUMBER + " LIKE '%" + MachineCode + "%' and " + Database.MDATE + "='" + MDate + "' and " + Database.MSTATUS + "='1' group by machineNo", null);


        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchSpecificWMachine(String MachineCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateTimeFormatB = null;
        dateTimeFormatB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String MDate = dateTimeFormatB.format(cal.getTime());
        //Cursor myCursor = db.query(true, Database.MACHINE_TABLE_NAME, null, Database.MC_ID + "='" + MachineCode + "'", null, null, null, null, null, null);
        Cursor myCursor = db.rawQuery("select * from " + Database.MACHINE_TABLE_NAME + "," + Database.MACHINEOP_TABLE_NAME + "" +
                " where " + Database.MC_ID + "=" + Database.MACHINENUMBER + " and " + Database.MACHINENUMBER + "='" + MachineCode + "' and " + Database.MDATE + "='" + MDate + "' and " + Database.MSTATUS + "='1' group by machineNo", null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }


    public long AddMachineOperators(String sDate, String sterminalID, String smachineNo, String semployeeNo, String scheckinTime,
                                    int checkinWeighment, String smTaskCode, String smCompany, String smEstate) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.MDATE, sDate);
        initialValues.put(Database.TERMINALID, sterminalID);
        initialValues.put(Database.MACHINENUMBER, smachineNo);
        initialValues.put(Database.EMPLOYEENUMBER, semployeeNo);
        initialValues.put(Database.CHECKINTIME, scheckinTime);
        initialValues.put(Database.CHECKINWEIGHMENT, checkinWeighment);
        initialValues.put(Database.MTASKCODE, smTaskCode);
        initialValues.put(Database.MCOMPANY, smCompany);
        initialValues.put(Database.MESTATE, smEstate);
        initialValues.put(Database.MSTATUS, 1);
        initialValues.put(Database.CloudID, 0);
        return db.insert(Database.MACHINEOP_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckMachineOperator(String semployeeNo, String sDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.MACHINEOP_TABLE_NAME,
                new String[]{"_id", Database.EMPLOYEENUMBER, Database.MDATE},
                Database.EMPLOYEENUMBER + "='" + semployeeNo + "' and " + Database.MDATE + "='" + sDate + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    public Cursor CheckMaxOperators(String smachineNo, String sDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.MACHINEOP_TABLE_NAME,
                new String[]{"_id", Database.EMPLOYEENUMBER, Database.MDATE},
                Database.MACHINENUMBER + "='" + smachineNo + "' and " + Database.MDATE + "='" + sDate + "' and mStatus=1", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    public Cursor SearchMOperator(String EmployeeCode, String smachineNo, String sDate) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(Database.MACHINEOP_TABLE_NAME,
                new String[]{Database.ROW_ID, Database.MDATE, Database.CHECKINTIME, Database.MACHINENUMBER, Database.EMPLOYEENUMBER}, Database.EMPLOYEENUMBER + " LIKE ? and " + Database.MACHINENUMBER + "=? and " + Database.MDATE + "=?",
                new String[]{"%" + EmployeeCode + "%", smachineNo, sDate}, null, null, Database.EMPLOYEENUMBER + " ASC");

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchSpecificMOperator(String EmployeeCode, String smachineNo, String sDate) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(true, Database.MACHINEOP_TABLE_NAME, null, Database.EMPLOYEENUMBER + "='" + EmployeeCode + "' and " + Database.MACHINENUMBER + "='" + smachineNo + "' and " + Database.MDATE + "='" + sDate + "'", null, null, null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }

    public Cursor SearchEmployeeOperator(String EmployeeCode) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(Database.MACHINEOP_TABLE_NAME,
                new String[]{Database.ROW_ID, Database.MDATE, Database.CHECKINTIME, Database.MACHINENUMBER, Database.EMPLOYEENUMBER}, Database.EMPLOYEENUMBER + " LIKE ?",
                new String[]{"%" + EmployeeCode + "%"}, null, Database.MSTATUS + "='1'", Database.EMPLOYEENUMBER + " ASC");

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchSpecificEmployeeOperator(String EmployeeCode) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(true, Database.MACHINEOP_TABLE_NAME, null, Database.EMPLOYEENUMBER + "='" + EmployeeCode + "' and mStatus=1", null, null, null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }


    /////////////////////////////////////////////////////////////////////
    //MACHINE FUELING///////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////

    public long AddMachineFuel(String sDate, String sterminalID, String smachineNo, String mfTime,
                               String mfLitres, String smTaskCode, String smCompany, String smEstate) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.MFDATE, sDate);
        initialValues.put(Database.MFTERMINALID, sterminalID);
        initialValues.put(Database.MFMACHINENUMBER, smachineNo);
        initialValues.put(Database.MFTIME, mfTime);
        initialValues.put(Database.MFLitres, mfLitres);
        initialValues.put(Database.MFTASKCODE, smTaskCode);
        initialValues.put(Database.MFCOMPANY, smCompany);
        initialValues.put(Database.MFESTATE, smEstate);
        initialValues.put(Database.MFSTATUS, 1);
        initialValues.put(Database.CloudID, 0);
        return db.insert(Database.MACHINEFUEL_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckFuel(String smachineNo, String sDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.MACHINEFUEL_TABLE_NAME,
                new String[]{"_id", Database.MFMACHINENUMBER, Database.MFDATE},
                Database.MFMACHINENUMBER + "='" + smachineNo + "' and " + Database.MFDATE + "='" + sDate + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    /////////////////////////////////////////////////////////////////////
    //CAPITALP FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddCapitalP(String s_CPID, String s_CPName, String s_CPRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.CP_ID, s_CPID);
        initialValues.put(Database.CP_NAME, s_CPName);
        initialValues.put(Database.CloudID, s_CPRecordIndex);
        return db.insert(Database.CAPITALP_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckCapitalP(String s_CPID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.CAPITALP_TABLE_NAME,
                new String[]{"_id", Database.CP_ID},
                Database.CP_ID + "='" + s_CPID + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }


    /////////////////////////////////////////////////////////////////////
    //TRANSPORTER FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddTransporter(String s_tptID, String s_tptName, String TRecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.TPT_ID, s_tptID);
        initialValues.put(Database.TPT_NAME, s_tptName);
        initialValues.put(Database.CloudID, TRecordIndex);
        return db.insert(Database.TRANSPORTER_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckTransporterID(String tptID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.TRANSPORTER_TABLE_NAME,
                new String[]{"_id", Database.CloudID},
                Database.CloudID + "='" + tptID + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    public Cursor CheckTransporter(String tptID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.TRANSPORTER_TABLE_NAME,
                new String[]{"_id", Database.TPT_ID},
                Database.TPT_ID + "='" + tptID + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }


    /////////////////////////////////////////////////////////////////////
    //BATCH FUNCTIONS///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddBatch(String BatchDate, String DeliverNoteNumber, String DataDevice, String BatchNumber, String UserID, String OpeningTime, String BEstate, String BDivision) {

        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.BatchDate, BatchDate);
        initialValues.put(Database.DeliveryNoteNumber, DeliverNoteNumber);
        initialValues.put(Database.DataDevice, DataDevice);
        initialValues.put(Database.BatchNumber, BatchNumber);
        initialValues.put(Database.Userid, UserID);
        initialValues.put(Database.OpeningTime, OpeningTime);
        initialValues.put(Database.BEstate, BEstate);
        initialValues.put(Database.BDivision, BDivision);
        initialValues.put(Database.Closed, 0);
        initialValues.put(Database.SignedOff, 0);
        initialValues.put(Database.BatCloudID, 0);

        return db.insert(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, initialValues);

    }

    /////////////////////////////////////////////////////////////////////
    //EMPLOYEE PRODUCE COLLECTION TRANSACTIONS FUNCTIONS///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddEmployeeTrans(String ColDate, String Time, String DataDevice, String BatchNumber, String EmployeeNo,
                                 String FieldClerk, String TaskCode, String TaskType, String ProduceCode,
                                 String VarietyCode, String GradeCode, String Estate, String Division, String Field, String Block,
                                 String NetWeight, String TareWeight, String UnitCount,
                                 String UnitPrice, String RecieptNo, String LoadCount, String CheckinMethod) {

        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.CollDate, ColDate);
        initialValues.put(Database.CaptureTime, Time);
        initialValues.put(Database.DataCaptureDevice, DataDevice);
        initialValues.put(Database.BatchNo, BatchNumber);
        initialValues.put(Database.EmployeeNo, EmployeeNo);
        initialValues.put(Database.FieldClerk, FieldClerk);
        initialValues.put(Database.TaskCode, TaskCode);
        initialValues.put(Database.TaskType, TaskType);
        initialValues.put(Database.DeliveredProduce, ProduceCode);
        initialValues.put(Database.ProduceVariety, VarietyCode);
        initialValues.put(Database.ProduceGrade, GradeCode);
        initialValues.put(Database.SourceEstate, Estate);
        initialValues.put(Database.SourceDivision, Division);
        initialValues.put(Database.SourceField, Field);
        initialValues.put(Database.SourceBlock, Block);
        initialValues.put(Database.NetWeight, NetWeight);
        initialValues.put(Database.Tareweight, TareWeight);
        initialValues.put(Database.BagCount, UnitCount);
        initialValues.put(Database.UnitPrice, UnitPrice);
        initialValues.put(Database.ReceiptNo, RecieptNo);
        initialValues.put(Database.LoadCount, LoadCount);
        initialValues.put(Database.UsedSmartCard, CheckinMethod);
        initialValues.put(Database.CloudID, 0);

        return db.insert(Database.EM_PRODUCE_COLLECTION_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckWeighmentCloudID(String CloudID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.EM_PRODUCE_COLLECTION_TABLE_NAME,
                new String[]{"_id", Database.CloudID},
                Database.CloudID + "='" + CloudID + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    public Cursor SearchReciept(String RecieptNo) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(Database.EM_PRODUCE_COLLECTION_TABLE_NAME,
                new String[]{Database.ROW_ID, Database.EmployeeNo, Database.DataCaptureDevice, Database.ReceiptNo, Database.CollDate}, Database.ReceiptNo + " LIKE ?",
                new String[]{"%" + RecieptNo + "%"}, null, null, Database.ReceiptNo + " ASC");

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        db.close();
        return myCursor;

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchSpecificReciept(String RecieptNo) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(true, Database.EM_PRODUCE_COLLECTION_TABLE_NAME, null, Database.ReceiptNo + "='" + RecieptNo + "'", null, null, null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        db.close();
        return myCursor;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchRecieptByDate(String condition) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(true, Database.SESSION_TABLE_NAME, null, "" + condition + "", null, null, null, null, null, null);


        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        db.close();
        return myCursor;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchGenReciept(String condition) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(true, Database.EM_PRODUCE_COLLECTION_TABLE_NAME, null, "" + condition + "", null, null, null, null, null, null);


        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        db.close();
        return myCursor;
    }

    public Cursor SearchBatch(String RecieptNo) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME,
                new String[]{Database.ROW_ID, Database.DeliveryNoteNumber, Database.DataDevice, Database.BatchNumber, Database.BatchDate}, Database.DeliveryNoteNumber + " LIKE ?",
                new String[]{"%" + RecieptNo + "%"}, null, null, Database.DeliveryNoteNumber + " ASC");

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        db.close();
        return myCursor;

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchSpecificBatch(String RecieptNo) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(true, Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, Database.DeliveryNoteNumber + "='" + RecieptNo + "'", null, null, null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        db.close();
        return myCursor;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchBatchByDate(String condition) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(true, Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, "" + condition + "", null, null, null, null, null, null);


        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        db.close();
        return myCursor;
    }


    public Cursor SearchOnR(String employeeNo) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(Database.EM_PRODUCE_COLLECTION_TABLE_NAME,
                new String[]{Database.ROW_ID, Database.EmployeeNo, Database.NetWeight}, Database.EmployeeNo + " LIKE ?",
                new String[]{"%" + employeeNo + "%"}, null, null, Database.EmployeeNo + " ASC");

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        db.close();
        return myCursor;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchSpecificOnR(String employeeNo, String condition) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(true, Database.EM_PRODUCE_COLLECTION_TABLE_NAME, null, Database.EmployeeNo + "='" + employeeNo + "'", null, "" + condition + "", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        db.close();
        return myCursor;
    }

    public long AddSession(String SessionID, String SessionNo, String SessionDate, String SessionTime, String SessionDevice,
                           String SessionFarmerNo, String SessionBags, String SessionNet, String SessionTare
            , String SessionField, String SessionBlock, String SessionGrade) {

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.ROW_ID, SessionID);
        initialValues.put(Database.SessionNo, SessionNo);
        initialValues.put(Database.SessionDate, SessionDate);
        initialValues.put(Database.SessionTime, SessionTime);
        initialValues.put(Database.SessionDevice, SessionDevice);
        initialValues.put(Database.SessionEmployeeNo, SessionFarmerNo);
        initialValues.put(Database.SessionBags, SessionBags);
        initialValues.put(Database.SessionNet, SessionNet);
        initialValues.put(Database.SessionTare, SessionTare);
        initialValues.put(Database.SessionField, SessionField);
        initialValues.put(Database.SessionBlock, SessionBlock);
        initialValues.put(Database.SessionGrade, SessionGrade);


        return db.insert(Database.SESSION_TABLE_NAME, null, initialValues);

    }

    /////////////////////////////////////////////////////////////////////
    //DELIVARY FUNCTIONS///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddDelivery(String EstateCode, String DNoteNo, String Date, String Factory, String Transporter, String Vehicle, String Tractor, String Driver, String TurnMan, String ArrivalTime, String FieldWt) {

        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.FdWeighbridgeTicket, EstateCode);
        initialValues.put(Database.FdDNoteNum, DNoteNo);
        initialValues.put(Database.FdDate, Date);
        initialValues.put(Database.FdFactory, Factory);
        initialValues.put(Database.FdTransporter, Transporter);
        initialValues.put(Database.FdVehicle, Vehicle);
        initialValues.put(Database.FdTractor, Tractor);
        initialValues.put(Database.FdDriver, Driver);
        initialValues.put(Database.FdTurnMan, TurnMan);
        initialValues.put(Database.FdArrivalTime, ArrivalTime);
        initialValues.put(Database.FdDepartureTime, ArrivalTime);
        initialValues.put(Database.FdFieldWt, FieldWt);
        initialValues.put(Database.FdStatus, 0);
        initialValues.put(Database.CloudID, 0);

        return db.insert(Database.Fmr_FactoryDeliveries, null, initialValues);

    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor CheckDelivary(String DNoteNo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.Fmr_FactoryDeliveries,
                new String[]{"_id", Database.FdDNoteNum},
                Database.FdDNoteNum + "='" + DNoteNo + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor SearchDeliveryByDate(String condition) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(true, Database.Fmr_FactoryDeliveries, null, "" + condition + "", null, null, null, null, null, null);


        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        db.close();
        return myCursor;
    }
}
