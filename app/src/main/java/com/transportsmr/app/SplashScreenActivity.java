package com.transportsmr.app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
    //TODO update design
    private UpdatingFragment mTaskFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private ProgressDialog progress;
    private AlertDialog dialog;
    private boolean isUpdating = false;
    private boolean isDialog = false;
    public static final String IS_UPDATING = "isUpdating";
    public static final String IS_DIALOG = "isDialog";

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
    protected void onRestart() {
        super.onRestart();
        if (isUpdating) {
            onHaveUpdate();
        }

        if (isDialog) {
            onShowDialog();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isUpdating) {
            progress.dismiss();
        }
        if (isDialog) {
            dialog.dismiss();
        }

        outState.putBoolean(IS_UPDATING, isUpdating);
        outState.putBoolean(IS_DIALOG, isDialog);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.getBoolean(IS_UPDATING)) {
            onHaveUpdate();
        }

        if (savedInstanceState.getBoolean(IS_DIALOG)) {
            onShowDialog();
        }
    }

    @Override
    public void onFinishUpdating(boolean isSuccessful) {
        if (isUpdating) {
            progress.dismiss();
            isUpdating = false;
        }

        if (isSuccessful) {
            startApp();
        } else {
            onShowDialog();
        }
    }

    private void startApp() {
        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void onShowDialog() {
        isDialog = true;
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(getString(R.string.update_failed));
        adb.setMessage(getString(R.string.update_again));
        adb.setCancelable(false);
        adb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mTaskFragment.startUpdate();
            }
        });
        adb.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startApp();
            }
        });
        dialog = adb.create();
        dialog.show();
    }

    @Override
    public void onHaveUpdate() {
        isUpdating = true;
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getString(R.string.update_info));
        progress.setTitle(getString(R.string.update));
        progress.show();
    }
}
