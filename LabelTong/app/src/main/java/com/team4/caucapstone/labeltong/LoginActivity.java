package com.team4.caucapstone.labeltong;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_SIGNUP = 0;
    static boolean ret;

    EditText emailText;
    Button loginButton;
    TextView signupLink;
    LoginButton fbAuthButton;

    String authToken;
    String authEmail;
    String authName;
    String authId;
    String JWTToken;

    CallbackManager callbackManager;

    Retrofit retrofit;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailText = (EditText) findViewById(R.id.input_email_signin);
        facebooksetting();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ApiService.API_URL)
                .addConverterFactory(GsonConverterFactory.create());
        retrofit = builder.build();
        apiService = retrofit.create(ApiService.class);

        loginButton = (Button)findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                authEmail = emailText.getText().toString();
                authToken = "TEST";
                tryLogin();
            }
        });

        signupLink = (TextView) findViewById(R.id.link_signup);
        signupLink.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
               Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }
    private void facebooksetting(){
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        fbAuthButton = (LoginButton) findViewById(R.id.btn_fb_auth_login);
        fbAuthButton.setReadPermissions("email");
        fbAuthButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                authToken = loginResult.getAccessToken().getToken();
                requestMe(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Facebook Login Cancel", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void tryLogin(){
        if (OAuthServer.validate(null, emailText, null,
                authToken, this))
            getJWT();
    }
    private void getJWT() {
        Call<ResponseBody> comment = apiService.getComment(authEmail, authToken);
        comment.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JWTToken = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startNextActivity();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Fail to Login",Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK){
                Bundle extras = data.getExtras();
                authEmail = extras.getString("AuthEmail");
                authToken = extras.getString("AuthToken");
                authName = extras.getString("AuthName");
                authId = extras.getString("AuthId");
                getJWT();
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    public void requestMe(AccessToken token) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try{
                            authEmail = object.getString("email");
                            authName = object.getString("id");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }
    @Override
    public void onDestroy() {
        disconnectFromFacebook();
        super.onDestroy();
    }

    private void startNextActivity(){
        Intent intent = new Intent(LoginActivity.this, UserInfoActivity.class);
        Bundle extras = new Bundle();
        extras.putString("AuthEmail", authEmail);
        extras.putString("AuthToken", authToken);
        extras.putString("AuthName", authName);
        extras.putString("AuthId", authId);
        extras.putString("JWT", JWTToken);
        intent.putExtras(extras);
        startActivity(intent);
        finish();
    }

    public void disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null)
            return; // already logged out

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/",
                null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                LoginManager.getInstance().logOut();
            }
        }).executeAsync();
    }
}
