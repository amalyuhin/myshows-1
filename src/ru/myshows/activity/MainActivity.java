package ru.myshows.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import ru.myshows.adapters.EpisodesAdapter;
import ru.myshows.adapters.TabsAdapter;
import ru.myshows.domain.Searchable;
import ru.myshows.fragments.*;
import ru.myshows.tasks.Taskable;
import ru.myshows.util.Settings;

import java.util.List;

/**
 * @Author: Georgy Gobozov
 * @Date: 12.05.2011
 */
public class MainActivity extends MenuActivity {

    private ViewPager pager;
    private PagerTabStrip pagerTabStrip;
    private EpisodesAdapter adapter;
   // private EditText search;
   // private Bundle savedInstanceState;


    @Override
    protected int getContentViewId() {
        return R.layout.main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
       // this.savedInstanceState = savedInstanceState;
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //adapter = new TabsAdapter(getSupportFragmentManager(), false);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(2);
       // pager.setAdapter(adapter);
        pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagerTabStrip);
        pagerTabStrip.setTabIndicatorColorResource(R.color.light_red);

//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.ic_list_logo);

        BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(R.drawable.stripe_red);
        bg.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        getSupportActionBar().setBackgroundDrawable(bg);
        new LoginTask().execute();

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.menu_update).setIcon(R.drawable.ic_navigation_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        menu.add(0, 2, 2, R.string.menu_settings).setIcon(R.drawable.ic_action_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        menu.add(0, 3, 3, R.string.menu_search).setIcon(R.drawable.ic_action_search).setActionView(R.layout.action_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menu.add(0, 4, 4, R.string.menu_exit).setIcon(R.drawable.ic_exit).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(1).setVisible(MyShows.isLoggedIn);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case 1:
                int position = pager.getCurrentItem();
                if (!MyShows.isLoggedIn)
                    break;

                //Fragment currentFragment = adapter.getItem(position);
                Fragment currentFragment = getFragment(position);
                if (currentFragment != null)
                    ((Taskable) currentFragment).executeUpdateTask();

                break;
//            case 2:
//                startActivity(new Intent(this, SettingsAcrivity.class));
//                break;
//            case 3:
//                search = (EditText) item.getActionView();
//                //search.addTextChangedListener(filterTextWatcher);
//                search.requestFocus();
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//                break;
            case 4:
                final AlertDialog alert;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.request_exit)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Settings.setString(Settings.KEY_LOGIN, null);
                                Settings.setString(Settings.KEY_PASSWORD, null);
                                Settings.setBoolean(Settings.KEY_LOGGED_IN, false);
                                MyShows.isLoggedIn = false;
                                MyShows.invalidateUserData();
                                finish();
                                startActivity(new Intent(MainActivity.this, MainActivity.class));
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                alert = builder.create();
                alert.show();
                break;
        }
        return true;
    }

//    private TextWatcher filterTextWatcher = new TextWatcher() {
//        public void afterTextChanged(Editable s) {
//        }
//
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//        }
//
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            Fragment fragment = getFragment(pager.getCurrentItem());
//            if (fragment instanceof Searchable) {
//                ((Searchable) fragment).getAdapter().getFilter().filter(s);
//            }
//        }
//
//    };
//
//    private void getPrivateTabs() {
//
//        if (savedInstanceState == null) {
//
//            adapter.addFragment(new ShowsFragment(ShowsFragment.SHOWS_USER), getResources().getString(R.string.tab_shows_title));
//            adapter.addFragment(new NewEpisodesFragment(), getResources().getString(R.string.tab_new));
//            if (Settings.getBoolean(Settings.PREF_SHOW_NEXT))
//                adapter.addFragment(new NextEpisodesFragment(), getResources().getString(R.string.tab_next));
//            if (Settings.getBoolean(Settings.PREF_SHOW_NEWS))
//                adapter.addFragment(new NewsFragment(), getResources().getString(R.string.tab_news_title));
//            if (Settings.getBoolean(Settings.PREF_SHOW_PROFILE))
//                adapter.addFragment(new ProfileFragment(), getResources().getString(R.string.tab_profile_title));
//            adapter.addFragment(new SearchFragment(), getResources().getString(R.string.tab_search_title));
//
//        } else {
//            Integer count = savedInstanceState.getInt("tabsCount");
//            String[] titles = savedInstanceState.getStringArray("titles");
//            for (int i = 0; i < count; i++) {
//                adapter.addFragment(getFragment(i), titles[i]);
//            }
//        }
//
//        //indicator.notifyDataSetChanged();
//        adapter.notifyDataSetChanged();
//
//        if (savedInstanceState != null)
//            pager.setCurrentItem(savedInstanceState.getInt("currentTab"));
//
//
//    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (adapter != null) {
//            outState.putInt("tabsCount", adapter.getCount());
//            List<String> titles = adapter.getTitles();
//            outState.putStringArray("titles", titles.toArray(new String[titles.size()]));
//        }
//        if (pager != null)
//            outState.putInt("currentTab", pager.getCurrentItem());
//    }
//
    private Fragment getFragment(int position) {
        return  getSupportFragmentManager().findFragmentByTag(getFragmentTag(position));
    }
//
    private String getFragmentTag(int position) {
        return "android:switcher:" + R.id.pager + ":" + position;
    }
//
//    private void getPublicTabs() {
//        adapter.addFragment(new LoginFragment(), getResources().getString(R.string.tab_login_title));
//        adapter.notifyDataSetChanged();
//    }


    private class LoginTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... objects) {
            if (MyShows.isLoggedIn) return true;
            // if credentials exists in preferences, try to login
            if (Settings.getBoolean(Settings.KEY_LOGGED_IN)) {
                String login = Settings.getString(Settings.KEY_LOGIN);
                String pass = Settings.getString(Settings.KEY_PASSWORD);
                return MyShows.client.login(login, pass);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            //Fragment fragment = null;
            invalidateOptionsMenu();
            if (result) {
               // getPrivateTabs();
                //fragment = new NewEpisodesFragment();
                adapter = new EpisodesAdapter(MainActivity.this, getSupportFragmentManager());
                pager.setAdapter(adapter);
                setupDrawer();
            } else {
                //getPublicTabs();

                //setupDrawer();
                //fragment = new LoginFragment();

                pager.setVisibility(View.GONE);
                FragmentManager fragmentManager = getSupportFragmentManager();
                // fragmentManager.beginTransaction().add(R.id.main, fragment).commit();
                  fragmentManager.beginTransaction().add(R.id.main, new LoginFragment()).commitAllowingStateLoss();

            }

           // FragmentManager fragmentManager = getSupportFragmentManager();
           // fragmentManager.beginTransaction().add(R.id.main, fragment).commit();
          //  fragmentManager.beginTransaction().add(R.id.main, fragment).commitAllowingStateLoss();

        }

    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (MyShows.isLoggedIn && MyShows.isUserShowsChanged) {
//
//            Fragment showsFragment = getFragment(0);
//            if (showsFragment != null)
//                ((Taskable) showsFragment).executeTask();
//
//
//            Fragment newEpisodesFragment = getFragment(1);
//            if (newEpisodesFragment != null)
//                ((Taskable) newEpisodesFragment).executeUpdateTask();
//
//        }
//    }
}
