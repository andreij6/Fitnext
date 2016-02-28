package com.creativejones.andre.fitnext;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.tiles.BandTile;
import com.microsoft.band.tiles.TileButtonEvent;
import com.microsoft.band.tiles.TileEvent;
import com.microsoft.band.tiles.pages.FilledButton;
import com.microsoft.band.tiles.pages.FilledButtonData;
import com.microsoft.band.tiles.pages.FlowPanel;
import com.microsoft.band.tiles.pages.FlowPanelOrientation;
import com.microsoft.band.tiles.pages.PageData;
import com.microsoft.band.tiles.pages.PageLayout;
import com.microsoft.band.tiles.pages.TextBlock;
import com.microsoft.band.tiles.pages.TextBlockData;
import com.microsoft.band.tiles.pages.TextBlockFont;
import com.microsoft.band.tiles.pages.TextButton;
import com.microsoft.band.tiles.pages.TextButtonData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BandClient mClient = null;
    private static final UUID tileId = UUID.randomUUID();
    private static final UUID pageId1 = UUID.randomUUID();
    

    List<String> mExercises = new ArrayList<>();
    int mExerciseIndex = 0;

    FloatingActionButton mFab;
    Button mStart;
    Button mStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
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
                new StartTask().execute();
            }
        });

        mStop.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                new StopTask().execute();
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        processIntent(intent);
        super.onNewIntent(intent);
    }

    private void processIntent(Intent intent) {
        String extraString = intent.getStringExtra(getString(R.string.intent_key));

        if(extraString != null && extraString.equals(getString(R.string.intent_value))){
            if (intent.getAction() == TileEvent.ACTION_TILE_OPENED) {
                TileEvent tileOpenData = intent.getParcelableExtra(TileEvent.TILE_EVENT_DATA);

            } else if (intent.getAction() == TileEvent.ACTION_TILE_BUTTON_PRESSED) {
                TileButtonEvent buttonData = intent.getParcelableExtra(TileEvent.TILE_EVENT_DATA);
                try {
                    updatePages();
                } catch (Exception e){

                }

            } else if (intent.getAction() == TileEvent.ACTION_TILE_CLOSED) {
                TileEvent tileCloseData = intent.getParcelableExtra(TileEvent.TILE_EVENT_DATA);

            }
        }
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
        if(mClient != null){
            try{
                mClient.disconnect().await();
            } catch (InterruptedException e){

            } catch (BandException b){

            }
        }
        super.onDestroy();
    }

    private void removeTile() throws BandIOException, InterruptedException, BandException {
        if (doesTileExist()) {
            mClient.getTileManager().removeTile(tileId).await();
        }
    }

    private boolean doesTileExist() throws BandIOException, InterruptedException, BandException {
        List<BandTile> tiles = mClient.getTileManager().getTiles().await();
        for (BandTile tile : tiles) {
            if (tile.getTileId().equals(tileId)) {
                return true;
            }
        }
        return false;
    }

    private boolean addTile() throws Exception {
        if (doesTileExist()) {
            return true;
        }

		/* Set the options */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap tileIcon = BitmapFactory.decodeResource(getBaseContext().getResources(), R.raw.b_icon, options);

        BandTile tile = new BandTile.Builder(tileId, "Exercise Title", tileIcon)
                .setPageLayouts(createButtonLayout())
                .build();

        if (mClient.getTileManager().addTile(this, tile).await()) {
            userMessage("Button Tile is added.\n");
            return true;
        } else {
            userMessage("Unable to add button tile to the band.\n");
            return false;
        }
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (mClient == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                Toast.makeText(this, "Band isn't paired with your phone.\n", Toast.LENGTH_LONG).show();
                return false;
            }
            mClient = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == mClient.getConnectionState()) {
            return true;
        }

        userMessage("Band is connecting...\n");
        return ConnectionState.CONNECTED == mClient.connect().await();
    }

    private PageLayout createButtonLayout() {

        return new PageLayout(
                new FlowPanel(15, 0, 260, 105, FlowPanelOrientation.VERTICAL)
                        .addElements(new TextBlock(0, 5, 210, 45, TextBlockFont.SMALL, 1).setMargins(0, 5, 0, 0).setId(12))
                        .addElements(new TextButton(0, 0, 210, 45).setMargins(0, 5, 0, 0).setId(21).setPressedColor(Color.BLUE)));
    }

    private void userMessage(String message) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void handleBandException(BandException e) {
        String exceptionMessage = "";
        switch (e.getErrorType()) {
            case DEVICE_ERROR:
                exceptionMessage = "Please make sure bluetooth is on and the band is in range.\n";
                break;
            case UNSUPPORTED_SDK_VERSION_ERROR:
                exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                break;
            case SERVICE_ERROR:
                exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                break;
            case BAND_FULL_ERROR:
                exceptionMessage = "Band is full. Please use Microsoft Health to remove a tile.\n";
                break;
            default:
                exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                break;
        }
        userMessage(exceptionMessage);
    }

    private void updatePages() throws BandIOException {
        mClient.getTileManager().setPages(tileId,
                new PageData(pageId1, 0)
                        .update(new TextBlockData(12, mExercises.get(mExerciseIndex)))
                        .update(new TextButtonData(21, "Complete")));

        mExerciseIndex++;

        if(mExerciseIndex >= mExercises.size()){
            mExerciseIndex = 0;
        }
    }

    private class StartTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    if (addTile()) {
                        updatePages();
                    } else {
                        userMessage("Unable to Add Tile to band");

                    }
                } else {
                    userMessage("Unable to Connect to band");
                    return false;
                }
            } catch (BandException e) {
                handleBandException(e);
                return false;
            } catch (Exception e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mStop.setEnabled(true);
            } else {
                mStart.setEnabled(true);
            }
        }
    }

    private class StopTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    removeTile();
                } else {
                }
            } catch (BandException e) {
                handleBandException(e);
                return false;
            } catch (Exception e) {
                return false;
            }

            return true;
        }
    }

}
