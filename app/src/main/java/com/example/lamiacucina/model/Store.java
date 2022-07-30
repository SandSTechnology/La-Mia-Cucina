package com.example.lamiacucina.model;

public class Store {
    String ID;
    String NAME;
    String LAT;
    String LONG;

    public Store(){}

    public Store(String ID, String NAME, String LAT, String LONG) {
        this.ID = ID;
        this.NAME = NAME;
        this.LAT = LAT;
        this.LONG = LONG;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getLAT() {
        return LAT;
    }

    public void setLAT(String LAT) {
        this.LAT = LAT;
    }

    public String getLONG() {
        return LONG;
    }

    public void setLONG(String LONG) {
        this.LONG = LONG;
    }
}
