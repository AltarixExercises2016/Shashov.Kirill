package com.transportsmr.app;

import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.Toast;
import com.transportsmr.app.fragments.StopsFragment;
import com.transportsmr.app.model.Stop;

import java.util.List;


public class MainActivity extends AppCompatActivity implements StopsFragment.OnFragmentInteractionListener {

    private DrawerLayout dLayout;
    private TransportApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (TransportApp) getApplication();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.ic_action_white_hamburger);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dLayout.openDrawer(GravityCompat.START);
            }
        });

        View view = toolbar.getChildAt(1);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dLayout.openDrawer(GravityCompat.START);
            }
        });
        setNavigationDrawer();
    }

    private void setNavigationDrawer() {
        dLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        NavigationView navView = (NavigationView) findViewById(R.id.navigation);
        navView.setItemIconTintList(null);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                Fragment fragment = null;
                int itemId = menuItem.getItemId();

                if (itemId == R.id.stops) {
                    fragment = new StopsFragment();
                    Toast.makeText(getApplication(), getPreferences(MODE_PRIVATE).getString("STOPS_LAST_UPDATE", "2"), Toast.LENGTH_LONG).show();
                } else if (itemId == R.id.settings) {

                } else if (itemId == R.id.exit) {
                    app.finish();
                    finish();
                }

                if (fragment != null) {
                    openFragment(fragment);
                    dLayout.closeDrawers();
                    return true;
                }

                return false;
            }
        });

        //on_startup
        navView.setCheckedItem(R.id.stops);
        openFragment(new StopsFragment());
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    public String[] onFragmentInteraction(Uri uri) {
        String[] list = new String[50];
        List<Stop> stops = app.getDaoSession().getStopDao().loadAll();
        for (int i = 0; i < 50; i++) {
            list[i] = stops.get(i).getAdjacentStreet();
        }
        return list;
    }
}
