package com.example.user.teamproject;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.MutableDocument;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ImageView HomeImage;
    TextView HomeUUID, HomeUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(getApplicationContext(), UserDiscovery.class);
                startActivity(intent);
            }
        });

        //navigation drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //user's information
        Intent intent = getIntent();

        HomeImage = navigationView.getHeaderView(0).findViewById(R.id.NavHeaderImageView);
        byte[] imageInByte = intent.getByteArrayExtra("ProfileImage");
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageInByte, 0, imageInByte.length);
        HomeImage.setImageBitmap(bitmap);

        HomeUUID = navigationView.getHeaderView(0).findViewById(R.id.NavHeaderUUID);
        HomeUUID.setText(intent.getStringExtra("UUID"));
        HomeUsername = navigationView.getHeaderView(0).findViewById(R.id.NavHeaderUsername);
        HomeUsername.setText(intent.getStringExtra("Username"));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.action_settings) {

        } else if (id == R.id.nav_logout) {
            try {
                // Get the database (and create it if it doesn’t exist).
                DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
                Database userDatabase = new Database("userList", config);
                Intent intent = getIntent();
                String userDocId = intent.getStringExtra("UserDocId");
                Log.d("ID", userDocId);
                MutableDocument userDoc = userDatabase.getDocument(userDocId).toMutable();
                userDoc.setString("hasLogin", "false");
                userDatabase.save(userDoc);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(HomeActivity.this, LoginPageActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
