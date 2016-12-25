package com.transportsmr.app.fragments;

import android.app.Activity;
import android.app.Application;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.EditorInfo;
import android.widget.FilterQueryProvider;
import com.transportsmr.app.FavoriteUpdaterListener;
import com.transportsmr.app.R;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.adapters.SearchAdapter;
import com.transportsmr.app.adapters.StopsRecyclerAdapter;
import com.transportsmr.app.fragments.base.BaseStopsRecyclerFragment;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;

import java.util.List;

public class StopsFragment extends Fragment implements FavoriteUpdaterListener {
    public static final String SEARCH_KEY = "SEARCH_KEY";
    private StopsPagerAdapter stopsPagerAdapter;
    private Activity context;
    private SearchAdapter searchAdapter;
    private SearchView searchBox;
    private StopsRecyclerAdapter.OnStopClickListener onStopClickListener;
    private TransportApp app;

    public StopsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();
        app = (TransportApp) context.getApplication();
        stopsPagerAdapter = new StopsPagerAdapter(getChildFragmentManager(), context.getApplication());
        searchAdapter = new SearchAdapter(context, null, true, app.getDaoSession());
    }

    public void setOnStopClickListener(StopsRecyclerAdapter.OnStopClickListener onStopClickListener) {
        this.onStopClickListener = onStopClickListener;
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

        searchBox = (SearchView) view.findViewById(R.id.search_box);
        int options = searchBox.getImeOptions();
        searchBox.setImeOptions(options | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                if ((constraint != null) && (constraint.length() != 0)) {
                    constraint = "%" + constraint.toString().toLowerCase() + "%";
                    Cursor dbList = ((TransportApp) getActivity().getApplication()).
                            getDaoSession().
                            getStopDao().
                            queryBuilder().
                            whereOr(StopDao.Properties.Title_lc.like(constraint.toString()), StopDao.Properties.AdjacentStreet_lc.like(constraint.toString())).
                            buildCursor().
                            forCurrentThread().
                            query();
                    return dbList;
                }

                return null;
            }
        });
        searchBox.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) searchAdapter.getItem(position);
                if (cursor != null) {
                    onStopClickListener.onStopClick(cursor.getString(1), cursor.getString(2));
                }
                return false;
            }
        });
        searchBox.setSuggestionsAdapter(searchAdapter);
        return view;
    }

    @Override
    public void setFavorite(Stop stopDirection, boolean favorite) {
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
