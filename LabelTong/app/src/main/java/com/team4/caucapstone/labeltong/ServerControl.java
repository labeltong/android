package com.team4.caucapstone.labeltong;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class ServerControl {
    private static final String GOOGLE_URL = "https://www.googleapis.com/";
    private static final String API_URL = "http://54.180.195.179:13230/";

    public static ServerControl.OAuthServerIntface oAuthServerIntface = null;
    public static ServerControl.APIServiceIntface apiServerIntface = null;

    public static ServerControl.OAuthServerIntface getGoogleServerIntface(){
        if(oAuthServerIntface == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(GOOGLE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            oAuthServerIntface = retrofit.create(ServerControl.OAuthServerIntface.class);
        }
        return oAuthServerIntface;
    }

    public static ServerControl.APIServiceIntface getAPIServerIntface(){
        if(apiServerIntface == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiServerIntface = retrofit.create(ServerControl.APIServiceIntface.class);
        }
        return apiServerIntface;
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
                @Field("client_secret") String client_secret,
                @Field("redirect_uri") String redirect_uri,
                @Field("grant_type") String grant_type
        );
        @POST("oauth2/v4/token")
        Call<ResponseBody> testAccessToken(@Body RequestBody body);
    }
    public interface APIServiceIntface {
        @FormUrlEncoded
        @POST("auth")
        Call<SignUpModel>postSignUP(@Field("email") String email, @Field("token") String token,
                                    @Field("name") String name, @Field("phone_num")String phone_num);
        @FormUrlEncoded
        @POST("answer/answer")
        Call<ResponseBody>postAnswer(@Header("Authorization") String jwtToken,
                                     @Field("email") String email, @Field("data_id") int data_id,
                                     @Field("answer_data") String answer_data);

        @GET("info")
        Call<UserData>getUserInfo(@Header("Authorization") String jwtToken);
        @GET("auth")
        Call<ResponseBody>getComment(@Query("email") String email, @Query("token") String token);
        @GET("dataset/tags")
        Call<List<TagList>>getTagList(@Header("Authorization") String jwtToken);
        @GET("dataset/dmethods/{methodid}")
        Call<LabelData>getImageByMethod(@Header("Authorization") String jwtToken, @Path("methodid") int methodid);
        @GET("dataset/dtags/{uniqueTagID}")
        Call<LabelData>getImageByTag(@Header("Authorization") String jwtToken, @Path("uniqueTagID") int uniqueTagID);
    }

    public static boolean validate(@Nullable EditText nameText, EditText emailText,
                                   @Nullable EditText phoneText, String AuthToken, String AuthEmail, Context context) {
        boolean valid = true;

        String email = emailText.getText().toString();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else if (!email.equals(AuthEmail)) {
            emailText.setError("enter a authorized email address");
            valid = false;
        }  else {
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
