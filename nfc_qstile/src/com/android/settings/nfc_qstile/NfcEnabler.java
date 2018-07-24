package com.android.settings.nfc_qstile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.service.quicksettings.Tile;

class NfcEnabler {
    private final Context mContext;
    private final NfcTile mNfcTile;
    private final NfcAdapter mNfcAdapter;
    private final IntentFilter mIntentFilter;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (NfcAdapter.ACTION_ADAPTER_STATE_CHANGED.equals(action)) {
                handleNfcStateChanged(intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE,
                        NfcAdapter.STATE_OFF));
            }
        }
    };

    NfcEnabler(NfcTile tile) {
        mContext = tile;
        mNfcTile = tile;
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
        if (mNfcAdapter == null) {
            // NFC is not supported
            mIntentFilter = null;
            return;
        }
        mIntentFilter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
    }

    void resume() {
        if (mNfcAdapter == null) {
            mNfcTile.refresh(Tile.STATE_UNAVAILABLE);
            return;
        }
        handleNfcStateChanged(mNfcAdapter.getAdapterState());
        mContext.registerReceiver(mReceiver, mIntentFilter);
    }

    void pause() {
        if (mNfcAdapter == null) {
            return;
        }
        mContext.unregisterReceiver(mReceiver);
    }

    boolean toggleNfcStateChange(boolean enabled) {
        // Turn NFC on/off
        if (enabled) {
            mNfcAdapter.enable();
        } else {
            mNfcAdapter.disable();
        }
        return false;
    }

    private void handleNfcStateChanged(int newState) {
        switch (newState) {
        case NfcAdapter.STATE_OFF:
            mNfcTile.refresh(Tile.STATE_INACTIVE);
            break;
        case NfcAdapter.STATE_ON:
            mNfcTile.refresh(Tile.STATE_ACTIVE);
            break;
        case NfcAdapter.STATE_TURNING_ON:
        case NfcAdapter.STATE_TURNING_OFF:
            mNfcTile.refresh(Tile.STATE_UNAVAILABLE);
            break;
        }
    }
}