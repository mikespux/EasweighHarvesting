package com.plantation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.plantation.R;

/**
 * Created by Abderrahim on 10/14/2015.
 */
public class TabsFragment extends Fragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;
    public int nb_items = 4;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tabs_layout, null);
        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new TabsAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(2); // if you use 3 tabs
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
                tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
                tabLayout.setTabMode(TabLayout.MODE_FIXED);
                setupTabIcons();
            }
        });
        return view;
    }

    /**
     * method to setup tabs icons
     */
    private void setupTabIcons() {

        tabLayout.getTabAt(0).setText("Home");
        tabLayout.getTabAt(1).setText("Harvest");
        tabLayout.getTabAt(2).setText("Tasking");
        tabLayout.getTabAt(3).setText("Delivery");

    }


    class TabsAdapter extends FragmentPagerAdapter {
        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HomePageFragment();

                case 1:
                    return new HarvestFragment();

                case 2:
                    return new TaskingFragment();

                case 3:
                    return new DelivaryFragment();

            }
            return null;
        }

        @Override
        public int getCount() {
            return nb_items;

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

}