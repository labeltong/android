package com.team4.caucapstone.labeltong;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
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

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.team4.caucapstone.labeltong.SendDeviceDetails.GET_METHOD;
import static com.team4.caucapstone.labeltong.SendDeviceDetails.POST_METHOD;

public class SignupActivity extends AppCompatActivity {

    //private static final String OAUTH_SCOPE = "https://www.googleapis.com/auth/webmasters";
    private static final String OAUTH_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private static final String CODE = "code";
    static final String CLIENT_ID = "391963958726-r75h6aaa0ef8h5i4kop9vothtlarbcu0.apps.googleusercontent.com";
    private static final String REDIRECT_URI = "com.team4.caucapstone.labeltong:/oauth2redirect";

    //Authorization
    static String AUTHORIZATION_CODE;
    private static final String GRANT_TYPE = "authorization_code";

    //Response
    static String Authcode;

    Button testButton;
    EditText nameText;
    EditText emailText;
    EditText phoneText;
    LoginButton fbAuthButton;
    Button signupButton;
    TextView loginLink;

    private static final String POST_URL = "http://54.180.195.179:13230/auth2";

    CallbackManager callbackManager;
    SendDeviceDetails sendDeviceDetails;

    static String authToken;
    String authEmail;
    String authName;
    String authId;

    String name;
    String email;
    String phone;

    @Override
    protected void onResume() {
        super.onResume();
        //Check response not null
        Uri data = getIntent().getData();
        if (data != null && !TextUtils.isEmpty(data.getScheme())){
            String code = data.getQueryParameter(CODE);
            if (!TextUtils.isEmpty(code)) {
                AUTHORIZATION_CODE = code;
                Log.d("GOOGLEOAUTH", code);
                // Using Retrofit builder getting Authorization code
                Retrofit.Builder builder = new Retrofit.Builder()
                        .baseUrl("https://www.googleapis.com/")
                        .addConverterFactory(GsonConverterFactory.create());

                Retrofit retrofit = builder.build();
                OAuthServer.OAuthServerIntface oAuthServerIntface = retrofit.create(OAuthServer.OAuthServerIntface.class);
                final Call<OAuthToken> accessTokenCall = oAuthServerIntface.getAccessToken(
                        AUTHORIZATION_CODE,
                        CLIENT_ID,
                        REDIRECT_URI,
                        GRANT_TYPE
                );

                accessTokenCall.enqueue(new Callback<OAuthToken>() {
                    @Override
                    public void onResponse(Call<OAuthToken> call, Response<OAuthToken> response) {
                        //authToken = response.body().getAccessToken();
                        authToken = response.body().getAccessToken();
                        String refreshToken = response.body().getRefreshToken();
                        Log.d("GOOGLEOAUTH", authToken);
                        Log.d("GOOGLEOAUTH", refreshToken);
                        testButton.setText("Google Signed In!");
                        fbAuthButton.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<OAuthToken> call, Throwable t) {
                        Toast.makeText(SignupActivity.this, "GOOGLE TOKEN ERROR",Toast.LENGTH_LONG).show();
                    }
                });
            }
            if(TextUtils.isEmpty(code)) {
                //a problem occurs, the user reject our granting request or something like that
                Toast.makeText(this, "User reject our request",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        facebookSetUp();

        nameText = (EditText) findViewById(R.id.input_name);
        emailText = (EditText) findViewById(R.id.input_email);
        phoneText = (EditText) findViewById(R.id.input_phone);

        signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink = (TextView) findViewById(R.id.link_login);
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        testButton = (Button) findViewById(R.id.btn_test);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://accounts.google.com/o/oauth2/v2/auth" +
                        "?client_id=" + CLIENT_ID + "&response_type=" + CODE +
                        "&redirect_uri=" + REDIRECT_URI + "&scope=" + OAUTH_SCOPE));
                if(intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

    }

    private void facebookSetUp(){
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        fbAuthButton = (LoginButton) findViewById(R.id.btn_fb_auth);
        fbAuthButton.setReadPermissions("email");
        fbAuthButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                authToken = loginResult.getAccessToken().getToken();

                requestMe(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Toast.makeText(SignupActivity.this, "Facebook Login Cancel", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(SignupActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    public void requestMe(AccessToken token) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try{
                            authEmail = object.getString("email");
                            authName = object.getString("name");
                            authId = object.getString("id");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, name, email");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }
        authEmail = email;
        authName = name;
        if (authId == null) {
            authId = "Google";
        }

        signupButton.setEnabled(false);
        postToServer();
        while(true){
            if (sendDeviceDetails.isConnFinish())
                break;
        }
        if (sendDeviceDetails.isErrorHappen())
            onSignupFailed();
        else
            onSignupSuccess();

        onSignupSuccess();

    }

    private void postToServer() {
        JSONObject postData = new JSONObject();
        try{
            postData.put("email", email);
            postData.put("token", authToken);
            postData.put("name", name);
            postData.put("phone_num", phone);
            Log.d("TOKENTEST", postData.toString());
            sendDeviceDetails = new SendDeviceDetails();
            sendDeviceDetails.execute(POST_URL, postData.toString(), POST_METHOD);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onSignupSuccess() {
        signupButton.setEnabled(true);
        Intent resultIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putString("AuthEmail", authEmail);
        extras.putString("AuthToken", authToken);
        extras.putString("AuthName", authName);
        extras.putString("AuthId", authId);
        resultIntent.putExtras(extras);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void onSignupFailed() {
        if (sendDeviceDetails.getResponseCode() == 400)
            Toast.makeText(getBaseContext(), "Already User", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_LONG).show();

        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        name = nameText.getText().toString();
        email = emailText.getText().toString();
        phone = phoneText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("at least 3 characters");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } /*else if (!email.equals(authEmail)) {
            emailText.setError("enter a authorized email address");
            valid = false;
        } */else {
            emailText.setError(null);
        }

        if (phone.isEmpty() || phone.length() != 11) {
            phoneText.setError("enter valid phone number without -");
            valid = false;
        } else {
            phoneText.setError(null);
        }

        if (authToken == null) {
            Log.d("TOKKEN ERROR", "MISSING  TOKEN");
            Toast.makeText(SignupActivity.this,
                    "Please Authorize by Facebook or Google", Toast.LENGTH_LONG).show();
            valid = false;
        }

        return valid;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        disconnectFromFacebook();
        super.onDestroy();
    }
    public void disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                LoginManager.getInstance().logOut();
            }
        }).executeAsync();
    }
}
