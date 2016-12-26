package com.transportsmr.app.fragments.base;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.transportsmr.app.FavoriteUpdaterListener;
import com.transportsmr.app.R;
import com.transportsmr.app.adapters.StopsRecyclerAdapter;
import com.transportsmr.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirill on 20.12.16.
 */
public abstract class BaseStopsRecyclerFragment extends Fragment {
    private int distance;
    private ArrayList<StopsRecyclerAdapter.StopWithDirections> stopsList;
    private StopsRecyclerAdapter recyclerAdapter;
    private Activity context;
    private RecyclerView recyclerView;
    private Parcelable listState;
    private final String LIST_STATE_KEY = "list";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();
        SharedPreferences sPref = context.getApplication().getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE);
        distance = sPref.getInt(Constants.SHARED_DISTANCE_SEARCH_STOPS, Constants.DEFAULT_DISTANCE);
        stopsList = new ArrayList<>();
        recyclerAdapter = new StopsRecyclerAdapter(context, stopsList); //new StopsRecyclerAdapter(this.context, this, stopsList);
        initRecyclerAdapter(recyclerAdapter);
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }
    }

    protected void initRecyclerAdapter(StopsRecyclerAdapter recyclerAdapter) {
        if (getActivity() != null) {
            try {
                recyclerAdapter.setOnFavoriteChangeListener((FavoriteUpdaterListener) getActivity());
            } catch (ClassCastException ex) {
                throw new ClassCastException(getActivity().toString()
                        + " must implement StopsRecyclerAdapter.FavoriteUpdaterListener");
            }
        }
        recyclerAdapter.setOnStopClickListener((StopsRecyclerAdapter.OnStopClickListener) getActivity());
    }

    protected abstract List<StopsRecyclerAdapter.StopWithDirections> getStops();

    public abstract void onFavoriteChanged();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_stops_pager, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.rvItems);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refresh);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateStops(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        return layout;
    }

    protected void updateStops() {
        updateStops(null);
    }

    protected void updateStops(final Runnable postExecute) {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                List<StopsRecyclerAdapter.StopWithDirections> stops = getStops();
                updateAdapterInUiThread(stops);

                if (postExecute != null) {
                    context.runOnUiThread(postExecute);
                }
            }
        })).start();
    }

    private void updateAdapterInUiThread(final List<StopsRecyclerAdapter.StopWithDirections> source) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopsList.clear();
                stopsList.addAll(source);
                recyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    public int getDistance() {
        return distance;
    }

    public ArrayList<StopsRecyclerAdapter.StopWithDirections> getStopsList() {
        return stopsList;
    }

    public StopsRecyclerAdapter getRecyclerAdapter() {
        return recyclerAdapter;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (recyclerView != null) {
            listState = recyclerView.getLayoutManager().onSaveInstanceState();
            outState.putParcelable(LIST_STATE_KEY, listState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateStops(new Runnable() {
            @Override
            public void run() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listState != null) {
                            recyclerView.getLayoutManager().onRestoreInstanceState(listState);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (recyclerView != null) {
            listState = recyclerView.getLayoutManager().onSaveInstanceState();
        }
    }
}
