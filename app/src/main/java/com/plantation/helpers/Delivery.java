package com.plantation.helpers;

public class Delivery {
    private String id;
    private String number;
    private String deldate;
    private String totalkgs;
    private String CloudID;

    public Delivery(String id, String number, String deldate, String totalkgs, String CloudID) {
        super();
        this.id = id;
        this.number = number;
        this.deldate = deldate;
        this.totalkgs = totalkgs;
        this.CloudID = CloudID;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getDNoteNo() {
        return number;
    }

    public void setDNoteNo(String number) {
        this.number = number;
    }

    public String getCloudID() {
        return CloudID;
    }

    public void setCloudID(String CloudID) {
        this.CloudID = CloudID;
    }

    public String getDeldate() {
        return deldate;
    }

    public void setDeldate(String deldate) {
        this.deldate = deldate;
    }

    public String getTotalkgs() {
        return totalkgs;
    }

    public void setTotalkgs(String totalkgs) {
        this.totalkgs = totalkgs;
    }

}