package com.example.user.teamproject;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class LoginPageActivity extends AppCompatActivity {
    DatabaseHelper myDatabase;

    EditText loginUsername, loginPassword;
    TextView loginNeedAccount;
    Button login, userList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        myDatabase = new DatabaseHelper(this);
        loginUsername = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);
        login = findViewById(R.id.btn_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = false;
                String usernameCol = loginUsername.getText().toString();
                String passwordCol = loginPassword.getText().toString();
                Cursor data = myDatabase.getData();

                while (data.moveToNext()) {
                    //check if username is existed
                    if (usernameCol.equals(data.getString(2))) {
                        //if username existed, check password
                        if (passwordCol.equals(data.getString(3))) {
                            result = true;
                            byte[] imageInByte = data.getBlob(1);
                            String nameCol = data.getString(4);
                            Intent intent = new Intent(LoginPageActivity.this, HomeActivity.class);
                            intent.putExtra("ProfileImage", imageInByte);
                            intent.putExtra("Name", nameCol);
                            intent.putExtra("Username", usernameCol);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
                if (!result) {
                    toastNote("Username/password is invalid");
                }
            }
        });

        //link to signup page if user doesn't have an account
        loginNeedAccount = findViewById(R.id.loginNeedAccount);
        SpannableString ss = new SpannableString("Need an account? Click");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(LoginPageActivity.this, SignupPageActivity.class);
                startActivity(intent);
            }
        };
        ss.setSpan(clickableSpan, 17, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        loginNeedAccount.setText(ss);
        loginNeedAccount.setMovementMethod(LinkMovementMethod.getInstance());

        //admin login. Will delete later
        userList = findViewById(R.id.btn_user_list);
        userList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginPageActivity.this, HomeActivity.class);

                //set admin account
                //profile pic
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.little_man);
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

    public void toastNote(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
