package com.transportsmr.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.transportsmr.app.R;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import com.transportsmr.app.utils.Constants;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by kirill on 10.12.2016.
 */
public class StopsPagerAdapter extends PagerAdapter implements StopsRecyclerAdapter.FavoriteUpdaterListener {
    public static final int NEAREST_TAB_POSITION = 0;
    private final TransportApp app;
    private final int distance;
    private ArrayList<StopsRecyclerAdapter.StopWithDirections> favoriteStopsWithDirections;
    private ArrayList<StopsRecyclerAdapter.StopWithDirections> nearestStopsWithDirections;
    private Context context;
    private StopsRecyclerAdapter favoriteAdapter;
    private StopsRecyclerAdapter nearestAdapter;

    public StopsPagerAdapter(Context context, TransportApp app) {
        this.context = context;
        this.app = app;
        SharedPreferences sPref = app.getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE);
        distance = sPref.getInt(Constants.SHARED_DISTANCE_SEARCH_STOPS, Constants.DEFAULT_DISTANCE);

        favoriteStopsWithDirections = new ArrayList<>();
        nearestStopsWithDirections = new ArrayList<>();


        nearestAdapter = new StopsRecyclerAdapter((StopsRecyclerAdapter.StopClickListener) this.context, this, nearestStopsWithDirections);
        nearestAdapter.setFavorite(false);
        favoriteAdapter = new StopsRecyclerAdapter((StopsRecyclerAdapter.StopClickListener) this.context, this, favoriteStopsWithDirections);
        favoriteAdapter.setFavorite(true);

        updateStops(true);
        updateStops(false);
    }

    public void updateFavoriteList(){
        updateStops(true);
    }

    @Override
    public Object instantiateItem(ViewGroup collection, final int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.stops_pager_item, collection, false);

        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.rvItems);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refresh);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        if (position == NEAREST_TAB_POSITION) {
            recyclerView.setAdapter(nearestAdapter);
        } else {
            recyclerView.setAdapter(favoriteAdapter);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateStops(position != NEAREST_TAB_POSITION, new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return (position == NEAREST_TAB_POSITION) ? app.getString(R.string.stops_nearest) : app.getString(R.string.stops_favorite);
    }

    @Override
    public void setFavorite(final Stop stopDirection, final boolean favorite, final boolean isFavoriteTab) {
        stopDirection.setFavorite(favorite);
        app.getDaoSession().getStopDao().update(stopDirection);
        updateStops(!isFavoriteTab); //update another tab
    }

    private void runOnUiThread(Runnable runnable) {
        ((Activity) context).runOnUiThread(runnable);
    }

    private void updateStops(final boolean isFavoriteTab) {
        updateStops(isFavoriteTab, null);
    }

    private void updateStops(final boolean isFavoriteTab, final Runnable postExecute) {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                if (isFavoriteTab) {
                    updateAdapterInUiThread(getFavoriteStops(), favoriteStopsWithDirections, favoriteAdapter);
                } else {
                    updateAdapterInUiThread(getNearestStops(), nearestStopsWithDirections, nearestAdapter);
                }

                if (postExecute != null) {
                    runOnUiThread(postExecute);
                }
            }
        })).start();
    }

    private void updateAdapterInUiThread(final List<StopsRecyclerAdapter.StopWithDirections> source,
                                         final List<StopsRecyclerAdapter.StopWithDirections> data,
                                         final StopsRecyclerAdapter adapter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                data.clear();
                data.addAll(source);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private List<StopsRecyclerAdapter.StopWithDirections> getFavoriteStops() {
        List<StopsRecyclerAdapter.StopWithDirections> directionsList = new ArrayList<>();
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


    private List<StopsRecyclerAdapter.StopWithDirections> getNearestStops() {
        List<StopsRecyclerAdapter.StopWithDirections> directionsList = new ArrayList<>();
        Location location = app.getCurrentLocation();
        List<Stop> stops = app.getDaoSession().getStopDao().queryBuilder().where(new WhereCondition.StringCondition("1 GROUP BY TITLE, ADJACENT_STREET")).list();

        //nearestStopsWithDirections.clear();
        for (Stop stop : stops) {
            List<Stop> stopsDirections = app.getDaoSession().getStopDao().queryBuilder().where(StopDao.Properties.AdjacentStreet.eq(stop.getAdjacentStreet()), StopDao.Properties.Title.eq(stop.getTitle())).list();
            if (location == null) {
                directionsList.add(new StopsRecyclerAdapter.StopWithDirections(stop.getTitle(), stop.getAdjacentStreet(), -1, stopsDirections));
            } else {
                float minDistance = distance + 1;
                for (Stop direction : stopsDirections) {
                    float[] distance = new float[1];
                    Location.distanceBetween(direction.getLatitude(), direction.getLongitude(), location.getLatitude(), location.getLongitude(), distance);
                    if (minDistance > distance[0]) {
                        minDistance = distance[0];
                        break;
                    }
                }
                if (minDistance <= distance) {
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

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
