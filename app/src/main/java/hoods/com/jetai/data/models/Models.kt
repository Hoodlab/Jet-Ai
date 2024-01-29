package hoods.com.jetai.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

enum class ModelName(val modelName: String) {
    TEXT("gemini-pro"),
    MULTIMODAL("gemini-pro-vision")
}

enum class Participant {
    USER, MODEL, ERROR
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val participant: Participant = Participant.USER,
    val timestamp: Timestamp = Timestamp.now(),
)

data class ChatRoom(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val timestamp: Timestamp = Timestamp.now(),
    val userId: String = "",
)






