package com.example.wordwave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecyclerAdapterIndividualChat extends RecyclerView.Adapter<RecyclerAdapterIndividualChat.ViewHolder> {

    private final Context context;
    private ArrayList<MessageModel> messageModels;
    private final String currentUserId;
    private final String targetUserId;
    private final MenuInflater menuInflater;

    RecyclerAdapterIndividualChat(Context context, ArrayList<MessageModel> messageModels,
                                  String currentUserId, String targetUserId, MenuInflater menuInflater) {
        this.context = context;
        this.messageModels = messageModels;
        this.currentUserId = currentUserId;
        this.targetUserId = targetUserId;
        this.menuInflater = menuInflater;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView messageText, timestamp;
        ImageView messageImage;

        public ViewHolder(@NonNull View itemView, int type) {
            super(itemView);
            if (type == 1) { // Sender
                messageText = itemView.findViewById(R.id.senderMessageText);
                messageImage = itemView.findViewById(R.id.senderMessageImage);
                timestamp = itemView.findViewById(R.id.senderMessageTime);
                itemView.setOnCreateContextMenuListener(this);
            } else { // Receiver
                messageText = itemView.findViewById(R.id.receiverMessageText);
                messageImage = itemView.findViewById(R.id.receiverMessageImage);
                timestamp = itemView.findViewById(R.id.recieverMessageTime);
            }
        }

        @Override
        public void onCreateContextMenu(android.view.ContextMenu menu, View v,
                                        android.view.ContextMenu.ContextMenuInfo menuInfo) {
            menuInflater.inflate(R.menu.contextmenurecyclerview, menu);

            // Delete for everyone
            menu.findItem(R.id.deleteforeveryone_menu_id).setOnMenuItemClickListener(item -> {
                removeMessageByTimestamp(messageModels.get(getAdapterPosition()).getTimeStamp(), true);
                return true;
            });

            // Delete for me
            menu.findItem(R.id.deleteforme_menu_id).setOnMenuItemClickListener(item -> {
                removeMessageByTimestamp(messageModels.get(getAdapterPosition()).getTimeStamp(), false);
                return true;
            });

            // Cancel
            menu.findItem(R.id.cancel_menu_id).setOnMenuItemClickListener(item -> true);
        }
    }

    private void removeMessageByTimestamp(long timestamp, boolean forEveryone) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference()
                .child("chats").child(currentUserId + targetUserId).child("messages");

        // Delete from current user's chat
        deleteFromDatabase(messagesRef, timestamp);

        // Also delete from target user's chat if needed
        if (forEveryone && !targetUserId.equals(currentUserId)) {
            DatabaseReference targetRef = FirebaseDatabase.getInstance().getReference()
                    .child("chats").child(targetUserId + currentUserId).child("messages");
            deleteFromDatabase(targetRef, timestamp);
        }
    }

    private void deleteFromDatabase(DatabaseReference ref, long timestamp) {
        ref.orderByChild("timeStamp").equalTo(timestamp)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot msgSnap : snapshot.getChildren()) {
                            msgSnap.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                for (int i = 0; i < messageModels.size(); i++) {
                                    if (messageModels.get(i).getTimeStamp() == timestamp) {
                                        messageModels.remove(i);
                                        notifyItemRemoved(i);
                                        break;
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public int getItemViewType(int position) {
        return messageModels.get(position).getSenderId().equals(currentUserId) ? 1 : 2;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == 1)
                ? R.layout.adapter_sample_sender_layout
                : R.layout.adapter_sample_receiver_layout;
        return new ViewHolder(LayoutInflater.from(context).inflate(layout, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel msg = messageModels.get(position);

        // Format timestamp
        String time = new SimpleDateFormat("hh:mm a").format(new Date(msg.getTimeStamp()));
        holder.timestamp.setText(time);

        // Show image if type is "image", otherwise show text
        if ("image".equalsIgnoreCase(msg.getType())) {
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(msg.getContent())
                    .into(holder.messageImage);
        } else {
            holder.messageImage.setVisibility(View.GONE);
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(msg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public void function(ArrayList<MessageModel> temp) {
        messageModels.clear();
        messageModels.addAll(temp);
        notifyDataSetChanged();
    }
}
