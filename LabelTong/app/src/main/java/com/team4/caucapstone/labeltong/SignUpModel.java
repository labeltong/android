package com.team4.caucapstone.labeltong;


public class SignUpModel {
    private String email;
    private String token;
    private String name;
    private String phone_num;

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhone_num() {
        return phone_num;
    }

    public String getToken() {
        return token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
