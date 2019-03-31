package com.team4.caucapstone.labeltong;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class BoardActivity extends AppCompatActivity {
    FragmentManager manager = getSupportFragmentManager();
    final Fragment boardFragment = new BoardFragment();
    final Fragment settingFragment = new SettingFragment();
    FragmentTransaction transaction;
    Fragment active = boardFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            transaction = manager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    if (active == boardFragment) return true;
                    transaction.hide(active).show(boardFragment).commit();
                    active = boardFragment;
                    return true;
                case R.id.navigation_settings:
                    if (active == settingFragment) return true;
                    transaction.hide(active).show(settingFragment).commit();
                    active = settingFragment;
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        transaction = manager.beginTransaction();
        transaction.add(R.id.frameLayout, settingFragment).hide(settingFragment);
        transaction.add(R.id.frameLayout, boardFragment).commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
