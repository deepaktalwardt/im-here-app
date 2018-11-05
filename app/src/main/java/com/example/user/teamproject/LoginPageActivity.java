package com.example.user.teamproject;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import java.io.ByteArrayOutputStream;

public class LoginPageActivity extends AppCompatActivity {
    EditText loginUsername, loginPassword;
    TextView needAccount;
    Button login, userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        try {
            // Get the database (and create it if it doesn’t exist).
            DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
            final Database userDatabase = new Database("userList", config);

            loginUsername = findViewById(R.id.loginUsername);
            loginPassword = findViewById(R.id.loginPassword);
            login = findViewById(R.id.btn_login);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        boolean result = false;
                        String usernameCol = loginUsername.getText().toString();
                        String passwordCol = loginPassword.getText().toString();

                        Query query = QueryBuilder
                                .select(SelectResult.property("username"))
                                .from(DataSource.database(userDatabase))
                                .where(Expression.property("username").equalTo(Expression.string(usernameCol)));
                        ResultSet rs = query.execute();
                        if (rs.allResults().size() > 0) {
                            //check if username is existed
                            rs = query.execute();
                            if (rs.allResults().get(0).getString("username").equals(usernameCol)) {
                                //if username existed, check password
                                query = QueryBuilder
                                        .select(SelectResult.property("password"))
                                        .from(DataSource.database(userDatabase))
                                        .where(Expression.property("username").equalTo(Expression.string(usernameCol)));
                                rs = query.execute();
                                if (rs.allResults().get(0).getString("password").equals(passwordCol)) {
                                    result = true;

                                    query = QueryBuilder
                                            .select(SelectResult.property("image"))
                                            .from(DataSource.database(userDatabase))
                                            .where(Expression.property("username").equalTo(Expression.string(usernameCol)));
                                    rs = query.execute();
                                    byte[] imageInByte = rs.allResults().get(0).getBlob("image").getContent();

                                    query = QueryBuilder
                                            .select(SelectResult.property("name"))
                                            .from(DataSource.database(userDatabase))
                                            .where(Expression.property("username").equalTo(Expression.string(usernameCol)));
                                    rs = query.execute();
                                    String nameCol = rs.allResults().get(0).getString("name");

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
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        //link to signup page if user doesn't have an account
        needAccount = findViewById(R.id.needAccount);
        SpannableString ss = new SpannableString("Need an account? Click");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(LoginPageActivity.this, SignupPageActivity.class);
                startActivity(intent);
                finish();
            }
        };
        ss.setSpan(clickableSpan, 17, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        needAccount.setText(ss);
        needAccount.setMovementMethod(LinkMovementMethod.getInstance());

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
