package com.example.user.teamproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.Blob;
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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;

import android.provider.Settings.Secure;


public class SignupPageActivity extends AppCompatActivity {
    Button signup;
    EditText signupUsername, signupPassword;
    TextView signupHaveAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);

        signupUsername = findViewById(R.id.signupUsername);
        signupPassword = findViewById(R.id.signupPassword);

        signup = findViewById(R.id.btn_signup);
        try {
            // Get the database (and create it if it doesn’t exist).
            DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
            final Database userDatabase = new Database("userList", config);

            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create a new document (i.e. a record) in the database.
                    MutableDocument userDoc = new MutableDocument();

                    String docId = userDoc.getId();
                    String usernameCol = signupUsername.getText().toString();
                    String passwordCol = signupPassword.getText().toString();
                    String UUID;

                    //check data is fulfilled
                    if (usernameCol.length() != 0 && passwordCol.length() != 0) {
                        //check if username is used
                        Query query = QueryBuilder.select(SelectResult.property("username"))
                                .from(DataSource.database(userDatabase))
                                .where(Expression.property("username").equalTo(Expression.string(usernameCol)));
                        try {
                            ResultSet result = query.execute();
                            if (result.allResults().size() != 0) {
                                toastNote("Username is used");
                                signupUsername.setText("");
                                signupPassword.setText("");
                                return;
                            }
                            //save user information to database
                            userDoc.setString("userDocId", docId);
                            userDoc.setString("username", usernameCol);
                            userDoc.setString("password", passwordCol);

                            //generate Unique ID for each user
                            Random random = new Random();
                            int randomNum = random.nextInt(1000000000);
                            while(randomNum < 100000000) {
                                randomNum = random.nextInt(1000000000);
                            }
                            UUID = usernameCol + randomNum;
                            userDoc.setString("UUID", UUID);
                            userDoc.setString("hasLogin", "true");

                            // Save it to the database.
                            userDatabase.save(userDoc);

                            if (userDatabase.getDocument(docId) != null) {
                                //do success
                                Intent intent = new Intent(SignupPageActivity.this, HomeActivity.class);
                                intent.putExtra("UserDocId", docId);
                                intent.putExtra("UUID", UUID);
                                intent.putExtra("Username", usernameCol);
                                startActivity(intent);
                                finish();
                            } else {
                                toastNote("Signup failed. Please check input");
                            }
                        } catch (CouchbaseLiteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        toastNote("Information is not enough");
                    }
                }
            });
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        //link to login page if user has an account
        signupHaveAccount = findViewById(R.id.signupHaveAccount);
        SpannableString ss = new SpannableString("Already have an account? Click");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(SignupPageActivity.this, LoginPageActivity.class);
                startActivity(intent);
                finish();
            }
        };
        ss.setSpan(clickableSpan, 25, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signupHaveAccount.setText(ss);
        signupHaveAccount.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void toastNote(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
