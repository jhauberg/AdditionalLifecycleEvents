package com.github.lifecycle.activity;

import com.github.lifecycle.R;

import android.os.Bundle;

public class Example extends MonitoredActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_example);
    }
}