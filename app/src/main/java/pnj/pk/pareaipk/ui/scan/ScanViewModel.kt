package pnj.pk.pareaipk.ui.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import pnj.pk.pareaipk.data.response.ModelMLResponse
import pnj.pk.pareaipk.data.retrofit.ApiConfig
import pnj.pk.pareaipk.utils.reduceFileImage
import java.io.File

class ScanViewModel : ViewModel() {
    private val apiService = ApiConfig.getApiService()

    private val _predictionResult = MutableLiveData<Result<ModelMLResponse>>()
    val predictionResult: LiveData<Result<ModelMLResponse>> = _predictionResult

    fun predictImage(imageFile: File, description: String = "Apple classification") {
        viewModelScope.launch {
            try {
                // Gunakan fungsi reduceFileImage dari utils
                val compressedFile = imageFile.reduceFileImage()

                // Prepare image multipart
                val requestImageFile = compressedFile.asRequestBody("image/jpeg".toMediaType())
                val imageMultipart = MultipartBody.Part.createFormData(
                    "image",
                    compressedFile.name,
                    requestImageFile
                )

                // Prepare description
                val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())

                // Make API call
                val response = apiService.predictImage(imageMultipart, descriptionRequestBody)

                if (response.isSuccessful) {
                    response.body()?.let {
                        _predictionResult.value = Result.success(it)
                    } ?: run {
                        _predictionResult.value = Result.failure(Exception("Empty response body"))
                    }
                } else {
                    _predictionResult.value = Result.failure(
                        Exception("Error: ${response.code()} - ${response.message()}")
                    )
                }
            } catch (e: Exception) {
                _predictionResult.value = Result.failure(e)
            }
        }
    }
}

