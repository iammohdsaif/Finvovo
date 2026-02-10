package com.example.finvovo.ui.askai

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finvovo.data.DataRepository
import com.example.finvovo.utils.ExportUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.content.FileProvider
import com.example.finvovo.R // Assuming R is available here, otherwise we might need to pass context for strings or hardcode for now.
import java.util.Calendar // For default dates

enum class AIModel(val packageName: String, val displayName: String, val iconRes: Int) {
    // Note: Resources are named incorrectly based on upload order, mapping visually correctly here
    CHATGPT("com.openai.chatgpt", "ChatGPT", R.drawable.ic_gemini), // ic_gemini contains the OpenAI logo
    GEMINI("com.google.android.apps.bard", "Gemini", R.drawable.ic_claude), // ic_claude contains the Google/Gemini logo
    CLAUDE("com.anthropic.claude", "Claude", R.drawable.ic_chatgpt) // ic_chatgpt contains the Anthropic logo
}

sealed class AskAIEvent {
    data class SharePdf(val uri: Uri, val packageName: String) : AskAIEvent()
    data class ShowError(val message: String) : AskAIEvent()
}

class AskAIViewModel(
    private val repository: DataRepository
) : ViewModel() {

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate: StateFlow<Long?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate: StateFlow<Long?> = _endDate.asStateFlow()

    private val _selectedModel = MutableStateFlow<AIModel>(AIModel.CHATGPT)
    val selectedModel: StateFlow<AIModel> = _selectedModel.asStateFlow()
    
    // UI Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AskAIEvent>()
    val eventFlow: SharedFlow<AskAIEvent> = _eventFlow.asSharedFlow()

    fun setStartDate(date: Long) {
        val normalizedDate = getStartOfDay(date)
        _startDate.value = normalizedDate
        // Auto-correct end date if it's before start date? Or just validate.
        if (_endDate.value != null && _endDate.value!! < normalizedDate) {
            _endDate.value = null
        }
    }

    fun setEndDate(date: Long) {
         _endDate.value = getStartOfDay(date)
    }

    private fun getStartOfDay(timeInMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun setSelectedModel(model: AIModel) {
        _selectedModel.value = model
    }

    fun onAskAIClicked(context: Context) {
        val start = _startDate.value
        val end = _endDate.value
        val model = _selectedModel.value

        if (start == null || end == null) {
            viewModelScope.launch {
                _eventFlow.emit(AskAIEvent.ShowError("Please select both start and end dates."))
            }
            return
        }

        if (start > end) {
             viewModelScope.launch {
                _eventFlow.emit(AskAIEvent.ShowError("Start date cannot be after end date."))
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Adjust end date to end of day? Usually date pickers give 00:00.
                // Let's add 24 hours (minus 1ms) to endDate to include the full day
                val adjustedEnd = end + (24 * 60 * 60 * 1000) - 1

                val transactions = repository.getTransactionsBetweenDates(start, adjustedEnd)
                
                if (transactions.isEmpty()) {
                    _eventFlow.emit(AskAIEvent.ShowError("No transactions found in this date range."))
                    _isLoading.value = false
                    return@launch
                }

                val uri = ExportUtils.exportToPdf(context, transactions)
                if (uri != null) {
                    _eventFlow.emit(AskAIEvent.SharePdf(uri, model.packageName))
                } else {
                    _eventFlow.emit(AskAIEvent.ShowError("Failed to generate PDF."))
                }

            } catch (e: Exception) {
                 _eventFlow.emit(AskAIEvent.ShowError("Error: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
