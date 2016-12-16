package com.transportsmr.app;

import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.*;
import com.transportsmr.app.adapters.StopsRecyclerAdapter;
import com.transportsmr.app.fragments.ArrivalsFragment;
import com.transportsmr.app.fragments.SettingsFragment;
import com.transportsmr.app.fragments.StopsFragment;
import com.transportsmr.app.model.Stop;


public class MainActivity extends AppCompatActivity implements StopsRecyclerAdapter.StopClickListener {
    public static final String CURRENT_FRAGMENT_KEY = "content";
    public static final String CURRENT_TITLE_KEY = "title";
    private TransportApp app;
    private Fragment content;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        if (savedInstanceState != null) {
            content = getSupportFragmentManager().getFragment(savedInstanceState, CURRENT_FRAGMENT_KEY);
            openFragment(content, savedInstanceState.containsKey(CURRENT_TITLE_KEY) ? savedInstanceState.getString(CURRENT_TITLE_KEY) : null, false);
        } else {
            navView.setCheckedItem(R.id.stops);
            openFragment(new StopsFragment(), getString(R.string.stops), false);
        }
    }

    private NavigationView getNavigationView() {
        NavigationView navView = (NavigationView) findViewById(R.id.navigation);
        navView.setItemIconTintList(null);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                String title = "";
                Fragment fragment = null;
                int itemId = menuItem.getItemId();

                if (itemId == R.id.stops) {
                    fragment = new StopsFragment();
                    title = getTitleForFragment(fragment);
                } else if (itemId == R.id.settings) {
                    fragment = new SettingsFragment();
                    title = getTitleForFragment(fragment);
                } else if (itemId == R.id.exit) {
                    app.finish();
                    finish();
                }

                if (fragment != null) {
                    openFragment(fragment, title, false);
                    drawerLayout.closeDrawers();
                    return true;
                }

                return false;
            }
        });

        return navView;
    }


    private void openFragment(Fragment fragment, String title, boolean isAddToBackStack) {
        if (title != null) {
            getSupportActionBar().setTitle(title);
        }
        content = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, CURRENT_FRAGMENT_KEY);
        if (isAddToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (content.isAdded()) {
            getSupportFragmentManager().putFragment(outState, CURRENT_FRAGMENT_KEY, content);
            outState.putString(CURRENT_TITLE_KEY, String.valueOf(getSupportActionBar().getTitle()));
        }
    }

    @Override
    public void onStopClick(Stop stopDirection) {
        openFragment(ArrivalsFragment.newInstance(stopDirection.getKs_id()), stopDirection.getTitle(), true);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            getSupportActionBar().setTitle(getTitleForFragment(fragment));
            if (fragment instanceof OnBackPressedListener) { //TODO :(((
                ((OnBackPressedListener) fragment).onBackPressed();
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

    public interface OnBackPressedListener {
        void onBackPressed();
    }
}
