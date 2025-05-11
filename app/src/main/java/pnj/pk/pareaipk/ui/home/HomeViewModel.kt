package pnj.pk.pareaipk.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.data.response.ArticleResponse
import pnj.pk.pareaipk.data.retrofit.ApiConfig
import java.io.IOException

class HomeViewModel : ViewModel() {

    // Existing content - keep this as requested
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    // Add new functionality
    private val _articles = MutableLiveData<List<ArticleResponse>>()
    val articles: LiveData<List<ArticleResponse>> = _articles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchArticles() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                val response = apiService.getArticles()

                if (response.isSuccessful) {
                    val articlesMap = response.body()
                    if (articlesMap != null) {
                        _articles.value = articlesMap.values.toList()
                    } else {
                        _error.value = "Empty response body"
                    }
                } else {
                    _error.value = "Error: ${response.message()}"
                }
            } catch (e: IOException) {
                _error.value = "Network error: ${e.message}"
            } catch (e: Exception) {
                _error.value = "Error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}