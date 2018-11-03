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

import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.Endpoint;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorChangeListener;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.lite.URLEndpoint;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class LoginPageActivity extends AppCompatActivity {
    EditText loginUsername, loginPassword;
    TextView needAccount;
    Button login, userList;
    ResultSet rs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Get the database (and create it if it doesn’t exist).
        DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
        Database userDatabase = null;
        try {
            userDatabase = new Database("userList", config);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        loginUsername = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);
        login = findViewById(R.id.btn_login);
        final Database finalUserDatabase = userDatabase;
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = false;
                String usernameCol = loginUsername.getText().toString();
                String passwordCol = loginPassword.getText().toString();

                Query query = QueryBuilder
                        .select(SelectResult.all())
                        .from(DataSource.database(finalUserDatabase))
                        .where(Expression.property("username").equalTo(Expression.string(usernameCol)));
                try {
                    rs = query.execute();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }


                //check if username is existed
                if (rs.allResults().size() ==1 && rs.allResults().get(0).equals(usernameCol)) {
                    //if username existed, check password
                    if (rs.allResults().get(0).equals(passwordCol)) {
                        result = true;
                        byte[] imageInByte = rs.allResults().get(0).getBlob("image").getContent();
                        String nameCol = rs.allResults().get(0).getString("name");
                        Intent intent = new Intent(LoginPageActivity.this, HomeActivity.class);
                        intent.putExtra("ProfileImage", imageInByte);
                        intent.putExtra("Name", nameCol);
                        intent.putExtra("Username", usernameCol);
                        startActivity(intent);
                        finish();
                    }
                }

                if (!result) {
                    toastNote("Username/password is invalid");
                }
            }
        });

        //link to signup page if user doesn't have an account
        needAccount = findViewById(R.id.needAccount);
        SpannableString ss = new SpannableString("Need an account? Click");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(LoginPageActivity.this, SignupPageActivity.class);
                startActivity(intent);
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

    public void didStart() throws CouchbaseLiteException, URISyntaxException {
        // Get the database (and create it if it doesn’t exist).
        DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
        Database database = new Database("userList", config);

        // Create a new document (i.e. a record) in the database.
        MutableDocument mutableDoc = new MutableDocument();

        // Save it to the database.
        database.save(mutableDoc);

        // Update a document.
        mutableDoc = database.getDocument(mutableDoc.getId()).toMutable();
        database.save(mutableDoc);
        Document document = database.getDocument(mutableDoc.getId());
        // Log the document ID (generated by the database) and properties
        Log.i("LoginPageActivity", "Document ID :: " + document.getId());
        Log.i("LoginPageActivity", "Learning " + document.getString("language"));

        // Create a query to fetch documents of type SDK.
        Query query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("type").equalTo(Expression.string("SDK")));
        ResultSet result = query.execute();
        Log.i("LoginPageActivity", "Number of rows ::  " + result.allResults().size());

        // Create replicators to push and pull changes to and from the cloud.
        Endpoint targetEndpoint = new URLEndpoint(new URI("ws://localhost:4984/example_sg_db"));
        ReplicatorConfiguration replConfig = new ReplicatorConfiguration(database, targetEndpoint);
        replConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL);

        // Add authentication.
        replConfig.setAuthenticator(new BasicAuthenticator("john", "pass"));

        // Create replicator.
        Replicator replicator = new Replicator(replConfig);

        // Listen to replicator change events.
        replicator.addChangeListener(new ReplicatorChangeListener() {
            @Override
            public void changed(ReplicatorChange change) {
                if (change.getStatus().getError() != null)
                    Log.i("LoginPageActivity", "Error code ::  " + change.getStatus().getError().getCode());
            }
        });

        // Start replication.
        replicator.start();
    }

    public void toastNote(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
