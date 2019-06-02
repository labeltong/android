package com.team4.caucapstone.labeltong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ortiz.touchview.TouchImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LabelingActivity extends AppCompatActivity implements View.OnTouchListener {
    // Related View
    TouchImageView imgView;
    TextView questionText;
    RadioGroup radioGroup;
    Bitmap baseBitmap;
    Bitmap canvasBitmap;
    Button submitBtn;
    Canvas canvas;
    MediaPlayer mediaPlayer;

    // Data from user info activity
    String JWTToken;
    String email;
    boolean isMethod;
    int tag_id;
    String desc;
    String title;

    // Data from server
    ServerControl.APIServiceIntface apiService;
    String uniqueID;
    String DataURL;
    String question;

    // Static Variables
    static boolean isPlaying = false;
    static boolean network_state = true;
    static int paused_time = 0;

    // Answer realted variables
    int method_type;
    String answer_data;
    ArrayList<PointF> points;
    static int count = 0;

    // Click Event Handler
    long startDown = 0;
    long beforeTouch = 0;
    static final long SCROLL_DURATION = 500;
    static final long DOUBLE_DURATION = 300;


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        TouchImageView view = (TouchImageView) v;
        view.setScaleType(TouchImageView.ScaleType.MATRIX);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startDown = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                long scrollDuration = System.currentTimeMillis() - startDown;
                if (scrollDuration > SCROLL_DURATION)  // This is Scroll
                    break;
                long doubleDuration = System.currentTimeMillis() - beforeTouch;
                if (beforeTouch != 0 && doubleDuration <= DOUBLE_DURATION) // This is Double Click
                    break;
                beforeTouch = System.currentTimeMillis();
                switch (method_type){
                    case 1:
                        if (count == 2)
                            break;
                        Matrix inverse = new Matrix();
                        imgView.getImageMatrix().invert(inverse);
                        float[] pts = {event.getX(), event.getY()};
                        inverse.mapPoints(pts);
                        PointF pos = new PointF(pts[0], pts[1]);
                        points.add(pos);
                        count = count + 1;
                        drawCircle(pos);
                        imgView.invalidate();
                        break;
                    case 3:
                        if (isPlaying){
                            imgView.setImageResource(R.drawable.play);
                            mediaPlayer.pause();
                            paused_time = mediaPlayer.getCurrentPosition();
                            isPlaying = false;
                        }
                        else{
                            isPlaying = true;
                            if (paused_time != 0) {
                                mediaPlayer.seekTo(paused_time);
                            }
                            mediaPlayer.start();
                            imgView.setImageResource(R.drawable.stop);
                        }
                        break;
                }
                break;
        }
        return true;
    }

    private void drawCircle(PointF pos){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(4f);
        float x = pos.x;
        float y = pos.y;
        canvas.drawCircle(x, y, 9, paint);
        if (count % 2 == 0)
            drawRectangle();
        imgView.invalidate();
    }
    private void drawRectangle() {
        Paint paint = new Paint();
        float x = points.get(0).x;
        float y = points.get(0).y;
        float x_1 = points.get(1).x;
        float y_1 = points.get(1).y;
        float max_X = Math.max(x, x_1);
        float min_X = Math.min(x, x_1);
        float max_Y = Math.max(y, y_1);
        float min_Y = Math.min(y, y_1);

        paint.setColor(Color.rgb(0,0,0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        canvas.drawRect(min_X, min_Y, max_X, max_Y, paint);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labeling);
        // Default Settings
        getExtrasFromBundle(getIntent().getExtras());
        mediaPlayer = new MediaPlayer();

        chkNetworkConnected(this);
        viewDefault();
        getNextData();
        Progressbar.progressOFF();
    }
    // Get File From Server
    private void getNextData(){
        Progressbar.progressON(LabelingActivity.this);
        Log.d("GetNextData-TAG", String.valueOf(tag_id));
        points = new ArrayList<>();
        Call<LabelData> comment;
        if (isMethod)
            comment = apiService.getImageByMethod(JWTToken, tag_id);
        else
            comment = apiService.getImageByTag(JWTToken, tag_id);
        comment.enqueue(new Callback<LabelData>() {
            @Override
            public void onResponse(Call<LabelData> call, Response<LabelData> response) {
                try{
                    question = response.body().getQuestion();
                    uniqueID = response.body().getID();
                    DataURL = response.body().getDataPath();
                    Log.w("GetNextData-uniqueid", uniqueID);
                    Log.w("GetNextData-dataurl", DataURL);
                    method_type = Integer.valueOf(response.body().getAnswerType());
                } catch (Exception e) {
                    e.printStackTrace();
                    onDataRequestFailed();
                }
                if (method_type != 3)
                    getBitmapFromURL();
                else
                    getMediaFromURL();
                setQuestionText();
            }
            @Override
            public void onFailure(Call<LabelData> call, Throwable t) {
                Log.d("Get Next Data", "FAIL");
            }
        });
    }

    public void onDataRequestFailed() {
        Progressbar.progressOFF();
        Toast.makeText(getBaseContext(), "You've Done all labeling work!", Toast.LENGTH_LONG).show();
    }

    private void getBitmapFromURL() {
        if (baseBitmap != null) {
            baseBitmap.recycle();
            baseBitmap = null;
            canvasBitmap.recycle();
            canvasBitmap = null;
            count = 0;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url = new URL(DataURL);
                    HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                    myConnection.setDoInput(true);
                    myConnection.connect();
                    InputStream is = myConnection.getInputStream();
                    baseBitmap = BitmapFactory.decodeStream(is);
                    canvasBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);
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
        Progressbar.progressOFF();
        imgView.setImageBitmap(canvasBitmap);
        canvas = new Canvas(canvasBitmap);
    }
    private void getMediaFromURL() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        isPlaying = false;
        mediaPlayer = new MediaPlayer();
        try {
            Log.d("MEDIA", DataURL);
            imgView.setImageResource(R.drawable.play);
            imgView.getLayoutParams().height = 300;
            imgView.getLayoutParams().width = 300;
            imgView.requestLayout();
            mediaPlayer.setDataSource(DataURL);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            Progressbar.progressOFF();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // 질문지들 설정
    private void setQuestionText(){
        RadioButton radioButton;
        radioGroup.removeAllViews();
        if (method_type == 1){// If Question is Bounding Box
            if (!isMethod)
                questionText.setText("Bound " + desc);
            else
                questionText.setText(desc + " : " + question);
            return;
        }
        if(isMethod)
            questionText.setText(desc);
        else
            questionText.setText("Classify " + desc);

        for (int i = 0; i < question.split(",").length; i++) {
            radioButton = new RadioButton(this);
            radioButton.setText(question.split(",")[i]);
            radioGroup.addView(radioButton);
        }
    }
    // Bunlde에서 데이터 읽어오기
    private void getExtrasFromBundle(Bundle extras){
        JWTToken = "Bearer " + extras.getString("JWTToken");
        isMethod = extras.getBoolean("isMethod");
        tag_id = extras.getInt("tagId");
        desc = extras.getString("desc");
        email = extras.getString("email");
        title = extras.getString("Title");
        if (isMethod)
            method_type = tag_id;
    }
    // 초기 TextView 세팅용
    private void viewDefault(){
        apiService = ServerControl.getAPIServerIntface();
        baseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.labeltong);
        canvasBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);

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
            }
        });
    }
    // 정답 가져오기
    private boolean getAnswer() {
        answer_data = "";
        if (method_type == 1) { // Bounding Box
            if(points.size() != 2)
                return false;
            float x = points.get(0).x;
            float y = points.get(0).y;
            float x_1 = points.get(1).x;
            float y_1 = points.get(1).y;
            float max_X = Math.max(x, x_1);
            float min_X = Math.min(x, x_1);
            float max_Y = Math.max(y, y_1);
            float min_Y = Math.min(y, y_1);
            if (max_X - min_X <= 15 || max_Y - min_Y <= 15) {
                MyAlertDialog.showWarning(this, "Labeling",
                        "Bounded Area is Too Small!\nPlease Try again");
                Log.d("Answer", "False-Toobig");
                return false;
            }
            float area = (max_X - min_X) * (max_Y - min_Y);
            if (area * 3 > baseBitmap.getHeight() * baseBitmap.getWidth()) {
                MyAlertDialog.showWarning(this, "Labeling",
                        "Bounded Area is Too Big!\nPlease Try again");
                Log.d("Answer", "False-Toobig");
                return false;
            }
            answer_data = min_X + "," + max_Y + "," + max_X + "," + min_Y;
        }
        else{
            int checkedRadioButton = radioGroup.getCheckedRadioButtonId();
            if (checkedRadioButton != -1)
                answer_data = ((RadioButton)findViewById(checkedRadioButton)).getText().toString();
        }
        if (answer_data.equals("")) {
            MyAlertDialog.showWarning(this, "Labeling",
                    "Please make an Answer!");
            return false;
        }
        return true;
    }
    // Post Answer To Server
    private void postAnswer(){
        if (!getAnswer())
            return;
        int post_id = Integer.valueOf(uniqueID);
        AnswerData answerData = new AnswerData();
        answerData.setAnswer_data(answer_data);
        answerData.setData_id(post_id);
        answerData.setEmail(email);
        Call<ResponseBody> comment = apiService.postAnswer(JWTToken, answerData);
        comment.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try{
                        getNextData();
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

    @Override
    public void onBackPressed(){
        if (method_type == 1 && points.size() > 0) {
            points.remove(points.size() - 1);
            count--;
            canvasBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);
            imgView.setImageBitmap(canvasBitmap);
            canvas = new Canvas(canvasBitmap);
            if (count != 0)
                drawCircle(points.get(0));
        }
        else if (method_type == 3) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            super.onBackPressed();
        }
        else{
            super.onBackPressed();
        }
    }
}