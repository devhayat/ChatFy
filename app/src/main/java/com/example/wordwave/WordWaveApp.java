package com.example.wordwave;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class WordWaveApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Cloudinary config
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "djoqa1fp9");
        config.put("api_key", "196146353369721");
        config.put("api_secret", "3qdoblDBF17xyw3DYsf7nVR6324");
        try {
            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }


    }
}
