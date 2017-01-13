package com.transportsmr.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Transport {
    @SerializedName("number")
    @Expose
    private String number;
    @SerializedName("KR_ID")
    @Expose
    private String kRID;
    @SerializedName("modelTitle")
    @Expose
    private String modelTitle;
    @SerializedName("hullNo")
    @Expose
    private String hullNo;
    @SerializedName("nextStopId")
    @Expose
    private String nextStopId;
    @SerializedName("timeInSeconds")
    @Expose
    private String timeInSeconds;
    @SerializedName("stateNumber")
    @Expose
    private String stateNumber;
    @SerializedName("forInvalid")
    @Expose
    private String forInvalid;
    @SerializedName("nextStopName")
    @Expose
    private String nextStopName;
    @SerializedName("requestedStopId")
    @Expose
    private String requestedStopId;
    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("spanLength")
    @Expose
    private String spanLength;
    @SerializedName("remainingLength")
    @Expose
    private String remainingLength;

    public String getNumber() {
        return number;
    }

    public String getkRID() {
        return kRID;
    }

    public String getModelTitle() {
        return modelTitle;
    }

    public String getHullNo() {
        return hullNo;
    }

    public String getNextStopId() {
        return nextStopId;
    }

    public String getTimeInSeconds() {
        return timeInSeconds;
    }

    public String getStateNumber() {
        return stateNumber;
    }

    public String getForInvalid() {
        return forInvalid;
    }

    public String getNextStopName() {
        return nextStopName;
    }

    public String getRequestedStopId() {
        return requestedStopId;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public String getSpanLength() {
        return spanLength;
    }

    public String getRemainingLength() {
        return remainingLength;
    }
}