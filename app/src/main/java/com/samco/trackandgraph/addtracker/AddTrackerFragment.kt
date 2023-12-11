/* 
* This file is part of Track & Graph
* 
* Track & Graph is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Track & Graph is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Track & Graph.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.samco.trackandgraph.addtracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButtonToggleGroup
import com.samco.trackandgraph.MainActivity
import com.samco.trackandgraph.NavButtonStyle
import com.samco.trackandgraph.R
import com.samco.trackandgraph.base.database.dto.DataType
import com.samco.trackandgraph.base.database.dto.TrackerSuggestionOrder
import com.samco.trackandgraph.base.database.dto.TrackerSuggestionType
import com.samco.trackandgraph.databinding.AddTrackerBinding
import com.samco.trackandgraph.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class AddTrackerFragment : Fragment() {
    private val args: AddTrackerFragmentArgs by navArgs()
    private val viewModel: AddTrackerViewModel by viewModels()
    private val uiData = UiData()
    private var navController: NavController? = null

    class UiData {
        val isNewTracker = ObservableBoolean(true)

        val defaultSection = ObservableBoolean(false)
        val suggestionSection = ObservableBoolean(false)

        val errorNameEmpty = ObservableBoolean(false)
        val errorNameTaken = ObservableBoolean(false)
        val errorInternal = ObservableBoolean(false)

        var trackerName = ObservableField<String>()
        var trackerDescription = ObservableField<String>()
        var dataType = ObservableField<Int>()
        var defaultValue = ObservableField<String>()
        var defaultLabel = ObservableField<String>()
        var suggestionType = ObservableField<Int>()
        var suggestionOrder = ObservableField<Int>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uiData.isNewTracker.set(args.editFeatureId == -1L)

        val binding = AddTrackerBinding.inflate(inflater, container, false)
        binding.uiData = uiData

        navController = container?.findNavController()

        val view = binding.root

        // Configure action button
        view.findViewById<View>(R.id.actionButton).setOnClickListener { submitForm() }

        view.findViewById<View>(R.id.closeButton).setOnClickListener { navController?.popBackStack() }

        configureButtonToggleGroup(view, R.id.inputDataType)

        // Handle errors from view model
        viewModel.errors.observe(viewLifecycleOwner) { errors ->
            errors.map {
                uiData.errorNameEmpty.set(errors.contains(AddTrackerViewModel.TrackerEditionError.NAME_EMPTY))
                uiData.errorNameTaken.set(errors.contains(AddTrackerViewModel.TrackerEditionError.NAME_TAKEN))
                uiData.errorInternal.set(errors.contains(AddTrackerViewModel.TrackerEditionError.INTERNAL))
            }
        }

        // Remove errors related to name when the name changed
        uiData.trackerName.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable?, propertyId: Int) {
                if (uiData.trackerName.get()?.isNotBlank() == true)
                {
                    uiData.errorNameEmpty.set(false)
                    uiData.errorNameTaken.set(false)
                }
            }
        })

        fillForm(view)

        return view
    }

    override fun onStart() {
        super.onStart()
        viewModel.success.observe(viewLifecycleOwner) {
            if (it) navController?.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setActionBarConfig(
            NavButtonStyle.UP,
            getString(R.string.add_tracker)
        )
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.hideKeyboard(requireActivity().currentFocus?.windowToken)
    }

    private fun configureButtonToggleGroup(view: View, toggleId: Int) {
        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(toggleId)
        toggleGroup.addOnButtonCheckedListener { _, buttonId, isChecked ->
            if (isChecked) {
                uiData.dataType.set(buttonId)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun fillForm(view: View) {
        val deferredFormData = viewModel.getInitialValues(args.editFeatureId)
        deferredFormData.invokeOnCompletion {
            val formInput = deferredFormData.getCompleted()
            uiData.trackerName.set(formInput.name)
            uiData.trackerDescription.set(formInput.description)

            view.findViewById<MaterialButtonToggleGroup>(R.id.inputDataType).check(when (formInput.dataType) {
                DataType.CONTINUOUS -> R.id.dataTypeContinuous
                DataType.DURATION -> R.id.dataTypeDuration
            })

            uiData.defaultSection.set(formInput.hasDefaultValue)
            uiData.defaultValue.set(formInput.defaultValue.toString())
            uiData.defaultLabel.set(formInput.defaultLabel)
            uiData.trackerDescription.set(formInput.description)
            uiData.suggestionType.set(formInput.suggestionType.ordinal)
            uiData.suggestionOrder.set(formInput.suggestionOrder.ordinal)


        }
    }

    private fun submitForm() {
        val formInput = AddTrackerViewModel.TrackerData()

        formInput.trackerId = args.editFeatureId
        formInput.groupId = args.groupId
        uiData.trackerName.get()?.let { formInput.name = it.trim() }
        uiData.trackerDescription.get()?.let { formInput.description = it.trim() }

        uiData.dataType.get()?.let {
            formInput.dataType = when (it) {
                R.id.dataTypeContinuous -> DataType.CONTINUOUS
                R.id.dataTypeDuration -> DataType.DURATION
                else -> DataType.CONTINUOUS
            }
        }

        if (uiData.defaultSection.get()) {
            formInput.hasDefaultValue = true
            uiData.defaultValue.get()?.let { formInput.defaultValue = it.toDouble() }
            uiData.defaultLabel.get()?.let { formInput.defaultLabel = it.trim() }
        } else {
            formInput.hasDefaultValue = false
            formInput.defaultValue = 0.0
            formInput.defaultLabel = ""
        }
        uiData.suggestionType.get()?.let { formInput.suggestionType = TrackerSuggestionType.values()[it] }
        uiData.suggestionOrder.get()?.let { formInput.suggestionOrder = TrackerSuggestionOrder.values()[it] }

        viewModel.processForm(formInput)
    }
}
