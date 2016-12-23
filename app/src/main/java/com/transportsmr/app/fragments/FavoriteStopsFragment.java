package com.transportsmr.app.fragments;

import com.transportsmr.app.TransportApp;
import com.transportsmr.app.adapters.StopsRecyclerAdapter;
import com.transportsmr.app.fragments.base.BaseStopsRecyclerFragment;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirill on 20.12.16.
 */
public class FavoriteStopsFragment extends BaseStopsRecyclerFragment {

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
        recyclerAdapter.setFavorite(true);
    }

    @Override
    protected List<StopsRecyclerAdapter.StopWithDirections> getStops() {
        List<StopsRecyclerAdapter.StopWithDirections> directionsList = new ArrayList<>();
        TransportApp app = (TransportApp) getActivity().getApplication();
        List<Stop> stops = app.getDaoSession().getStopDao().queryBuilder().where(StopDao.Properties.Favorite.eq(true), new WhereCondition.StringCondition("1 GROUP BY TITLE, ADJACENT_STREET")).list();
        //favoriteStopsWithDirections.clear();

        for (Stop stop : stops) {
            List<Stop> stopsDirections = app.getDaoSession().getStopDao().queryBuilder().where(StopDao.Properties.Favorite.eq(true), StopDao.Properties.AdjacentStreet.eq(stop.getAdjacentStreet()), StopDao.Properties.Title.eq(stop.getTitle())).list();
            if (stopsDirections.size() > 0) {
                directionsList.add(new StopsRecyclerAdapter.StopWithDirections(stop.getTitle(), stop.getAdjacentStreet(), -1, stopsDirections));
            }
        }
        return directionsList;
    }

    @Override
    public void onFavoriteChanged() {
        //if (!isFavoriteTab) {
        updateStops();
        //}
    }
}
