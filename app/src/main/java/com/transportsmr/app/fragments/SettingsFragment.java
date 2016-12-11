package com.transportsmr.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import com.transportsmr.app.R;
import com.transportsmr.app.utils.Constants;

/**
 * Created by kirill on 10.12.2016.
 */
public class SettingsFragment  extends Fragment {
    private SharedPreferences sPref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPref = getActivity().getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        DiscreteSeekBar distanceSB = (DiscreteSeekBar) view.findViewById(R.id.distanceSeekBar);


        distanceSB.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean b) {
                SharedPreferences.Editor ed = sPref.edit();
                ed.putInt(Constants.SHARED_DISTANCE_SEARCH_STOPS, i);
                ed.commit();
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }
        });

        distanceSB.setProgress(sPref.getInt(Constants.SHARED_DISTANCE_SEARCH_STOPS, Constants.DEFAULT_DISTANCE));
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
