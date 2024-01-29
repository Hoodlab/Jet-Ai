package hoods.com.jetai.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import hoods.com.jetai.Graph
import hoods.com.jetai.data.models.ChatMessage
import hoods.com.jetai.data.models.ChatRoom
import hoods.com.jetai.data.models.ModelName
import hoods.com.jetai.data.models.Participant
import hoods.com.jetai.utils.Response
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val generativeModel: GenerativeModel = Graph.generativeModel(ModelName.TEXT.modelName),
    private val auth: FirebaseAuth = Firebase.auth,
    db: FirebaseFirestore = Firebase.firestore,
) {
    private var chat = generativeModel.startChat()
    private val chatRef = db.collection(USER_CHATS_COLLECTION)

    companion object {
        const val USER_CHATS_COLLECTION = "user_chats"
        const val CHAT_COLLECTION = "chats"
        const val MESSAGE_SUB_COLLECTION = "messages"
    }

    suspend fun createChatRoom(): String? {
        val documentId = chatRef.document().id
        return try {
            auth.currentUser?.uid?.let {
                chatRef.document(documentId)
                    .set(ChatRoom(id = documentId, userId = it))
                    .await()
                documentId
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getChatRoomList(): Flow<Response<List<ChatRoom>>> = callbackFlow {
        var snapshotListener: ListenerRegistration? = null
        try {
            trySend(Response.Loading())
            snapshotListener = chatRef
                .orderBy("timestamp")
                .whereEqualTo("userId", auth.currentUser?.uid)
                .addSnapshotListener { snapShot, error ->
                    val response = if (snapShot != null) {
                        val chatRooms = snapShot.toObjects(ChatRoom::class.java)
                        Response.Success(dataSuccess = chatRooms)
                    } else {
                        Response.Error(throwable = error)
                    }
                    trySend(response)
                }


        } catch (e: Exception) {
            trySend(Response.Error(e.cause))
        }
        awaitClose { snapshotListener?.remove() }
    }

    suspend fun sendMessage(userPrompt: String, chatId: String): Flow<Response<Unit>> = flow {
        try {
            saveChat(
                ChatMessage(text = userPrompt),
                chatId
            )
            emit(Response.Loading())
            val response = chat.sendMessage(userPrompt)
            response.text?.let { modelResponse ->
                val chatMessage = ChatMessage(
                    text = modelResponse,
                    participant = Participant.MODEL
                )
                saveChat(chatMessage, chatId)
                emit(Response.Success(Unit))
            }


        } catch (e: Exception) {
            saveChat(
                ChatMessage(
                    text = e.localizedMessage ?: "Error Occured",
                    participant = Participant.ERROR
                ),
                chatId
            )
            emit(Response.Error(e.cause))
            e.printStackTrace()
        }

    }

    suspend fun fetchHistoryMsg(chatId: String, title: String) {
        if (title == "New Chat") {
            try {
                val msg = chatRef.document(chatId)
                    .collection(MESSAGE_SUB_COLLECTION)
                    .orderBy("timestamp")
                    .limit(5).get().await().toObjects(ChatMessage::class.java)
                if (msg.isNotEmpty()) {
                    val chatTitle = chat.sendMessage("Give me one best title for this content $msg")
                    chatRef.document(chatId)
                        .update("title", chatTitle.text)
                        .await()
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getMessage(chatId: String): Flow<Response<List<ChatMessage>>> = callbackFlow {
        var snapshotListener: ListenerRegistration? = null
        trySend(Response.Loading())
        try {
            snapshotListener = chatRef.document(chatId)
                .collection(MESSAGE_SUB_COLLECTION)
                .orderBy("timestamp")
                .addSnapshotListener { snapShot, error ->
                    val response = if (snapShot != null) {
                        val message = snapShot.toObjects(ChatMessage::class.java)
                        Response.Success(dataSuccess = message)
                    } else {
                        Response.Error(error?.cause)
                    }
                    trySend(response)
                }

        } catch (e: Exception) {
            trySend(Response.Error(e.cause))
        }

        awaitClose { snapshotListener?.remove() }

    }


    suspend fun saveChat(chatMessage: ChatMessage, chatId: String) {
        chatRef.document(chatId)
            .collection(MESSAGE_SUB_COLLECTION)
            .add(chatMessage)
            .await()
    }


}