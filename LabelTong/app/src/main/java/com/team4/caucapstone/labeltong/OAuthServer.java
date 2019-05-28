package com.team4.caucapstone.labeltong;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class OAuthServer {
    private static final String siteURL = "https://www.googleapis.com/";
    private static String code = SignupActivity.Authcode;
    public static OAuthServerIntface oAuthServerIntface = null;
    public static OAuthServerIntface getoAuthServerIntface(){
        if(oAuthServerIntface == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(siteURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            oAuthServerIntface = retrofit.create(OAuthServerIntface.class);

        }
        return oAuthServerIntface;
    }

    public interface OAuthServerIntface {

        // @Headers("Accept: application/json")
        /**
         * The call to request a token
         */
        @POST("oauth2/v4/token")
        @FormUrlEncoded
        Call<OAuthToken> getAccessToken(
                @Field("code") String code,
                @Field("client_id") String client_id,
                @Field("redirect_uri") String redirect_uri,
                @Field("grant_type") String grant_type
        );

    }
    public static boolean validate(@Nullable EditText nameText, EditText emailText,
                                   @Nullable EditText phoneText, String AuthToken, Context context) {
        boolean valid = true;

        String email = emailText.getText().toString();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } /*else if (!email.equals(authEmail)) {
            emailText.setError("enter a authorized email address");
            valid = false;
        } */ else {
            emailText.setError(null);
        }

        if (nameText != null) {
            String name = nameText.getText().toString();
            if (name.isEmpty() || name.length() < 3) {
                nameText.setError("at least 3 characters");
                valid = false;
            } else {
                nameText.setError(null);
            }
            String phone = phoneText.getText().toString();
            if (phone.isEmpty() || phone.length() != 11) {
                phoneText.setError("enter valid phone number without -");
                valid = false;
            } else {
                phoneText.setError(null);
            }
        }

        if (AuthToken == null) {
            Log.d("TOKKEN ERROR", "MISSING  TOKEN");
            Toast.makeText(context,
                    "Please Authorize by Facebook or Google", Toast.LENGTH_LONG).show();
            valid = false;
        }

        return valid;
    }

}
