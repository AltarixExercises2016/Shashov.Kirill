package com.transportsmr.app.fragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.transportsmr.app.R;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.adapters.TransportRecyclerAdapter;
import com.transportsmr.app.async.DownloadArrivalForStopTask;
import com.transportsmr.app.model.ArrivalTransport;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import com.transportsmr.app.model.Transport;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ArrivalsFragment extends Fragment {
    private static final String STOP_KS_ID = "stopKsId";

    private List<ArrivalTransport> transports;
    private String stopKsId;
    private TransportApp app;
    private Stop stop;
    private TransportRecyclerAdapter transportAdapter;
    private Context context;
    private StopDao stopDao;


    public static ArrivalsFragment newInstance(String stopKsId) {
        ArrivalsFragment fragment = new ArrivalsFragment();
        Bundle args = new Bundle();
        args.putString(STOP_KS_ID, stopKsId);
        fragment.setArguments(args);
        return fragment;
    }

    public ArrivalsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        app = ((TransportApp) getActivity().getApplication());
        transports = new ArrayList<ArrivalTransport>();
        transportAdapter = new TransportRecyclerAdapter(context, transports);

        if (getArguments() != null) {
            stopKsId = getArguments().getString(STOP_KS_ID);
            stopDao = app.getDaoSession().getStopDao();
            stop = stopDao.queryBuilder().where(StopDao.Properties.Ks_id.eq(stopKsId)).list().get(0);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_arrivals, container, false);

        TextView stopTitle = (TextView) view.findViewById(R.id.stop_direction_title);
        stopTitle.setText(stop.getTitle());
        TextView stopStreet = (TextView) view.findViewById(R.id.stop_direction_street);
        stopStreet.setText(stop.getAdjacentStreet());
        TextView stopDirection = (TextView) view.findViewById(R.id.stop_direction_direction);
        stopDirection.setText(stop.getDirection());
        MaterialFavoriteButton fav = (MaterialFavoriteButton) view.findViewById(R.id.stop_direction_favorite);
        fav.setFavorite(stop.getFavorite(), false);
        fav.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
            @Override
            public void onFavoriteChanged(MaterialFavoriteButton materialFavoriteButton, boolean b) {
                stop.setFavorite(b);
                stopDao.update(stop);
            }
        });

        final TextView emptyView = (TextView) view.findViewById(R.id.empty_view);
        final RecyclerView transportRecycler = (RecyclerView) view.findViewById(R.id.rvItems);
        transportRecycler.setLayoutManager(new LinearLayoutManager(context));
        transportRecycler.setAdapter(transportAdapter);
        transportRecycler.setNestedScrollingEnabled(false);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                (createDownloadToTransportsTask(swipeRefreshLayout, emptyView)).execute(stop.getKs_id());
            }
        });

        if (transports.isEmpty()) {
            (createDownloadToTransportsTask(swipeRefreshLayout, emptyView)).execute(stop.getKs_id());
        }
        return view;
    }

    private DownloadArrivalForStopTask createDownloadToTransportsTask(final SwipeRefreshLayout swipeRefreshLayout, final View emptyView){
        return new DownloadArrivalForStopTask() {
            @Override
            protected void onPreExecute() {
                swipeRefreshLayout.setRefreshing(true);
            }
            @Override
            public void onPost(List<ArrivalTransport> arrival) {
                transports.clear();
                transports.addAll(arrival);
                transportAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                emptyView.setVisibility(arrival.isEmpty() ? View.VISIBLE : View.GONE);
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.context = context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }
}
