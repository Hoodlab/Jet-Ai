package hoods.com.jetai.message

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import hoods.com.jetai.data.models.ChatMessage
import hoods.com.jetai.data.models.Participant
import kotlinx.coroutines.launch

@Composable
fun MessageScreen(
    modifier: Modifier = Modifier,
    messageViewModel: MessageViewModel,
) {
    val chatMessage = messageViewModel.chatState
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            MessageInput(
                onSendMessage = { inputText ->
                    messageViewModel.sendMessage(inputText)
                },
                resetScroll = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(paddingValues = innerPadding)
                .fillMaxSize()
        ) {
            ChatList(
                chatMessages = chatMessage.messages,
                isChatPending = chatMessage.isProcessingMessage,
                listState = listState
            )
        }
    }


}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatList(
    chatMessages: List<ChatMessage>,
    isChatPending: Boolean,
    listState: LazyListState,
) {
    LazyColumn(
        reverseLayout = true,
        state = listState,
        modifier = Modifier.animateContentSize()
    ) {
        item {
            AnimatedVisibility(isChatPending) {
                LinearProgressIndicator(Modifier.padding(8.dp))
            }
        }
        items(chatMessages.reversed()) { message ->
            ChatBubbleItem(
                message,
                modifier = Modifier.animateItemPlacement()
            )
        }
    }

}

@Composable
fun ChatBubbleItem(
    chatMessage: ChatMessage,
    modifier: Modifier = Modifier,
) {
    val isModelMessage = chatMessage.participant != Participant.USER
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isChatCopied by remember { mutableStateOf(false) }
    val backgroundColor = when (chatMessage.participant) {
        Participant.MODEL -> MaterialTheme.colorScheme.surfaceVariant
        Participant.USER -> MaterialTheme.colorScheme.primaryContainer
        Participant.ERROR -> MaterialTheme.colorScheme.errorContainer
    }
    val bubbleShape = if (isModelMessage) {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    }
    val horizontalAlignment = if (isModelMessage) {
        Alignment.Start
    } else {
        Alignment.End
    }
    LaunchedEffect(isChatCopied) {
        if (isChatCopied) {
            Toast.makeText(context, "Text Copied", Toast.LENGTH_SHORT).show()
            isChatCopied = false
        }
    }
    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = chatMessage.participant.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        BoxWithConstraints {
            Card(
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                shape = bubbleShape,
                modifier = Modifier.widthIn(0.dp, maxWidth * .9f)
            ) {
                MarkdownText(
                    markdown = chatMessage.text,
                    modifier = Modifier
                        .padding(16.dp),
                    style = TextStyle(
                        color = contentColorFor(backgroundColor)
                    ),
                    isTextSelectable = true
                )
                if (chatMessage.participant == Participant.MODEL) {
                    Icon(
                        Icons.Default.ContentCopy,
                        "Content Copy",
                        modifier = Modifier
                            .clickable(
                                indication = rememberRipple(),
                                interactionSource = MutableInteractionSource(),
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(chatMessage.text))
                                    isChatCopied = true
                                }
                            )
                            .padding(start = 8.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }


}


@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    resetScroll: () -> Unit = {},
) {
    var userMessage by rememberSaveable { mutableStateOf("") }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            TextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                label = { Text("Prompt") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(.85f),
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )
            IconButton(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        onSendMessage(userMessage)
                        userMessage = ""
                        resetScroll()
                    }
                },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(.15f)
            ) {
                Icon(
                    Icons.Default.Send,
                    "Send Prompt"
                )
            }
        }
    }

}







