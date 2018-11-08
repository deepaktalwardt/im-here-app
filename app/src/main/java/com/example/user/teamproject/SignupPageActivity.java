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
    public static final int RESULT_LOAD_IMG = 1;
    Button signup, loadPicture;
    ImageView image;
    EditText signupUsername, signupPassword, signupName;
    TextView signupHaveAccount;
    int upload = 0, randNum;
    InputStream is;
    Blob blob;
    Random rand = new Random();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);

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
        try {
            // Get the database (and create it if it doesn’t exist).
            DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
            final Database userDatabase = new Database("userList", config);

            // Create a new document (i.e. a record) in the database.
            final MutableDocument userDoc = new MutableDocument();

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

                    // convert bitmap to byte
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte imageInByte[] = stream.toByteArray();
                    blob = new Blob("image/*", imageInByte);

                    String usernameCol = signupUsername.getText().toString();
                    String passwordCol = signupPassword.getText().toString();
                    String nameCol = signupName.getText().toString();
                    //getting unique id for device
                    String deviceIdCol = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

                    //check data is fulfilled
                    if (imageInByte != null &&
                            usernameCol.length() != 0 &&
                            passwordCol.length() != 0 &&
                            nameCol.length() != 0) {
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
                            userDoc.setBlob("image", blob);
                            userDoc.setString("username", usernameCol);
                            userDoc.setString("password", passwordCol);
                            userDoc.setString("name", nameCol);
                            userDoc.setString("deviceId", deviceIdCol);

                            // Save it to the database.
                            userDatabase.save(userDoc);

                            if (userDatabase.getDocument(userDoc.getId()) != null) {
                                //do success
                                Intent intent = new Intent(SignupPageActivity.this, HomeActivity.class);
                                intent.putExtra("ProfileImage", imageInByte);
                                intent.putExtra("Name", nameCol);
                                intent.putExtra("Username", usernameCol);
                                intent.putExtra("DeviceId", deviceIdCol);
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

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                image.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                toastNote("Something went wrong");
            }
        } else {
            toastNote("You haven't picked Image");
        }
    }

    public void toastNote(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
