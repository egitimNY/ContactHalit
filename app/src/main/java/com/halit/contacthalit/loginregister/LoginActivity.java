package com.halit.contacthalit.loginregister;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.halit.contacthalit.MainActivity;
import com.halit.contacthalit.R;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    Button btnlogin;
    TextView forgot,registerBtn;
    DBHelper DB;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);

        username = (EditText) findViewById(R.id.username1);
        password = (EditText) findViewById(R.id.password1);
        btnlogin = (Button) findViewById(R.id.btnsignin1);
        registerBtn = findViewById(R.id.registerBtn);
        forgot = findViewById(R.id.btnForgot);

        DB = new DBHelper(this);

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String user = username.getText().toString();
                String pass = password.getText().toString();

                if(user.equals("")||pass.equals(""))
                    Toast.makeText(LoginActivity.this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
                else{
                    Boolean checkuserpass = DB.checkusernamepassword(user, pass);
                    if(checkuserpass){
                        sharedPreferences.edit().putString("userName",user).apply();
                        sharedPreferences.edit().putString("passWord",pass).apply();
                        Toast.makeText(LoginActivity.this, "Sign in successfull", Toast.LENGTH_SHORT).show();
                        login();
                    }else{
                        Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);

            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, PasswordActivity.class);
                startActivity(intent);

            }
        });

        if(sharedPreferences.getBoolean("loggedIn", false)){
            login();
        }
    }

    private void login() {
        Intent intent  = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
