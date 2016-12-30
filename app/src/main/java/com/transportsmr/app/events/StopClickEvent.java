package com.transportsmr.app.events;

import com.transportsmr.app.model.Stop;

/**
 * Created by kirill on 30.12.2016.
 */
public class StopClickEvent {
    public final Stop stop;

    public StopClickEvent(Stop stop) {
        this.stop = stop;
    }
}
