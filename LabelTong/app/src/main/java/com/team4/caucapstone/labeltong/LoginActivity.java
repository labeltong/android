package com.team4.caucapstone.labeltong;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONException;
import org.json.JSONObject;

import static com.team4.caucapstone.labeltong.SendDeviceDetails.GET_METHOD;
import static com.team4.caucapstone.labeltong.SendDeviceDetails.POST_METHOD;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_SIGNUP = 0;

    EditText emailText;
    Button loginButton;
    TextView signupLink;
    LoginButton fbAuthButton;

    String authToken;
    String authEmail;
    String JWTToken;

    CallbackManager callbackManager;
    SendDeviceDetails sendDeviceDetails;

    @Override
    protected void onStart(){
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null)
            Log.d("TEST", account.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailText = (EditText) findViewById(R.id.input_email_signin);
        facebooksetting();

        loginButton = (Button)findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                if (canLogin())
                    startNextActivity();
                else
                    Toast.makeText(LoginActivity.this, "Login Fail", Toast.LENGTH_LONG).show();
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
    private boolean canLogin(){
        if (!validate())
            return false;
        return getJWT();
    }
    private boolean validate(){
        boolean valid = true;
        String email = emailText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else if (!email.equals(authEmail)) {
            emailText.setError("enter a authorized email address");
            valid = false;
        } else {
            emailText.setError(null);
        }
        if (authToken == null) {
            Log.d("TOKKEN ERROR", "MISSING  TOKEN");
            Toast.makeText(LoginActivity.this,
                    "Please Authorize by Facebook or Google", Toast.LENGTH_LONG).show();
            valid = false;
        }

        return valid;
    }

    private boolean getJWT(){
        String GET_URL = "http://54.180.195.179:13230/auth";
        String Params = "?email=" + authEmail + "&token=" + authToken;
        sendDeviceDetails = new SendDeviceDetails();
        sendDeviceDetails.execute(GET_URL + Params, Params, GET_METHOD);
        while(true){
            if (sendDeviceDetails.isConnFinish())
                break;
        }
        Log.d("SERVER TEST", String.valueOf(sendDeviceDetails.getResponseCode()));
        if (sendDeviceDetails.isErrorHappen()){
            if (sendDeviceDetails.getResponseCode() == 401) {
                Toast.makeText(getBaseContext(), "Please Create Account First", Toast.LENGTH_LONG).show();
                return false;
            }
            else {
                Toast.makeText(getBaseContext(), "Server Error Occurred", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        JWTToken = sendDeviceDetails.getResultData();
        Log.d("JWT DEBUG", JWTToken);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK){
                Bundle extras = data.getExtras();
                authEmail = extras.getString("AuthEmail");
                authToken = extras.getString("AuthToken");
                if (getJWT())
                    startNextActivity();
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
        Intent intent = new Intent(LoginActivity.this, BoardActivity.class);
        intent.putExtra("JWT", JWTToken);
        startActivity(intent);
        finish();
    }
    public void disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

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
