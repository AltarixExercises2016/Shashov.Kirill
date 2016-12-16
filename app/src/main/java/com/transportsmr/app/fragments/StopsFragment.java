package com.transportsmr.app.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transportsmr.app.MainActivity;
import com.transportsmr.app.R;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.adapters.StopsPagerAdapter;

public class StopsFragment extends Fragment implements MainActivity.OnBackPressedListener{
    public static final String SELECTED_TAB_KEY = "selected_tab";
    private TransportApp app;
    private TabLayout tabLayout;
    private StopsPagerAdapter stopsPagerAdapter;

    public StopsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (TransportApp) getActivity().getApplication();
    }

    public void updateFavoriteList() {
        stopsPagerAdapter.updateFavoriteList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stops, container, false);

        //neatest / favorite list
        tabLayout = (TabLayout) view.findViewById(R.id.stops_tabs);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        if (stopsPagerAdapter == null) {
            stopsPagerAdapter = new StopsPagerAdapter(getActivity(), app);
        }
        viewPager.setAdapter(stopsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if ((savedInstanceState != null) && (savedInstanceState.containsKey(SELECTED_TAB_KEY))) {
            tabLayout.getTabAt(savedInstanceState.getInt(SELECTED_TAB_KEY)).select();
        } else {
            tabLayout.getTabAt(0).select();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tabLayout != null) {
            outState.putInt(SELECTED_TAB_KEY, tabLayout.getSelectedTabPosition());
        }
    }

    @Override
    public void onBackPressed() {
        stopsPagerAdapter.updateFavoriteList();
    }
}
