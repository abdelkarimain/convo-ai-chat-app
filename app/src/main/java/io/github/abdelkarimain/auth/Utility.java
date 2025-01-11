package io.github.abdelkarimain.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Utility {
    static CollectionReference getMessagesCollection() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance().collection("chats")
                .document(user.getUid()).collection("messages");
    }

    static DocumentReference getChatDocument() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance().collection("chats")
                .document(user.getUid());
    }
}