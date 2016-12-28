package com.transportsmr.app.async;

import android.os.AsyncTask;
import com.transportsmr.app.model.DaoSession;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import com.transportsmr.app.utils.ClassifierUpdater;
import com.transportsmr.app.utils.Constants;
import com.transportsmr.app.utils.RoutesClassifierUpdater;
import com.transportsmr.app.utils.StopsClassifierUpdater;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Created by kirill on 27.11.2016.
 */
public class ClassifiersUpdateTask extends AsyncTask<Void, Void, Void> {
    private final DaoSession daoSession;
    private final UpdateTaskListener listener;
    private boolean isSuccessful;
    private HashMap<String, String> currentUpdateMap;
    private HashMap<String, String> lastUpdateMap;

    public ClassifiersUpdateTask(UpdateTaskListener listener, DaoSession daoSession, Map<String, String> currentUpdateMap) {
        this.listener = listener;
        this.daoSession = daoSession;
        this.currentUpdateMap = new HashMap(currentUpdateMap);
        this.lastUpdateMap = new HashMap();
        this.isSuccessful = true;
    }

    private XmlPullParser downloadXML(String path) throws IOException, XmlPullParserException {
        URL url = new URL(path);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(Constants.NETWORK_TIMEOUT);
        conn.setReadTimeout(Constants.NETWORK_TIMEOUT);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new InputStreamReader(conn.getInputStream()));
        return xpp;
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

        try {
            XmlPullParser parser = downloadXML(Constants.LAST_UPDATES_URL);

            String fileName = "";
            String text = "";

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase("file")) {
                            fileName = parser.getAttributeValue(0);
                        }
                        text = "";
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText().trim();
                        break;
                    case XmlPullParser.END_TAG:
                        if (tagname.equalsIgnoreCase("modified")) {
                            lastUpdateMap.put(fileName, text);
                        } else if (tagname.equalsIgnoreCase("file")) {
                            fileName = "";
                        } else {
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            lastUpdateMap.clear();
        }
    }

    private void restoreUpdateDate(String filename) {
        lastUpdateMap.put(filename, currentUpdateMap.get(filename));
    }

    @Override
    protected Void doInBackground(Void... params) {
        List<ClassifierUpdater> updaters = new ArrayList<>();
        updaters.add(new StopsClassifierUpdater(daoSession, Constants.SHARED_STOPS_FILENAME, Constants.STOPS_CLASSIFIER_URL));
        updaters.add(new RoutesClassifierUpdater(daoSession, Constants.SHARED_ROUTES_FILENAME, Constants.ROUTES_CLASSIFIER_URL));

        updateLastUpdateTime();
        for (ClassifierUpdater updater : updaters) {
            boolean isSuccess = true;
            if (!isOldClassifier(updater.getFileName())) {
                restoreUpdateDate(updater.getFileName());
            } else {
                try {
                    isSuccess = updater.update(downloadXML(updater.getClassifierUrl()));
                } catch (Exception ex) {
                    isSuccess = false;
                }

                if (!isSuccess) {
                    restoreUpdateDate(updater.getFileName());
                }
            }
            isSuccessful = isSuccess && isSuccessful;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        listener.onFinishUpdating(isSuccessful, lastUpdateMap);
    }


    public interface UpdateTaskListener {
        void onFinishUpdating(boolean isSuccessful, Map<String, String> lastUpdateMap);
    }

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
        listener.onFinishUpdating(false, lastUpdateMap);
    }
}
