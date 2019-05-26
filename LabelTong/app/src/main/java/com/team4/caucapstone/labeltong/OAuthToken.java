package com.team4.caucapstone.labeltong;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OAuthToken {
    @SerializedName("access_token")
    @Expose
    private String accessToken;
    @SerializedName("expires_in")
    @Expose
    private Long expiresIn;
    @SerializedName("token_type")
    @Expose
    private String tokenType;
    @SerializedName("refresh_token")
    @Expose
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
