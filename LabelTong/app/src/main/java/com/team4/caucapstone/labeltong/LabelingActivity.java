package com.team4.caucapstone.labeltong;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ortiz.touchview.TouchImageView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LabelingActivity extends AppCompatActivity implements View.OnTouchListener {
    // Related View
    TouchImageView imgView;
    TextView questionText;
    RadioGroup radioGroup;
    Bitmap baseBitmap;
    Button submitBtn;
    Canvas canvas;
    MediaPlayer mediaPlayer;

    // Data from user info activity
    String JWTToken;
    String email;
    boolean isMethod;
    int type;
    String desc;
    String title;

    // Data from server
    String uniqueID;
    String ImageURL;
    String question;

    int demo = 0;

    //Retrofit
    Retrofit retrofit;
    ApiService apiService;

    // Static Variables
    int method_type;
    static boolean isPlaying = false;
    static boolean network_state = true;

    ArrayList<Pos> points;
    String answer_data;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        TouchImageView view = (TouchImageView) v;
        view.setScaleType(TouchImageView.ScaleType.MATRIX);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                switch (type){
                    case 1:
                        Pos pos = new Pos(event.getX(), event.getY());
                        points.add(pos);
                        drawCircle(pos);
                        imgView.invalidate();
                        break;
                    case 2:
                        if (isPlaying){
                            imgView.setImageResource(R.drawable.play);
                            mediaPlayer.stop();
                            isPlaying = false;
                        }
                        else{
                            isPlaying = true;
                            mediaPlayer.start();
                            imgView.setImageResource(R.drawable.stop);
                        }
                        break;
                }

                break;
        }
        return true;
    }

    private void drawCircle(Pos pos){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5f);
        float x = 0;
        float y = 0;
        if (demo == 0){
            x = (float) (528.47266);
            y = (float) (375.3125);
            demo++;
        }
        else if (demo == 1){
           x = (float) (835.74927);
           y = (float) (375.3125);
            demo++;
        }
        else if (demo == 2) {
            x = (float) (498.44702);
            y = (float) (700.4492);
            demo++;
        }
        else if (demo == 3) {
            x = (float) (835.44702);
            y = (float) (724.4492);
            demo++;
        }
        canvas.drawCircle(x, y, 10, paint);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labeling);

        // Default Settings
        getExtrasFromBundle(getIntent().getExtras());
        chkNetworkConnected(this);
        viewDefault();
        getNextData();
    }
    // Get File From Server
    private void getNextData(){
        if (isMethod) {
            Call<LabelData> comment = apiService.getImageByMethod("Bearer " +
                            JWTToken, type);
            comment.enqueue(new Callback<LabelData>() {
                @Override
                public void onResponse(Call<LabelData> call, Response<LabelData> response) {
                    question = response.body().getQuestion();
                    uniqueID = response.body().getID();
                    ImageURL = response.body().getDataPath();
                    if (method_type != 3)
                        getBitmapFromURL();
                    else
                        getMediaFromURL();
                }
                @Override
                public void onFailure(Call<LabelData> call, Throwable t) {
                    Log.d("RETRO_MTDImg", "FAIL");
                }
            });
        }
        else {
            Call<LabelData> comment = apiService.getImageByTag("Bearer " +
                    JWTToken, type);
            comment.enqueue(new Callback<LabelData>() {
                @Override
                public void onResponse(Call<LabelData> call, Response<LabelData> response) {
                    question = response.body().getQuestion();
                    uniqueID = response.body().getID();
                    ImageURL = response.body().getDataPath();
                    method_type = Integer.valueOf(response.body().getAnswerType());
                    if (method_type != 3)
                        getBitmapFromURL();
                    else
                        getMediaFromURL();
                }
                @Override
                public void onFailure(Call<LabelData> call, Throwable t) {
                    Log.d("RETRO_TagImg", "FAIL");
                }
            });
        }
        setQuestionText();
    }
    private void getBitmapFromURL() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url = new URL(ImageURL);
                    HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                    myConnection.setDoInput(true);
                    myConnection.connect();
                    InputStream is = myConnection.getInputStream();
                    baseBitmap = BitmapFactory.decodeStream(is);
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
        imgView.setImageBitmap(baseBitmap);
    }
    private void getMediaFromURL() {

    }

    // 질문지들 설정
    private void setQuestionText(){
        RadioButton radioButton;
        radioGroup.removeAllViews();
        if (method_type == 1){// If Question is not Bounding Box
            if (!isMethod)
                questionText.setText("Bound " + desc);
            return;
        }
        if(isMethod)
            questionText.setText("Classify " + desc);

        for (int i = 0; i < question.split(",").length; i++) {
            radioButton = new RadioButton(this);
            radioButton.setText(question.split(",")[i]);
            radioGroup.addView(radioButton);
        }
    }

    private void getExtrasFromBundle(Bundle extras){
        JWTToken = extras.getString("JWTToken");
        isMethod = extras.getBoolean("isMethod");
        type = extras.getInt("tagId");
        desc = extras.getString("desc");
        email = extras.getString("email");
        title = extras.getString("Title");
        if (isMethod)
            method_type = type;
    }
    // 초기 TextView 세팅용
    private void viewDefault(){
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ApiService.API_URL)
                .addConverterFactory(GsonConverterFactory.create());
        retrofit = builder.build();
        apiService = retrofit.create(ApiService.class);

        baseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.labeltong);

        questionText = (TextView) findViewById(R.id.labelImgQuestion);
        questionText.setText(desc);

        imgView = (TouchImageView) findViewById(R.id.labelImg);
        imgView.setOnTouchListener(this);

        setTitle(title);

        radioGroup = (RadioGroup) findViewById(R.id.labelImgRadio);
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);

        submitBtn = (Button) findViewById(R.id.labelImgSubmit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postAnswer();
                getNextData();
            }
        });
    }
    // Post Answer To Server
    private void postAnswer(){
        Call<ResponseBody> comment = apiService.postAnswer("Bearer " +
                JWTToken, email, uniqueID, answer_data);
        comment.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try{
                        Log.d("RETRO_TagImg", response.body().string());
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("RETRO_PostAnswer", "FAIL");
            }
        });
    }
    // 네트워크 연결 상태 체크
    private static void chkNetworkConnected(final Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Network Error");
        alertDialogBuilder.setMessage("Internet Connection Lost!")
                .setCancelable(true)
                .setPositiveButton("Go back",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                network_state = false;
                                dialog.cancel();
                            }
                        });
        manager.registerNetworkCallback(
                builder.build(),
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        network_state = true;
                    }

                    @Override
                    public void onLost(Network network) {
                        alertDialogBuilder.create();
                        alertDialogBuilder.show();
                    }
                }

        );
    }
}

//X, Y좌표를 모두 저장하기 위한 임시 class
class Pos {
    float x, y;
    Pos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}