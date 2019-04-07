package com.team4.caucapstone.labeltong;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import static com.team4.caucapstone.labeltong.BoardActivity.*;

public class LabelingActivity extends AppCompatActivity {
    //FragmentManager manager = getSupportFragmentManager();
    //final Fragment imgLabeling = new ImgFragment();
    //FragmentTransaction transaction;
    ImageView imgView;
    TextView headerText;
    TextView questionText;
    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labeling);

        headerText = (TextView) findViewById(R.id.labelImgTitle);
        questionText = (TextView) findViewById(R.id.labelImgQuestion);
        imgView = (ImageView) findViewById(R.id.labelImg);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] values = new int[2];
                int[] test = new int[2];
                v.getLocationInWindow(values);
                v.getLocationOnScreen(test);

                Log.d("X&Y in WINDOW", values[0] + " " + values[1]);
                Log.d("X&Y in SCREEN", test[0] + " " + test[1]);
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.labelImgRadio);
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);
        setLabelSettings();

        //transaction = manager.beginTransaction();
        //transaction.add(R.id.frameLayout, settingFragment).hide(settingFragment);
        //transaction.add(R.id.labelFrame, imgLabeling).commit();

    }

    private void setLabelSettings(){
        switch (getIntent().getIntExtra("INFO", INTENT_ERROR)){
            case METHOD_BOUNDING:
                headerText.setText("Bounding BOX");
                questionText.setText("PLEASE BOUND AREA ABOUT TEST");
                for (int i = 0; i < 4; i++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText("Point" + (i+1));
                    radioGroup.addView(radioButton);
                }
                break;
            case METHOD_CLASSIFY:
                headerText.setText("Clssification");
                break;
            case METHOD_SENTIMENT:
                headerText.setText("Sentiment");
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

}
