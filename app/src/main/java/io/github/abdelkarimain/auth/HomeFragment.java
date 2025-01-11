package io.github.abdelkarimain.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private LinearLayout historyContainer;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    FirebaseAuth fAuth;

    private TextView textView2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        historyContainer = view.findViewById(R.id.historyContainer);
        progressBar = view.findViewById(R.id.progressBar2);
        LinearLayout startNewTextChat = view.findViewById(R.id.startNewTextChat); // Changed to LinearLayout


        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();
        textView2 = view.findViewById(R.id.textView2);
        textView2.setText("Welcome \uD83D\uDC4B " + user.getDisplayName() );

        // Set click listener
        startNewTextChat.setOnClickListener(this::startNewChat);

        // Add this new click listener
        LinearLayout startNewImageChat = view.findViewById(R.id.startNewImageChat);
        startNewImageChat.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ImageChatActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();

        // Fetch chat history
        fetchChatHistory();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchChatHistory(); // Reload chat history when fragment becomes visible
    }

    private void fetchChatHistory() {
        if (historyContainer != null) {
            historyContainer.removeAllViews();
        }
        
        // Show progress bar before starting fetch
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
                .limit(6)
                .get()
                .addOnCompleteListener(task -> {
                    // Hide progress bar and show container
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
                    // Hide progress bar on failure too
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
        // Truncate long messages
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

    public void startNewChat(View view) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("is_new_chat", true); 
        startActivity(intent);
    }
}
