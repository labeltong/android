package com.team4.caucapstone.labeltong;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    public static final String API_URL = "http://54.180.195.179:13230/";

    @FormUrlEncoded
    @POST("auth")
    Call<SignUpModel>postSignUP(@Field("email") String email, @Field("token") String token,
                                 @Field("name") String name, @Field("phone_num")String phone_num);
    @FormUrlEncoded
    @POST("dataset/answer/answer")
    Call<ResponseBody>postAnswer(@Header("Authorization") String jwtToken,
                                 @Field("email") String answer, @Field("data_id") String data_id,
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
