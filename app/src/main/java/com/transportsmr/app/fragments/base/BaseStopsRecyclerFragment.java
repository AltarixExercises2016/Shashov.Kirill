package com.transportsmr.app.fragments.base;

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
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.transportsmr.app.R;
import com.transportsmr.app.adapters.StopsRecyclerAdapter;
import com.transportsmr.app.events.FavoriteUpdateEvent;
import com.transportsmr.app.utils.Constants;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirill on 20.12.16.
 */
public abstract class BaseStopsRecyclerFragment extends Fragment {
    private int distance;
    private ArrayList<StopsRecyclerAdapter.StopWithDirections> stopsList;
    private StopsRecyclerAdapter recyclerAdapter;
    private Parcelable listState;
    private final String LIST_STATE_KEY = "list";
    private Unbinder unbinder;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_items)
    RecyclerView recyclerView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        SharedPreferences sPref = getContext().getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE);
        distance = sPref.getInt(Constants.SHARED_DISTANCE_SEARCH_STOPS, Constants.DEFAULT_DISTANCE);
        stopsList = new ArrayList<>();
        recyclerAdapter = new StopsRecyclerAdapter(getContext(), stopsList); //new StopsRecyclerAdapter(this.context, this, stopsList);
        initRecyclerAdapter(recyclerAdapter);
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }
    }

    protected void initRecyclerAdapter(StopsRecyclerAdapter recyclerAdapter) {
    }

    protected abstract List<StopsRecyclerAdapter.StopWithDirections> getStops();

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 2)
    public abstract void onChangeFavorite(FavoriteUpdateEvent event);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_stops_pager, container, false);
        unbinder = ButterKnife.bind(this, layout);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
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
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(postExecute);
                    }
                }
            }
        })).start();
    }

    private void updateAdapterInUiThread(final List<StopsRecyclerAdapter.StopWithDirections> source) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopsList.clear();
                    stopsList.addAll(source);
                    recyclerAdapter.notifyDataSetChanged();
                }
            });
        }
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listState != null) {
                                recyclerView.getLayoutManager().onRestoreInstanceState(listState);
                            }
                        }
                    });
                }
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

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
