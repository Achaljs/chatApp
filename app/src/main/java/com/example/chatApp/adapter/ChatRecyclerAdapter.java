package com.example.chatApp.adapter;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatApp.R;
import com.example.chatApp.model.ChatMessageModel;
import com.example.chatApp.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    Context context;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options,Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {

        if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);

            if (model.getFileUrl() != null && !model.getFileUrl().isEmpty()) {
                holder.rightChatTextview.setText("File Sent: " + model.getFileUrl());
                holder.rightChatTextview.setOnClickListener(v -> {
                    if (!model.getFileUrl().startsWith("http") || !model.getFileUrl().startsWith("https")) {
                        Uri uri = Uri.parse(model.getFileUrl());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                    } else {
                        Uri fileUri = Uri.parse(model.getFileUrl());
                        downloadFile(context, fileUri);
                    }
                });
            } else {
                holder.rightChatTextview.setText(model.getMessage());
            }
        } else {
            // Similar logic for handling file URLs on the receiver side

            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);

            if (model.getFileUrl() != null && !model.getFileUrl().isEmpty()) {
                holder.leftChatTextview.setText("File Sent: " + model.getFileUrl());
                holder.leftChatTextview.setOnClickListener(v -> {
                    if (!model.getFileUrl().startsWith("http") || !model.getFileUrl().startsWith("https")) {
                        Uri uri = Uri.parse(model.getFileUrl());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                    } else {
                        Uri fileUri = Uri.parse(model.getFileUrl());
                        downloadFile(context, fileUri);
                    }
                });
            } else {
                holder.leftChatTextview.setText(model.getMessage());
            }
       }
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row,parent,false);
        return new ChatModelViewHolder(view);
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder{

        LinearLayout leftChatLayout,rightChatLayout;
        TextView leftChatTextview,rightChatTextview;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
        }
    }
    private void downloadFile(Context context, Uri fileUri) {
        // Implement file download logic here
        // For example, you can use DownloadManager to download the file
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(fileUri);
        request.setTitle("Downloading file");
        request.setDescription("File download in progress");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "filename.extension"); // Set the file destination

        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }

}
