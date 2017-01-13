package com.transportsmr.app;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.transportsmr.app.model.DaoMaster;
import com.transportsmr.app.model.DaoSession;
import com.transportsmr.app.model.Route;
import com.transportsmr.app.utils.ToSamaraApi;
import org.greenrobot.greendao.database.Database;
import retrofit2.Retrofit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kirill on 25.11.2016.
 */
public class TransportApp extends Application {
    private DaoSession daoSession;
    private Database db;
    private Map<String, Route> routes;
    private ToSamaraApi api;

    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "stops-db");
        db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

        //cache routes
        routes = new HashMap<String, Route>();
        List<Route> list = getDaoSession().getRouteDao().loadAll();
        for (Route route : list) {
            routes.put(route.getKr_id(), route);
        }

        api = new Retrofit.Builder().
                baseUrl("http://tosamara.ru").
                addConverterFactory(new ToSamaraApi.RetrofitUniversalConverter()).
                build().
                create(ToSamaraApi.class);
    }

    public ToSamaraApi getApi() {
        return api;
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

    public Location getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc == null || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else {
                loc = null;
            }
        }

        return loc;
    }

    public Map<String, Route> getRoutes() {
        return routes;
    }

    public void finish() {
        //db.close();
    }
}
