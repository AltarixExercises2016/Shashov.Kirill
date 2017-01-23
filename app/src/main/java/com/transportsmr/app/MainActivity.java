package com.transportsmr.app;

import android.Manifest;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FilterQueryProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.transportsmr.app.adapters.SearchAdapter;
import com.transportsmr.app.events.FavoriteUpdateEvent;
import com.transportsmr.app.events.StopClickEvent;
import com.transportsmr.app.fragments.ArrivalsFragment;
import com.transportsmr.app.fragments.SettingsFragment;
import com.transportsmr.app.fragments.StopsFragment;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.StopDao;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;


public class MainActivity extends AppCompatActivity {
    public static final String CURRENT_TITLE_KEY = "title";
    private TransportApp app;
    private static final String FRAGMENT_RIGHT = "FRAGMENT_RIGHT";
    private static final String FRAGMENT_LEFT = "FRAGMENT_LEFT";
    private static final String LAST_FRAGMENT_KEY = "LAST_FRAGMENT_KEY";
    private static final String LAST_ARRIVAL_KEY = "LAST_ARRIVAL_KEY";
    private static final String LAST_ARRIVAL_FILTER_KEY = "LAST_ARRIVAL_FILTER_KEY";
    private String lastContainerKey;
    private String lastArrival = "";
    private Serializable arrivalFilter;
    private SearchAdapter searchAdapter;

    @BindView(R.id.navigation_drawer)
    DrawerLayout drawerLayout;
    SearchView searchBox;
    @BindView(R.id.navigation)
    NavigationView navView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResources().getIdentifier("main_layout", "layout", getPackageName())); //R.layout.activity_main));
        ButterKnife.bind(this);
        app = (TransportApp) getApplication();
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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        initNavigationView();

        if ((savedInstanceState != null)) {
            //TODO refactoring !!!
            String title = savedInstanceState.containsKey(CURRENT_TITLE_KEY) ? savedInstanceState.getString(CURRENT_TITLE_KEY) : null;
            lastArrival = savedInstanceState.containsKey(LAST_ARRIVAL_KEY) ? savedInstanceState.getString(LAST_ARRIVAL_KEY) : null;
            arrivalFilter = savedInstanceState.containsKey(LAST_ARRIVAL_FILTER_KEY) ? savedInstanceState.getSerializable(LAST_ARRIVAL_FILTER_KEY) : null;
            if (hasTwoPanels()) {
                Fragment left = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LEFT);
                Fragment right = getSupportFragmentManager().findFragmentByTag(FRAGMENT_RIGHT);

                if (left != null) {
                    openFragment(left, title, false);
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
                }
            }
        }
        if (getSupportFragmentManager().findFragmentById(R.id.container) == null) {
            openStops();
        }
        searchAdapter = new SearchAdapter(this, null, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1);
        }

    }

    private void initNavigationView() {
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
    }

    private void openStops() {
        navView.setCheckedItem(R.id.stops);
        openFragment(new StopsFragment(), getString(R.string.stops), false);
    }

    private void openFragment(Fragment fragment, String title, boolean isRightContainer) {
        if (title != null) {
            getSupportActionBar().setTitle(title);
        }

        lastContainerKey = fragment instanceof ArrivalsFragment ? FRAGMENT_RIGHT : FRAGMENT_LEFT;
        getSupportFragmentManager().
                beginTransaction().
                replace(isRightContainer ? R.id.containerRight : R.id.container, fragment, fragment instanceof ArrivalsFragment ? FRAGMENT_RIGHT : FRAGMENT_LEFT).
                addToBackStack(null).
                commit();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStopClick(StopClickEvent event) {
        ArrivalsFragment fragment = ArrivalsFragment.newInstance(lastArrival = event.stop.getKs_id());
        openFragment(fragment, event.stop.getTitle(), hasTwoPanels());
    }

    private boolean hasTwoPanels() {
        return getResources().getBoolean(R.bool.has_two_panes);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (!hasTwoPanels()) {
            if (lastContainerKey.equals(FRAGMENT_RIGHT)) {
                Fragment newFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LEFT);
                if (newFragment != null) {
                    openFragment(newFragment, getTitleForFragment(newFragment), false);
                    return;
                }
            }
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
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

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    public void onFavoriteChange(FavoriteUpdateEvent event) {
        event.stop.setFavorite(event.isFavorite);
        app.getDaoSession().getStopDao().update(event.stop);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (item == null) {
            return false;
        }
        searchBox = (SearchView) item.getActionView();
        int options = searchBox.getImeOptions();
        searchBox.setImeOptions(options | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                if ((constraint != null) && (constraint.length() != 0)) {
                    constraint = "%" + constraint.toString().toLowerCase() + "%";
                    Cursor dbList = ((TransportApp) MainActivity.this.getApplication()).
                            getDaoSession().
                            getStopDao().
                            queryBuilder().
                            whereOr(StopDao.Properties.Title_lc.like(constraint.toString()), StopDao.Properties.AdjacentStreet_lc.like(constraint.toString())).
                            orderAsc(StopDao.Properties.Title_lc).
                            buildCursor().
                            forCurrentThread().
                            query();
                    return dbList;
                }

                return null;
            }
        });
        searchBox.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) searchAdapter.getItem(position);
                Stop stop = app.getDaoSession().getStopDao().readEntity(cursor, 0);
                if (stop != null) {
                    searchBox.onActionViewCollapsed();
                    EventBus.getDefault().post(new StopClickEvent(stop));
                    return true;
                }
                return false;
            }
        });
        searchBox.setSuggestionsAdapter(searchAdapter);

        return true;
    }
}
