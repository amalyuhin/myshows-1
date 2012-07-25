package ru.myshows.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

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

    private static final int TAB_SHOWS = 0;
    private static final int TAB_NEW_EPISODES = 1;
    private static final int TAB_NEWS = 2;
    private static final int TAB_PROFILE = 3;
    private static final int TAB_SEARCH = 4;
    private static final int TAB_LOGIN = 5;

  //  public List<Fragment> fragments;
    public List<String> titles;
    public boolean destroyItem;
    FragmentManager fm;

    public TabsAdapter(FragmentManager fm, boolean destroyItem) {
        super(fm);
        this.destroyItem = destroyItem;
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int position) {
        //return fragments.get(position);
        return fm.findFragmentByTag(titles.get(position));
    }

    @Override
    public int getCount() {
        if (titles == null) return 0;
        return titles.size();

//        if (fragments == null) return 0;
//        return fragments.size();
    }

    public void addFragment(Fragment fragment, String title) {
//        if (fragments == null)
//            fragments = new ArrayList<Fragment>();
        if (titles == null)
            titles = new ArrayList<String>();
//        fragments.add(fragment);
        titles.add(title);
        fm.beginTransaction().add(fragment,title).commit();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    public void clear(){
       // fragments = null;
        titles = null;
    }

}
