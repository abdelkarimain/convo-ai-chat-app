package io.github.abdelkarimain.auth;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.noties.markwon.Markwon;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    List<Message> messageList;
    private final Markwon markwon;

    // Update constructor to accept Context
    public MessageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.markwon = Markwon.create(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, null);
        MyViewHolder myViewHolder = new MyViewHolder(chatView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.getSender().equals(Message.SENDER)) {
            holder.rightChatView.setVisibility(View.VISIBLE);
            holder.leftChatView.setVisibility(View.GONE);
            holder.rightChatText.setText(message.getMessage());
        } else {
            holder.leftChatView.setVisibility(View.VISIBLE);
            holder.rightChatView.setVisibility(View.GONE);
            // Render markdown for receiver messages
            markwon.setMarkdown(holder.leftChatText, message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftChatView, rightChatView;
        TextView leftChatText, rightChatText;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatView = itemView.findViewById(R.id.left_chat_view);
            rightChatView = itemView.findViewById(R.id.right_chat_view);
            leftChatText = itemView.findViewById(R.id.left_chat_text_view);
            rightChatText = itemView.findViewById(R.id.right_chat_text_view);
        }
    }
}

