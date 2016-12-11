package com.transportsmr.app.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;
import com.transportsmr.app.R;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import org.w3c.dom.Text;

public class ArrivalsFragment extends Fragment {
    private static final String STOP_KS_ID = "stopKsId";

    private String stopKsId;
    private TransportApp app;
    private Stop stop;


    public static ArrivalsFragment newInstance(String stopKsId) {
        ArrivalsFragment fragment = new ArrivalsFragment();
        Bundle args = new Bundle();
        args.putString(STOP_KS_ID, stopKsId);
        fragment.setArguments(args);
        return fragment;
    }

    public ArrivalsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = ((TransportApp) getActivity().getApplication());
        if (getArguments() != null) {
            stopKsId = getArguments().getString(STOP_KS_ID);
            StopDao stopDao = app.getDaoSession().getStopDao();
            stop = stopDao.queryBuilder().where(StopDao.Properties.Ks_id.eq(stopKsId)).list().get(0);
        }
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
        return view;
    }


}
