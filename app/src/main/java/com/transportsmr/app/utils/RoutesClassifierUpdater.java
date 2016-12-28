package com.transportsmr.app.utils;

import com.transportsmr.app.model.DaoSession;
import com.transportsmr.app.model.Route;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by kirill on 28.12.16.
 */
public class RoutesClassifierUpdater extends ClassifierUpdater<Route> {

    public RoutesClassifierUpdater(DaoSession daoSession, String fileName, String classifierUrl) {
        super(daoSession, fileName, classifierUrl);
    }

    @Override
    protected List<Route> parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        Route route = null;
        String text = "";
        ArrayList<Route> routes = new ArrayList<>();

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagname = parser.getName();

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (tagname.equalsIgnoreCase("route")) {
                        route = new Route();
                    }
                    text = "";
                    break;
                case XmlPullParser.TEXT:
                    text = parser.getText().trim();
                    break;

                case XmlPullParser.END_TAG:
                    if (tagname.equalsIgnoreCase("route")) {
                        routes.add(route);
                    } else if (tagname.equalsIgnoreCase("KR_ID")) {
                        route.setKr_id(text);
                    } else if (tagname.equalsIgnoreCase("number")) {
                        route.setNumber(text);
                    } else if (tagname.equalsIgnoreCase("transportType")) {
                        route.setTransportType(text);
                    } else if (tagname.equalsIgnoreCase("affiliationID")) {
                        route.setAffiliationID(text);
                    } else if (tagname.equalsIgnoreCase("affiliation")) {
                        route.setAffiliation(text);
                    } else {
                    }
                    break;

                default:
                    break;
            }
            eventType = parser.next();
        }
        return routes;
    }

    @Override
    protected void update(List<Route> list) {
        getDaoSession().getRouteDao().deleteAll();
        getDaoSession().getRouteDao().insertInTx(list);
    }

}
