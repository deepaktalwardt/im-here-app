package com.example.user.teamproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
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
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import java.io.ByteArrayOutputStream;

public class LoginPageActivity extends AppCompatActivity {
    EditText loginUsername, loginPassword;
    TextView needAccount;
    Button login;

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
                        String username = loginUsername.getText().toString();
                        String password = loginPassword.getText().toString();

                        Query query = QueryBuilder
                                .select(SelectResult.property("username"))
                                .from(DataSource.database(userDatabase))
                                .where(Expression.property("username").equalTo(Expression.string(username)));
                        ResultSet rs = query.execute();

                        //check if username is existed and only one in db
                        if (rs.allResults().size() == 1) {
                            //get document
                            query = QueryBuilder
                                    .select(SelectResult.property("userDocId"))
                                    .from(DataSource.database(userDatabase))
                                    .where(Expression.property("username").equalTo(Expression.string(username)));
                            rs = query.execute();
                            String userDocId = rs.allResults().get(0).getString("userDocId");
                            MutableDocument userDoc = userDatabase.getDocument(userDocId).toMutable();

                            //if username existed, check password
                            if (userDoc.getString("password").equals(password)) {
                                byte[] imageInByte = userDoc.getBlob("image").getContent();
                                String name = userDoc.getString("name");
                                String deviceId = userDoc.getString("deviceId");

                                userDoc.setString("hasLogin", "true");
                                userDatabase.save(userDoc);

                                Intent intent = new Intent(LoginPageActivity.this, HomeActivity.class);
                                intent.putExtra("ProfileImage", imageInByte);
                                intent.putExtra("Name", name);
                                intent.putExtra("Username", username);
                                intent.putExtra("DeviceId", deviceId);
                                startActivity(intent);
                                finish();
                            } else {
                                toastNote("Password is invalid");
                            }
                        } else {
                            toastNote("Username is not exist");
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
    }

    public void toastNote(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
