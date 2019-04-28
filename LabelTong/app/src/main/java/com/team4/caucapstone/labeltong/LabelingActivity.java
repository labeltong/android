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

import static com.team4.caucapstone.labeltong.BoardActivity.*;

public class LabelingActivity extends AppCompatActivity implements View.OnTouchListener {
    AppCompatDialog progressDialog;
    TouchImageView imgView;
    TextView headerText;
    TextView questionText;
    RadioGroup radioGroup;
    Bitmap baseBitmap;
    Button submitBtn;
    Canvas canvas;
    MediaPlayer mediaPlayer;
    String imgBase64;

    int demo = 0;

    static int method_state = METHOD_BOUNDING;
    static boolean isPlaying = false;
    static boolean network_state = true;

    ArrayList<String> choice;
    ArrayList<Pos> points;

    //Get SoundFile From Server
    private void requestNewSong(){
        mediaPlayer = MediaPlayer.create(this, R.raw.sentimentsample);
        imgView.setImageResource(R.drawable.play);
        isPlaying = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        TouchImageView view = (TouchImageView) v;
        view.setScaleType(TouchImageView.ScaleType.MATRIX);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                switch (method_state){
                    case METHOD_BOUNDING:
                        Pos pos = new Pos(event.getX(), event.getY());
                        points.add(pos);
                        drawCircle(pos);
                        imgView.invalidate();
                        break;
                    case METHOD_SENTIMENT:
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

    // List below End code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labeling);
        choice = new ArrayList<>();
        chkNetworkConnected(this);

        baseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.labeltong);

        headerText = (TextView) findViewById(R.id.labelImgTitle);
        questionText = (TextView) findViewById(R.id.labelImgQuestion);
        imgView = (TouchImageView) findViewById(R.id.labelImg);
        imgView.setOnTouchListener(this);
        radioGroup = (RadioGroup) findViewById(R.id.labelImgRadio);
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);
        submitBtn = (Button) findViewById(R.id.labelImgSubmit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (method_state != METHOD_SENTIMENT)
                    requestNewImg();
                else
                    requestNewSong();
            }
        });
        setLabelSettings();
        // Initial Request
        if (method_state == METHOD_SENTIMENT)
            requestNewSong();
        else
            requestNewImg();

    }

    //Get imageFile From Server
    private void requestNewImg(){
        if (!network_state){
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
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
            alertDialogBuilder.create();
            alertDialogBuilder.show();
            return;
        }


        choice = new ArrayList<>();
        points = new ArrayList<>();
        progressOn(this);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL imageRes;
                    if (method_state == METHOD_BOUNDING)
                        imageRes = new URL("http://54.180.195.179:19432/dataset/list/db_test2/get");
                    else
                        imageRes = new URL("http://54.180.195.179:19432/dataset/list/db_test/get");

                    HttpURLConnection myConnection =
                            (HttpURLConnection) imageRes.openConnection();
                    if (myConnection.getResponseCode() == 200) {
                        Log.d("SERVER", "SUCCESS");
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader =
                                new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);
                        jsonReader.beginObject(); // Start processing the JSON object
                        while (jsonReader.hasNext()) { // Loop through all keys
                            String key = jsonReader.nextName(); // Fetch the next key
                            if (key.equals("base_64_data"))  // Check if desired key
                                imgBase64 = jsonReader.nextString();
                            else if (key.equals("Dataq")) {
                                jsonReader.beginArray();
                                while(jsonReader.hasNext()) {
                                    String tmp = jsonReader.nextString();
                                    choice.add(tmp);
                                }
                                jsonReader.endArray();
                                break;
                            }
                            else
                                jsonReader.skipValue(); // Skip values of other keys
                        }
                        jsonReader.endObject();
                    }
                    else
                        Log.d("SERVER", "CONNECTION FAIL CODE " + myConnection.getResponseCode());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        try {
            t.join();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        progressOFF();
        byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
        Bitmap decodeByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Bitmap tmpBitmap = Bitmap.createBitmap(decodeByte.getWidth(), decodeByte.getHeight(), Bitmap.Config.RGB_565);
        canvas = new Canvas(tmpBitmap);
        canvas.drawBitmap(decodeByte, 0, 0, null);
        imgView.setImageBitmap(tmpBitmap);
        imgBase64 = null;
        if (method_state == METHOD_CLASSIFY)
            setQuestionText();

    }
    //Loading Bar 생성
    private void progressOn(Activity activity){
        if (activity == null || activity.isFinishing())
            return;

        Log.d("TEST", "Thread start");
        progressDialog = new AppCompatDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.progress);
        progressDialog.show();
    }
    //Loading Bar 해제
    public void progressOFF() {
        Log.d("TEST", "Thread end");
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }
    // 초기 TextView 세팅용
    private void setLabelSettings(){
        RadioButton radioButton = new RadioButton(this);
        radioButton.setText("Object");
        radioButton.setChecked(true);
        radioGroup.addView(radioButton);

        switch (getIntent().getIntExtra("INFO", INTENT_ERROR)){
            case METHOD_BOUNDING:
                method_state = METHOD_BOUNDING;
                headerText.setText("Bounding BOX");
                questionText.setText("PLEASE BOUND AREA ABOUT TEST");
                break;
            case METHOD_CLASSIFY:
                method_state = METHOD_CLASSIFY;
                headerText.setText("Clssification");
                questionText.setText("PLEASE CLASSIFY ABOUT TEST");
                break;
            case METHOD_SENTIMENT:
                method_state = METHOD_SENTIMENT;
                headerText.setText("Sentiment");
                questionText.setText("PLEASE LEASTEN TO SOUND AND SELECT RIGHT SENTIMENT");
                break;
            case TOPIC_CAT:
                headerText.setText("Cat");
                break;
            case TOPIC_CAR:
                headerText.setText("Car");
                break;
            case TOPIC_ROADSIGN:
                headerText.setText("Road Sign");
                break;
            case TOPIC_EMOTION:
                headerText.setText("Emotion");
                break;
        }
    }
    // 질문지들 설정
    private void setQuestionText(){
        RadioButton radioButton;
        radioGroup.removeAllViews();
        for (String newTxt : choice) {
            radioButton = new RadioButton(this);
            radioButton.setText(newTxt);
            radioGroup.addView(radioButton);
        }
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