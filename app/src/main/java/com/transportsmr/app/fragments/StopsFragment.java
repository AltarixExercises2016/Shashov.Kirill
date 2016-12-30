package com.transportsmr.app.fragments;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.transportsmr.app.R;
import com.transportsmr.app.events.FavoriteUpdateEvent;
import com.transportsmr.app.fragments.base.BaseStopsRecyclerFragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class StopsFragment extends Fragment {
    private StopsPagerAdapter stopsPagerAdapter;
    private Activity context;

    public StopsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        stopsPagerAdapter = new StopsPagerAdapter(getChildFragmentManager(), context.getApplication());
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stops, container, false);

        //neatest / favorite list
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(stopsPagerAdapter);
        PagerTitleStrip pagerTabStrip = (PagerTitleStrip) view.findViewById(R.id.stops_pager_strip_title);
        pagerTabStrip.setTextColor(Color.BLACK);

        return view;
    }



    @Subscribe(threadMode = ThreadMode.MAIN,priority = 2)
    public void onChangeFavorite(FavoriteUpdateEvent event) {
        if (stopsPagerAdapter != null) {
            stopsPagerAdapter.onFavoriteChanged();
        }
    }


    public static class StopsPagerAdapter extends FragmentPagerAdapter {
        public static final int NEAREST_TAB_POSITION = 0;
        private final Application app;
        private BaseStopsRecyclerFragment favoriteFragment;
        private BaseStopsRecyclerFragment nearestFragment;

        public StopsPagerAdapter(FragmentManager fm, Application app) {
            super(fm);
            this.app = app;
        }

        @Override
        public Fragment getItem(int position) {
            return (position == NEAREST_TAB_POSITION) ?
                    (nearestFragment = new NearestStopsFragment())
                    : (favoriteFragment = new FavoriteStopsFragment());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            BaseStopsRecyclerFragment fragment = (BaseStopsRecyclerFragment) super.instantiateItem(container, position);
            if (position == NEAREST_TAB_POSITION)
                nearestFragment = fragment;
            else
                favoriteFragment = fragment;
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return (position == NEAREST_TAB_POSITION) ? app.getString(R.string.stops_nearest) : app.getString(R.string.stops_favorite);
        }

        public void onFavoriteChanged() {
            if (nearestFragment != null) {
                nearestFragment.onFavoriteChanged();
            }

            if (favoriteFragment != null) {
                favoriteFragment.onFavoriteChanged();
            }
        }
    }
}
