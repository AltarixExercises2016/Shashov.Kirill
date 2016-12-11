package com.transportsmr.app;

import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
    private DrawerLayout dLayout;
    private TransportApp app;
    private Fragment content;

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


        if (savedInstanceState != null) {
            content = getSupportFragmentManager().getFragment(savedInstanceState, CURRENT_FRAGMENT_KEY);
            if (savedInstanceState.containsKey(CURRENT_TITLE_KEY)) {
                getSupportActionBar().setTitle(savedInstanceState.getString(CURRENT_TITLE_KEY));
            }
        }
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
                    getSupportActionBar().setTitle(getString(R.string.stops));
                    //Toast.makeText(getApplication(), getPreferences(MODE_PRIVATE).getString("STOPS_LAST_UPDATE", "2"), Toast.LENGTH_LONG).show();
                } else if (itemId == R.id.settings) {
                    fragment = new SettingsFragment();
                    getSupportActionBar().setTitle(getString(R.string.settings));
                } else if (itemId == R.id.exit) {
                    app.finish();
                    finish();
                }

                if (fragment != null) {
                    openFragment(fragment, false);
                    dLayout.closeDrawers();
                    return true;
                }

                return false;
            }
        });

        //on_startup
        if (content == null) {
            navView.setCheckedItem(R.id.stops);
            openFragment(new StopsFragment(), false);
            getSupportActionBar().setTitle(getString(R.string.stops));
        } else {
            openFragment(content, false); //after activity.destroy
        }
    }


    private void openFragment(Fragment fragment, boolean isAddToBackStack) {
        content = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        if (isAddToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
        if (content.isAdded()) {
            getSupportFragmentManager().putFragment(outState, CURRENT_FRAGMENT_KEY, content);
            outState.putString(CURRENT_TITLE_KEY, String.valueOf(getSupportActionBar().getTitle()));
        }
    }

    @Override
    public void onStopClick(Stop stopDirection) {
        getSupportActionBar().setTitle(stopDirection.getTitle());
        openFragment(ArrivalsFragment.newInstance(stopDirection.getKs_id()), true);
    }
}
