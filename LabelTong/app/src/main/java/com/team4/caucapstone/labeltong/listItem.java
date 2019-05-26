package com.team4.caucapstone.labeltong;

public class listItem {
    private int image;
    private String title;
    private String desc;


    public listItem(int image,String title, String desc) {
        this.title = title;
        this.image = image;
        this.desc = desc;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
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




}
