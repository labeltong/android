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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignupActivity extends AppCompatActivity {
    EditText nameText;
    EditText emailText;
    EditText phoneText;
    LoginButton fbAuthButton;
    Button signupButton;
    TextView loginLink;

    CallbackManager callbackManager;

    String authToken;
    String authEmail;
    String authId;

    GoogleSignInClient mGoogleSignInClient;
    SignInButton googleButton;
    static final int RC_GET_TOKEN = 9002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        facebookSetUp();
        googleSetUp();

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
                            authId = object.getString("id");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, email");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }
    private void googleSetUp(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleButton = findViewById(R.id.btn_gl_auth);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_GET_TOKEN);
            }
        });
    }

    public void signup() {
        if (!ServerControl.validate(nameText, emailText, phoneText,
                authToken, authEmail ,SignupActivity.this)) {
            onSignupFailed();
            return;
        }
        authId = emailText.getText().toString();
        signupButton.setEnabled(false);

        postToServer();
    }

    private void postToServer() {
        authEmail = emailText.getText().toString();
        ServerControl.APIServiceIntface apiService = ServerControl.getAPIServerIntface();
        try{
            Call<SignUpModel> comment = apiService.postSignUP(emailText.getText().toString(),
                    authToken, nameText.getText().toString(), phoneText.getText().toString());
            comment.enqueue(new Callback<SignUpModel>() {
                @Override
                public void onResponse(Call<SignUpModel> call, Response<SignUpModel> response) {
                    onSignupSuccess();
                }
                @Override
                public void onFailure(Call<SignUpModel> call, Throwable t) {
                    onSignupFailed();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSignupSuccess() {
        signupButton.setEnabled(true);
        Intent resultIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putString("AuthEmail", authEmail);
        extras.putString("AuthToken", authToken);
        resultIntent.putExtras(extras);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_LONG).show();
        signupButton.setEnabled(true);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_GET_TOKEN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else
            callbackManager.onActivityResult(requestCode, resultCode, data);
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
    protected void onDestroy() {
        disconnectFromFacebook();
        disconnectFromGoogle();
        super.onDestroy();
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
    public void disconnectFromGoogle() {
        mGoogleSignInClient.revokeAccess();
    }
}
