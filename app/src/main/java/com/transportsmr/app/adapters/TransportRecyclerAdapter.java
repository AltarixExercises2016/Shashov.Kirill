package com.transportsmr.app.adapters;

import android.app.Application;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.transportsmr.app.R;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.model.ArrivalTransport;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.Transport;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kirill on 12.12.2016.
 */
public class TransportRecyclerAdapter extends RecyclerView.Adapter<TransportRecyclerAdapter.ViewHolder> implements Filterable {
    private final TransportApp app;
    private Context context;
    private List<ArrivalTransport> arrival;
    private Filter transportFilter;

    public TransportRecyclerAdapter(Context context, Application application, List<ArrivalTransport> dataset) {
        this.app = application instanceof TransportApp ? (TransportApp) application : null;
        arrival = dataset;
        this.context = context;
    }

    @Override
    public Filter getFilter() {
        if (transportFilter == null)
            transportFilter = new TransportFilter(this, arrival);
        return transportFilter;
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
        final List<Transport> transports = arrivalTransport.getTransports();
        holder.number.setText(arrivalTransport.getNumber());

        holder.number.setBackground(ContextCompat.getDrawable(context, getBackgroundFromType(arrivalTransport.getType())));
        holder.type.setText(arrivalTransport.getType());
        ForegroundColorSpan fcs = null;
        SpannableStringBuilder sb = new SpannableStringBuilder(" ");
        int colorPosition = 0;
        for (int i = 0; (i < transports.size()) && (i < 5); i++) {
            colorPosition = sb.length();
            sb.append(" ").append(transports.get(i).getTime()).append(", ");

            if (app != null && app.getRoutes().containsKey(transports.get(i).getKR_ID())) {
                if (app.getRoutes().get(transports.get(i).getKR_ID()).getAffiliationID().equals("2")) {
                    //commercial
                    if (fcs == null) {
                        fcs = new ForegroundColorSpan(app.getResources().getColor(R.color.darkred));
                    }
                    sb.setSpan(fcs, colorPosition, sb.length() - 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                }
            }
        }
        sb.delete(sb.length() - 2, sb.length() - 1);
        holder.nextTime.setText(sb);
        holder.nextStop.setText(transports.get(0).getRemainingLength() + context.getString(R.string.length_to) + transports.get(0).getNextStopName());
        holder.time.setText(transports.get(0).getTime() + context.getString(R.string.arrival_minute));
    }

    private int getBackgroundFromType(String type) {
        if (type.equals(context.getString(R.string.tram))) {
            return R.drawable.bg_red;
        } else if (type.equals(context.getString(R.string.metro))) {
            return R.drawable.bg_gray;
        } else if (type.equals(context.getString(R.string.troll))) {
            return R.drawable.bg_green;
        }

        return R.drawable.bg_blue;
    }

    @Override
    public int getItemCount() {
        return arrival.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView time;
        private TextView nextStop;
        private TextView nextTime;
        private TextView number;
        private TextView type;

        public ViewHolder(View v) {
            super(v);
            number = (TextView) v.findViewById(R.id.transport_number);
            type = (TextView) v.findViewById(R.id.transport_type);
            nextTime = (TextView) v.findViewById(R.id.transport_next_time);
            time = (TextView) v.findViewById(R.id.transport_time);
            nextStop = (TextView) v.findViewById(R.id.transport_next_stop);
        }

    }

    public static class TransportFilter extends Filter {

        private final TransportRecyclerAdapter adapter;

        private final List<ArrivalTransport> originalList;

        private final List<ArrivalTransport> filteredList;

        private TransportFilter(TransportRecyclerAdapter adapter, List<ArrivalTransport> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                for (final ArrivalTransport transport : originalList) {
                    if (constraint.toString().contains(transport.getType())) {
                        filteredList.add(transport);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.arrival.clear();
            adapter.arrival.addAll((ArrayList<ArrivalTransport>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}
