package com.transportsmr.app.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.transportsmr.app.R;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.adapters.StopsRecyclerAdapter;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StopsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StopsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StopsFragment extends Fragment implements StopsRecyclerAdapter.FavoriteUpdaterListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private StopsRecyclerAdapter adapter;
    private ArrayList<StopsRecyclerAdapter.StopWithDirections> stopsWithDirections;
    private TransportApp app;
    //private SwipeRefreshLayout swipeContainer;
    //private MaterialFavoriteButton favoriteButton;
    private TabLayout tabLayout;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StopsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StopsFragment newInstance(String param1, String param2) {
        StopsFragment fragment = new StopsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public StopsFragment() {
        // Required empty public constructor
        stopsWithDirections = new ArrayList<StopsRecyclerAdapter.StopWithDirections>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        app = (TransportApp) getActivity().getApplication();

    }

    private void updateStops(boolean isFavorite) {
        if (isFavorite) {
            updateFavoriteStops();
        } else {
            updateNearestStops();
        }

        adapter.notifyDataSetChanged();
    }

    private void updateFavoriteStops() {
        List<Stop> stops = app.getDaoSession().getStopDao().queryBuilder().where(StopDao.Properties.Favorite.eq(true), new WhereCondition.StringCondition("1 GROUP BY TITLE, ADJACENT_STREET")).list();
        stopsWithDirections.clear();

        for (Stop stop : stops) {
            List<Stop> stopsDirections = app.getDaoSession().getStopDao().queryBuilder().where(StopDao.Properties.Favorite.eq(true), StopDao.Properties.AdjacentStreet.eq(stop.getAdjacentStreet()), StopDao.Properties.Title.eq(stop.getTitle())).list();
            if (stopsDirections.size() > 0) {
                stopsWithDirections.add(new StopsRecyclerAdapter.StopWithDirections(stop.getTitle(), stop.getAdjacentStreet(), -1, stopsDirections));
            }
        }

    }


    private void updateNearestStops() {
        List<Stop> stops;
        Location location = app.getCurrentLocation();
        stops = app.getDaoSession().getStopDao().queryBuilder().where(new WhereCondition.StringCondition("1 GROUP BY TITLE, ADJACENT_STREET")).list();

        stopsWithDirections.clear();
        for (Stop stop : stops) {
            List<Stop> stopsDirections = app.getDaoSession().getStopDao().queryBuilder().where(StopDao.Properties.AdjacentStreet.eq(stop.getAdjacentStreet()), StopDao.Properties.Title.eq(stop.getTitle())).list();
            if (location == null) {
                stopsWithDirections.add(new StopsRecyclerAdapter.StopWithDirections(stop.getTitle(), stop.getAdjacentStreet(), -1, stopsDirections));
            } else {
                float minDistance = 1000;
                for (Stop direction : stopsDirections) {
                    float[] distance = new float[1];
                    Location.distanceBetween(direction.getLatitude(), direction.getLongitude(), location.getLatitude(), location.getLongitude(), distance);
                    if (minDistance > distance[0]) {
                        minDistance = distance[0];
                    }
                }
                if (minDistance < 1000) {
                    stopsWithDirections.add(new StopsRecyclerAdapter.StopWithDirections(stop.getTitle(), stop.getAdjacentStreet(), (int) minDistance, stopsDirections));
                }
            }
        }

        if (location != null) {
            java.util.Collections.sort(stopsWithDirections, new Comparator<StopsRecyclerAdapter.StopWithDirections>() {

                @Override
                public int compare(StopsRecyclerAdapter.StopWithDirections stopWithDirections, StopsRecyclerAdapter.StopWithDirections t1) {
                    return ((Integer) stopWithDirections.getMinDistance()).compareTo(t1.getMinDistance());
                }

            });
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stops, container, false);
        // Inflate the layout for this fragment
        recyclerView = (RecyclerView) view.findViewById(R.id.rvItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplication());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new StopsRecyclerAdapter(getActivity(), this, stopsWithDirections);
        recyclerView.setAdapter(adapter);

        //neatest / favorite list
        /*favoriteButton = (MaterialFavoriteButton) view.findViewById(R.id.stops_favorite);
        final TextView isFavouriteListLabel = (TextView) view.findViewById(R.id.isFavouriteListLabel);
        favoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
            @Override
            public void onFavoriteChanged(MaterialFavoriteButton materialFavoriteButton, boolean b) {
                isFavouriteListLabel.setText(getString(b ? R.string.stops_favorite : R.string.stops_nearest));
                updateStops();
            }
        });*/

        tabLayout = (TabLayout) view.findViewById(R.id.stops_tabs);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.stops_nearest)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.stops_favorite)));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateStops(tab.getPosition() == 1);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                updateStops(tab.getPosition() == 1);
            }
        });
        tabLayout.getTabAt(0).select();

        //updateStops();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //Activity activity = (Activity) context;
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(mListener.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void setFavorite(Stop stopDirection, boolean favorite) {
        stopDirection.setFavorite(favorite);
        app.getDaoSession().getStopDao().update(stopDirection);

        if (tabLayout.getSelectedTabPosition() == 1) {
            updateStops(true);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //public ArrayList<StopsRecyclerAdapter.StopWithDirections> onFragmentInteraction(Uri uri);
    }

}
