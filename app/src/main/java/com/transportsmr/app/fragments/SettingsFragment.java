package com.transportsmr.app.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.transportsmr.app.R;
import com.transportsmr.app.utils.Constants;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * Created by kirill on 10.12.2016.
 */
public class SettingsFragment extends Fragment {
    private SharedPreferences sPref;
    private Unbinder unbinder;

    @BindView(R.id.distance_seekbar)
    DiscreteSeekBar distanceSB;
    @BindView(R.id.settings_commercial_spinner)
    Spinner spinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPref = getActivity().getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        unbinder = ButterKnife.bind(this, view);

        distanceSB.setProgress(sPref.getInt(Constants.SHARED_DISTANCE_SEARCH_STOPS, Constants.DEFAULT_DISTANCE));
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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.settings_commercial_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt(getString(R.string.settings_commercial));
        spinner.setAdapter(adapter);
        spinner.setSelection(sPref.getInt(Constants.SHARED_MARK_COMMERCIAL, Constants.DEFAULT_MARK_COMMERCIAL));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor ed = sPref.edit();
                ed.putInt(Constants.SHARED_MARK_COMMERCIAL, position);
                ed.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
