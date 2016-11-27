package com.transportsmr.app.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.async.ClassifiersUpdateTask;
import com.transportsmr.app.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kirill on 27.11.2016.
 */
public class UpdatingFragment extends Fragment implements ClassifiersUpdateTask.UpdateTaskListener {
    private ClassifiersUpdateTask updateTask;
    private TransportApp app;
    private OnUpdatingListener listener;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = (OnUpdatingListener) getActivity();
        app = (TransportApp) getActivity().getApplication();
        sp = app.getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE);
        setRetainInstance(true);

        listener.onHaveUpdate();
        if (app.isOnline()) {
            HashMap<String, String> currentUpdateMap = new HashMap<String, String>();
            currentUpdateMap.put(Constants.SHARED_ROUTES_AND_STOPS_FILENAME, sp.getString(Constants.SHARED_ROUTES_AND_STOPS_FILENAME, "0"));
            currentUpdateMap.put(Constants.SHARED_ROUTES_FILENAME, sp.getString(Constants.SHARED_ROUTES_FILENAME, "0"));
            currentUpdateMap.put(Constants.SHARED_STOPS_FILENAME, sp.getString(Constants.SHARED_STOPS_FILENAME, "0"));
            updateTask = new ClassifiersUpdateTask(this, app.getDaoSession(), currentUpdateMap);
            updateTask.execute();
        } else {
            listener.onFinishUpdating();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void updateUpdateTime(HashMap<String, String> lastUpdateMap) {
        for (Map.Entry<String, String> entry : lastUpdateMap.entrySet()) {
            sp.edit().putString(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void onFinishUpdating(boolean isSuccessful, Map<String, String> lastUpdateMap) {
        //updateUpdateTime((HashMap<String, String>) lastUpdateMap);
        if (!isSuccessful) {
            listener.onFinishUpdating();
        } else {
            Toast.makeText(app, "Обновление не удалось", Toast.LENGTH_LONG);
            listener.onFinishUpdating();             //TODO mb restart
        }
    }

    public interface OnUpdatingListener {
        void onFinishUpdating();

        void onHaveUpdate();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnUpdatingListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(listener.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
