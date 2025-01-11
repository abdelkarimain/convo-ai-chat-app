package io.github.abdelkarimain.auth;

import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.ImagesPlugin;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private static final MediaType JSON = MediaType.get("application/json");
    private final OkHttpClient client = new OkHttpClient();

    private RecyclerView recyclerView;
    private TextView welcomeText;
    private TextView btnGoBack;
    private EditText messageEditText;
    private MaterialButton sendButton;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    private boolean isNewChat = true;
    private String chatId;
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        isNewChat = getIntent().getBooleanExtra("is_new_chat", false);
        chatId = getIntent().getStringExtra("chat_id");
        
        // Generate chatId only when it's a new chat and after first message
        if (isNewChat) {
            chatId = null;
        }

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupWindowInsets();
        setupRecyclerView();
        setupClickListeners();
        loadChatHistory();

        // Initialize Markwon
        markwon = Markwon.builder(this)
            .usePlugin(TablePlugin.create(this))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .build();
    }

    private void loadChatHistory() {
        Query query = Utility.getMessagesCollection()
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING);

        query.get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                messageList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String message = document.getString("message");
                    String sender = document.getString("sender");
                    long timestamp = document.getLong("timestamp");
                    
                    Message msg = new Message(message, sender, chatId);
                    msg.setTimestamp(timestamp);
                    messageList.add(msg);
                }
                messageAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    recyclerView.scrollToPosition(messageList.size() - 1);
                    welcomeText.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(e -> {
                if (e.getMessage().contains("FAILED_PRECONDITION")) {
                    Toast.makeText(this, 
                        "Setting up database, please wait a moment and try again...", 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, 
                        "Error loading messages: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view);
        welcomeText = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        btnGoBack = findViewById(R.id.btnGoBack);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, this); // Pass context here
    }

    private void setupWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View rootView = findViewById(R.id.main);
        View bottomLayout = findViewById(R.id.bottom_layout);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            WindowInsetsCompat insets = WindowInsetsCompat.toWindowInsetsCompat(windowInsets.toWindowInsets());
            int bottomPadding = insets.getSystemWindowInsetBottom();
            int topPadding = insets.getSystemWindowInsetTop();

            // Apply padding to main layout
            v.setPadding(0, topPadding, 0, 14);

            // Handle bottom layout
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bottomLayout.getLayoutParams();
            params.bottomMargin = bottomPadding;
            bottomLayout.setLayoutParams(params);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        btnGoBack.setOnClickListener(v -> finish());

        sendButton.setOnClickListener(v -> {
            String question = messageEditText.getText().toString().trim();
            if (!question.isEmpty()) {
                addToChat(question, Message.SENDER);
                messageEditText.setText("");
                callGemini(question);
                welcomeText.setVisibility(View.GONE);
            }
        });
    }

    private void addToChat(String message, String sender) {
        runOnUiThread(() -> {
            // Generate chatId for new chat when sending first message
            if (isNewChat && chatId == null && sender.equals(Message.SENDER)) {
                chatId = "chat_" + System.currentTimeMillis();
            }

            Message newMessage = new Message(message, sender, chatId);
            messageList.add(newMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.smoothScrollToPosition(messageList.size() - 1);

            if (!"Typing...".equals(message)) {
                // Convert message to HashMap for Firestore
                Map<String, Object> messageData = new HashMap<>();
                messageData.put("message", message);
                messageData.put("sender", sender);
                messageData.put("chatId", chatId);
                messageData.put("timestamp", System.currentTimeMillis());

                // Save message to messages collection
                Utility.getMessagesCollection()
                    .add(messageData)
                    .addOnSuccessListener(documentReference -> {
                        // Only create chat session for first user message
                        if (isNewChat && sender.equals(Message.SENDER)) {
                            createChatSession(message);
                            isNewChat = false;
                        }
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(this, "Error saving message: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
            }
        });
    }

    private void createChatSession(String firstMessage) {
        Map<String, Object> chatSession = new HashMap<>();
        chatSession.put("firstMessage", firstMessage);
        chatSession.put("timestamp", System.currentTimeMillis());
        
        Utility.getChatDocument()
            .collection("chat_sessions")
            .document(chatId)
            .set(chatSession)
            .addOnSuccessListener(aVoid -> 
                Log.d("ChatActivity", "Chat session created successfully")
            )
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error creating chat: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
    }

    private void addResponseToChat(String response) {
        runOnUiThread(() -> {
            if (!messageList.isEmpty()) {
                messageList.remove(messageList.size() - 1);
                messageAdapter.notifyItemRemoved(messageList.size());
            }
            addToChat(response, Message.RECEIVER);
        });
    }

    private void callGemini(String question) {
        addToChat("Typing...", Message.RECEIVER);

        JSONObject jsonBody = createRequestBody(question);
        if (jsonBody == null) return;

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" +
                        getString(R.string.gemini_api_key))
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponseToChat("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                handleGeminiResponse(response);
            }
        });
    }

    private JSONObject createRequestBody(String question) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("contents", new JSONArray()
                    .put(new JSONObject()
                            .put("parts", new JSONArray()
                                    .put(new JSONObject()
                                            .put("text", question)))));
            return jsonBody;
        } catch (JSONException e) {
            addResponseToChat("Failed to create request: " + e.getMessage());
            return null;
        }
    }

    private void handleGeminiResponse(@NonNull Response response) throws IOException {
        if (response.isSuccessful() && response.body() != null) {
            try {
                JSONObject jsonResponse = new JSONObject(response.body().string());
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                String responseText = parts.getJSONObject(0).getString("text");
                renderMarkdown(responseText);
            } catch (JSONException e) {
                addResponseToChat("Failed to parse response: " + e.getMessage());
            }
        } else {
            addResponseToChat("Failed to load response: " + response.message());
        }
    }

    private void renderMarkdown(String markdownText) {
        runOnUiThread(() -> {
            // First remove the "Typing..." message
            if (!messageList.isEmpty()) {
                messageList.remove(messageList.size() - 1);
                messageAdapter.notifyItemRemoved(messageList.size());
            }

            // Create a new Message with the processed markdown
            Message message = new Message(markdownText, Message.RECEIVER, chatId);
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);

            // Save the message to Firestore
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("message", markdownText);
            messageData.put("sender", Message.RECEIVER);
            messageData.put("chatId", chatId);
            messageData.put("timestamp", System.currentTimeMillis());

            Utility.getMessagesCollection()
                .add(messageData)
                .addOnFailureListener(e -> 
                    Toast.makeText(ChatActivity.this, 
                        "Error saving message: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show()
                );
        });
    }
}