package com.transportsmr.app.async;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.utils.*;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kirill on 27.11.2016.
 */
public class ClassifiersUpdateTask extends AsyncTask<Void, Void, Void> {
    private final TransportApp app;
    private boolean isSuccessful;
    private Map<String, String> currentUpdateMap;
    private Map<String, String> lastUpdateMap;

    public ClassifiersUpdateTask(TransportApp app) {
        this.app = app;
        this.lastUpdateMap = new HashMap<>();
        this.isSuccessful = true;
        initCurrentUpdateMap();
    }

    private void initCurrentUpdateMap() {
        SharedPreferences sp = app.getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE);
        currentUpdateMap = new HashMap<String, String>();
        currentUpdateMap.put(Constants.SHARED_ROUTES_AND_STOPS_FILENAME, sp.getString(Constants.SHARED_ROUTES_AND_STOPS_FILENAME, "0"));
        currentUpdateMap.put(Constants.SHARED_ROUTES_FILENAME, sp.getString(Constants.SHARED_ROUTES_FILENAME, "0"));
        currentUpdateMap.put(Constants.SHARED_STOPS_FILENAME, sp.getString(Constants.SHARED_STOPS_FILENAME, "0"));
    }

    private boolean isOldClassifier(String filename) {
        if (!lastUpdateMap.containsKey(filename)) {
            isSuccessful = false;
            return false;
        }

        return lastUpdateMap.containsKey(filename)
                && !lastUpdateMap.get(filename).equals(currentUpdateMap.get(filename));
    }

    private void updateLastUpdateTime() {
        if (!lastUpdateMap.isEmpty()) {
            return;
        }

        Response<ToSamaraApi.ClassifiersResponse> response;
        try {
            response = app.getApi().getClassifiersUpdate().execute();
        } catch (IOException e) {
            return;
        }

        if (response.body() == null) {
            return;
        }

        for (ToSamaraApi.File file : response.body().getFiles()) {
            lastUpdateMap.put(file.getName(), file.getModified());
        }
    }

    private void restoreUpdateDate(String filename) {
        lastUpdateMap.put(filename, currentUpdateMap.get(filename));
    }

    @Override
    protected Void doInBackground(Void... params) {
        List<ClassifierUpdater> updaters = new ArrayList<>();
        updaters.add(new StopsClassifierUpdater(app.getDaoSession(), app.getApi()));
        updaters.add(new RoutesClassifierUpdater(app.getDaoSession(), app.getApi()));

        updateLastUpdateTime();
        for (ClassifierUpdater updater : updaters) {
            boolean isSuccess = true;
            if (!isOldClassifier(updater.getFileName())) {
                restoreUpdateDate(updater.getFileName());
            } else {
                isSuccess = updater.update();
                if (!isSuccess) {
                    restoreUpdateDate(updater.getFileName());
                }
            }
            isSuccessful = isSuccess && isSuccessful;
        }
        return null;
    }

    protected boolean getIsSuccessful() {
        return isSuccessful;
    }

    protected Map<String, String> getLastUpdateMap() {
        return lastUpdateMap;
    }
}
