package com.transportsmr.app.fragments;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.transportsmr.app.R;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.adapters.TransportRecyclerAdapter;
import com.transportsmr.app.async.ArrivalCallback;
import com.transportsmr.app.events.FavoriteUpdateEvent;
import com.transportsmr.app.model.ArrivalTransport;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import com.transportsmr.app.utils.BabushkaText;
import com.transportsmr.app.utils.Constants;
import com.transportsmr.app.utils.ToSamaraApi;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import retrofit2.Call;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ArrivalsFragment extends Fragment {
    private static final String STOP_KS_ID = "stopKsId";
    private static final String FILTER_STATE = "FILTER_STATE";
    private List<ArrivalTransport> transports;
    private Stop stop;
    private TransportRecyclerAdapter transportAdapter;
    private Timer timer;
    private TimerTask timerTask;
    private Filter filter;
    private Unbinder unbinder;
    private Call<ToSamaraApi.ArrivalResponse> responseCall;

    @BindView(R.id.stop_direction_favorite)
    MaterialFavoriteButton fav;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.rv_items)
    RecyclerView transportRecyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.stop_direction_title)
    TextView directionTitle;
    @BindView(R.id.stop_direction_street)
    TextView directionStreet;
    @BindView(R.id.stop_direction_direction)
    TextView directionDirection;

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
        transports = new ArrayList<>();
        transportAdapter = new TransportRecyclerAdapter(getContext(), getActivity().getApplication(), transports);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_arrivals, container, false);
        unbinder = ButterKnife.bind(this, view);
        directionTitle.setText(stop.getTitle());
        directionStreet.setText(stop.getAdjacentStreet());
        directionDirection.setText(stop.getDirection());
        fav.setFavorite(stop.getFavorite(), false);
        fav.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
            @Override
            public void onFavoriteChanged(MaterialFavoriteButton materialFavoriteButton, boolean b) {
                EventBus.getDefault().post(new FavoriteUpdateEvent(stop, b));
            }
        });

        transportRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transportRecyclerView.setAdapter(transportAdapter);
        transportRecyclerView.setNestedScrollingEnabled(false);

        if (savedInstanceState != null) {
            //filters
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

        final ImageSwitcher info_switcher = (ImageSwitcher) view.findViewById(R.id.info_switcher);
        final ExpandableLinearLayout expandableLinearLayout = (ExpandableLinearLayout) view.findViewById(R.id.arrival_expandableLayout);
        info_switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandableLinearLayout.isExpanded()) {
                    expandableLinearLayout.collapse();
                } else {
                    expandableLinearLayout.expand();
                }
                info_switcher.showNext();
            }
        });

        initTransportList(view, R.id.arrival_buses_municipal, stop.getBusesMunicipal(), getString(R.string.bus_municipal));
        initTransportList(view, R.id.arrival_buses_commercial, stop.getBusesCommercial(), getString(R.string.bus_commercial));
        initTransportList(view, R.id.arrival_buses_prigorod, stop.getBusesPrigorod(), getString(R.string.bus_prigorod));
        initTransportList(view, R.id.arrival_buses_season, stop.getBusesSeason(), getString(R.string.bus_season));
        initTransportList(view, R.id.arrival_buses_special, stop.getBusesSpecial(), getString(R.string.bus_special));
        initTransportList(view, R.id.arrival_trams, stop.getTrams(), getString(R.string.tram));
        initTransportList(view, R.id.arrival_trolleybuses, stop.getTrolleybuses(), getString(R.string.troll));
        initTransportList(view, R.id.arrival_metros, stop.getMetros(), getString(R.string.metro));

        return view;
    }

    private void initFilter(View layout, int filterViewId, final String filterKey) {
        CheckBox isBusCheckBox = ButterKnife.findById(layout, filterViewId);
        isBusCheckBox.setChecked(filter.getFiltersMap().get(filterKey));
        isBusCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filter.getFiltersMap().put(filterKey, isChecked);
                updateArrival(stop.getKs_id());
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateArrival(stop.getKs_id());
            }
        });
    }

    private void initTransportList(View layout, int textViewId, String text, String label) {
        BabushkaText textView = (BabushkaText) layout.findViewById(textViewId);
        if ((text == null) || text.isEmpty()) {
            textView.setVisibility(View.GONE);
            return;
        }
        textView.reset();
        textView.addPiece(new BabushkaText.Piece.Builder(label + "\n")
                .textSize((int) getContext().getResources().getDimension(R.dimen.material_text_caption))
                .style(Typeface.BOLD)
                .textColor(getResources().getColor(R.color.backgr1))
                .build()
        );

        textView.addPiece(new BabushkaText.Piece.Builder(text)
                .textSize((int) getContext().getResources().getDimension(R.dimen.material_text_body1))
                .textColor(Color.WHITE)
                .build()
        );

        textView.display();
        textView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (transports.isEmpty()) {
            updateArrival(stop.getKs_id());
        }

        try {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    updateArrival(stop.getKs_id());
                }
            };
            timer.schedule(timerTask, Constants.UPDATE_TRANSPORT_DELAY, Constants.UPDATE_TRANSPORT_DELAY);
        } catch (IllegalStateException e) {
            Toast.makeText(getContext(), getString(R.string.autoRefreshFailed), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateArrival(String ksid) {
        updateArrival(ksid, 30);
    }

    private void updateArrival(String ksid, int count) {
        String sha = "";
        try {
            sha = sha1(ksid + count + Constants.TOSAMARA_PASSWORD);
        } catch (NoSuchAlgorithmException e) {
            return;
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
        responseCall = ((TransportApp) getActivity().getApplication()).getApi().getArrival("getFirstArrivalToStop", ksid, count, "android", "envoy93", sha.toLowerCase());
        responseCall.enqueue(new ArrivalCallback() {
            @Override
            protected void OnPost(final ArrayList<ArrivalTransport> arrival) {
                transports.clear();
                transports.addAll(arrival);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (swipeRefreshLayout != null) {
                                transportAdapter.getFilter().filter(filter.getFilterConstraint());
                                swipeRefreshLayout.setRefreshing(false);
                                emptyView.setVisibility(arrival.isEmpty() ? View.VISIBLE : View.GONE);
                            }
                        }
                    });
                }
            }
        });
    }

    private String sha1(String s) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        digest.reset();
        byte[] data = digest.digest(s.getBytes());
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 2)
    public void onFavoriteChange(FavoriteUpdateEvent event) {
        if (fav != null) {
            if (event.stop.equals(stop)) {
                fav.setFavorite(event.isFavorite, false);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (filter != null) {
            for (Map.Entry<String, Boolean> filterEntry : filter.getFiltersMap().entrySet()) {
                outState.putBoolean(filterEntry.getKey(), filterEntry.getValue());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        if (responseCall != null) {
            responseCall.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
        if (timerTask != null) {
            timerTask.cancel();
        }
        super.onStop();
    }

    public Serializable getFilter() {
        return filter;
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
}
