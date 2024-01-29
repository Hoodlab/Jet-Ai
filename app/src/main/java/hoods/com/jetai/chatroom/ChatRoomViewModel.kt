package hoods.com.jetai.chatroom

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import hoods.com.jetai.Graph
import hoods.com.jetai.data.models.ChatRoom
import hoods.com.jetai.data.repository.AuthRepository
import hoods.com.jetai.data.repository.ChatRepository
import hoods.com.jetai.utils.ext.collectAndHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatRoomViewModel(
    private val chatRepository: ChatRepository = Graph.chatRepository,
    private val authRepository: AuthRepository = Graph.authRepository,
) : ViewModel() {
    var chatRoomState by mutableStateOf(ChatRoomState())
        private set

    /*private val _chatRoomState: MutableStateFlow<ChatRoomState> = MutableStateFlow(ChatRoomState())
    val chatRoomState: StateFlow<ChatRoomState>
        get() = _chatRoomState*/

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest {
                chatRoomState = chatRoomState.copy(
                    currentUse = it
                )
            }
        }
    }

    fun initChatRoom() {
        viewModelScope.launch {
            chatRepository.getChatRoomList().collectAndHandle(
                onError = {
                    chatRoomState = chatRoomState.copy(
                        errorMsg = it?.message,
                        loading = false
                    )
                },
                onLoading = {
                    chatRoomState = chatRoomState.copy(
                        errorMsg = null,
                        loading = true
                    )
                }
            ) { chatRooms ->
                chatRoomState = chatRoomState.copy(
                    chatRooms = chatRooms,
                    errorMsg = null,
                    loading = false
                )
            }
        }
    }

    fun resetChatId() {
        chatRoomState = chatRoomState.copy(
            newChatId = null
        )
    }

    fun newChatRoom() {
        viewModelScope.launch {
            val chatId = chatRepository.createChatRoom()
            chatRoomState = chatRoomState.copy(
                newChatId = chatId
            )
        }
    }


}

data class ChatRoomState(
    val chatRooms: List<ChatRoom> = emptyList(),
    val loading: Boolean = false,
    val errorMsg: String? = null,
    val newChatId: String? = null,
    val currentUse: FirebaseUser? = null,
)