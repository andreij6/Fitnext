package com.creativejones.andre.fitnext.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.creativejones.andre.fitnext.R;
import com.creativejones.andre.fitnext.band.IFitwearable;
import com.creativejones.andre.fitnext.band.implementations.MicrosoftBandFitWearable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IFitwearable.Listener{

    IFitwearable mFitWearable;

    List<String> mExercises = new ArrayList<>();
    int mExerciseIndex = 0;

    Button mStart;
    Button mStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFitWearable = new MicrosoftBandFitWearable(this);

        mStart = (Button)findViewById(R.id.start);
        mStop = (Button)findViewById(R.id.stop);

        mExercises.add("Push Ups 4x20");
        mExercises.add("Set Ups 5x20");
        mExercises.add("V Ups 5x20");
        mExercises.add("Bench 5x20");
        mExercises.add("Curls 5x20");
        mExercises.add("Squats 5x20");

        mStart.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mFitWearable.connectAsync();
            }
        });

        mStop.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mFitWearable.disconnectAsync();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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

    @Override
    protected void onNewIntent(Intent intent) {
        mFitWearable.processIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        mFitWearable.onDestroy();
        super.onDestroy();
    }

    //region IFitwearable.Listener
    @Override
    public void userMessage(String message) {
        Snackbar.make(mStart, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public String nextExercise() {
        String result = mExercises.get(mExerciseIndex);

        mExerciseIndex++;

        if(mExerciseIndex >= mExercises.size()){
            mExerciseIndex = 0;
        }

        return result;
    }

    @Override
    public void onConnected(Boolean isConnected) {
        if (isConnected) {
            mStop.setEnabled(true);
        } else {
            mStart.setEnabled(true);
        }
    }
    //endregion

}
