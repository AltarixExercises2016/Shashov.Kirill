package com.transportsmr.app;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.transportsmr.app.model.DaoMaster;
import com.transportsmr.app.model.DaoSession;
import org.greenrobot.greendao.database.Database;

/**
 * Created by kirill on 25.11.2016.
 */
public class TransportApp extends Application {
    private DaoSession daoSession;
    private Database db;

    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "stops-db");
        db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }


    public DaoSession getDaoSession() {
        return daoSession;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void finish() {
        //db.close();
    }
}
