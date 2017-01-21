package com.transportsmr.app.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.transportsmr.app.TransportApp;
import com.transportsmr.app.async.ClassifiersUpdateTask;
import com.transportsmr.app.utils.Constants;

import java.util.Map;

/**
 * Created by kirill on 27.11.2016.
 */
public class UpdatingFragment extends Fragment {
    private TransportApp app;
    private OnUpdatingListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = (OnUpdatingListener) getActivity();
        app = (TransportApp) getActivity().getApplication();
        startUpdate();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    private void updateUpdateTime(Map<String, String> lastUpdateMap) {
        SharedPreferences sp = app.getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE);
        for (Map.Entry<String, String> entry : lastUpdateMap.entrySet()) {
            sp.edit().putString(entry.getKey(), entry.getValue());
        }
    }

    public void onFinishUpdating(boolean isSuccessful, Map<String, String> lastUpdateMap) {
        listener.onFinishUpdating(isSuccessful);
        if (isSuccessful) updateUpdateTime(lastUpdateMap);
        //if (isSuccessful) updateUpdateTime(lastUpdateMap); TODO delete comment
    }

    public void startUpdate() {
        listener.onHaveUpdate();
        if (app.isOnline()) {
            (new ClassifiersUpdateTask(app) {
                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    onFinishUpdating(getIsSuccessful(), getLastUpdateMap());
                }
            }).execute();
        } else {
            listener.onFinishUpdating(false);
        }
    }

    public interface OnUpdatingListener {

        void onFinishUpdating(boolean isSuccessful);

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
