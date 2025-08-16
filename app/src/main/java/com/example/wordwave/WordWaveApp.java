package com.example.wordwave;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallService;



import java.util.HashMap;
import java.util.Map;

public class WordWaveApp extends Application {

    // TODO: Replace with your real Zego credentials
    private static final long ZEGO_APP_ID = 123456789L;
    private static final String ZEGO_APP_SIGN = "your_zego_app_sign_here";

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

        // Auto-login to Zego if Firebase user exists
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            initZegoForUser(currentUser);
        }
    }

    private void initZegoForUser(FirebaseUser firebaseUser) {
        String zegoUserID = firebaseUser.getUid();
        String zegoUserName = firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName().isEmpty()
                ? firebaseUser.getDisplayName()
                : "User_" + zegoUserID.substring(0, 5);

        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallService.init(
                getApplicationContext(),
                ZEGO_APP_ID,
                ZEGO_APP_SIGN,
                zegoUserID,
                zegoUserName,
                invitationConfig
        );
    }
}
