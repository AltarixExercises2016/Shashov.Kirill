package com.transportsmr.app.utils;

import com.transportsmr.app.model.DaoSession;
import com.transportsmr.app.model.Route;
import retrofit2.Response;

import java.util.List;

/**
 * Created by kirill on 28.12.16.
 */
public class RoutesClassifierUpdater extends ClassifierUpdater {

    public RoutesClassifierUpdater(DaoSession daoSession, ToSamaraApi api) {
        super(daoSession, api);
    }

    @Override
    public String getFileName() {
        return Constants.SHARED_ROUTES_FILENAME;
    }

    @Override
    public boolean update() {
        Response<ToSamaraApi.RoutesResponse> response = null;
        try {
            response = getApi().getRoutes().execute();
        } catch (Exception e) {
            return false;
        }

        if (response.body() == null) {
            return false;
        }

        List<Route> routes = response.body().getRoutes();
        if (routes != null && !routes.isEmpty()) {
            getDaoSession().getRouteDao().deleteAll();
            getDaoSession().getRouteDao().insertInTx(routes);
            return true;
        }
        return false;
    }
}
