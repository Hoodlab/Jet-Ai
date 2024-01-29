package hoods.com.jetai

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.android.gms.auth.api.identity.Identity
import hoods.com.jetai.data.PhotoReasoningRepository
import hoods.com.jetai.data.repository.AuthRepository
import hoods.com.jetai.data.repository.AuthRepositoryImpl
import hoods.com.jetai.data.repository.ChatRepository
import hoods.com.jetai.data.repository.GoogleAuthClient

object Graph {

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }
    val chatRepository: ChatRepository by lazy {
        ChatRepository()
    }
    val photoReasoningRepository: PhotoReasoningRepository by lazy {
        PhotoReasoningRepository()
    }

    lateinit var googleAuthClient: GoogleAuthClient
    private val config = generationConfig {
        temperature = .7f
    }

    fun generativeModel(modelName: String) = GenerativeModel(
        modelName = modelName,
        apiKey = BuildConfig.apiKey,
        generationConfig = config
    )

    fun provide(context: Context) {
        googleAuthClient = GoogleAuthClient(
            oneTapClient = Identity.getSignInClient(context)
        )
    }
}