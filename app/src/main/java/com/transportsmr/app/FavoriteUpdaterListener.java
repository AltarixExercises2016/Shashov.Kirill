package com.transportsmr.app;

import com.transportsmr.app.model.Stop;

/**
 * Created by kirill on 24.12.16.
 */
public interface FavoriteUpdaterListener {
    void setFavorite(Stop stopDirection, boolean favorite);
}
