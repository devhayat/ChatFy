package com.example.wordwave;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class StatusFragment extends Fragment {

    private static final int REQUEST_CODE_OPENGARALRYINTENT = 100;
    com.google.android.material.imageview.ShapeableImageView addstatusbutton;
    androidx.recyclerview.widget.RecyclerView recyclerViewstatus;
    ImageView storestatus; // temporary preview holder
    String un;

    StatusFragment(String un) {
        this.un = un;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        recyclerViewstatus = view.findViewById(R.id.recyclerview_mainactivity_statusfragment);
        addstatusbutton = view.findViewById(R.id.statusfragment_addstatusbutton);
        storestatus = view.findViewById(R.id.imageview_store_status);

        recyclerViewstatus.setLayoutManager(new LinearLayoutManager(getActivity()));

        RecyclerAdapterPhotoStatus raps = new RecyclerAdapterPhotoStatus(getActivity(), new ArrayList<>());
        recyclerViewstatus.setAdapter(raps);

        // open gallery
        addstatusbutton.setOnClickListener(v -> {
            Intent openGalaryIntent = new Intent();
            openGalaryIntent.setAction(Intent.ACTION_GET_CONTENT);
            openGalaryIntent.setType("image/*");
            startActivityForResult(openGalaryIntent, REQUEST_CODE_OPENGARALRYINTENT);
        });

        ArrayList<String> connections = new ArrayList<>();

        // load connections
        FirebaseDatabase.getInstance().getReference()
                .child("Connections")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        connections.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            connections.add(snapshot.getKey());
                        }

                        // load statuses
                        FirebaseDatabase.getInstance().getReference().child("photoStatus")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        ArrayList<Row_RecyclerView_Photo_Status> rows = new ArrayList<>();
                                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                            String userId = userSnapshot.getKey();

                                            if (connections.contains(userId) || userId.equals(FirebaseAuth.getInstance().getUid())) {
                                                for (DataSnapshot statusSnap : userSnapshot.getChildren()) {
                                                    Row_RecyclerView_Photo_Status r = statusSnap.getValue(Row_RecyclerView_Photo_Status.class);
                                                    if (r != null) {
                                                        if (userId.equals(FirebaseAuth.getInstance().getUid())) {
                                                            r.userName = "My Status";
                                                            rows.add(0, r);
                                                        } else {
                                                            rows.add(r);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        raps.fun(rows);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e("Firebase", "Failed to read statuses: " + databaseError.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Firebase", "Failed to read connections: " + databaseError.getMessage());
                    }
                });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OPENGARALRYINTENT && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            if (imageUri != null) {
                // Preview selected image
                Picasso.get().load(imageUri).into(storestatus, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        uploadStatusToCloudinary(imageUri);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getActivity(), "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    protected void uploadStatusToCloudinary(Uri imageUri) {
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setCancelable(false);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Updating status...");
        pd.show();

        com.cloudinary.android.MediaManager.get().upload(imageUri)
                .callback(new com.cloudinary.android.callback.UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        pd.dismiss();
                        String imageUrl = resultData.get("secure_url") != null
                                ? resultData.get("secure_url").toString()
                                : null;

                        if (imageUrl == null) {
                            Toast.makeText(getActivity(), "Upload failed: no URL returned", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        long timestamp = System.currentTimeMillis();
                        String timeStamp = formatTimestamp(timestamp);

                        Row_RecyclerView_Photo_Status r = new Row_RecyclerView_Photo_Status(
                                timeStamp, imageUrl, un
                        );

                        FirebaseDatabase.getInstance().getReference()
                                .child("photoStatus")
                                .child(FirebaseAuth.getInstance().getUid())
                                .push() // <-- multiple statuses allowed
                                .setValue(r)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Status updated successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), "Failed to save status", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        pd.dismiss();
                    }
                }).dispatch();
    }

    public String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a , dd MMM", Locale.getDefault());
        return sdf.format(date);
    }
}
