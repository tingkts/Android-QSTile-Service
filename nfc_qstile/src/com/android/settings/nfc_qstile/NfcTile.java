package com.android.settings.nfc_qstile;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class NfcTile extends TileService {
    private static final String LOG_TAG = NfcTile.class.getSimpleName();
    private NfcEnabler mNfcEnabler;

    @Override
    public void onStartListening () {
        if (getQsTile() == null) {
            Log.d(LOG_TAG, "onStartListening: getQsTile() is null!");
            return;
        }
        if (mNfcEnabler == null) {
            mNfcEnabler = new NfcEnabler(this);
        }
        Log.d(LOG_TAG, "onStartListening");
        mNfcEnabler.resume();
    }

    @Override
    public void onStopListening () {
        if (getQsTile() == null) {
            Log.d(LOG_TAG, "onStopListening: getQsTile() is null!");
            return;
        }
        Log.d(LOG_TAG, "onStopListening");
        mNfcEnabler.pause();
    }

    @Override
    public void onClick() {
        if (getQsTile() == null) {
            Log.d(LOG_TAG, "onClick: getQsTile() is null!");
            return;
        }
        int state = getQsTile().getState();
        Log.d(LOG_TAG, "onClick state = " + state);
        int newState = -1; // undefined
        if (state == Tile.STATE_ACTIVE) {
            newState = Tile.STATE_INACTIVE; 
        } else if (state == Tile.STATE_INACTIVE) {
            newState = Tile.STATE_ACTIVE;
        }
        if (newState != -1) {
            mNfcEnabler.toggleNfcStateChange(newState == Tile.STATE_ACTIVE);
            refresh(newState);
        }
    }

    void refresh(int tileState) {
        getQsTile().setState(tileState);
        getQsTile().updateTile();
    }

    private static class Log {
        static final boolean DEBUG = "eng".equals(Build.TYPE)
                || "userdebug".equals(Build.TYPE);

        static void d(String tag, String msg, Throwable tr) {
            if (DEBUG) {
                android.util.Log.d(tag, msg, tr);
            }
        }

        static void d(String tag, String msg) {
            if (DEBUG) {
                android.util.Log.d(tag, msg, null);
            }
        }
    }
}
