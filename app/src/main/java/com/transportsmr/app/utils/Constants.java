package com.transportsmr.app.utils;

/**
 * Created by kirill on 26.11.2016.
 */
public class Constants {
    //NETWORK RESOURCES
    public static final String STOPS_CLASSIFIER_URL = "http://tosamara.ru/api/classifiers/stopsFullDB.xml";
    public static final String ROUTES_CLASSIFIER_URL = "http://tosamara.ru/api/classifiers/routes.xml";
    public static final String LAST_UPDATES_URL = "http://tosamara.ru/api/classifiers";

    public static final int DEFAULT_DISTANCE = 500;
    public static final int NETWORK_TIMEOUT = 10000;
    public static final String TOSAMARA_PASSWORD = "J2QMMls";

    public static final int UPDATE_TRANSPORT_DELAY = 45000;

    //SHARED
    public static final String SHARED_NAME = "com.transportsmr.app.sharedPrefs";
    public static final String SHARED_STOPS_FILENAME = "stopsFullDB.xml";
    public static final String SHARED_ROUTES_FILENAME = "routes.xml";
    public static final String SHARED_ROUTES_AND_STOPS_FILENAME = "routesAndStopsCorrespondence.xml";
    public static final String SHARED_DISTANCE_SEARCH_STOPS = "distance_stops";
    //public static final String SHARED_STOPS_LAST_UPDATE = "stops_last_update";



}
