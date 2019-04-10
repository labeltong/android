package com.team4.caucapstone.labeltong;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ortiz.touchview.TouchImageView;
import static com.team4.caucapstone.labeltong.BoardActivity.*;

public class LabelingActivity extends AppCompatActivity implements View.OnTouchListener {
    ImageView imgView;
    TextView headerText;
    TextView questionText;
    RadioGroup radioGroup;
    Bitmap baseBitmap;
    Button submitBtn;

    int[][] boxArea;
    int boxCount = 0;
    int method_state;

    private static final String TAG = "Touch";
    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 4 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int TOUCH = 3;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labeling);

        baseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.labeltong);

        headerText = (TextView) findViewById(R.id.labelImgTitle);
        questionText = (TextView) findViewById(R.id.labelImgQuestion);
        imgView = (ImageView) findViewById(R.id.labelImg);

        imgView.setOnTouchListener(this);

        radioGroup = (RadioGroup) findViewById(R.id.labelImgRadio);
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);
        setLabelSettings();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        dumpEvent(event);
        // Handle touch events here...
        // ToDo : Test getRawW,Y Functions Working Properly
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                savedMatrix.set(matrix);
                start.set(event.getRawX(), event.getRawY());
                Log.d(TAG, "mode=DRAG"); // write to LogCat
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted
                if (method_state != METHOD_BOUNDING) break;
                if (boxCount == 5) {
                    imgView.setImageBitmap(drawCircle(boxArea));
                }
                else if (boxCount == 0) {
                    boxCount++;
                }
                else if ((event.getRawX() - start.x) == 0 && (event.getRawY() - start.y) == 0) {
                    boxArea[boxCount - 1][0] = (int) start.x;
                    boxArea[boxCount - 1][1] = (int) start.y;
                    Log.d(TAG, boxCount + " : X/Y -> " + start.x + "/" + start.y);
                    boxCount++;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP: // second finger lifted
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getRawX() - start.x,
                            event.getRawY() - start.y); // create the transformation in the matrix  of points
                }
                else if (mode == ZOOM) {
                    // pinch zooming
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f)
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix); // display the transformation on screen
        return true; // indicate event was handled
    }

    private Bitmap drawCircle(int position[][]) {
        Bitmap bitmap = baseBitmap.copy(Bitmap.Config.ARGB_4444, true);
        bitmap.setPixel(1335, 813, 0);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(10);
        // ToDo: By Id number, change color
        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            Log.d("TEST", "NOTHING");
        }
        else{
            RadioButton selectedRadioButton = (RadioButton) findViewById(selectedRadioButtonId);
            String selectedRadioButtonText = selectedRadioButton.getText().toString().substring(2);
            Log.d("radioButtonID", selectedRadioButtonText);
        }
        paint.setColor(Color.BLACK);
        return bitmap;
    }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event)
    {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }


    private void setLabelSettings(){
        RadioButton radioButton;
        switch (getIntent().getIntExtra("INFO", INTENT_ERROR)){
            case METHOD_BOUNDING:
                method_state = METHOD_BOUNDING;
                boxArea = new int[4][2];
                headerText.setText("Bounding BOX");
                questionText.setText("PLEASE BOUND AREA ABOUT TEST");
                radioButton = new RadioButton(this);
                radioButton.setText("Object");
                radioGroup.addView(radioButton);
                break;
            case METHOD_CLASSIFY:
                method_state = METHOD_CLASSIFY;
                headerText.setText("Clssification");
                questionText.setText("PLEASE CLASSIFY ABOUT TEST");
                for (int i = 0; i < 4; i++) {
                    radioButton = new RadioButton(this);
                    radioButton.setText("ANS" + (i+1));
                    radioGroup.addView(radioButton);
                }
                break;
            case METHOD_SENTIMENT:
                method_state = METHOD_SENTIMENT;
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
