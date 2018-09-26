package com.radarapp.mjr9r.radar.helpers;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;

import com.radarapp.mjr9r.radar.fragments.ComposeFragment;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.activities.MapsActivity;
import com.radarapp.mjr9r.radar.fragments.MainFragment;

import java.util.List;

/**
 * Created by Matias on 19.09.2018.
 */

public class OnBottomNavigationItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {

    MapsActivity activity;

    public OnBottomNavigationItemSelectedListener(MapsActivity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        List<Fragment> fragments = activity.getSupportFragmentManager().getFragments();
        Fragment currentFragment = getCurrentFragment(activity, fragments);
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft;

        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.bottom_nav_map:
                //CHECK IF FRAGMENT HAS BEEN ADDED YET, IF NOT, ADD IT
                if (fm.findFragmentByTag("MAP_FRAGMENT") == null) {
                    ft = fm.beginTransaction();
                    ft.add(R.id.container_main, MainFragment.newInstance(), "MAP_FRAGMENT").commit();
                    fm.executePendingTransactions();
                    ft = fm.beginTransaction();
                    ft.show(fm.findFragmentByTag("MAP_FRAGMENT"));
                    ft.hide(currentFragment);
                    ft.commit();
                    break;
                }
                //CHECK IF SAME VIEW HAS BEEN CLICKED AGAIN, IF YES, BREAK
                if(fm.findFragmentByTag("MAP_FRAGMENT").isVisible()) {
                    break;
                }
                //HIDE CURRENT FRAGMENT, SHOW FRAGMENT THAT HAS BEEN SELECTED
                ft = fm.beginTransaction();
                ft.show(fm.findFragmentByTag("MAP_FRAGMENT"));
                ft.hide(currentFragment);
                ft.commit();
                return true;
            case R.id.bottom_nav_compose:
                //CHECK IF FRAGMENT HAS BEEN ADDED YET, IF NOT, ADD IT
                if (fm.findFragmentByTag("COMPOSE_FRAGMENT") == null) {
                    ft = fm.beginTransaction();
                    ft.add(R.id.container_main, ComposeFragment.newInstance(), "COMPOSE_FRAGMENT").commit();
                    fm.executePendingTransactions();
                    ft = fm.beginTransaction();
                    ft.show(fm.findFragmentByTag("COMPOSE_FRAGMENT"));
                    ft.hide(currentFragment);
                    ft.commit();
                    break;
                }
                //CHECK IF SAME VIEW HAS BEEN CLICKED AGAIN, IF YES, BREAK
                if(fm.findFragmentByTag("COMPOSE_FRAGMENT").isVisible()) {
                    break;
                }
                //HIDE CURRENT FRAGMENT, SHOW FRAGMENT THAT HAS BEEN SELECTED
                ft = fm.beginTransaction();
                ft.show(fm.findFragmentByTag("COMPOSE_FRAGMENT"));
                ft.hide(currentFragment);
                ft.commit();
                return true;
            case R.id.bottom_nav_bookmark:
                Log.v("BOTTOMNAV", "Click on Bookmarks");
                break;
        }
        return false;
    }

    private Fragment getCurrentFragment(MapsActivity activity, List<Fragment> fragments) {
        for(Fragment f: fragments) {
            if(f.isVisible()) {
                return f;
            }
        }
        return null;
    }
}
