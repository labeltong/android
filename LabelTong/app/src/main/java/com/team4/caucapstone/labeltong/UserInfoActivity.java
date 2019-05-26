package com.team4.caucapstone.labeltong;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class UserInfoActivity extends AppCompatActivity {
    ImageView userImg;
    TextView userEmail, userName, credditText, warningsText, creddit, warnings;
    ListView listView;
    ArrayList<listItem> items;
    Bitmap bitmap;

    String userId;

    String JWTToken;
    private int[] Image = {R.drawable.classification,R.drawable.boundingbox,R.drawable.sentiment};
    private String[] Title ={"Classification","Boundingbox","Sentiment"};
    private String[] Desc = {"Classify Image","Bound box of Image","Sentiment classify with Sounds"};


    Typeface font1 , font2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Bundle extras = getIntent().getExtras();
        JWTToken = extras.getString("JWT");
        userId = extras.getString("AuthId");

        font1 = Typeface.createFromAsset(UserInfoActivity.this.getAssets(),"fonts/Montserrat-Bold.ttf");
        credditText = (TextView) findViewById(R.id.creddittext);
        credditText.setTypeface(font1);
        warningsText = (TextView) findViewById(R.id.warningstext);
        warningsText.setTypeface(font1);

        font2 = Typeface.createFromAsset(UserInfoActivity.this.getAssets(),"fonts/Montserrat-Regular.ttf");
        creddit = (TextView) findViewById(R.id.creddit);
        creddit.setTypeface(font2);
        warningsText = (TextView) findViewById(R.id.warnings);
        warningsText.setTypeface(font2);

        if (!userId.equals("Google"))
            setUserImg();

        userName = (TextView) findViewById(R.id.user_name);
        userName.setText(extras.getString("AuthName"));
        userName.setTypeface(font1);

        userEmail = (TextView) findViewById(R.id.user_email);
        userEmail.setText(extras.getString("AuthEmail"));
        userEmail.setTypeface(font2);

        setListView();

    }
    private void setUserImg() {
        userImg = (ImageView) findViewById(R.id.logo);
        String imgUrl = "https://graph.facebook.com/" + userId + "/picture";
        getImageFromServer(imgUrl, userImg);
        GradientDrawable drawable = (GradientDrawable) getApplicationContext().getDrawable(R.drawable.background_rounding);
        userImg.setBackground(drawable);
        userImg.setClipToOutline(true);
    }
    private void setListView() {
        listView = (ListView) findViewById(R.id.listview_account);
        items = new ArrayList<listItem>();
        for (int i= 0; i< Title.length; i++) {
            listItem beanclass = new listItem(Image[i], Title[i], Desc[i]);
            items.add(beanclass);
        }
        ListBaseAdapter listBaseAdapter = new ListBaseAdapter(UserInfoActivity.this, items);
        listView.setAdapter(listBaseAdapter);
    }
    private void getImageFromServer(final String imgUrl, ImageView v){

        Thread mThread = new Thread() {
            @Override
            public void run() {
                try{
                    URL url = new URL(imgUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.connect();
                    InputStream in = httpURLConnection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(in);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        mThread.start();
        try{
            mThread.join();
            v.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
