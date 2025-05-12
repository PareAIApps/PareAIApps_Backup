package pnj.pk.pareaipk.ui.article

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import pnj.pk.pareaipk.R

class ArticleViewModel : ViewModel() {
    // Article details
    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _date = MutableLiveData<String>()
    val date: LiveData<String> = _date

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> = _imageUrl

    // Loading and error states
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Set article data from intent extras
    fun setArticleData(title: String, date: String, description: String, imageUrl: String) {
        _title.value = title
        _date.value = date
        _description.value = description
        _imageUrl.value = imageUrl
    }

    // For sharing functionality
    fun getShareText(): String {
        return "${_title.value ?: ""}\n\n${_description.value ?: ""}\n\nDibagikan dari aplikasi PAREAI"
    }
}