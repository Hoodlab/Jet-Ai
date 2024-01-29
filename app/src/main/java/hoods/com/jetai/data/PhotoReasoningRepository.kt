package hoods.com.jetai.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import hoods.com.jetai.Graph
import hoods.com.jetai.data.models.ModelName
import hoods.com.jetai.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PhotoReasoningRepository(
    private val generativeModel: GenerativeModel = Graph.generativeModel(
        ModelName.MULTIMODAL.modelName
    ),
) {
    suspend fun reason(
        userInput: String, selectedImages: List<Bitmap>,
    ): Flow<Response<String>> = flow {
        val prompt = "Look at the image(s) and then answer the following question:$userInput"
        try {
            emit(Response.Loading())
            val inputContent = content {
                for (bitmap in selectedImages) {
                    image(bitmap)
                }
                text(prompt)
            }
            var outputContent = ""
            generativeModel.generateContentStream(inputContent)
                .collect { response ->
                    outputContent += response.text
                    emit(Response.Success(outputContent))
                }
        } catch (e: Exception) {
            emit(Response.Error(e.cause))
        }


    }
}