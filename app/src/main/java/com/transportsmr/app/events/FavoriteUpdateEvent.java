package com.transportsmr.app.events;

import com.transportsmr.app.model.Stop;

/**
 * Created by kirill on 30.12.2016.
 */
public class FavoriteUpdateEvent {
    public final Stop stop;
    public final boolean isFavorite;

    public FavoriteUpdateEvent(Stop stop, boolean isFavorite) {
        this.stop = stop;
        this.isFavorite = isFavorite;
    }
}
