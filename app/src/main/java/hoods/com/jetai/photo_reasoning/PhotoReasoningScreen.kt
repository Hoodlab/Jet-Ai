package hoods.com.jetai.photo_reasoning

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import dev.jeziellago.compose.markdowntext.MarkdownText
import hoods.com.jetai.utils.saveImageToFileAndGetUri
import kotlinx.coroutines.launch

@Composable
fun PhotoReasoningScreen(
    modifier: Modifier,
    viewModel: PhotoReasoningViewModel = viewModel(),
) {
    val photoReasoningUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val imageRequestBuilder = ImageRequest.Builder(LocalContext.current)
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()
    PhotoReasoningScreen(
        modifier = modifier,
        uiState = photoReasoningUiState,
        onReasonClicked = { inputText, selectedItems ->
            coroutineScope.launch {
                val bitmaps = selectedItems.mapNotNull {
                    val imageRequest = imageRequestBuilder
                        .data(it)
                        .size(size = 768)
                        .precision(Precision.EXACT)
                        .build()
                    try {
                        val result = imageLoader.execute(imageRequest)
                        if (result is SuccessResult) {
                            return@mapNotNull (result.drawable as BitmapDrawable).bitmap
                        } else {
                            return@mapNotNull null
                        }

                    } catch (e: Exception) {
                        return@mapNotNull null
                    }
                }
                viewModel.reason(inputText, bitmaps)
            }
        }
    )

}

@Composable
private fun PhotoReasoningScreen(
    modifier: Modifier,
    uiState: PhotoReasoningUiState,
    onReasonClicked: (String, List<Uri>) -> Unit = { _, _ -> },
) {
    var userQuestion: TextFieldValue by rememberSaveable(
        stateSaver = TextFieldValue.Saver
    ) { mutableStateOf(TextFieldValue()) }
    val imageUris = rememberSaveable(saver = UriSaver()) { mutableStateListOf() }
    val context = LocalContext.current

    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val imageUri =
                saveImageToFileAndGetUri(bitmap, context)
            imageUri?.let { imageUris.add(it) }
        }
    }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { imageUri ->
        imageUri?.let {
            imageUris.add(it)
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                3.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                UserInputText(
                    textFieldValue = userQuestion,
                    onTextChanged = { userQuestion = it },
                    focusState = false,
                    onTextFieldFocused = {},
                    modifier = Modifier
                        .fillMaxWidth(.8f)
                )
                IconButton(
                    onClick = {
                        if (userQuestion.text.isNotBlank()) {
                            onReasonClicked(userQuestion.text, imageUris.toList())
                        }
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Send, null)
                }
            }
            Row(
                modifier = Modifier.padding(top = 16.dp),
            ) {
                IconButton(
                    onClick = {
                        pickMedia.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.PhotoLibrary, null)
                }
                IconButton(
                    onClick = {
                        takePhotoLauncher.launch()
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.PhotoCamera, null)
                }
            }
            LazyRow(
                modifier = Modifier.padding(8.dp)
            ) {
                items(imageUris) { imageUri ->
                    Column {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(
                                .5f
                            ),
                            onClick = {
                                imageUris.remove(imageUri)
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Close, "close")
                        }
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(
                                    top = 0.dp, bottom = 4.dp, start = 4.dp, end = 4.dp
                                )
                                .requiredSize(102.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                    }
                }
            }

            when (uiState) {
                PhotoReasoningUiState.Initial -> {}
                PhotoReasoningUiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PhotoReasoningUiState.Success -> {
                    Card(
                        modifier = Modifier.padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        MarkdownText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                            markdown = uiState.output,
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                is PhotoReasoningUiState.Error -> {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun UserInputText(
    modifier: Modifier = Modifier,
    textFieldValue: TextFieldValue,
    keyboardType: KeyboardType = KeyboardType.Text,
    onTextChanged: (TextFieldValue) -> Unit,
    onTextFieldFocused: (Boolean) -> Unit,
    focusState: Boolean,
) {
    val labelText = "Text here"
    Row(
        modifier = modifier
            .height(64.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            UserInputTextField(
                textFieldValue = textFieldValue,
                onTextChanged = onTextChanged,
                onTextFieldFocused = onTextFieldFocused,
                keyboardType = keyboardType,
                focusState = focusState,
                modifier = Modifier.semantics {
                    contentDescription = labelText
                }
            )
        }
    }

}

@Composable
private fun BoxScope.UserInputTextField(
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onTextFieldFocused: (Boolean) -> Unit,
    keyboardType: KeyboardType,
    focusState: Boolean,
    modifier: Modifier = Modifier,
) {
    var lastFocusState by remember { mutableStateOf(false) }
    BasicTextField(
        value = textFieldValue,
        onValueChange = { onTextChanged(it) },
        modifier = modifier
            .padding(start = 32.dp)
            .align(Alignment.CenterStart)
            .onFocusChanged { state ->
                if (lastFocusState != state.isFocused) {
                    onTextFieldFocused(state.isFocused)
                }
                lastFocusState = state.isFocused
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Send
        ),
        maxLines = 1,
        cursorBrush = SolidColor(LocalContentColor.current),
        textStyle = LocalTextStyle.current.copy(
            color = LocalContentColor.current
        )
    )
    val disableColor = MaterialTheme.colorScheme.onSurfaceVariant
    if (textFieldValue.text.isEmpty() && !focusState) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp),
            text = "Write a Prompt",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = disableColor
            )
        )
    }

}