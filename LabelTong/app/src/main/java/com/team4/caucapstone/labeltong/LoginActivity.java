package com.team4.caucapstone.labeltong;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    GoogleSignInClient mGoogleSignInClient;
    SignInButton googleButton;
    static final int RC_GET_TOKEN = 9002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailText = (EditText) findViewById(R.id.input_email_signin);
        facebooksetting();
        googleSetUp();

        loginButton = (Button)findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                Progressbar.progressON(LoginActivity.this);
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
    private void googleSetUp(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleButton = findViewById(R.id.btn_gl_auth_login);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_GET_TOKEN);
            }
        });
    }
    private void tryLogin(){
        if (ServerControl.validate(null, emailText, null,
                authToken, authEmail, this))
            getJWT();
        else
            Progressbar.progressOFF();
    }
    private void getJWT() {
        ServerControl.APIServiceIntface apiService = ServerControl.getAPIServerIntface();
        Log.d("getJWT", authEmail);
        Log.d("getJWT", authToken);
        Call<ResponseBody> comment = apiService.getComment(authEmail, authToken);
        comment.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("JWT", response.toString());
                try {
                    JWTToken = response.body().string();
                    startNextActivity();
                } catch (Exception e) {
                    Progressbar.progressOFF();
                    MyAlertDialog.showWarning(LoginActivity.this, "Login",
                            "Fail to Login");
                    fbAuthButton.setVisibility(View.VISIBLE);
                    googleButton.setEnabled(true);
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                MyAlertDialog.showWarning(LoginActivity.this, "Login",
                        "Fail to Login");
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK){
                Progressbar.progressON(this);
                Bundle extras = data.getExtras();
                authEmail = extras.getString("AuthEmail");
                authToken = extras.getString("AuthToken");
                getJWT();
            }
        }
        else if (requestCode == RC_GET_TOKEN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
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
        parameters.putString("fields", "email");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }
    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            authEmail = account.getEmail();
            authToken = account.getId();
            fbAuthButton.setVisibility(View.GONE);
            googleButton.setEnabled(false);
        } catch (ApiException e) {
            Log.w("TESTGOOGLE", "signInResult:failed code=" + e.getStatusCode());
        }
    }
    @Override
    public void onDestroy() {
        disconnectFromFacebook();
        disconnectFromGoogle();
        super.onDestroy();
    }

    public void disconnectFromGoogle() {
        mGoogleSignInClient.revokeAccess();
    }
    private void startNextActivity(){
        Progressbar.progressOFF();
        Intent intent = new Intent(LoginActivity.this, UserInfoActivity.class);
        Bundle extras = new Bundle();
        extras.putString("AuthEmail", authEmail);
        extras.putString("AuthToken", authToken);
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
