package com.example.user.teamproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.Blob;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.Endpoint;
import com.couchbase.lite.Expression;
import com.couchbase.lite.From;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import static android.icu.text.MessagePattern.ArgType.SELECT;

public class SignupPageActivity extends AppCompatActivity {
    public static final int RESULT_LOAD_IMG = 1;
    Button signup, loadPicture;
    ImageView image;
    EditText signupUsername, signupPassword, signupName;
    TextView signupHaveAccount;
    int upload = 0;
    InputStream is;
    Blob blob;
    Random rand = new Random();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);

        // Get the database (and create it if it doesn’t exist).
        DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
        Database userDatabase = null;
        try {
            userDatabase = new Database("userList", config);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        final Database finalUserDatabase = userDatabase;
        // Create a new document (i.e. a record) in the database.
        final MutableDocument userDoc = new MutableDocument();


        image = findViewById(R.id.signupProfileImage);
        loadPicture = findViewById(R.id.btn_loadPicture);
        loadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
                upload++;
            }
        });

        signupUsername = findViewById(R.id.signupUsername);
        signupPassword = findViewById(R.id.signupPassword);
        signupName = findViewById(R.id.signupName);

        signup = findViewById(R.id.btn_signup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap;
                // get image from drawable
                if (upload == 0) {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile_image);
                } else {
                    image.buildDrawingCache();
                    bitmap = image.getDrawingCache();
                }
                Log.d("sign", String.valueOf(bitmap));
                // convert bitmap to byte
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte imageInByte[] = stream.toByteArray();

                String usernameCol = signupUsername.getText().toString();
                String passwordCol = signupPassword.getText().toString();
                String nameCol = signupName.getText().toString();
                rand.nextInt(1000000000);
                String extension = usernameCol + rand.toString();

                //check data is fulfilled
                if (imageInByte != null &&
                        usernameCol.length() != 0 &&
                        passwordCol.length() != 0 &&
                        nameCol.length() != 0) {
                    //check if username is used
                    Query query = QueryBuilder
                            .select(SelectResult.all())
                            .from(DataSource.database(finalUserDatabase))
                            .where(Expression.property("username").equalTo(Expression.string(usernameCol)));
                    try {
                        ResultSet rs = query.execute();
                        if (rs.allResults().size() > 0) {
                            //get the value from the data in username
                            toastNote("Username is used");
                            signupUsername.setText("");
                            signupPassword.setText("");
                            return;
                        }
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                    userDoc.setBlob("image", blob);
                    userDoc.setString("username", usernameCol);
                    userDoc.setString("password", passwordCol);
                    userDoc.setString("name", nameCol);
                    userDoc.setString("extension", extension);
                    try {
                        // Save it to the database.
                        finalUserDatabase.save(userDoc);
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                    if (finalUserDatabase.getDocument(userDoc.getId()) != null) {
                        //do success
                        Intent intent = new Intent(SignupPageActivity.this, HomeActivity.class);
                        intent.putExtra("UserID", userDoc.getId());
                        intent.putExtra("ProfileImage", imageInByte);
                        intent.putExtra("Name", nameCol);
                        intent.putExtra("Username", usernameCol);
                        intent.putExtra("Extension", extension);
                        startActivity(intent);
                        finish();
                    } else {
                        toastNote("Signup failed. Please check input");
                    }
                } else {
                    toastNote("Information is not enough");
                }
            }
        });

        //link to login page if user has an account
        signupHaveAccount = findViewById(R.id.signupHaveAccount);
        SpannableString ss = new SpannableString("Already have an account? Click");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                finish();
            }
        };
        ss.setSpan(clickableSpan, 25, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signupHaveAccount.setText(ss);
        signupHaveAccount.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                image.setImageBitmap(selectedImage);
                is = imageStream;
                blob = new Blob("image/*", is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                toastNote("Something went wrong");
            }
        } else {
            toastNote("You haven't picked Image");
        }
    }
/*
    public void createDB() throws CouchbaseLiteException, URISyntaxException {

        // Save it to the database.
        userDatabase.save(mutableDoc);

        // Update a document.
        mutableDoc = userDatabase.getDocument(mutableDoc.getId()).toMutable();
        userDatabase.save(mutableDoc);
        Document document = userDatabase.getDocument(mutableDoc.getId());
        // Log the document ID (generated by the database) and properties
        Log.i("LoginPageActivity", "Document ID :: " + document.getId());
        Log.i("LoginPageActivity", "Learning " + document.getString("language"));

        // Create a query to fetch documents of type SDK.
        Query query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(userDatabase))
                .where(Expression.property("type").equalTo(Expression.string("SDK")));
        ResultSet result = query.execute();
        Log.i("LoginPageActivity", "Number of rows ::  " + result.allResults().size());

        // Create replicators to push and pull changes to and from the cloud.
        Endpoint targetEndpoint = new URLEndpoint(new URI("ws://localhost:4984/example_sg_db"));
        ReplicatorConfiguration replConfig = new ReplicatorConfiguration(userDatabase, targetEndpoint);
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
*/
    public void toastNote(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
