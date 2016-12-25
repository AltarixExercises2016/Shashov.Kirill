package com.transportsmr.app;

import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import com.transportsmr.app.adapters.StopsRecyclerAdapter;
import com.transportsmr.app.fragments.ArrivalsFragment;
import com.transportsmr.app.fragments.SettingsFragment;
import com.transportsmr.app.fragments.StopsFragment;
import com.transportsmr.app.model.Stop;

import java.io.Serializable;


public class MainActivity extends AppCompatActivity implements StopsRecyclerAdapter.OnStopClickListener, FavoriteUpdaterListener {
    //public static final String CURRENT_FRAGMENT_KEY = "content";
    public static final String CURRENT_TITLE_KEY = "title";
    private TransportApp app;
    //private Fragment content;
    private DrawerLayout drawerLayout;
    private static final String FRAGMENT_RIGHT = "FRAGMENT_RIGHT";
    private static final String FRAGMENT_LEFT = "FRAGMENT_LEFT";
    private static final String LAST_FRAGMENT_KEY = "LAST_FRAGMENT_KEY";
    private static final String LAST_ARRIVAL_KEY = "LAST_ARRIVAL_KEY";
    private static final String LAST_ARRIVAL_FILTER_KEY = "LAST_ARRIVAL_FILTER_KEY";
    private String lastContainerKey;
    private String lastArrival = "";
    private NavigationView navView;
    private Serializable arrivalFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResources().getIdentifier("main_layout", "layout", getPackageName())); //R.layout.activity_main));

        app = (TransportApp) getApplication();
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navView = getNavigationView();

        if ((savedInstanceState != null)) {
            //content = getSupportFragmentManager().getFragment(savedInstanceState, CURRENT_FRAGMENT_KEY);\
            String title = savedInstanceState.containsKey(CURRENT_TITLE_KEY) ? savedInstanceState.getString(CURRENT_TITLE_KEY) : null;
            lastArrival = savedInstanceState.containsKey(LAST_ARRIVAL_KEY) ? savedInstanceState.getString(LAST_ARRIVAL_KEY) : null;
            arrivalFilter = savedInstanceState.containsKey(LAST_ARRIVAL_FILTER_KEY) ? savedInstanceState.getSerializable(LAST_ARRIVAL_FILTER_KEY) : null;
            if (hasTwoPanels()) {
                Fragment left = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LEFT);
                Fragment right = getSupportFragmentManager().findFragmentByTag(FRAGMENT_RIGHT);

                if (left != null) {
                    openFragment(left, title, false);
                } else {
                    openStops();
                }
                if (right != null && (right instanceof ArrivalsFragment)) {
                    openFragment(ArrivalsFragment.newInstance(lastArrival, arrivalFilter), title, true);
                }
            } else {
                lastContainerKey = savedInstanceState.getString(LAST_FRAGMENT_KEY);
                Fragment lastFragment = getSupportFragmentManager().findFragmentByTag(lastContainerKey);

                if (lastFragment != null) {
                    if (lastFragment instanceof ArrivalsFragment) {
                        lastFragment = ArrivalsFragment.newInstance(lastArrival, arrivalFilter);
                    }
                    openFragment(lastFragment, title, false);
                } else {
                    openStops();
                }
            }
        } else {
            openStops();
        }
    }

    private NavigationView getNavigationView() {
        navView = (NavigationView) findViewById(R.id.navigation);
        navView.setItemIconTintList(null);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Fragment fragment = null;
                int itemId = menuItem.getItemId();

                if (itemId == R.id.stops) {
                    fragment = new StopsFragment();
                } else if (itemId == R.id.settings) {
                    fragment = new SettingsFragment();
                } else if (itemId == R.id.exit) {
                    app.finish();
                    finish();
                }
                if (fragment != null) {
                    openFragment(fragment, getTitleForFragment(fragment), false);
                    drawerLayout.closeDrawers();
                    return true;
                }
                return false;
            }
        });

        return navView;
    }

    private void openStops() {
        navView.setCheckedItem(R.id.stops);
        openFragment(new StopsFragment(), getString(R.string.stops), false);
    }

    private void openFragment(Fragment fragment, String title, boolean isRightContainer) {
        if (title != null) {
            getSupportActionBar().setTitle(title);
        }

        if (fragment instanceof StopsFragment) {
            ((StopsFragment) fragment).setOnStopClickListener(MainActivity.this);
        }

        String containerKey = lastContainerKey;
        lastContainerKey = fragment instanceof ArrivalsFragment ? FRAGMENT_RIGHT : FRAGMENT_LEFT;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(isRightContainer ? R.id.containerRight : R.id.container, fragment, fragment instanceof ArrivalsFragment ? FRAGMENT_RIGHT : FRAGMENT_LEFT);
        if ((lastContainerKey.equals(FRAGMENT_RIGHT)) && (containerKey != null) && !(containerKey.equals(FRAGMENT_RIGHT))) {  //TODO bad code
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_TITLE_KEY, String.valueOf(getSupportActionBar().getTitle()));
        outState.putString(LAST_FRAGMENT_KEY, lastContainerKey);
        outState.putString(LAST_ARRIVAL_KEY, lastArrival);
        Fragment arrivals = getSupportFragmentManager().findFragmentByTag(FRAGMENT_RIGHT);
        if (arrivals != null) {
            if (((ArrivalsFragment) arrivals).getFilter() != null)
                outState.putSerializable(LAST_ARRIVAL_FILTER_KEY, ((ArrivalsFragment) arrivals).getFilter());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStopClick(Stop stop) {
        onStopClick(stop.getKs_id(), stop.getTitle());
    }

    @Override
    public void onStopClick(String ksId, String title) {
        ArrivalsFragment fragment = ArrivalsFragment.newInstance(lastArrival = ksId);
        fragment.setFavoriteChangeListener(this);
        openFragment(fragment, title, hasTwoPanels());
    }

    private boolean hasTwoPanels() {
        return getResources().getBoolean(R.bool.has_two_panes);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (hasTwoPanels()) {
                return;
            }

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (fragment instanceof ArrivalsFragment) {
                super.onBackPressed();
                lastContainerKey = FRAGMENT_LEFT;
                getSupportActionBar().setTitle(getTitleForFragment(getSupportFragmentManager().findFragmentById(R.id.container)));
            }
        }
    }

    private String getTitleForFragment(Fragment fragment) {
        int resource = R.string.app_name;
        if (fragment instanceof SettingsFragment) {
            resource = R.string.settings;
        } else if (fragment instanceof StopsFragment) {
            resource = R.string.stops;
        }

        return getString(resource);
    }

    @Override
    public void setFavorite(Stop stopDirection, boolean favorite) {
        stopDirection.setFavorite(favorite);
        app.getDaoSession().getStopDao().update(stopDirection);
        Fragment left = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LEFT);
        Fragment right = getSupportFragmentManager().findFragmentByTag(FRAGMENT_RIGHT);
        if (left instanceof FavoriteUpdaterListener) {
            ((FavoriteUpdaterListener) left).setFavorite(stopDirection, favorite);
        }

        if (right instanceof FavoriteUpdaterListener) {
            ((FavoriteUpdaterListener) right).setFavorite(stopDirection, favorite);
        }
    }

    public interface OnBackPressedListener {
        void onBackPressed();
    }
}
