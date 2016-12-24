package com.transportsmr.app.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.transportsmr.app.FavoriteUpdaterListener;
import com.transportsmr.app.R;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.adapters.StopsRecyclerAdapter;
import com.transportsmr.app.adapters.TransportRecyclerAdapter;
import com.transportsmr.app.async.DownloadArrivalForStopTask;
import com.transportsmr.app.model.ArrivalTransport;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import com.transportsmr.app.utils.Constants;

import java.io.Serializable;
import java.util.*;

public class ArrivalsFragment extends Fragment implements FavoriteUpdaterListener {
    private static final String STOP_KS_ID = "stopKsId";
    private static final String FILTER_STATE = "FILTER_STATE";
    private List<ArrivalTransport> transports;
    private Stop stop;
    private TransportRecyclerAdapter transportAdapter;
    private Activity context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private Timer timer;
    private TimerTask timerTask;
    private Filter filter;
    private Parcelable listState;
    private final String LIST_STATE_KEY = "list";
    private RecyclerView transportRecyclerView;
    private MaterialFavoriteButton fav;
    private FavoriteUpdaterListener favoriteChangeListener;


    public static ArrivalsFragment newInstance(String stopKsId) {
        ArrivalsFragment fragment = new ArrivalsFragment();
        Bundle args = new Bundle();
        args.putString(STOP_KS_ID, stopKsId);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstance(String stopKsId, Serializable filter) {
        ArrivalsFragment fragment = new ArrivalsFragment();
        Bundle args = new Bundle();
        args.putString(STOP_KS_ID, stopKsId);
        args.putSerializable(FILTER_STATE, filter);
        fragment.setArguments(args);
        return fragment;

    }

    public ArrivalsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        transports = new ArrayList<ArrivalTransport>();
        transportAdapter = new TransportRecyclerAdapter(context, transports);
        filter = new Filter(this);

        if (getArguments() != null) {
            String stopKsId = getArguments().getString(STOP_KS_ID);
            StopDao stopDao = ((TransportApp) getActivity().getApplication()).getDaoSession().getStopDao();
            stop = stopDao.queryBuilder().where(StopDao.Properties.Ks_id.eq(stopKsId)).list().get(0);
            if (getArguments().containsKey(FILTER_STATE)) {
                filter = (Filter) getArguments().getSerializable(FILTER_STATE);
                filter.setContext(this);
            }
        }

        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_arrivals, container, false);

        ((TextView) view.findViewById(R.id.stop_direction_title)).setText(stop.getTitle());
        ((TextView) view.findViewById(R.id.stop_direction_street)).setText(stop.getAdjacentStreet());
        ((TextView) view.findViewById(R.id.stop_direction_direction)).setText(stop.getDirection());

        fav = (MaterialFavoriteButton) view.findViewById(R.id.stop_direction_favorite);
        fav.setFavorite(stop.getFavorite(), false);
        fav.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
            @Override
            public void onFavoriteChanged(MaterialFavoriteButton materialFavoriteButton, boolean b) {
                if (favoriteChangeListener != null) {
                    favoriteChangeListener.setFavorite(stop, b);
                }
            }
        });

        emptyView = (TextView) view.findViewById(R.id.empty_view);
        transportRecyclerView = (RecyclerView) view.findViewById(R.id.rvItems);
        transportRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        transportRecyclerView.setAdapter(transportAdapter);
        transportRecyclerView.setNestedScrollingEnabled(false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);

        //filters
        if (savedInstanceState != null) {
            for (String key : filter.getFiltersMap().keySet()) {
                if (savedInstanceState.containsKey(key)) {
                    filter.getFiltersMap().put(key, savedInstanceState.getBoolean(key));
                }
            }
        }

        initFilter(view, R.id.arrival_filter_bus, filter.SHOW_BUS);
        initFilter(view, R.id.arrival_filter_tram, filter.SHOW_TRAM);
        initFilter(view, R.id.arrival_filter_metro, filter.SHOW_METRO);
        initFilter(view, R.id.arrival_filter_trolleybus, filter.SHOW_TROLLEYBUS);
        return view;
    }

    private void initFilter(View layout, int filterViewId, final String filterKey) {
        CheckBox isBusCheckBox = (CheckBox) layout.findViewById(filterViewId);
        isBusCheckBox.setChecked(filter.getFiltersMap().get(filterKey));
        isBusCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filter.getFiltersMap().put(filterKey, isChecked);
                createDownloadToTransportsTask().execute(stop.getKs_id()); //TODO update
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                createDownloadToTransportsTask().execute(stop.getKs_id());
            }
        });
        String srt = "";
        if (transports.isEmpty()) {
            createDownloadToTransportsTask().execute(stop.getKs_id());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    createDownloadToTransportsTask().execute(stop.getKs_id());
                }
            };
            timer.schedule(timerTask, Constants.UPDATE_TRANSPORT_DELAY, Constants.UPDATE_TRANSPORT_DELAY);
        } catch (IllegalStateException e) {
            //srt = e.toString(); TODO
        }
    }

    private DownloadArrivalForStopTask createDownloadToTransportsTask() {
        return new DownloadArrivalForStopTask() {
            @Override
            protected void onPreExecute() {
                if (context != null) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(true);
                        }
                    });
                }
            }

            @Override
            public void onPost(final List<ArrivalTransport> arrival) {
                if (context != null) {
                    transports.clear();
                    //TODO filter

                    transports.addAll(arrival);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            transportAdapter.getFilter().filter(filter.getFilterConstraint());
                            //transportAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            emptyView.setVisibility(arrival.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    });
                }
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.context = (Activity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement Activity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (timer != null) {
            timer.cancel();
        }
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (filter != null) {
            for (Map.Entry<String, Boolean> filterEntry : filter.getFiltersMap().entrySet()) {
                outState.putBoolean(filterEntry.getKey(), filterEntry.getValue());
            }
        }

        if (transportRecyclerView != null) {
            listState = transportRecyclerView.getLayoutManager().onSaveInstanceState();
            outState.putParcelable(LIST_STATE_KEY, listState);
        }
    }

    @Override
    public void setFavorite(Stop stopDirection, boolean favorite) {
        if (fav != null) {
            fav.setFavorite(favorite, false);
        }
    }

    public void setFavoriteChangeListener(FavoriteUpdaterListener favoriteChangeListener) {
        this.favoriteChangeListener = favoriteChangeListener;
    }

    public static class Filter implements Serializable {
        private static final String SHOW_BUS = "SHOW_BUS";
        private static final String SHOW_TRAM = "SHOW_TRAM";
        private static final String SHOW_METRO = "SHOW_METRO";
        private static final String SHOW_TROLLEYBUS = "SHOW_TROLLEYBUS";
        transient private Fragment context;
        private Map<String, Boolean> filtersMap;

        public Filter(Fragment context) {
            this.context = context;
            filtersMap = new HashMap<>();
            filtersMap.put(SHOW_BUS, true);
            filtersMap.put(SHOW_TRAM, true);
            filtersMap.put(SHOW_METRO, true);
            filtersMap.put(SHOW_TROLLEYBUS, true);
        }

        public Map<String, Boolean> getFiltersMap() {
            return filtersMap;
        }

        public String getFilterConstraint() {
            StringBuilder constraint = new StringBuilder();
            if (filtersMap.get(SHOW_TRAM)) {
                constraint.append(context.getString(R.string.tram));
            }
            if (filtersMap.get(SHOW_TROLLEYBUS)) {
                constraint.append(context.getString(R.string.troll));
            }
            if (filtersMap.get(SHOW_METRO)) {
                constraint.append(context.getString(R.string.metro));
            }
            if (filtersMap.get(SHOW_BUS)) {
                constraint.append(context.getString(R.string.bus));
            }
            return constraint.toString();
        }

        public void setContext(Fragment context) {
            this.context = context;
        }
    }

    public String getKS_ID() {
        return (stop != null) ? stop.getKs_id() : "1";
    }

    public Serializable getFilter() {
        return filter;
    }
}
