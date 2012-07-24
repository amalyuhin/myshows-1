package ru.myshows.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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

    public List<Fragment> fragments;
    public List<String> titles;
    public boolean destroyItem;

    public TabsAdapter(FragmentManager fm, boolean destroyItem) {
        super(fm);
        this.destroyItem = destroyItem;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        if (fragments == null) return 0;
        return fragments.size();
    }

    public void addFragment(Fragment fragment, String title) {
        if (fragments == null)
            fragments = new ArrayList<Fragment>();
        if (titles == null)
            titles = new ArrayList<String>();
        fragments.add(fragment);
        titles.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    public void clear(){
        fragments = null;
        titles = null;
    }

}
