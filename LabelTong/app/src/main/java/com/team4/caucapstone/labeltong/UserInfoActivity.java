package com.team4.caucapstone.labeltong;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputContentInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserInfoActivity extends AppCompatActivity {
    ImageView userImg;
    TextView userEmail, userName, credditText, warningsText, creddit, warnings;
    ListView listView;
    ArrayList<listItem> items;
    Bitmap bitmap;

    String userId;
    String JWTToken;
    String authName;
    String authEmail;
    String imgUrl;
    boolean isClicked = false;

    List<TagList> tagLists;
    private Bitmap[] Image;
    private String[] Title ={"Boundingbox", "Classification","Audio"};
    private String[] Desc = {"Bound box of Image", "Classify Image","Classification with Audio"};

    Typeface font1 , font2;
    ServerControl.APIServiceIntface apiService;
    @Override
    protected void onResume(){
        super.onResume();
        getUserInfo();
        isClicked = false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        basicSettings(getIntent().getExtras());
        viewSetting();
        Progressbar.progressON(UserInfoActivity.this);

        getUserInfo();
        setListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isClicked) {
                    isClicked = true;
                    startNextActivity((listItem) parent.getItemAtPosition(position));
                }
            }
        });
        setGoogleUserImg();
        Progressbar.progressOFF();
    }
    public void basicSettings(Bundle extras) {
        JWTToken = extras.getString("JWT");
        userId = extras.getString("AuthEmail");
        authName = extras.getString("AuthName");
        authEmail = extras.getString("AuthEmail");

        apiService = ServerControl.getAPIServerIntface();

        Image = new Bitmap[3];
        Image[0] = ((BitmapDrawable)getResources().getDrawable(R.drawable.boundingbox)).getBitmap();
        Image[1] = ((BitmapDrawable)getResources().getDrawable(R.drawable.classification)).getBitmap();
        Image[2] = ((BitmapDrawable)getResources().getDrawable(R.drawable.sentiment)).getBitmap();
    }
    public void getUserInfo() {
        Progressbar.progressON(UserInfoActivity.this, "Login");
        Call<UserData> comment = apiService.getUserInfo("Bearer " + JWTToken);
        comment.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                try{
                    credditText.setText(response.body().getPoints());
                    warningsText.setText(response.body().getBanPoint());
                    userEmail.setText(response.body().getEmail());
                    userName.setText(response.body().getName());
                    Progressbar.progressOFF();
                    //setUserImg();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("ERROR", response.toString());
                }
            }
            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                Log.d("RETRO_USERINFO", "FAIL");
            }
        });
    }
    private void viewSetting() {
        font1 = Typeface.createFromAsset(UserInfoActivity.this.getAssets(),"fonts/Montserrat-Bold.ttf");
        credditText = (TextView) findViewById(R.id.creddittext);
        credditText.setTypeface(font1);
        warningsText = (TextView) findViewById(R.id.warningstext);
        warningsText.setTypeface(font1);

        font2 = Typeface.createFromAsset(UserInfoActivity.this.getAssets(),"fonts/Montserrat-Regular.ttf");
        creddit = (TextView) findViewById(R.id.creddit);
        creddit.setTypeface(font2);
        warnings = (TextView) findViewById(R.id.warnings);
        warnings.setTypeface(font2);

        userName = (TextView) findViewById(R.id.user_name);
        userName.setText(authName);
        userName.setTypeface(font1);

        userEmail = (TextView) findViewById(R.id.user_email);
        userEmail.setText(authEmail);
        userEmail.setTypeface(font2);
    }
    private void setListView() {
        listView = (ListView) findViewById(R.id.listview_account);
        // Get items from source code
        items = new ArrayList<listItem>();
        for (int i= 0; i< Title.length; i++) {
            items.add(new listItem(Image[i], Title[i], Desc[i], true, i + 1));
        }
        // Get tags from server
        getTagListFromServer();
    }
    private void getTagListFromServer() {
        Progressbar.progressON(UserInfoActivity.this, "Get Tag list..");
        Call<List<TagList>> comment = apiService.getTagList(JWTToken);
        comment.enqueue(new Callback<List<TagList>>() {
            @Override
            public void onResponse(Call<List<TagList>> call, Response<List<TagList>> response) {
                try {
                    tagLists = response.body();
                    getBitmapFromURL();
                    ListBaseAdapter listBaseAdapter = new ListBaseAdapter(UserInfoActivity.this, items);
                    listView.setAdapter(listBaseAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<List<TagList>> call, Throwable t) {
                Log.d("RETROFIT_TAGLIST", "FAIL");
            }
        });
    }
    public void getBitmapFromURL() {
        for (int i = 0; i < tagLists.size(); i++) {
            TagList item = tagLists.get(i);
            imgUrl = item.getTagThumbnail();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        URL url = new URL(imgUrl);
                        HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                        myConnection.setDoInput(true);
                        myConnection.connect();
                        InputStream is = myConnection.getInputStream();
                        bitmap = BitmapFactory.decodeStream(is);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            items.add(new listItem(bitmap, item.getTagName(),
                    item.getTagDescription(), false, Integer.parseInt(item.getTagId())));
        }
    }
    private void setFacebookUserImg() {
        userImg = (ImageView) findViewById(R.id.logo);
        String imgUrl = "https://graph.facebook.com/" + userId + "/picture";
        getImageFromServer(imgUrl, userImg);
        GradientDrawable drawable = (GradientDrawable) getApplicationContext().getDrawable(R.drawable.background_rounding);
        userImg.setBackground(drawable);
        userImg.setClipToOutline(true);
    }
    private void setGoogleUserImg() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.w("imgURIGOOGLE", "account catch");
            Uri googleProfile = account.getPhotoUrl();
            try{
                Log.w("imgURIGOOGLE", googleProfile.toString());
                userImg = (ImageView) findViewById(R.id.logo);
                userImg.setImageURI(googleProfile);
                GradientDrawable drawable = (GradientDrawable) getApplicationContext().getDrawable(R.drawable.background_rounding);
                userImg.setBackground(drawable);
                userImg.setClipToOutline(true);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    private void startNextActivity(listItem chosen){
        Intent intent = new Intent(UserInfoActivity.this, LabelingActivity.class);
        Bundle newExtras = new Bundle();
        newExtras.putBoolean("isMethod", chosen.isMethod());
        newExtras.putInt("tagId", chosen.getType());
        newExtras.putString("Title", chosen.getTitle());
        newExtras.putString("JWTToken", JWTToken);
        newExtras.putString("desc", chosen.getDesc());
        newExtras.putString("email", userId);
        intent.putExtras(newExtras);
        startActivity(intent);
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
