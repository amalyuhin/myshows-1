package ru.myshows.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;
import ru.myshows.activity.R;
import ru.myshows.fragments.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gb
 * Date: 17.07.12
 * Time: 23:05
 * To change this template use File | Settings | File Templates.
 */
public class TabsAdapter extends FragmentPagerAdapter {


    public List<Integer> titles;
    private Context context;

    public TabsAdapter(FragmentManager fm, Context context, List<Integer> titles) {
        super(fm);
        this.titles = titles;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        //return fragments.get(position);
        Integer title = titles.get(position);
        switch (title){
            case R.string.tab_shows_title:
                return new ShowsFragment(ShowsFragment.SHOWS_USER);
            case R.string.tab_new:
                return new NewEpisodesFragment();
            case R.string.tab_next:
                return new NextEpisodesFragment();
            case R.string.tab_news_title:
                return new NewsFragment();
            case R.string.tab_profile_title:
                return new ProfileFragment();
            case R.string.tab_search_title:
                return new SearchFragment();
            case R.string.tab_show:
                return new ShowFragment(null, null);
            case R.string.tab_episodes:
                return new EpisodesFragment(null);
            case R.string.tab_login_title:
                return new LoginFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        if (titles == null || titles.isEmpty()) return 0;
        return titles.size();

    }



    @Override
    public CharSequence getPageTitle(int position) {
        return context.getResources().getString(titles.get(position));
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        FragmentManager manager = ((Fragment) object).getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove((Fragment) object);
        trans.commit();
    }
}
