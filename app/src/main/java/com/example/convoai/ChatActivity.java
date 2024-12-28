package com.example.convoai;

import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView welcomeText;
    EditText messageEditText;
    Button sendButton;

    List<Message> messageList;

    MessageAdapter messageAdapter;

    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        messageList = new ArrayList<>();


        recyclerView = findViewById(R.id.recycler_view);
        welcomeText = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        sendButton.setOnClickListener(view -> {
            String question = messageEditText.getText().toString().trim();
            addToChat(question, Message.SENDER);
            messageEditText.setText("");
            callGemini(question);
            welcomeText.setVisibility(View.GONE);
        });
    }

    void addToChat(String message, String sender) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sender));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageList.size() - 1);
            }
        });
    }

    void addResponseToChat(String response) {
        messageList.remove(messageList.size() - 1);
        addToChat(response, Message.RECEIVER);
    }
    void callGemini(String question) {
        messageList.add(new Message("Typing...", Message.RECEIVER));
        // Call Gemini API
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put(
                    "contents",
                    new JSONArray().put(
                            new JSONObject().put(
                                    "parts",
                                    new JSONArray().put(
                                            new JSONObject().put(
                                                    "text",
                                                    question
                                            )
                                    )
                            )
                    )
            );
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Add the code to call the Gemini API here
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + getString(R.string.gemini_api_key))
                .header("Content-Type","application/json")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponseToChat("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONArray candidates = jsonResponse.getJSONArray("candidates");

                        // Access the first candidate and its content
                        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");

                        // Get the text from the first part
                        String responseText = parts.getJSONObject(0).getString("text");

                        // Call renderMarkdown to format the text before adding to chat
                        renderMarkdown(responseText);

                    } catch (JSONException e) {
                        addResponseToChat("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    addResponseToChat("Failed to load response: " + response.message());
                }
            }
        });

    }


    public void renderMarkdown(String markdownText) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownText);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        // Convert HTML to Spanned
        Spanned spannedText = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
        addResponseToChat(spannedText.toString());
    }
}