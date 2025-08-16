package com.example.wordwave;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.HashMap;
import java.util.Map;

public class Profile_Initlization extends AppCompatActivity {

    private EditText phoneNumber, Email, fullName, userName;
    private Button finishButton;
    private ProgressBar progressBar;
    private TextView usernameTakenOrNot;
    private ImageView profileinitlization_approvalicon;
    private ShapeableImageView profilePic, changeProfilePicButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fstore;
    private String sign, isNewUser, comeFrom, userID, profilePicUri = "";
    private int REQUEST_CODE_OPENGARALRYINTENT;
    private FirebaseUser currentUser;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_initlization);
        firstinitlization();
    }

    protected void firstinitlization() {
        fullName = findViewById(R.id.profileinitlization_fullname);
        userName = findViewById(R.id.profileinitlization_username);
        Email = findViewById(R.id.profileinitlization_email);
        phoneNumber = findViewById(R.id.profileinitlization_phonenumber);
        finishButton = findViewById(R.id.profileinitlization_finish_button);
        progressBar = findViewById(R.id.profileinitlization_progressbar);
        usernameTakenOrNot = findViewById(R.id.profileinitlization_usernametakenornot_dis);
        profileinitlization_approvalicon = findViewById(R.id.profileinitlization_smallicon);
        profilePic = findViewById(R.id.setupprofile_profilepic);
        changeProfilePicButton = findViewById(R.id.profileinitlization_profilepicButton);
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        bundle = getIntent().getExtras();
        sign = bundle.getString("sign");
        isNewUser = bundle.getString("isNewUser");
        comeFrom = bundle.getString("comeFrom");
        userID = mAuth.getCurrentUser().getUid();
        REQUEST_CODE_OPENGARALRYINTENT = 1000;
        currentUser = mAuth.getCurrentUser();

        secondinitlization();
    }

    protected void secondinitlization() {
        if (isNewUser.equals("false")) {
            setInProgress(true);
            fstore.collection("users").document(userID).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && doc.getString("profilePicUri") != null) {
                            Picasso.get().load(doc.getString("profilePicUri")).into(profilePic);
                            profilePicUri = doc.getString("profilePicUri");
                        }
                        setInProgress(false);
                    })
                    .addOnFailureListener(e -> {
                        ToastMaker.show(this, "Failed to load image from Firestore");
                        setInProgress(false);
                    });
        } else if (comeFrom.equals("Google")) {
            setInProgress(true);
            Picasso.get().load(currentUser.getPhotoUrl()).into(profilePic, new Callback() {
                @Override
                public void onSuccess() {
                    uploadImageToCloudinary(currentUser.getPhotoUrl());
                }
                @Override
                public void onError(Exception e) {
                    ToastMaker.show(Profile_Initlization.this, "Error loading Google image");
                    setInProgress(false);
                }
            });
        } else {
            profilePic.setImageResource(R.drawable.default_profile_icon);
        }

        changeProfilePicButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_OPENGARALRYINTENT);
        });

        thirdinitlization();
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        setInProgress(true);
        MediaManager.get().upload(imageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        ToastMaker.show(Profile_Initlization.this, "Uploading image...");
                    }
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        profilePicUri = resultData.get("secure_url").toString();
                        ToastMaker.show(Profile_Initlization.this, "Image uploaded");
                        setInProgress(false);
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        ToastMaker.show(Profile_Initlization.this, "Cloudinary upload failed: " + error.getDescription());
                        Log.e("CloudinaryUpload", error.getDescription());
                        setInProgress(false);
                    }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPENGARALRYINTENT && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            Picasso.get().load(imageUri).into(profilePic);
            uploadImageToCloudinary(imageUri);
        }
    }

    protected void thirdinitlization() {
        if (isNewUser.equals("false")) {
            DocumentReference docRef = fstore.collection("users").document(userID);
            docRef.addSnapshotListener(this, (snapshot, e) -> {
                if (snapshot != null) {
                    fullName.setText(snapshot.getString("fullName"));
                    userName.setText(snapshot.getString("userName"));
                    Email.setText(snapshot.getString("email"));
                    phoneNumber.setText(snapshot.getString("phone"));
                    userName.setEnabled(false);
                    usernameTakenOrNot.setText("Username is already set");
                }
            });
        } else if (comeFrom.equals("Google")) {
            Email.setText(currentUser.getEmail());
        } else if (comeFrom.equals("Phone")) {
            String s = currentUser.getPhoneNumber();
            phoneNumber.setText(s.substring(s.length() - 10));
        } else if (comeFrom.equals("Email")) {
            Email.setText(bundle.getString("email"));
            Email.setEnabled(false);
            userName.setText(bundle.getString("username"));
            userName.setEnabled(false);
        }

        if (isNewUser.equals("true")) {
            userName.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    fstore.collection("users").whereEqualTo("userName", s.toString().trim())
                            .get().addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    changeDiscription(false);
                                    finishButton.setEnabled(false);
                                } else {
                                    changeDiscription(true);
                                    finishButton.setEnabled(true);
                                }
                            });
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    protected void setInProgress(boolean isProgress) {
        progressBar.setVisibility(isProgress ? View.VISIBLE : View.GONE);
        finishButton.setTextColor(getResources().getColor(isProgress ? android.R.color.transparent : R.color.black));
    }

    private void changeDiscription(boolean isAvailable) {
        usernameTakenOrNot.setText(isAvailable ? "Username available" : "Username is already taken");
        usernameTakenOrNot.setTextColor(getResources().getColor(isAvailable ? R.color.green : R.color.red));
        profileinitlization_approvalicon.setImageDrawable(AppCompatResources.getDrawable(this,
                isAvailable ? R.drawable.aprove_img : R.drawable.reject_img));
    }

    public void finishButtonclicked(View view) {
        setInProgress(true);
        String fullname = fullName.getText().toString().trim();
        String username = userName.getText().toString().trim();
        String email = Email.getText().toString().trim();
        String phonenumber = phoneNumber.getText().toString().trim();

        if (fullname.isEmpty() || username.isEmpty() || email.isEmpty() || phonenumber.isEmpty()) {
            ToastMaker.show(this, "Fill all fields");
            setInProgress(false);
            return;
        }
        if (profilePicUri.isEmpty()) {
            ToastMaker.show(this, "Please wait until profile image is uploaded");
            setInProgress(false);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", fullname);
        data.put("userName", username);
        data.put("email", email);
        data.put("phone", phonenumber);
        data.put("profilePicUri", profilePicUri);

        fstore.collection("users").document(userID).set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ToastMaker.show(this, "Data added successfully");
                        if (sign.equals("signUp")) {
                            mAuth.signOut();
                            startActivity(new Intent(this, Authentication.class));
                        } else {
                            startActivity(new Intent(this, MainActivity.class));
                        }
                        finish();
                    } else {
                        ToastMaker.show(this, "Error storing data: " + task.getException());
                    }
                    setInProgress(false);
                });
    }
}
