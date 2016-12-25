package com.transportsmr.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.transportsmr.app.FavoriteUpdaterListener;
import com.transportsmr.app.R;
import com.transportsmr.app.model.Stop;

import java.util.List;

/**
 * Created by kirill on 26.11.2016.
 */
public class StopsRecyclerAdapter extends RecyclerView.Adapter<StopsRecyclerAdapter.ViewHolder> {
    private boolean isFavorite;
    private FavoriteUpdaterListener favoriteChangeListener;
    private Context context;
    private List<StopWithDirections> stopsWithDirections;
    private OnStopClickListener onStopClickListener;

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

    public StopsRecyclerAdapter(Context context, List<StopWithDirections> dataset) {
        stopsWithDirections = dataset;
        this.context = context;
        isFavorite = false;
    }

    public void setOnStopClickListener(OnStopClickListener onStopClickListener) {
        this.onStopClickListener = onStopClickListener;
    }

    public void setOnFavoriteChangeListener(FavoriteUpdaterListener favoriteChangeListener) {
        this.favoriteChangeListener = favoriteChangeListener;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    @Override
    public StopsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_stop_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final StopWithDirections stop = stopsWithDirections.get(position);
        holder.title.setText(stop.getTitle());// + " " + stop.getMinDistance());
        holder.street.setText(stop.getAdjacentStreet());
        for (final Stop stopDirection : stop.getStops()) {
            final LinearLayout ll = (LinearLayout) LayoutInflater.from((Context) context).inflate(R.layout.stop_direction, null);
            final TextView stopDirectionTV = ((TextView) ll.findViewById(R.id.stop_direction_direction));
            stopDirectionTV.setText(stopDirection.getDirection());
            stopDirectionTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onStopClickListener != null)
                        onStopClickListener.onStopClick(stopDirection);
                }
            });
            MaterialFavoriteButton fav = ((MaterialFavoriteButton) ll.findViewById(R.id.stop_direction_favorite));
            fav.setFavorite(stopDirection.getFavorite(), false);
            fav.setOnFavoriteChangeListener(
                    new MaterialFavoriteButton.OnFavoriteChangeListener() {
                        @Override
                        public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                            if (isFavorite) {
                                if (stop.getStops().size() == 1) { // delete last direction = delete item in list
                                    stopsWithDirections.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, stopsWithDirections.size());
                                } else {
                                    stop.getStops().remove(stopDirection);
                                    holder.stopDirections.removeView(ll);
                                }
                            }
                            if (favoriteChangeListener != null)
                                favoriteChangeListener.setFavorite(stopDirection, favorite);
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

    public interface OnStopClickListener {
        void onStopClick(Stop stopDirection);
        void onStopClick(String ksId, String title);
    }
}
