package com.transportsmr.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import com.transportsmr.app.fragments.UpdatingFragment;
import com.victor.loading.rotate.RotateLoading;

/**
 * Created by kirill on 24.11.2016.
 */
public class SplashScreenActivity extends AppCompatActivity implements UpdatingFragment.OnUpdatingListener {

    private UpdatingFragment taskFragment;
    private static final String TAG_TASK_FRAGMENT = "taskFragment";
    private AlertDialog dialog;
    private boolean isUpdating = false;
    private boolean isDialog = false;
    public static final String IS_UPDATING = "isUpdating";
    public static final String IS_DIALOG = "isDialog";
    private RotateLoading rotateLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        FragmentManager fm = getSupportFragmentManager();
        taskFragment = (UpdatingFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        if (taskFragment == null) {
            taskFragment = new UpdatingFragment();
            fm.beginTransaction().add(taskFragment, TAG_TASK_FRAGMENT).commit();
        }
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (rotateLoading == null) {
            rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);
        }

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
            rotateLoading.start();
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
            rotateLoading.stop();
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
                taskFragment.startUpdate();
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
        rotateLoading.start();
    }
}
