package com.team4.caucapstone.labeltong;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static com.team4.caucapstone.labeltong.BoardActivity.INTENT_ERROR;
import static com.team4.caucapstone.labeltong.BoardActivity.METHOD_BOUNDING;
import static com.team4.caucapstone.labeltong.BoardActivity.METHOD_CLASSIFY;
import static com.team4.caucapstone.labeltong.BoardActivity.METHOD_SENTIMENT;
import static com.team4.caucapstone.labeltong.BoardActivity.TOPIC_CAR;
import static com.team4.caucapstone.labeltong.BoardActivity.TOPIC_CAT;
import static com.team4.caucapstone.labeltong.BoardActivity.TOPIC_EMOTION;
import static com.team4.caucapstone.labeltong.BoardActivity.TOPIC_ROADSIGN;

public class ImgFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_labelimg,container,false);
        /*
        // Sound Labeling fragment must added


        Log.d("IMGVIEW", imgView.toString());
        //ToDo : Get Question and image from server

        */

    }
}

