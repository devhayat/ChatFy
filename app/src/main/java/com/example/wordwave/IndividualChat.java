package com.example.wordwave;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class IndividualChat extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;

    private String targetUserId, targetUserName, targetUserProfilePicUri, currentUserId;
    private ImageView backButtonindividualchatHeading, profileicon_individualchat_heading, attachImageButton;

    private TextView username_individualchat_heading, status_individualchat_heading;
    private LinearLayout linearlayout_individualchat_heading;
    private FirebaseDatabase database;
    private EditText messageEdittext;
    private ShapeableImageView sendbutton;
    private Button callicon_individualchat_heading,videocallicon_individualchat_heading;
    private androidx.recyclerview.widget.RecyclerView recylerView_individualchat;
    private RecyclerAdapterIndividualChat adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_chat);
        initlization();
    }

    private void initlization() {
        // UI references
        backButtonindividualchatHeading = findViewById(R.id.backbutton_individualchat_heading);
        profileicon_individualchat_heading = findViewById(R.id.profileicon_individualchat_heading);
        callicon_individualchat_heading = findViewById(R.id.callicon_individualchat_heading);
        videocallicon_individualchat_heading = findViewById(R.id.videocallicon_individualchat_heading);
        username_individualchat_heading = findViewById(R.id.username_individualchat_heading);
        status_individualchat_heading = findViewById(R.id.status_individualchat_heading);
        linearlayout_individualchat_heading = findViewById(R.id.linearlayout_individualchat_heading);
        attachImageButton = findViewById(R.id.individualchat_sendimage);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        targetUserId = getIntent().getStringExtra("userId");

        // Fetch user info from Firestore
        FirebaseFirestore.getInstance().collection("users").document(targetUserId)
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException error) {
                        if (doc != null) {
                            targetUserName = doc.getString("userName");
                            targetUserProfilePicUri = doc.getString("profilePicUri");

                            username_individualchat_heading.setText(
                                    currentUserId.equals(targetUserId) ? targetUserName + " (Me)" : targetUserName
                            );
                            Glide.with(IndividualChat.this)
                                    .load(targetUserProfilePicUri)
                                    .into(profileicon_individualchat_heading);
                        }
                    }
                });

        backButtonindividualchatHeading.setOnClickListener(v -> finish());

        linearlayout_individualchat_heading.setOnClickListener(v -> {
            Intent intent = new Intent(IndividualChat.this, IndividualUserInfo.class);
            intent.putExtra("userId", targetUserId);
            startActivity(intent);
        });

        attachImageButton.setOnClickListener(v -> pickImageFromGallery());

        setupChat();
    }

    private void setupChat() {
        recylerView_individualchat = findViewById(R.id.individualchat_recyclerview);
        sendbutton = findViewById(R.id.individualchat_sendbutton);
        messageEdittext = findViewById(R.id.individualchat_message_edittext);

        adapter = new RecyclerAdapterIndividualChat(
                this, new ArrayList<>(), currentUserId, targetUserId, getMenuInflater()
        );

        recylerView_individualchat.setLayoutManager(new LinearLayoutManager(this));
        recylerView_individualchat.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();
        DatabaseReference messagesRef = database.getReference()
                .child("chats")
                .child(currentUserId + targetUserId)
                .child("messages");

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<MessageModel> messages = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel mm = dataSnapshot.getValue(MessageModel.class);
                    messages.add(mm);
                }
                adapter.function(messages);
                recylerView_individualchat.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        sendbutton.setOnClickListener(v -> sendTextMessage());
        setupUserStatus();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadImageToCloudinary(imageUri);
            }
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
        MediaManager.get().upload(imageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        sendImageMessage(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(IndividualChat.this,
                                "Upload failed: " + error.getDescription(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void sendTextMessage() {
        String message = messageEdittext.getText().toString().trim();
        if (!message.isEmpty()) {
            messageEdittext.setText("");
            sendMessageToFirebase(message, "text");
        }
    }

    private void sendImageMessage(String imageUrl) {
        sendMessageToFirebase(imageUrl, "image");
    }

    private void sendMessageToFirebase(String content, String type) {
        MessageModel messageModel = new MessageModel(content, currentUserId, new Date().getTime(), type);
        Map<String, Object> connectionMap = new HashMap<>();
        connectionMap.put("key", "value");

        // Update connection for current user
        database.getReference().child("Connections/" + currentUserId + "/" + targetUserId).updateChildren(connectionMap);

        // Add message for current user
        database.getReference().child("chats").child(currentUserId + targetUserId).child("messages").push()
                .setValue(messageModel)
                .addOnCompleteListener(task -> {
                    if (!currentUserId.equals(targetUserId)) {
                        // Add message for target user
                        database.getReference().child("chats").child(targetUserId + currentUserId)
                                .child("messages").push().setValue(messageModel);

                        // Update connection for target user
                        database.getReference().child("Connections/" + targetUserId + "/" + currentUserId).updateChildren(connectionMap);
                    }
                });
    }

    private void setupUserStatus() {
        database.getReference().child("userStatus").child(targetUserId).child("lastseen")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String lastSeen = snapshot.getValue(String.class);
                        status_individualchat_heading.setText(lastSeen);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    protected void onResume() {
        database.getReference().child("userStatus").child(currentUserId).child("lastseen").setValue("Online");
        super.onResume();
    }

    @Override
    protected void onPause() {
        String lastSeen = "last seen at " +
                new SimpleDateFormat("hh:mm a dd MMM", Locale.getDefault()).format(new Date());
        database.getReference().child("userStatus").child(currentUserId).child("lastseen").setValue(lastSeen);
        super.onPause();
    }
}
