package com.transportsmr.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import com.transportsmr.app.fragments.UpdatingFragment;

/**
 * Created by kirill on 24.11.2016.
 */
public class SplashScreenActivity extends AppCompatActivity implements UpdatingFragment.OnUpdatingListener {
    //TODO print message while updating {MAJOR}
    //TODO update design and last refactoring
    private AlertDialog alert;
    private UpdatingFragment mTaskFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (UpdatingFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        if (mTaskFragment == null) {
            mTaskFragment = new UpdatingFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onHaveUpdate();
    }

    @Override
    public void onFinishUpdating() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onHaveUpdate() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.update_info));
        progressDialog.setTitle(getString(R.string.update));
        progressDialog.show();
    }
}
