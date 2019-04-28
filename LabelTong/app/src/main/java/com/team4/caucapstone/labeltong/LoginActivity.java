package com.team4.caucapstone.labeltong;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(LoginActivity.this, BoardActivity.class);
                startActivity(intent);
                finish();
            }
        });
        Button createButton = (Button)findViewById(R.id.createButton);
        createButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                // ToDo : Social Login Buttons
            }
        });
    }
}
