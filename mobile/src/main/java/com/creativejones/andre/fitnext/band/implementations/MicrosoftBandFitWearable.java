package com.creativejones.andre.fitnext.band.implementations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;

import com.creativejones.andre.fitnext.R;
import com.creativejones.andre.fitnext.band.IFitwearable;
import com.creativejones.andre.fitnext.app.MainActivity;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.tiles.BandTile;
import com.microsoft.band.tiles.TileButtonEvent;
import com.microsoft.band.tiles.TileEvent;
import com.microsoft.band.tiles.pages.FlowPanel;
import com.microsoft.band.tiles.pages.FlowPanelOrientation;
import com.microsoft.band.tiles.pages.PageData;
import com.microsoft.band.tiles.pages.PageLayout;
import com.microsoft.band.tiles.pages.TextBlock;
import com.microsoft.band.tiles.pages.TextBlockData;
import com.microsoft.band.tiles.pages.TextBlockFont;
import com.microsoft.band.tiles.pages.TextButton;
import com.microsoft.band.tiles.pages.TextButtonData;

import java.util.List;
import java.util.UUID;

public class MicrosoftBandFitWearable implements IFitwearable {

    private static final UUID tileId = UUID.fromString("f45fc5f0-de52-11e5-a837-0800200c9a66");
    private static final UUID pageId1 = UUID.fromString("06a70890-de53-11e5-a837-0800200c9a66");

    private Context mContext;
    private BandClient mClient;
    private IFitwearable.Listener mListener;

    public MicrosoftBandFitWearable(MainActivity context){
        mContext = context;
        mListener = context;
    }

    //region Interface
    @Override
    public void processIntent(Intent intent) {

        if(isValidBandIntent(intent)){

            String intentAction = intent.getAction();
            TileEvent tileEvent = intent.getParcelableExtra(TileEvent.TILE_EVENT_DATA);

            switch (intentAction){
                case TileEvent.ACTION_TILE_OPENED:
                    tileOpened(tileEvent);
                    break;
                case TileEvent.ACTION_TILE_BUTTON_PRESSED:
                    tileButtonPressed((TileButtonEvent)tileEvent);
                    break;
                case TileEvent.ACTION_TILE_CLOSED:
                    tileClosed(tileEvent);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        if(mClient != null){
            try{
                mClient.disconnect().await();
            } catch (BandException | InterruptedException e){

            }
        }
    }

    @Override
    public void connectAsync() {
        new StartTask().execute();
    }

    @Override
    public void disconnectAsync() {
        new StopTask().execute();
    }
    //endregion

    //region Helpers
    private boolean isValidBandIntent(Intent intent) {
        String extraString = intent.getStringExtra(getString(R.string.intent_key));
        return extraString != null && extraString.equals(getString(R.string.intent_value));
    }

    private void tileOpened(TileEvent event){

    }

    private void tileClosed(TileEvent event){

    }

    private void tileButtonPressed(TileButtonEvent event){

    }

    private void updatePages() throws BandIOException {
        mClient.getTileManager().setPages(tileId,
                new PageData(pageId1, 0)
                        .update(new TextBlockData(12, mListener.nextExercise()))
                        .update(new TextButtonData(21, "Complete")));


    }

    private void handleBandException(BandException e) {
        String exceptionMessage;
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
        mListener.userMessage(exceptionMessage);
    }

    private PageLayout createButtonLayout() {

        return new PageLayout(
                new FlowPanel(15, 0, 260, 105, FlowPanelOrientation.VERTICAL)
                        .addElements(new TextBlock(0, 5, 210, 45, TextBlockFont.SMALL, 1).setMargins(0, 5, 0, 0).setId(12))
                        .addElements(new TextButton(0, 0, 210, 45).setMargins(0, 5, 0, 0).setId(21).setPressedColor(Color.BLUE)));
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (mClient == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                mListener.userMessage("Band isn't paired with your phone");
                return false;
            }
            mClient = BandClientManager.getInstance().create(mContext, devices[0]);
        } else if (ConnectionState.CONNECTED == mClient.getConnectionState()) {
            return true;
        }

        mListener.userMessage("Band is connecting...\n");
        return ConnectionState.CONNECTED == mClient.connect().await();
    }

    private boolean addTile() throws Exception {
        boolean isTileAdded = true;

        if (!doesTileExist()) {
            /* Set the options */
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap tileIcon = BitmapFactory.decodeResource(mContext.getResources(), R.raw.b_icon, options);

            BandTile tile = new BandTile.Builder(tileId, "Exercise Title", tileIcon)
                    .setPageLayouts(createButtonLayout())
                    .build();

            if (mClient.getTileManager().addTile((Activity)mContext, tile).await()) {
                mListener.userMessage("Button Tile is added.\n");
            } else {
                mListener.userMessage("Unable to add button tile to the band.\n");
                isTileAdded = false;
            }
        }

        return isTileAdded;

    }

    private void removeTile() throws InterruptedException, BandException {
        if (doesTileExist()) {
            mClient.getTileManager().removeTile(tileId).await();
        }
    }

    private boolean doesTileExist() throws InterruptedException, BandException {
        List<BandTile> tiles = mClient.getTileManager().getTiles().await();
        for (BandTile tile : tiles) {
            if (tile.getTileId().equals(tileId)) {
                return true;
            }
        }
        return false;
    }

    private String getString(int resId){
        return mContext.getString(resId);
    }
    //endregion

    private class StartTask extends AsyncTask<Void, Void, Boolean> {

        String whyBandCantConnectMessage;
        boolean isBandConnected = false;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    if (addTile()) {
                        updatePages();
                        isBandConnected = true;
                    } else {
                        whyBandCantConnectMessage = "Unable to Add Tile to band";
                    }
                } else {
                    whyBandCantConnectMessage = "Unable to Connect to band";
                }
            } catch (BandException e) {
                handleBandException(e);
            } finally {
                return isBandConnected;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(!result){
                mListener.userMessage(whyBandCantConnectMessage);
            }

            mListener.onConnected(result);
        }
    }

    private class StopTask extends AsyncTask<Void, Void, Boolean> {
        boolean isConnectionStopped  = false;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    removeTile();
                    isConnectionStopped = true;
                }
            } catch (BandException e) {
                handleBandException(e);
            } finally {
                return isConnectionStopped;
            }
        }
    }
}
