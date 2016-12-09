package com.transportsmr.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.transportsmr.app.R;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.fragments.StopsFragment;
import com.transportsmr.app.model.Stop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kirill on 26.11.2016.
 */
public class StopsRecyclerAdapter extends RecyclerView.Adapter<StopsRecyclerAdapter.ViewHolder> {
    public static class StopWithDirections {
        private String title;
        private int minDistance;
        private String adjacentStreet;
        private List<Stop> stops;

        public StopWithDirections(String title, String adjacentStreet, int minDistance, List<Stop> stops) {
            this.title = title;
            this.stops = stops;
            this.minDistance = minDistance;
            this.adjacentStreet = adjacentStreet;
        }

        public String getTitle() {
            return title;
        }

        public int getMinDistance() {
            return minDistance;
        }

        public List<Stop> getStops() {
            return stops;
        }

        public String getAdjacentStreet() {
            return adjacentStreet;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView street;
        private LinearLayout stopDirections;
        public TextView title;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.stopTitle);
            street = (TextView) v.findViewById(R.id.stop_direction_street);
            stopDirections = (LinearLayout) v.findViewById(R.id.stopDirections);
        }

    }
    private FavoriteUpdaterListener listener;
    private Context context;
    private ArrayList<StopWithDirections> stopsWithDirections;

    public StopsRecyclerAdapter(Context context, FavoriteUpdaterListener listener, ArrayList<StopWithDirections> dataset) {
        stopsWithDirections = dataset;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public StopsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_stop_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final StopWithDirections stop = stopsWithDirections.get(position);
        holder.title.setText(stop.getTitle() + " " + stop.getMinDistance());
        holder.street.setText(stop.getAdjacentStreet());
        for (final Stop stopDirection: stop.getStops()) {
            LinearLayout ll = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.stop_direction, null);
            ((TextView) ll.findViewById(R.id.stop_direction_direction)).setText(stopDirection.getDirection());
            MaterialFavoriteButton fav = ((MaterialFavoriteButton) ll.findViewById(R.id.stop_direction_favorite));
            fav.setFavorite(stopDirection.getFavorite(), false);
            fav.setOnFavoriteChangeListener(
                    new MaterialFavoriteButton.OnFavoriteChangeListener() {
                        @Override
                        public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                            listener.setFavorite(stopDirection, favorite);
                        }
                    });
            holder.stopDirections.addView(ll);
        }

    }


    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.stopDirections.removeAllViews();
    }

    @Override
    public int getItemCount() {
        return stopsWithDirections.size();
    }

    public interface FavoriteUpdaterListener{
        void setFavorite(Stop stopDirection, boolean favorite);
    }
}
