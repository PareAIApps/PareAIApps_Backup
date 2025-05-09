package pnj.pk.pareaipk.ui.scan

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import pnj.pk.pareaipk.data.response.ModelMLResponse
import pnj.pk.pareaipk.data.retrofit.ApiConfig
import pnj.pk.pareaipk.database.entity.HistoryEntity
import pnj.pk.pareaipk.ui.history.HistoryViewModel
import pnj.pk.pareaipk.utils.Event
import pnj.pk.pareaipk.utils.reduceFileImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HasilScanViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiConfig.getApiService()
    private val historyScanViewModel = HistoryViewModel(application)

    private val _predictionResult = MutableLiveData<Result<ModelMLResponse>>()
    val predictionResult: LiveData<Result<ModelMLResponse>> = _predictionResult

    private val _popupMessage = MutableLiveData<Event<String>>()
    val popupMessage: LiveData<Event<String>> = _popupMessage

    fun predictImage(imageFile: File, description: String = "Apple classification") {
        viewModelScope.launch {
            try {
                val compressedFile = imageFile.reduceFileImage()

                val requestImageFile = compressedFile.asRequestBody("image/jpeg".toMediaType())
                val imageMultipart = MultipartBody.Part.createFormData(
                    "image", compressedFile.name, requestImageFile
                )

                val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())

                val currentDateTime = getCurrentDateTime()
                val response = apiService.predictImage(imageMultipart, descriptionRequestBody)

                if (response.isSuccessful) {
                    val mlResponse = response.body()
                    if (mlResponse != null) {
                        if (mlResponse.classLabel == "Objek Tidak Tersedia.") {
                            _popupMessage.value = Event("Objek Tidak Tersedia. Silakan scan ulang.")
                            _predictionResult.value = Result.success(mlResponse)
                            return@launch
                        }

                        val scanHistory = HistoryEntity(
                            imageUri = imageFile.toString(),
                            result = mlResponse.classLabel,
                            confidenceScore = (mlResponse.confidence * 100).toInt(),
                            scanDate = currentDateTime,
                            explanation = "No explanation from server",
                            suggestion = mlResponse.suggestion.ifEmpty { "No suggestion." },
                            description = mlResponse.description.ifEmpty { "No description." },
                            tools_receipt = mlResponse.tools_receipt.ifEmpty { "No receipt." },
                            tutorial = mlResponse.tutorial.ifEmpty { "No tutorial." },
                            medicine = "No medicine"
                        )

                        historyScanViewModel.insertScanHistory(scanHistory)
                        _predictionResult.value = Result.success(mlResponse)
                    } else {
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

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
