package hoods.com.jetai.photo_reasoning

sealed interface PhotoReasoningUiState {
    data object Initial : PhotoReasoningUiState
    data object Loading : PhotoReasoningUiState
    data class Success(
        val output: String,
    ) : PhotoReasoningUiState

    data class Error(
        val errorMsg: String,
    ) : PhotoReasoningUiState

}