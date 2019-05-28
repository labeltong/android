package com.team4.caucapstone.labeltong;

import android.graphics.Bitmap;
import android.net.Uri;

public class listItem {
    private Bitmap image;
    private String title;
    private String desc;
    private boolean isMethod; // If true, by method, If false, by topic
    private int type;

    public listItem(Bitmap image, String title, String desc,
                    boolean isMethod, int type) {
        this.title = title;
        this.image = image;
        this.desc = desc;
        this.isMethod = isMethod;
        this.type = type;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isMethod() {
        return isMethod;
    }

    public int getType() {
        return type;
    }

    public void setMethod(boolean method) {
        isMethod = method;
    }
}
