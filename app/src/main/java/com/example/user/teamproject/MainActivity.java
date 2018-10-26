package com.example.user.teamproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    Button login, signup, userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = findViewById(R.id.btn_login_page);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginPageActivity.class);
                startActivity(intent);
                finish();
            }
        });
        signup = findViewById(R.id.btn_signup_page);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignupPageActivity.class);
                startActivity(intent);
                finish();
            }
        });
        userList = findViewById(R.id.btn_user_list);
        userList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);

                //set admin account
                //profile pic
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile_image);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] imageInByte = stream.toByteArray();
                //profile info
                String nameCol = "admin";
                String usernameCol = "admin";
                intent.putExtra("ProfileImage", imageInByte);
                intent.putExtra("Name", nameCol);
                intent.putExtra("Username", usernameCol);
                startActivity(intent);
                finish();
            }
        });
    }
}
