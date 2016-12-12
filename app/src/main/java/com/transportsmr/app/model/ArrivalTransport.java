package com.transportsmr.app.model;

import java.util.List;

/**
 * Created by kirill on 12.12.2016.
 */
public class ArrivalTransport {
    private String type;
    private String number;
    private String time;
    private List<Transport> transports;

    public ArrivalTransport() {
    }

    public ArrivalTransport(String number, String type,  String time, List<Transport> transports) {
        this.type = type;
        this.number = number;
        this.time = time;
        this.transports = transports;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<Transport> getTransports() {
        return transports;
    }

    public void setTransports(List<Transport> transports) {
        this.transports = transports;
    }
}
