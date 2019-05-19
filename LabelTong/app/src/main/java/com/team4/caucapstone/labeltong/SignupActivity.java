package com.team4.caucapstone.labeltong;

import android.app.Activity;
import android.app.ProgressDialog;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.team4.caucapstone.labeltong.SendDeviceDetails.GET_METHOD;
import static com.team4.caucapstone.labeltong.SendDeviceDetails.POST_METHOD;

public class SignupActivity extends AppCompatActivity {
    static final int FACEBOOK = 1;

    EditText nameText;
    EditText emailText;
    EditText phoneText;
    LoginButton fbAuthButton;
    Button signupButton;
    TextView loginLink;

    private static final boolean isConnectionDone = false;
    private static final ArrayList<String> PERMISSIONS = new ArrayList<>();
    public static final int GL_SIGN_IN = 7;
    private static final String POST_URL = "http://54.180.195.179:13230/auth";

    GoogleSignInClient mGoogleSignInClient;
    CallbackManager callbackManager;
    SendDeviceDetails sendDeviceDetails;
    String authToken;
    String authEmail;
    String name;
    String email;
    String phone;

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
    private void googleSetUp(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton googleButton = findViewById(R.id.btn_gl_auth);
        googleButton.setColorScheme(SignInButton.COLOR_DARK);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GL_SIGN_IN);
            }
        });

    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
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
        } else if (!email.equals(authEmail)) {
            emailText.setError("enter a authorized email address");
            valid = false;
        } else {
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
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == GL_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            authToken = account.getIdToken().toString();
            Log.d("GOOGLE", authToken);
            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("GOOGLE", "signInResult:failed code=" + e.getStatusCode());
        }
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
