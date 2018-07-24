package com.android.settings.nfc_qstile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class FakeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String packageName = "com.android.settings";
        String className = "com.android.settings.Settings$WirelessSettingsActivity";
        intent.setClassName(packageName, className);
        startActivity(intent);
        finish();
    }
}