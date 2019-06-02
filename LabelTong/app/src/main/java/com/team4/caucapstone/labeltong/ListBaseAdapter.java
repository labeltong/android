package com.team4.caucapstone.labeltong;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class ListBaseAdapter extends BaseAdapter {

    Context context;
    ArrayList<listItem> items;

    public ListBaseAdapter(Context context, ArrayList<listItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size() ;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Montserrat-Regular.ttf");
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.listview_account,null);

            viewHolder.image= (ImageView)convertView.findViewById(R.id.image);
            viewHolder.title= (TextView)convertView.findViewById(R.id.title);
            viewHolder.desc= (TextView)convertView.findViewById(R.id.desc);

            viewHolder.title.setTypeface(type);
            viewHolder.desc.setTypeface(type);

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        listItem items = (listItem) getItem(position);

        viewHolder.image.setImageBitmap(items.getImage());
        viewHolder.title.setText(items.getTitle());
        viewHolder.desc.setText(items.getDesc());

        return convertView;
    }

    private class ViewHolder{
        ImageView image;
        TextView title;
        TextView desc;
    }

}
