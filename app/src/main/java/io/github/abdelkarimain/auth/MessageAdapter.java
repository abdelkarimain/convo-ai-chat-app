package io.github.abdelkarimain.auth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.noties.markwon.Markwon;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    List<Message> messageList;
    private final Markwon markwon;
    private final Context context;

    public MessageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.markwon = Markwon.create(context);
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, null);
        return new MyViewHolder(chatView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.getSender().equals(Message.SENDER)) {
            holder.rightChatView.setVisibility(View.VISIBLE);
            holder.leftChatView.setVisibility(View.GONE);
            holder.rightChatText.setText(message.getMessage());
            holder.rightCopyButton.setOnClickListener(v -> copyToClipboard(message.getMessage()));
        } else {
            holder.leftChatView.setVisibility(View.VISIBLE);
            holder.rightChatView.setVisibility(View.GONE);
            markwon.setMarkdown(holder.leftChatText, message.getMessage());
            holder.leftCopyButton.setOnClickListener(v -> copyToClipboard(message.getMessage()));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftChatView, rightChatView;
        TextView leftChatText, rightChatText;
        ImageButton leftCopyButton, rightCopyButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatView = itemView.findViewById(R.id.left_chat_view);
            rightChatView = itemView.findViewById(R.id.right_chat_view);
            leftChatText = itemView.findViewById(R.id.left_chat_text_view);
            rightChatText = itemView.findViewById(R.id.right_chat_text_view);
            leftCopyButton = itemView.findViewById(R.id.left_copy_button);
            rightCopyButton = itemView.findViewById(R.id.right_copy_button);
        }
    }
}