package com.transportsmr.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.transportsmr.app.R;
import com.transportsmr.app.model.ArrivalTransport;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.Transport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirill on 12.12.2016.
 */
public class TransportRecyclerAdapter extends RecyclerView.Adapter<TransportRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<ArrivalTransport> arrival;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nextTime;
        private TextView number;
        public TextView type;

        public ViewHolder(View v) {
            super(v);
            number = (TextView) v.findViewById(R.id.transport_number);
            type = (TextView) v.findViewById(R.id.transport_type);
            nextTime = (TextView) v.findViewById(R.id.transport_next_time);
        }

    }

    public TransportRecyclerAdapter(Context context, List<ArrivalTransport> dataset) {
        arrival = dataset;
        this.context = context;
    }

    @Override
    public TransportRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_transport_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ArrivalTransport arrivalTransport = arrival.get(position);
        holder.number.setText(arrivalTransport.getNumber());
        holder.type.setText(arrivalTransport.getType());
        String time = context.getString(R.string.arrival_time_to_next);
        for (Transport transport: arrivalTransport.getTransports()){
            time +=  " " + transport.getTime();
        }
        time += " " + context.getString(R.string.arrival_minute);
        holder.nextTime.setText(time);
    }


    @Override
    public int getItemCount() {
        return arrival.size();
    }
}
