package com.samco.trackandgraph.addtracker

import androidx.lifecycle.*
import com.samco.trackandgraph.base.database.dto.DataType
import com.samco.trackandgraph.base.database.dto.Tracker
import com.samco.trackandgraph.base.database.dto.TrackerSuggestionOrder
import com.samco.trackandgraph.base.database.dto.TrackerSuggestionType
import com.samco.trackandgraph.base.model.DataInteractor
import com.samco.trackandgraph.base.model.di.IODispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTrackerViewModel @Inject constructor(
    private val dataInteractor: DataInteractor,
    @IODispatcher private val io: CoroutineDispatcher
): ViewModel() {

    enum class TrackerEditionError {
        NAME_EMPTY,
        NAME_TAKEN,
        INTERNAL
    }

    class TrackerData {
        var trackerId: Long = -1L
        var groupId: Long = -1L
        var name: String = ""
        var description: String = ""
        var type: DataType = DataType.CONTINUOUS
        var hasDefaultValue: Boolean = false
        var defaultValue: Double = 0.0
        var defaultLabel: String = ""
        var dataType: DataType = DataType.CONTINUOUS
        var suggestionType: TrackerSuggestionType = TrackerSuggestionType.VALUE_AND_LABEL
        var suggestionOrder: TrackerSuggestionOrder = TrackerSuggestionOrder.VALUE_ASCENDING
    }

    val success = MutableLiveData<Boolean>(false)
    val errors = MutableLiveData<Set<TrackerEditionError>>()

    fun getInitialValues(trackerId: Long): CompletableDeferred<TrackerData> {
        val deferredFormInput = CompletableDeferred<TrackerData>()

        viewModelScope.launch(io) {
            dataInteractor.getTrackerById(trackerId)?.let {
                val trackerData = TrackerData()
                trackerData.name = it.name
                trackerData.description = it.description
                trackerData.dataType = it.dataType
                trackerData.hasDefaultValue = it.hasDefaultValue
                trackerData.defaultValue = it.defaultValue
                trackerData.defaultLabel = it.defaultLabel
                trackerData.suggestionType = it.suggestionType
                trackerData.suggestionOrder = it.suggestionOrder
                deferredFormInput.complete(trackerData)
            }
        }

        return deferredFormInput
    }

    fun processForm(formInput: TrackerData) {
        var currentStatus = false
        val currentErrors = mutableSetOf<TrackerEditionError>()

        viewModelScope.launch(io) {
            if (formInput.name.isBlank()) {
                currentErrors.add(TrackerEditionError.NAME_EMPTY)
            }

            if (currentErrors.isEmpty()) {
                var existingTracker: Tracker? = null

                var disallowedNames = dataInteractor.getFeaturesForGroupSync(formInput.groupId)
                    .map { it.name.trim() }

                if (formInput.trackerId > 0) {
                    existingTracker = dataInteractor.getTrackerById(formInput.trackerId)

                    if (existingTracker != null) {
                        disallowedNames = disallowedNames.filter { it != existingTracker.name }
                    } else {
                        currentErrors.add(TrackerEditionError.INTERNAL)
                    }
                }

                if (disallowedNames.contains(formInput.name)) {
                    currentErrors.add(TrackerEditionError.NAME_TAKEN)
                }

                // Everything is fine, go for update
                if (currentErrors.isEmpty()) {
                    if (formInput.trackerId > 0 && existingTracker != null) {
                        dataInteractor.updateTracker(
                            oldTracker = existingTracker,
                            newName = formInput.name,
                            featureDescription = formInput.description,
                            newType = formInput.dataType,
                            hasDefaultValue = formInput.hasDefaultValue,
                            defaultValue = formInput.defaultValue,
                            defaultLabel = formInput.defaultLabel,
                            suggestionType = formInput.suggestionType,
                            suggestionOrder = formInput.suggestionOrder
                        )
                    } else {
                        val tracker = Tracker(
                            id = 0L,
                            name = formInput.name,
                            groupId = formInput.groupId,
                            featureId = 0L,
                            displayIndex = 0,
                            description = formInput.description,
                            dataType = formInput.dataType,
                            hasDefaultValue = formInput.hasDefaultValue,
                            defaultValue = formInput.defaultValue,
                            defaultLabel = formInput.defaultLabel,
                            suggestionType = formInput.suggestionType,
                            suggestionOrder = formInput.suggestionOrder
                        )
                        dataInteractor.insertTracker(tracker)
                    }

                    currentStatus = true
                }
            }

            errors.postValue(currentErrors)
            success.postValue(currentStatus)
        }
    }
}