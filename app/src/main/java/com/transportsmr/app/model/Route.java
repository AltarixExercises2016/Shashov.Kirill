package com.transportsmr.app.model;

import java.util.HashSet;

/**
 * Created by kirill on 26.11.2016.
 */
public class Route {
    private String KR_ID;
    private String transportType;
    private String affiliation;
    private String direction;
    private String number;
    private int transportTypeId;
    private int affiliationId;

    public Route() {
    }

    public String getKR_ID() {
        return KR_ID;
    }

    public void setKR_ID(String KR_ID) {
        this.KR_ID = KR_ID;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getTransportTypeId() {
        return transportTypeId;
    }

    public void setTransportTypeId(int transportTypeId) {
        this.transportTypeId = transportTypeId;
    }

    public int getAffiliationId() {
        return affiliationId;
    }

    public void setAffiliationId(int affiliationId) {
        this.affiliationId = affiliationId;
    }
}

