package com.android.settings.audioprofile_qstile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.service.quicksettings.TileService;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.audioprofile_qstile.R;
import com.mediatek.audioprofile.AudioProfileManager;
import com.mediatek.audioprofile.AudioProfileManager.Scenario;
import com.mediatek.common.audioprofile.AudioProfileListener;

public class AudioProfileTile extends TileService {
    private static final String LOG_TAG = AudioProfileTile.class.getSimpleName();
    private static final int PROFILE_SWITCH_DIALOG_LONG_TIMEOUT = 4000;

    private Dialog mProfileSwitchDialog;
    private ImageView mNormalProfileIcon;
    private ImageView mMettingProfileIcon;
    private ImageView mMuteProfileIcon;
    private ImageView mOutdoorSwitchIcon;

    private AudioProfileManager mProfileManager;
    private List<String> mProfileKeys;

    private Handler mHandler = new Handler();

    private Locale mLocale;

    private View.OnClickListener mProfileSwitchListener = new View.OnClickListener() {
        public void onClick(View v) {
            for (int i = 0; i < mProfileKeys.size(); i++) {
                if (v.getTag().equals(mProfileKeys.get(i))) {
                    Log.d(LOG_TAG, "onClick called, profile clicked is:" + mProfileKeys.get(i));
                    String key = mProfileKeys.get(i);
                    mProfileManager.setActiveProfile(key);
                    Scenario senario = AudioProfileManager.getScenario(key);
                    refreshIcon(senario);
                    if (mProfileSwitchDialog != null) {
                        mProfileSwitchDialog.dismiss();
                    }
                }
            }
        }
    };

    private AudioProfileListener mAudioProfileListenr = new AudioProfileListener() {
        @Override
        public void onProfileChanged(String profileKey) {
            if (profileKey != null) {
                Scenario senario = AudioProfileManager.getScenario(profileKey);
                Log.d(LOG_TAG, "onProfileChanged onReceive called, profile type is: " + senario);
                if (senario != null) {
                    refreshIcon(senario);
                }
            }
        }
    };

    @Override
    public void onTileAdded() {
        Log.d(LOG_TAG, "onTileAdded");
    }

    @Override
    public void onTileRemoved() {
        Log.d(LOG_TAG, "onTileRemoved");
    }

    @Override
    public void onStartListening () {
        if (getQsTile() == null) {
            Log.d(LOG_TAG, "onStartListening: getQsTile() is null!");
            return;
        }
        boolean isLocaleChanged = false;
        Locale revised = Locale.getDefault();
        if (revised == null || mLocale == null || !revised.equals(mLocale)) {
            Log.d(LOG_TAG, "onStartListening: " + mLocale + " to " + revised);
            mLocale = revised;
            isLocaleChanged = true;
            getQsTile().setLabel(getLocalizedResources(this, mLocale).getString(R.string.audio_profile));
        }

        init(isLocaleChanged);

        mProfileManager.listenAudioProfie(mAudioProfileListenr, AudioProfileListener.LISTEN_PROFILE_CHANGE);
        Scenario scenario = AudioProfileManager.getScenario(mProfileManager.getActiveProfileKey());
        Log.d(LOG_TAG, "onStartListening: " + scenario);
        refreshIcon(scenario);
    }

    @Override
    public void onStopListening () {
        if (getQsTile() == null) {
            Log.d(LOG_TAG, "onStopListening: getQsTile() is null!");
            return;
        }
        Log.d(LOG_TAG, "onStopListening");
        mProfileManager.listenAudioProfie(mAudioProfileListenr, AudioProfileListener.STOP_LISTEN);
        if (mProfileSwitchDialog != null) {
            mProfileSwitchDialog.dismiss();
        }
    }

    @Override
    public void onClick() {
        if (getQsTile() == null) {
            Log.d(LOG_TAG, "onClick: getQsTile() is null!");
            return;
        }
        int state = getQsTile().getState();
        Log.d(LOG_TAG, "onClick state = " + state);
        showProfileSwitchDialog();
    }

    private void init(boolean isLocaleChanged) {
        if (mProfileSwitchDialog == null || isLocaleChanged) {
            Log.d(LOG_TAG, "init");
            mProfileManager = (AudioProfileManager) getSystemService(Context.AUDIO_PROFILE_SERVICE);

            mProfileKeys = new ArrayList<String>();
            mProfileKeys = mProfileManager.getPredefinedProfileKeys();

            createProfileSwitchDialog();
        }
    }

    private void createProfileSwitchDialog() {
        Log.d(LOG_TAG, "createProfileSwitchDialog");
        mProfileSwitchDialog = new Dialog(this);
        mProfileSwitchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProfileSwitchDialog.setContentView(R.layout.quick_settings_profile_switch_dialog);
        mProfileSwitchDialog.setCanceledOnTouchOutside(true);
        mProfileSwitchDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL);
        mProfileSwitchDialog.getWindow().getAttributes().privateFlags |=
                WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;
        mProfileSwitchDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mProfileSwitchDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mProfileSwitchDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        mMettingProfileIcon =
                (ImageView) mProfileSwitchDialog.findViewById(R.id.meeting_profile_icon);
        mOutdoorSwitchIcon =
                (ImageView) mProfileSwitchDialog.findViewById(R.id.outdoor_profile_icon);
        mMuteProfileIcon =
                (ImageView) mProfileSwitchDialog.findViewById(R.id.mute_profile_icon);
        mNormalProfileIcon =
                (ImageView) mProfileSwitchDialog.findViewById(R.id.normal_profile_icon);

        Resources res = getLocalizedResources(this, Locale.getDefault());
        View normalProfile = (View) mProfileSwitchDialog.findViewById(R.id.normal_profile);
        TextView normalProfileText =
                (TextView) mProfileSwitchDialog.findViewById(R.id.normal_profile_text);
        normalProfileText.setText(res.getString(R.string.normal));
        FontSizeUtils.updateFontSize(normalProfileText, R.dimen.qs_tile_text_size);
        normalProfile.setOnClickListener(mProfileSwitchListener);
        normalProfile.setTag(AudioProfileManager.getProfileKey(Scenario.GENERAL));

        View muteProfile = (View) mProfileSwitchDialog.findViewById(R.id.mute_profile);
        TextView muteProfileText =
                (TextView) mProfileSwitchDialog.findViewById(R.id.mute_profile_text);
        muteProfileText.setText(res.getString(R.string.mute));
        FontSizeUtils.updateFontSize(muteProfileText, R.dimen.qs_tile_text_size);
        muteProfile.setOnClickListener(mProfileSwitchListener);
        muteProfile.setTag(AudioProfileManager.getProfileKey(Scenario.SILENT));

        View meetingProfile = (View) mProfileSwitchDialog.findViewById(R.id.meeting_profile);
        TextView meetingProfileText =
                (TextView) mProfileSwitchDialog.findViewById(R.id.meeting_profile_text);
        meetingProfileText.setText(res.getString(R.string.meeting));
        FontSizeUtils.updateFontSize(meetingProfileText, R.dimen.qs_tile_text_size);
        meetingProfile.setOnClickListener(mProfileSwitchListener);
        meetingProfile.setTag(AudioProfileManager.getProfileKey(Scenario.MEETING));

        View outdoorProfile = (View) mProfileSwitchDialog.findViewById(R.id.outdoor_profile);
        TextView outdoorProfileText =
                (TextView) mProfileSwitchDialog.findViewById(R.id.outdoor_profile_text);
        outdoorProfileText.setText(res.getString(R.string.outdoor));
        FontSizeUtils.updateFontSize(outdoorProfileText, R.dimen.qs_tile_text_size);
        outdoorProfile.setOnClickListener(mProfileSwitchListener);
        outdoorProfile.setTag(AudioProfileManager.getProfileKey(Scenario.OUTDOOR));
    }

    private void showProfileSwitchDialog() {
        if (!mProfileSwitchDialog.isShowing()) {
            mProfileSwitchDialog.show();
            mHandler.removeCallbacks(mDismissProfileSwitchDialogRunnable);
            mHandler.postDelayed(mDismissProfileSwitchDialogRunnable, PROFILE_SWITCH_DIALOG_LONG_TIMEOUT);
        }
    }

    private Runnable mDismissProfileSwitchDialogRunnable = new Runnable() {
        public void run() {
            Log.d(LOG_TAG, "mDismissProfileSwitchDialogRunnable");
            if (mProfileSwitchDialog != null && mProfileSwitchDialog.isShowing()) {
                mProfileSwitchDialog.dismiss();
            }
        };
    };

    private void loadEnabledIcon(Scenario scenario) {
        int audioState = 0;
        switch (scenario) {
            case GENERAL:
                mNormalProfileIcon.setImageResource(R.drawable.ic_qs_normal_profile_enable_tpv);
                audioState = R.drawable.ic_qs_general_on_tpv;
                break;
            case MEETING:
                mMettingProfileIcon.setImageResource(R.drawable.ic_qs_meeting_profile_enable_tpv);
                audioState = R.drawable.ic_qs_meeting_on_tpv;
                break;
            case OUTDOOR:
                mOutdoorSwitchIcon.setImageResource(R.drawable.ic_qs_outdoor_profile_enable_tpv);
                audioState = R.drawable.ic_qs_outdoor_on_tpv;
                break;
            case SILENT:
                mMuteProfileIcon.setImageResource(R.drawable.ic_qs_mute_profile_enable_tpv);
                audioState = R.drawable.ic_qs_silent_on_tpv;
                break;
            case CUSTOM:
                audioState = R.drawable.ic_qs_custom_on_tpv;
            default:
                audioState = R.drawable.ic_qs_custom_on_tpv;
                break;
        }
        getQsTile().setIcon(Icon.createWithResource(this, audioState));
        getQsTile().updateTile();
    }

    private void loadDisabledIcon() {
        mNormalProfileIcon.setImageResource(R.drawable.ic_qs_normal_off_tpv);
        mMettingProfileIcon.setImageResource(R.drawable.ic_qs_meeting_profile_off_tpv);
        mOutdoorSwitchIcon.setImageResource(R.drawable.ic_qs_outdoor_off_tpv);
        mMuteProfileIcon.setImageResource(R.drawable.ic_qs_mute_profile_off_tpv);
    }

    private void refreshIcon(Scenario scenario) {
        loadDisabledIcon();
        loadEnabledIcon(scenario);
    }

    private Resources getLocalizedResources(Context context, Locale desiredLocale) {
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources();
    }
    private static class Log {
        static final boolean DEBUG = "eng".equals(android.os.Build.TYPE)
                || "userdebug".equals(android.os.Build.TYPE);

        public static void d(String tag, String msg, Throwable tr) {
            if (DEBUG) {
                android.util.Log.d(tag, msg, tr);
            }
        }

        public static void d(String tag, String msg) {
            if (DEBUG) {
                android.util.Log.d(tag, msg, null);
            }
        }
    }

    private static class FontSizeUtils {
        static void updateFontSize(TextView v, int dimensId) {
            if (v != null) {
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        v.getResources().getDimensionPixelSize(dimensId));
            }
        }
    }
}
