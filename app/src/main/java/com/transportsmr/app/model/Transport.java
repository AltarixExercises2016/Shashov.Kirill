package com.transportsmr.app.model;

/**
 * Created by kirill on 12.12.2016.
 */
public class Transport {
    private String type;
    private String number;
    private String KR_ID;
    private String time;
    private String timeInSeconds;
    private String hullNo;
    private String stateNumber;
    private String modelTitle;
    private boolean forInvalid;
    private String nextStopName;
    private String remainingLength;
    private String spanLength;

    public Transport() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getKR_ID() {
        return KR_ID;
    }

    public void setKR_ID(String KR_ID) {
        this.KR_ID = KR_ID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(String timeInSecond) {
        this.timeInSeconds = timeInSecond;
    }

    public String getHullNo() {
        return hullNo;
    }

    public void setHullNo(String hullNo) {
        this.hullNo = hullNo;
    }

    public String getStateNumber() {
        return stateNumber;
    }

    public void setStateNumber(String stateNumber) {
        this.stateNumber = stateNumber;
    }

    public String getModelTitle() {
        return modelTitle;
    }

    public void setModelTitle(String modelTitle) {
        this.modelTitle = modelTitle;
    }

    public boolean isForInvalid() {
        return forInvalid;
    }

    public void setForInvalid(boolean forInvalid) {
        this.forInvalid = forInvalid;
    }

    public String getNextStopName() {
        return nextStopName;
    }

    public void setNextStopName(String nextStopName) {
        this.nextStopName = nextStopName;
    }

    public String getRemainingLength() {
        return remainingLength;
    }

    public void setRemainingLength(String remainingLength) {
        this.remainingLength = remainingLength;
    }

    public String getSpanLength() {
        return spanLength;
    }

    public void setSpanLength(String spanLength) {
        this.spanLength = spanLength;
    }
}
