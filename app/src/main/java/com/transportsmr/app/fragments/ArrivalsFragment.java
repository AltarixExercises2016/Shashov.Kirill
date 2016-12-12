package com.transportsmr.app.fragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;
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
            StopDao stopDao = app.getDaoSession().getStopDao();
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
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_arrivals, container, false);

        TextView stopStreet = (TextView) view.findViewById(R.id.stop_direction_street);
        stopStreet.setText(stop.getAdjacentStreet());
        TextView stopDirection = (TextView) view.findViewById(R.id.stop_direction_direction);
        stopDirection.setText(stop.getDirection());

        final RecyclerView transportRecycler = (RecyclerView) view.findViewById(R.id.rvItems);

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        transportRecycler.setLayoutManager(layoutManager);

        transportRecycler.setAdapter(transportAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                (new DownloadArrivalForStopTask() {

                    @Override
                    public void onPost(List<ArrivalTransport> arrival) {
                        transports.clear();
                        transports.addAll(arrival);
                        transportAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }).execute(stop.getKs_id());
            }
        });

        if (transports.isEmpty()) {
            (new DownloadArrivalForStopTask() {
                @Override
                protected void onPreExecute() {
                    swipeRefreshLayout.setRefreshing(true);
                }

                @Override
                public void onPost(List<ArrivalTransport> arrival) {
                    transports.addAll(arrival);
                    transportAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }).execute(stop.getKs_id());
        }
        return view;
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
