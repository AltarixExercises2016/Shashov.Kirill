package com.transportsmr.app.utils;

import com.transportsmr.app.model.DaoSession;

/**
 * Created by kirill on 28.12.16.
 */
public abstract class ClassifierUpdater {
    private final DaoSession daoSession;
    private final ToSamaraApi api;

    public ClassifierUpdater(DaoSession daoSession, ToSamaraApi api) {
        this.api = api;
        this.daoSession = daoSession;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public ToSamaraApi getApi() {
        return api;
    }

    public abstract String getFileName();

    public abstract boolean update();
}
