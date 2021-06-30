package com.plantation.helpers;

public class Fuel {
    private String id;
    private String machineNo;
    private String mFuel;

    public Fuel(String id, String machineNo, String mFuel) {
        super();
        this.id = id;
        this.machineNo = machineNo;
        this.mFuel = mFuel;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getMachineNo() {
        return machineNo;
    }

    public void setMachineNo(String machineNo) {
        this.machineNo = machineNo;
    }

    public String getFuel() {
        return mFuel;
    }

    public void setFuel(String mFuel) {
        this.mFuel = mFuel;
    }


}