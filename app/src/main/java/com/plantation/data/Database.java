package com.plantation.data;


public class Database {

    //Constants
    public static final String ROW_ID = "_id";
    public static final String CloudID = "CloudID";

    //Estates Table
    public static final String ESTATES_TABLE_NAME = "estates";
    public static final String ES_ID = "esID";
    public static final String ES_NAME = "esName";
    public static final String ES_COMPANY = "esCompany";

    //Divisions Table
    public static final String DIVISIONS_TABLE_NAME = "divisions";
    public static final String DV_ID = "dvID";
    public static final String DV_NAME = "dvName";
    public static final String DV_ESTATE = "dvEstate";

    //Fields Table
    public static final String FIELD_TABLE_NAME = "fields";
    public static final String FD_ID = "fdID";
    public static final String FD_DIVISION = "fdDivision";

    //Blocks Table
    public static final String BLOCK_TABLE_NAME = "blocks";
    public static final String BK_ID = "bkID";
    public static final String BK_FIELD = "bkFiled";

    //Factory Table
    public static final String FACTORY_TABLE_NAME = "factory";
    public static final String FRY_PREFIX = "FryPrefix";
    public static final String FRY_TITLE = "FryTitle";
    public static final String FRY_ClOUDID = "FryCloudID";

    //Produce Table
    public static final String PRODUCE_TABLE_NAME = "Produce";
    public static final String MP_CODE = "MpCode";
    public static final String MP_DESCRIPTION = "MpDescription";
    public static final String MP_RETAILPRICE = "MpRetailPrice";
    public static final String MP_SALESTAX = "MpSalesTax";
    public static final String MP_CLOUDID = "MpCloudID";

    //ProduceGrades Table
    public static final String PRODUCEGRADES_TABLE_NAME = "ProduceGrades";
    public static final String PG_DREF = "pgdRef";
    public static final String PG_DNAME = "pgdName";
    public static final String PG_DPRODUCE = "pgdProduce";
    public static final String PG_RETAILPRICE = "PgRetailPrice";
    public static final String PG_SALESTAX = "PgSalesTax";
    public static final String PG_DCLOUDID = "pgdCloudID";

    //ProduceVarieties Table
    public static final String PRODUCEVARIETIES_TABLE_NAME = "ProduceVarieties";
    public static final String VRT_REF = "vtrRef";
    public static final String VRT_NAME = "vrtName";
    public static final String VRT_PRODUCE = "vrtProduce";
    public static final String VRT_RETAILPRICE = "vrtRetailPrice";
    public static final String VRT_SALESTAX = "vrtSalesTax";
    public static final String VRT_CLOUDID = "vrtCloudID";


    //Task Table
    public static final String TASK_TABLE_NAME = "tasks";
    public static final String TK_ID = "tkID";
    public static final String TK_NAME = "tkName";
    public static final String TK_TYPE = "tkType";
    public static final String TK_OT = "tkOt";
    public static final String TK_MT = "tkMt";

    //Employee Table
    public static final String EM_TABLE_NAME = "employees";
    public static final String EM_ID = "emID";
    public static final String EM_PICKERNO = "emPickerNo";
    public static final String EM_NAME = "emName";
    public static final String EM_IDNO = "emIDNo";
    public static final String EM_CARDID = "emCardID";
    public static final String EM_TEAM = "emTeam";


    //Clerks Table
    public static final String OPERATORSMASTER_TABLE_NAME = "OperatorsMaster";
    public static final String USERIDENTIFIER = "UserIdentifier";
    public static final String CLERKNAME = "ClerkName";
    public static final String ACCESSLEVEL = "AccessLevel";
    public static final String USERPWD = "UserPwd";
    public static final String USERCLOUDID = "UserCloudID";

    //Commodities Table
    public static final String MACHINE_TABLE_NAME = "machines";
    public static final String MC_ID = "mcID";
    public static final String MC_NAME = "mcName";

    //Transporter Table
    public static final String TRANSPORTER_TABLE_NAME = "transporter";
    public static final String TPT_ID = "tptID";
    public static final String TPT_NAME = "tptName";

    //Commodities Table
    public static final String CAPITALP_TABLE_NAME = "capitalp";
    public static final String CP_ID = "cpID";
    public static final String CP_NAME = "cpName";

    //FarmersSuppliesConsignments Table
    public static final String FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME = "FarmersSuppliesConsignments";
    public static final String BatchDate = "BatchDate";
    public static final String DeliveryNoteNumber = "DeliveryNoteNumber";
    public static final String DataDevice = "DataDevice";
    public static final String BatchNumber = "BatchNumber";
    public static final String Userid = "Userid";
    public static final String OpeningTime = "OpeningTime";
    public static final String Closed = "Closed";
    public static final String ClosingTime = "ClosingTime";
    public static final String NoOfWeighments = "NoOfWeighments";
    public static final String NoOfTasks = "NoOfTasks";
    public static final String TotalWeights = "TotalWeights";
    public static final String Factory = "Factory";
    public static final String Transporter = "Transporter";
    public static final String Tractor = "Tractor";
    public static final String Trailer = "Trailer";
    public static final String DelivaryNO = "DelivaryNO";
    public static final String SignedOff = "SignedOff";
    public static final String SignedOffTime = "SignedOffTime";
    public static final String BatchSession = "BatchSession";
    public static final String BatchCount = "BatchCount";
    public static final String Dispatched = "Dispatched";
    public static final String BatchCrates = "BatchCrates";

    public static final String BEstate = "BEstate";
    public static final String BDivision = "BDivision";
    public static final String BatCloudID = "BatCloudID";

    //Session Table
    public static final String SESSION_TABLE_NAME = "SessionTbl";
    public static final String SessionNo = "sno";
    public static final String SessionDate = "sdate";
    public static final String SessionTime = "stime";
    public static final String SessionDevice = "sdev";
    public static final String SessionEmployeeNo = "semployeeno";
    public static final String SessionBags = "sbags";
    public static final String SessionNet = "snet";
    public static final String SessionTare = "stare";
    public static final String SessionField = "sfield";
    public static final String SessionBlock = "sblock";
    public static final String SessionGrade = "sshed";



    //EmployeeProduceCollection Table
    public static final String EM_PRODUCE_COLLECTION_TABLE_NAME = "EmployeeProduceCollection";
    public static final String CollDate = "CollDate";
    public static final String CaptureTime = "CaptureTime";
    public static final String DataCaptureDevice = "DataCaptureDevice";
    public static final String BatchNo = "BatchNo";
    public static final String EmployeeNo = "EmployeeNo";
    public static final String FieldClerk = "FieldClerk";
    public static final String SourceEstate = "SourceEstate";
    public static final String SourceDivision = "SourceDivision";
    public static final String SourceField = "SourceField";
    public static final String SourceBlock = "SourceBlock";
    public static final String TaskCode = "TaskCode";
    public static final String TaskType = "TaskType";
    public static final String TaskUnits = "TaskUnits";
    public static final String Checkout = "Checkout";
    public static final String CheckoutTime = "CheckoutTime";
    public static final String CheckinMethod = "CheckinMethod";
    public static final String CheckoutMethod = "CheckoutMethod";

    //Produce Collection Table
    public static final String DeliveredProduce = "DeliveredProduce";
    public static final String ProduceVariety = "ProduceVariety";
    public static final String ProduceGrade = "ProduceGrade";
    public static final String NetWeight = "NetWeight";
    public static final String Tareweight = "Tareweight";
    public static final String BagCount = "BagCount";
    public static final String UnitPrice = "UnitPrice";
    public static final String ReceiptNo = "ReceiptNo";
    public static final String LoadCount = "LoadCount";
    public static final String UsedSmartCard = "UsedSmartCard";


    // Machine Table
    public static final String MACHINEOP_TABLE_NAME = "machineoperators";
    public static final String MDATE = "date";
    public static final String TERMINALID = "terminalID";
    public static final String MACHINENUMBER = "machineNo";
    public static final String EMPLOYEENUMBER = "employeeNo";
    public static final String CHECKINTIME = "checkinTime";
    public static final String CHECKOUTTIME = "checkoutTime";
    public static final String CHECKINWEIGHMENT = "checkinWeighment";
    public static final String CHECKOUTWEIGHMENT = "checkoutWeighment";
    public static final String MTASKCODE = "mTaskCode";
    public static final String MCOMPANY = "mCompany";
    public static final String MESTATE = "mEstate";
    public static final String MSTATUS = "mStatus";

    // Machine Fueling Table
    public static final String MACHINEFUEL_TABLE_NAME = "machinefueling";
    public static final String MFDATE = "date";
    public static final String MFTERMINALID = "mfterminalID";
    public static final String MFMACHINENUMBER = "mfmachineNo";
    public static final String MFTIME = "mfTime";
    public static final String MFLitres = "mfLitres";
    public static final String MFTASKCODE = "mFTaskCode";
    public static final String MFCOMPANY = "mFCompany";
    public static final String MFESTATE = "mFEstate";
    public static final String MFSTATUS = "mFStatus";


    // Company Table
    public static final String COMPANY_TABLE_NAME = "company";
    public static final String CO_PREFIX = "CoPrefix";
    public static final String CO_NAME = "CoName";
    public static final String CO_LETTERBOX = "CoLetterBox";
    public static final String CO_POSTCODE = "CoPostCode";
    public static final String CO_POSTNAME = "CoPostName";
    public static final String CO_POSTREGION = "coPostRegion";
    public static final String CO_TELEPHONE = "CoTelephone";
    public static final String CO_ClOUDID = "CoCloudID";

    // Deliveries Table
    public static final String Fmr_FactoryDeliveries = "Fmr_FactoryDeliveries";
    public static final String FdWeighbridgeTicket = "FdWeighbridgeTicket";
    public static final String FdDNoteNum = "FdDNoteNum";
    public static final String FdDate = "FdDate";
    public static final String FdFactory = "FdFactory";
    public static final String FdTransporter = "FdTransporter";
    public static final String FdVehicle = "FdVehicle";
    public static final String FdTractor = "FdTractor";
    public static final String FdDriver = "FdDriver";
    public static final String FdTurnMan = "FdTurnMan";
    public static final String FdFieldWt = "FdFieldWt";
    public static final String FdArrivalTime = "FdArrivalTime";
    public static final String FdGrossWt = "FdGrossWt";
    public static final String FdTareWt = "FdTareWt";
    public static final String FdRejectWt = "FdRejectWt";
    public static final String FdQualityScore = "FdQualityScore";
    public static final String FdDepartureTime = "FdDepartureTime";
    public static final String FdStatus = "FdStatus";


}
