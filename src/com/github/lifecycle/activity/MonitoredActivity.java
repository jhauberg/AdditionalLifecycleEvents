package com.github.lifecycle.activity;

import com.github.lifecycle.MonitoredApplication;

import android.app.Activity;
import android.view.KeyEvent;

public class MonitoredActivity extends Activity {
    @Override
    protected void onResume() {
        super.onResume();

        MonitoredApplication.handleApplicationBroughtToForeground(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        MonitoredApplication.handleApplicationBroughtToBackground(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            MonitoredApplication.handleApplicationClosing(this);
        }

        return super.onKeyDown(keyCode, event);
    }
}