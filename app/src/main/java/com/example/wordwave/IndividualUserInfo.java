package com.example.wordwave;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IndividualUserInfo extends AppCompatActivity {

    private String targetUserId;
    private ImageView profilepic;
    private TextView username, fullname, email, phoneno;
    private androidx.appcompat.widget.Toolbar toolbar;
    private LinearLayout sharelayout;
    private String collecteddata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_user_info);

        initlization();
    }

    private void initlization() {

        toolbar = findViewById(R.id.toolbar_individualuserinfo);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_button_individualchat_heading);

        targetUserId = getIntent().getExtras().getString("userId");
        profilepic = findViewById(R.id.profilepic_individualinfo);
        username = findViewById(R.id.username_individualinfo);
        fullname = findViewById(R.id.fullname_individualinfo);
        email = findViewById(R.id.email_individualinfo);
        phoneno = findViewById(R.id.phoneno_individualinfo);
        sharelayout = findViewById(R.id.share_layout_individualinfo);

        // Fetch user info from Firestore
        FirebaseFirestore.getInstance().collection("users").document(targetUserId)
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                        if (documentSnapshot != null) {

                            String fn = documentSnapshot.getString("fullName");
                            String un = documentSnapshot.getString("userName");
                            String e = documentSnapshot.getString("email");
                            String p = documentSnapshot.getString("phone");
                            String ppu = documentSnapshot.getString("profilePicUri");

                            username.setText(un);
                            fullname.setText("~" + fn);
                            Glide.with(IndividualUserInfo.this).load(ppu).into(profilepic);
                            email.setText(e);
                            phoneno.setText("+91" + p);

                            collecteddata = "WORD WAVE ACCOUNT INFO\n\n" +
                                    "USERNAME :- " + un + "\nFULLNAME :- " + fn +
                                    "\nEMAIL :- " + e + "\nPHONE NO :- " + p;

                            sharelayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Word Wave account info");
                                    intent.putExtra(Intent.EXTRA_TEXT, collecteddata);
                                    startActivity(Intent.createChooser(intent, "Share Via"));
                                }
                            });

                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        FirebaseDatabase.getInstance().getReference()
                .child("userStatus")
                .child(FirebaseAuth.getInstance().getUid())
                .child("lastseen")
                .setValue("Online")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) { }
                });
        super.onResume();
    }

    @Override
    protected void onPause() {
        long timestamp = System.currentTimeMillis();
        String lastSeen = formatTimestamp(timestamp);
        FirebaseDatabase.getInstance().getReference()
                .child("userStatus")
                .child(FirebaseAuth.getInstance().getUid())
                .child("lastseen")
                .setValue(lastSeen)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) { }
                });
        super.onPause();
    }

    public String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a dd MMM", Locale.getDefault());
        return "last seen at " + sdf.format(date);
    }
}
