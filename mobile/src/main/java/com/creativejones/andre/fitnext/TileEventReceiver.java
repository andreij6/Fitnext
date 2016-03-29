package com.creativejones.andre.fitnext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.creativejones.andre.fitnext.app.MainActivity;
import com.microsoft.band.tiles.TileEvent;

public class TileEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class)
                                .setAction(intent.getAction())
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                .putExtra(context.getString(R.string.intent_key), context.getString(R.string.intent_value))
                                .putExtra(TileEvent.TILE_EVENT_DATA, intent.getParcelableExtra(TileEvent.TILE_EVENT_DATA));

        context.startActivity(i);
    }
}
