package com.team4.caucapstone.labeltong;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AnswerData {
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("data_id")
    @Expose
    private int data_id;
    @SerializedName("answer_data")
    @Expose
    private String answer_data;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getData_id() {
        return data_id;
    }

    public void setData_id(int data_id) {
        this.data_id = data_id;
    }

    public String getAnswer_data() {
        return answer_data;
    }

    public void setAnswer_data(String answer_data) {
        this.answer_data = answer_data;
    }
}
