package com.transportsmr.app.async;

import com.transportsmr.app.model.ArrivalTransport;
import com.transportsmr.app.model.Transport;
import com.transportsmr.app.utils.ToSamaraApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.*;

/**
 * Created by kirill on 13.01.17.
 */
public abstract class ArrivalCallback implements Callback<ToSamaraApi.ArrivalResponse> {
    final ArrayList<ArrivalTransport> arrival = new ArrayList<ArrivalTransport>();

    @Override
    public void onResponse(Call<ToSamaraApi.ArrivalResponse> call, Response<ToSamaraApi.ArrivalResponse> response) {
        Map<String, ArrayList<Transport>> transports = new HashMap<>();
        List<Transport> list = response.body().getArrival();
        for (Transport transport : list) {
            String key = transport.getNumber() + transport.getType();
            if (!transports.containsKey(key)) {
                transports.put(key, new ArrayList<Transport>());
            }
            transports.get(key).add(transport);
        }

        for (Map.Entry<String, ArrayList<Transport>> entry : transports.entrySet()) {
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

        OnPost(arrival);
    }

    @Override
    public void onFailure(Call<ToSamaraApi.ArrivalResponse> call, Throwable t) {
        OnPost(arrival);
    }

    protected abstract void OnPost(final ArrayList<ArrivalTransport> arrival);
}
