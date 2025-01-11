package io.github.abdelkarimain.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class HistoryFragment extends Fragment {

    private LinearLayout historyContainer;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        historyContainer = view.findViewById(R.id.historyContainer);
        progressBar = view.findViewById(R.id.progressBar2);
        db = FirebaseFirestore.getInstance();

        fetchAllChatHistory();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchAllChatHistory();
    }

    private void fetchAllChatHistory() {
        if (historyContainer != null) {
            historyContainer.removeAllViews();
        }
        
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            historyContainer.setVisibility(View.GONE);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("chats")
                .document(user.getUid())
                .collection("chat_sessions")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                // No limit here to show all chats
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    historyContainer.setVisibility(View.VISIBLE);
                    
                    if (task.isSuccessful()) {
                        historyContainer.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String firstMessage = document.getString("firstMessage");
                            String chatId = document.getId();
                            if (firstMessage != null && !firstMessage.isEmpty()) {
                                addChatHistoryItem(firstMessage, chatId);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    historyContainer.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), 
                        "Error loading chat history: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void addChatHistoryItem(String firstMessage, String chatId) {
        MaterialCardView cardView = (MaterialCardView) LayoutInflater.from(getContext())
                .inflate(R.layout.chat_history_item, historyContainer, false);

        TextView messageTextView = cardView.findViewById(R.id.messageTextView);
        String displayMessage = firstMessage.length() > 50 ? 
            firstMessage.substring(0, 47) + "..." : firstMessage;
        messageTextView.setText(displayMessage);

        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("chat_id", chatId);
            startActivity(intent);
        });

        historyContainer.addView(cardView, 0);
    }
}