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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SignupPageActivity extends AppCompatActivity {
    public static final int RESULT_LOAD_IMG = 1;
    DatabaseHelper myDatabase;
    Button signup, loadPicture;
    ImageView image;
    EditText signupUsername, signupPassword, signupName;
    TextView signupHaveAccount;
    int upload = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);

        myDatabase = new DatabaseHelper(this);
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
                if(upload == 0) {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile_image);
                }else{
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

                //check data is fulfilled
                if (imageInByte != null &&
                        usernameCol.length() != 0 &&
                        passwordCol.length() != 0 &&
                        nameCol.length() != 0) {
                    Cursor data = myDatabase.getData();
                    //check if username is used
                    while (data.moveToNext()) {
                        //get the value from the data in username
                        if (usernameCol.equals(data.getString(2))) {
                            toastNote("Username is used");
                            signupUsername.setText("");
                            signupPassword.setText("");
                            return;
                        }
                    }
                    boolean result = myDatabase.addData(imageInByte, usernameCol, passwordCol, nameCol);
                    if (result) {
                        //do success
                        Intent intent = new Intent(SignupPageActivity.this, HomeActivity.class);
                        intent.putExtra("ProfileImage", imageInByte);
                        intent.putExtra("Name", nameCol);
                        intent.putExtra("Username", usernameCol);
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
