package com.example.scanplus;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import com.journeyapps.barcodescanner.CaptureActivity;

public class CaptureAct extends CaptureActivity {
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
