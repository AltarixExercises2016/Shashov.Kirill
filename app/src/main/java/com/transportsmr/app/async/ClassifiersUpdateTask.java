package com.transportsmr.app.async;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.transportsmr.app.model.DaoSession;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import com.transportsmr.app.utils.Constants;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        this.currentUpdateMap = new HashMap<String, String>(currentUpdateMap);
        this.lastUpdateMap = new HashMap<String, String>();
        this.isSuccessful = true;
    }

    private XmlPullParser downloadXML(String path) throws IOException, XmlPullParserException {
        URL url = new URL(path);
        URLConnection conn = url.openConnection();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new InputStreamReader(conn.getInputStream()));
        return xpp;
    }

    private void parseStops(XmlPullParser parser) throws XmlPullParserException, IOException {
        Stop stop = null;
        String text = "";
        ArrayList<Stop>  stops = new ArrayList<Stop>();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagname = parser.getName();

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (tagname.equalsIgnoreCase("stop")) {
                        stop = new Stop();
                    }
                    text = "";
                    break;
                case XmlPullParser.TEXT:
                    text = parser.getText().trim();
                    break;

                case XmlPullParser.END_TAG:
                    if (tagname.equalsIgnoreCase("stop")) {
                        stops.add(stop);
                    } else if (tagname.equalsIgnoreCase("KS_ID")) {
                        stop.setKs_id(text);
                    } else if (tagname.equalsIgnoreCase("title")) {
                        stop.setTitle(text);
                    } else if (tagname.equalsIgnoreCase("adjacentStreet")) {
                        stop.setAdjacentStreet(text);
                    } else if (tagname.equalsIgnoreCase("direction")) {
                        stop.setDirection(text);
                    } else if (tagname.equalsIgnoreCase("latitude")) {
                        if (text.isEmpty()) {
                            stop.setLatitude(0.0f);
                        } else {
                            stop.setLatitude(Float.parseFloat(text));
                        }
                    } else if (tagname.equalsIgnoreCase("longitude")) {
                        if (text.isEmpty()) {
                            stop.setLongitude(0.0f);
                        } else {
                            stop.setLongitude(Float.parseFloat(text));
                        }
                    } else if (tagname.equalsIgnoreCase("busesMunicipal")) {
                        stop.setBusesMunicipal(text);
                    } else if (tagname.equalsIgnoreCase("busesCommercial")) {
                        stop.setBusesCommercial(text);
                    } else if (tagname.equalsIgnoreCase("busesPrigorod")) {
                        stop.setBusesPrigorod(text);
                    } else if (tagname.equalsIgnoreCase("busesSeason")) {
                        stop.setBusesSeason(text);
                    } else if (tagname.equalsIgnoreCase("busesSpecial")) {
                        stop.setBusesSpecial(text);
                    } else if (tagname.equalsIgnoreCase("trams")) {
                        stop.setTrams(text);
                    } else if (tagname.equalsIgnoreCase("trolleybuses")) {
                        stop.setTrolleybuses(text);
                    } else if (tagname.equalsIgnoreCase("metros")) {
                        stop.setMetros(text);
                    } else {
                    }
                    break;

                default:
                    break;
            }
            eventType = parser.next();
        }

        StopDao stopDao = daoSession.getStopDao();
        stopDao.deleteAll();
        stopDao.insertInTx(stops);
    }

    private boolean isOldStopsClassifier() {
        if (lastUpdateMap.containsKey(Constants.SHARED_STOPS_FILENAME) && !lastUpdateMap.get(Constants.SHARED_STOPS_FILENAME).equals(currentUpdateMap.get(Constants.SHARED_STOPS_FILENAME))) {
            return true;
        } else if (!lastUpdateMap.containsKey(Constants.SHARED_STOPS_FILENAME)) {
            isSuccessful = false;
        }

        return false;
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

    @Override
    protected Void doInBackground(Void... params) {
        updateLastUpdateTime();
        if (!isOldStopsClassifier()) {
            lastUpdateMap.put(Constants.SHARED_STOPS_FILENAME, currentUpdateMap.get(Constants.SHARED_STOPS_FILENAME));
        } else {
            try {
                parseStops(downloadXML(Constants.STOPS_CLASSIFIER_URL));
            } catch (Exception e) {
                isSuccessful = false;
                lastUpdateMap.put(Constants.SHARED_STOPS_FILENAME, currentUpdateMap.get(Constants.SHARED_STOPS_FILENAME));
            }
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
}
