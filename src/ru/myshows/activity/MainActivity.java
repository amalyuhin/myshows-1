package ru.myshows.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;
import ru.myshows.adapters.SectionedAdapter;
import ru.myshows.adapters.TabsAdapter;
import ru.myshows.domain.Searchable;
import ru.myshows.fragments.*;
import ru.myshows.tasks.*;
import ru.myshows.util.Settings;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: GGobozov
 * Date: 12.05.2011
 * Time: 15:47:52
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends SherlockFragmentActivity {

    private static final int TAB_SHOWS = 0;
    private static final int TAB_NEW_EPISODES = 1;
    private static final int TAB_NEWS = 2;
    private static final int TAB_PROFILE = 3;
    private static final int TAB_SEARCH = 4;
    private static final int TAB_LOGIN = 5;


    private ViewPager pager;
    private TitlePageIndicator indicator;
    private TabsAdapter adapter;
    private EditText search;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);



        Log.d("MyShows", "Main activity: OnCreate");

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getSupportActionBar().setIcon(R.drawable.ic_list_logo);

        BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(R.drawable.stripe_red);
        bg.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        getSupportActionBar().setBackgroundDrawable(bg);
        new LoginTask().execute();

    }


    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

        menu.add(0, 1, 1, R.string.menu_update).setIcon(R.drawable.ic_navigation_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 2, 2, R.string.menu_settings).setIcon(R.drawable.ic_action_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 3, 3, R.string.menu_search).setIcon(R.drawable.ic_action_search).setActionView(R.layout.action_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menu.add(0, 4, 4, R.string.menu_exit).setIcon(R.drawable.ic_exit).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                int position = pager.getCurrentItem();
                if (!MyShows.isLoggedIn)
                    break;

                Fragment currentFragment = adapter.getItem(position);
                ((Taskable) currentFragment).executeUpdateTask();

                break;
            case 2:
                startActivity(new Intent(this, SettingsAcrivity.class));
                break;
            case 3:
                search = (EditText) item.getActionView();
                search.addTextChangedListener(filterTextWatcher);
                search.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                break;
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

    private TextWatcher filterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Fragment fragment = adapter.getItem(pager.getCurrentItem());
            if (fragment instanceof Searchable) {
                ((Searchable) fragment).getAdapter().getFilter().filter(s);
            }
        }

    };

    private void getPrivateTabs() {

//        getSupportFragmentManager().beginTransaction().add(new ShowsFragment(ShowsFragment.SHOWS_USER), getResources().getString(R.string.tab_shows_title)).commit();
//        getSupportFragmentManager().beginTransaction().add(new NewEpisodesFragment(), getResources().getString(R.string.tab_new)).commit();
//        if (Settings.getBoolean(Settings.PREF_SHOW_NEXT))
//            getSupportFragmentManager().beginTransaction().add(new NextEpisodesFragment(), getResources().getString(R.string.tab_next)).commit();
//        if (Settings.getBoolean(Settings.PREF_SHOW_NEWS))
//            getSupportFragmentManager().beginTransaction().add(new NewsFragment(), getResources().getString(R.string.tab_news_title)).commit();
//        if (Settings.getBoolean(Settings.PREF_SHOW_PROFILE))
//            getSupportFragmentManager().beginTransaction().add(new ProfileFragment(), getResources().getString(R.string.tab_profile_title)).commit();
//        getSupportFragmentManager().beginTransaction().add(new SearchFragment(), getResources().getString(R.string.tab_search_title)).commit();

        List<Integer> tabs = new ArrayList<Integer>();
        tabs.add(R.string.tab_shows_title);
        tabs.add(R.string.tab_new);
        if (Settings.getBoolean(Settings.PREF_SHOW_NEXT))
            tabs.add(R.string.tab_next);
        if (Settings.getBoolean(Settings.PREF_SHOW_NEWS))
            tabs.add(R.string.tab_news_title);
        if (Settings.getBoolean(Settings.PREF_SHOW_PROFILE))
            tabs.add(R.string.tab_profile_title);
        tabs.add(R.string.tab_search_title);

        adapter = new TabsAdapter(getSupportFragmentManager(), this, tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(tabs.size());
        indicator = (TitlePageIndicator) findViewById(R.id.indicator);
        pager.setAdapter(adapter);
        indicator.setViewPager(pager);
        indicator.setTypeface(MyShows.font);

        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (!MyShows.isLoggedIn)
                    return;
                Fragment f = getSupportFragmentManager().findFragmentByTag(getFragmentTag(position));
                ((Taskable) f).executeTask();
            }

            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });


        indicator.notifyDataSetChanged();
        adapter.notifyDataSetChanged();


        ShowsFragment showsFragment = (ShowsFragment) adapter.getItem(0);
        String fragmentTag = showsFragment.getTag();
        Log.d("MyShows", "getTabs()" + fragmentTag);

//        if (fragmentTag == null){
//            finish();
//            startActivity(new Intent(this, MainActivity.class));
//        }
        Fragment f = getSupportFragmentManager().findFragmentByTag(getFragmentTag(0));

        //if (fragmentTag != null) {
            // fire first task manually
            GetShowsTask task = new GetShowsTask(MainActivity.this, GetShowsTask.SHOWS_USER);
            //ShowsFragment showsFragment = (ShowsFragment) adapter.getItem(0);
            task.setTaskListener((TaskListener)f);
            //task.setTaskListener(showsFragment);
            task.execute();
        //}
    }

    private String getFragmentTag(int pos){
        return "android:switcher:"+pager.getId()+":"+pos;
    }

    private void getPublicTabs() {


        List<Integer> tabs = new ArrayList<Integer>();
        tabs.add(R.string.tab_login_title);
        adapter = new TabsAdapter(getSupportFragmentManager(), this, tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(tabs.size());
        indicator = (TitlePageIndicator) findViewById(R.id.indicator);
        pager.setAdapter(adapter);
        indicator.setViewPager(pager);
        indicator.setTypeface(MyShows.font);

        indicator.notifyDataSetChanged();
        adapter.notifyDataSetChanged();
    }


    private class LoginTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Object... objects) {
            if (MyShows.isLoggedIn) return true;
            if (Settings.getBoolean(Settings.KEY_LOGGED_IN)) {
                String login = Settings.getString(Settings.KEY_LOGIN);
                String pass = Settings.getString(Settings.KEY_PASSWORD);
                return MyShows.client.login(login, pass);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) getPrivateTabs();
            else getPublicTabs();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MyShows", "Main activity on resume");
        if (MyShows.isLoggedIn && MyShows.isUserShowsChanged) {

            Fragment showsFragment = adapter.getItem(0);
            if (showsFragment instanceof ShowsFragment) {
                GetShowsTask getShowsTask = new GetShowsTask(this, GetShowsTask.SHOWS_USER);
                getShowsTask.setTaskListener((ShowsFragment) showsFragment);
                getShowsTask.execute();
            }

            Fragment newEpisodesFragment = adapter.getItem(1);
            if (newEpisodesFragment instanceof NewEpisodesFragment) {
                GetNewEpisodesTask episodesTask = new GetNewEpisodesTask(this, true);
                episodesTask.setTaskListener((NewEpisodesFragment) newEpisodesFragment);
                episodesTask.execute();
            }


        }
    }
}
