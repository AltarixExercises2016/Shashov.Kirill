package com.transportsmr.app.fragments;

import android.location.Location;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.adapters.StopsRecyclerAdapter;
import com.transportsmr.app.fragments.base.BaseStopsRecyclerFragment;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by kirill on 20.12.16.
 */
public class NearestStopsFragment extends BaseStopsRecyclerFragment {

    @Override
    protected void initRecyclerAdapter(StopsRecyclerAdapter recyclerAdapter) {
        if (getParentFragment() != null) {
            try {
                recyclerAdapter.setOnFavoriteChangeListener((StopsRecyclerAdapter.FavoriteUpdaterListener) getActivity());
            } catch (ClassCastException ex) {
                throw new ClassCastException(getActivity().toString()
                        + " must implement StopsRecyclerAdapter.FavoriteUpdaterListener");
            }
        }
        recyclerAdapter.setOnStopClickListener((StopsRecyclerAdapter.StopClickListener) getContext());
        recyclerAdapter.setFavorite(false);
    }

    @Override
    protected List<StopsRecyclerAdapter.StopWithDirections> getStops() {
        List<StopsRecyclerAdapter.StopWithDirections> directionsList = new ArrayList<>();
        TransportApp app = (TransportApp) getActivity().getApplication();
        Location location = app.getCurrentLocation();
        List<Stop> stops = app.getDaoSession().getStopDao().queryBuilder().where(new WhereCondition.StringCondition("1 GROUP BY TITLE, ADJACENT_STREET")).list();

        //nearestStopsWithDirections.clear();
        for (Stop stop : stops) {
            List<Stop> stopsDirections = app.getDaoSession().getStopDao().queryBuilder().where(StopDao.Properties.AdjacentStreet.eq(stop.getAdjacentStreet()), StopDao.Properties.Title.eq(stop.getTitle())).list();
            if (location == null) {
                directionsList.add(new StopsRecyclerAdapter.StopWithDirections(stop.getTitle(), stop.getAdjacentStreet(), -1, stopsDirections));
            } else {
                float minDistance = getDistance() + 1;
                for (Stop direction : stopsDirections) {
                    float[] distance = new float[1];
                    Location.distanceBetween(direction.getLatitude(), direction.getLongitude(), location.getLatitude(), location.getLongitude(), distance);
                    if (minDistance > distance[0]) {
                        minDistance = distance[0];
                        break;
                    }
                }
                if (minDistance <= getDistance()) {
                    directionsList.add(new StopsRecyclerAdapter.StopWithDirections(stop.getTitle(), stop.getAdjacentStreet(), (int) minDistance, stopsDirections));
                }
            }
        }

        if (location != null) {
            java.util.Collections.sort(directionsList, new Comparator<StopsRecyclerAdapter.StopWithDirections>() {
                @Override
                public int compare(StopsRecyclerAdapter.StopWithDirections stopWithDirections, StopsRecyclerAdapter.StopWithDirections t1) {
                    return ((Integer) stopWithDirections.getMinDistance()).compareTo(t1.getMinDistance());
                }
            });
        }

        return directionsList;
    }

    @Override
    public void onFavoriteChanged() {
        //if (isFavoriteTab) {
        updateStops();
        //}
    }


}
