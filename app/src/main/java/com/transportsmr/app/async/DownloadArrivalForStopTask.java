package com.transportsmr.app.async;

import android.os.AsyncTask;
import com.transportsmr.app.model.ArrivalTransport;
import com.transportsmr.app.model.Transport;
import com.transportsmr.app.utils.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by kirill on 12.12.2016.
 */
public abstract class DownloadArrivalForStopTask extends AsyncTask<String, Void, List<ArrivalTransport>> {

    private String downloadJson(String ks_id) {
        String output = "";
        try {
            String urlPath = "http://tosamara.ru/api/json?method=getFirstArrivalToStop&KS_ID=" +
                    ks_id + "&COUNT=30&os=android&clientid=envoy93&" +
                    "authkey=" + sha1(ks_id + 30 +  Constants.TOSAMARA_PASSWORD).toLowerCase();
            URL url = new URL(urlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(Constants.NETWORK_TIMEOUT);
            urlConnection.setReadTimeout(Constants.NETWORK_TIMEOUT);
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                output = sb.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            output = "";
        }

        return output;
    }

    private String sha1(String s) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        digest.reset();
        byte[] data = digest.digest(s.getBytes());
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    public static Map<String, ArrayList<Transport>> parseTransport(String json) throws JSONException {
        Map<String, ArrayList<Transport>> transports = new HashMap<>();
        JSONObject jObj = new JSONObject(json);
        if (!jObj.has("arrival")) {
            return transports;
        }

        JSONArray jArr = jObj.getJSONArray("arrival");
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject tr = jArr.getJSONObject(i);
            Transport transport = new Transport();
            transport.setId(tr.getString("KR_ID"));
            transport.setModelTitle(tr.getString("modelTitle"));
            transport.setNextStopName(tr.getString("nextStopName"));
            transport.setNumber(tr.getString("number"));
            transport.setRemainingLength(String.valueOf(new Float(tr.getString("remainingLength")).intValue()));
            transport.setSpanLength(tr.getString("spanLength"));
            transport.setStateNumber(tr.getString("stateNumber"));
            transport.setTime(tr.getString("time"));
            transport.setTimeInSeconds(tr.getString("timeInSeconds"));
            transport.setType(tr.getString("type"));
            transport.setForInvalid(tr.getBoolean("forInvalid"));
            transport.setHullNo(tr.getString("hullNo"));

            String key = transport.getNumber() + transport.getType();
            if (!transports.containsKey(key)) {
                transports.put(key, new ArrayList<Transport>());
            }
            transports.get(key).add(transport);
        }

        return transports;
    }

    @Override
    protected List<ArrivalTransport> doInBackground(String... params) {
        ArrayList<ArrivalTransport> arrival = new ArrayList<ArrivalTransport>();

        String response = downloadJson(params[0]);

        if ((response == null) || response.isEmpty()) {
            return arrival; //TODO mb error message?
        }
        Map<String, ArrayList<Transport>> transports;
        try {
            transports = parseTransport(response);
        } catch (JSONException e) {
            return arrival; //TODO mb error message?
        }

        for (Map.Entry<String, ArrayList<Transport>> entry: transports.entrySet()){
            Transport firstTransport = entry.getValue().get(0);
            ArrivalTransport arrivalTransport = new ArrivalTransport(firstTransport.getNumber(), firstTransport.getType(), firstTransport.getTime(), entry.getValue());
            arrival.add(arrivalTransport);
        }

        java.util.Collections.sort(arrival, new Comparator<ArrivalTransport>() {

            @Override
            public int compare(ArrivalTransport arrivalTransport, ArrivalTransport t1) {
                return (Integer.valueOf(arrivalTransport.getTime())).compareTo(Integer.valueOf(t1.getTime()));
            }
        });

        return arrival;
    }

    @Override
    protected void onPostExecute(List<ArrivalTransport> transports) {
        onPost(transports);
    }

    public abstract void onPost(List<ArrivalTransport> transports);
}