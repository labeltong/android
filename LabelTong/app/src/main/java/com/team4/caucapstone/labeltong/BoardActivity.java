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
import android.view.View;
import android.widget.ImageButton;


public class BoardActivity extends AppCompatActivity{
    FragmentManager manager = getSupportFragmentManager();
    final Fragment boardFragment = new BoardFragment();
    //final Fragment settingFragment = new SettingFragment();
    FragmentTransaction transaction;
    Fragment active = boardFragment;
    private ImageButton method_bounding;
    private ImageButton method_classify;
    private ImageButton method_sentiment;

    //ToDo : Buttons must be determined by server
    private ImageButton topic_car;
    private ImageButton topic_cat;
    private ImageButton topic_roadsign;
    private ImageButton topic_emotion;

    public final static int INTENT_ERROR = -1;
    public final static int METHOD_BOUNDING  = 1;
    public final static int METHOD_CLASSIFY  = 2;
    public final static int METHOD_SENTIMENT = 3;
    //ToDo : When Server ready, change it to dynamic values
    public final static int TOPIC_CAT = 4;
    public final static int TOPIC_CAR = 5;
    public final static int TOPIC_ROADSIGN = 6;
    public final static int TOPIC_EMOTION = 7;


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
                    //if (active == settingFragment) return true;
                    //transaction.hide(active).show(settingFragment).commit();
                    //active = settingFragment;
                    Intent intent = new Intent(BoardActivity.this, SettingFragment.class);
                    startActivity(intent);
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
        //transaction.add(R.id.frameLayout, settingFragment).hide(settingFragment);
        transaction.add(R.id.frameLayout, boardFragment).commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        method_bounding  = (ImageButton) findViewById(R.id.boundingBox);
        method_classify  = (ImageButton) findViewById(R.id.classification);
        method_sentiment = (ImageButton) findViewById(R.id.sentiment);

        //ToDo: Get Topic Information from server
        topic_roadsign = (ImageButton)findViewById(R.id.roadsign);
        topic_cat = (ImageButton)findViewById(R.id.cat);
        topic_car = (ImageButton)findViewById(R.id.car);
        topic_emotion = (ImageButton)findViewById(R.id.emotion);

    }

    public void buttonClicked(View v) {
        Intent intent = new Intent(BoardActivity.this, LabelingActivity.class);
        switch (v.getId()) {
            case R.id.boundingBox:
                intent.putExtra("INFO", METHOD_BOUNDING);
                break;
            case R.id.classification:
                intent.putExtra("INFO", METHOD_CLASSIFY);
                break;
            case R.id.sentiment:
                intent.putExtra("INFO", METHOD_SENTIMENT);
                break;
            case R.id.cat:
                intent.putExtra("INFO", TOPIC_CAT);
                break;
            case R.id.car:
                intent.putExtra("INFO", TOPIC_CAR);
                break;
            case R.id.roadsign:
                intent.putExtra("INFO", TOPIC_ROADSIGN);
                break;
            case R.id.emotion:
                intent.putExtra("INFO", TOPIC_EMOTION);
                break;
        }
        startActivity(intent);
    }

}
